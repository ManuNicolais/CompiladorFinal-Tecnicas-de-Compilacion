package com.compilador;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import java.util.*;

public class TestSintactico {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("USO: java TestSintactico <regla> \"<entrada>\"");
            System.out.println("     java TestSintactico list");
            System.out.println("");
            System.out.println("Ejemplos:");
            System.out.println("  java TestSintactico expr  \"a + b * c\"");
            System.out.println("  java TestSintactico term  \"x * y\"");
            System.out.println("  java TestSintactico factor \"(a + b)\"");
            return;
        }

        if (args[0].equals("list")) {
            System.out.println("Reglas disponibles en MiSintactico:");
            // Cargar parser solo para listar reglas
            MiSintacticoParser p = new MiSintacticoParser(null);
            String[] rules = p.getRuleNames();
            for (String r : rules) {
                if (!r.equals("token") && !r.equals("programa")) {
                    System.out.println("  " + r);
                }
            }
            return;
        }

        String regla = args[0];
        String entrada = args[1];

        CharStream input = CharStreams.fromString(entrada);

        // 1. LEXER
        MiSintacticoLexer lexer = new MiSintacticoLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        System.out.println("=== TABLA DE TOKENS ===");
        tokens.fill();
        int idx = 0;
        Vocabulary vocab = lexer.getVocabulary();
        for (Token t : tokens.getTokens()) {
            if (t.getType() == Token.EOF) continue;
            String nombre = vocab.getSymbolicName(t.getType());
            if (nombre == null) nombre = "'" + t.getText() + "'";
            System.out.printf("  %2d: %-12s -> \"%s\" (linea %d:%d)%n",
                    idx++, nombre, t.getText(), t.getLine(), t.getCharPositionInLine());
        }
        System.out.println("");

        // 2. PARSER
        tokens.seek(0);
        MiSintacticoParser parser = new MiSintacticoParser(tokens);

        List<String> errores = new ArrayList<>();
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                    int line, int charPositionInLine, String msg, RecognitionException e) {
                errores.add("ERROR en linea " + line + ":" + charPositionInLine + " - " + msg);
            }
        });

        // 3. INVOCAR REGLA POR NOMBRE usando reflection
        ParseTree tree;
        try {
            java.lang.reflect.Method metodo = MiSintacticoParser.class.getMethod(regla);
            tree = (ParseTree) metodo.invoke(parser);
        } catch (NoSuchMethodException e) {
            System.out.println("ERROR: La regla '" + regla + "' no existe en MiSintactico.");
            System.out.println("Usa 'java TestSintactico list' para ver las disponibles.");
            return;
        }

        // 4. RESULTADOS
        System.out.println("=== RESULTADO ===");
        System.out.println("  Regla:     " + regla);
        System.out.println("  Entrada:   \"" + entrada + "\"");
        if (errores.isEmpty()) {
            System.out.println("  -> Entrada VALIDA segun la gramatica");
        } else {
            System.out.println("  -> Entrada INVALIDA segun la gramatica:");
            for (String err : errores) {
                System.out.println("      " + err);
            }
        }
        System.out.println("");

        // 5. ARBOL (formato LISP con labels)
        System.out.println("=== ARBOL SINTACTICO (LISP con labels) ===");
        System.out.println("  " + treeToLisp(tree));
        System.out.println("");

        // 6. ARBOL (identado con labels)
        System.out.println("=== ARBOL SINTACTICO (identado) ===");
        printTree(tree, 2);
        System.out.println("");

        // 7. ARBOL ASCENDENTE (reducciones paso a paso)
        System.out.println("=== ARBOL ASCENDENTE (reducciones paso a paso) ===");
        List<String> pasos = new ArrayList<>();
        buildAscendente(tree, pasos, 0);
        for (String paso : pasos) {
            System.out.println("  " + paso);
        }
    }

    static String nodeLabel(ParseTree node) {
        if (node instanceof RuleNode) {
            String simpleName = node.getClass().getSimpleName();
            if (simpleName.endsWith("Context")) {
                return simpleName.substring(0, simpleName.length() - 7);
            }
            return simpleName;
        }
        String text = node.getText();
        return text;
    }

    static String treeToLisp(ParseTree node) {
        if (node.getChildCount() == 0) {
            String text = node.getText();
            if (text.trim().isEmpty()) return "";
            return "\"" + text + "\"";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(nodeLabel(node));
        for (int i = 0; i < node.getChildCount(); i++) {
            String child = treeToLisp(node.getChild(i));
            if (!child.isEmpty()) {
                sb.append(" ").append(child);
            }
        }
        sb.append(")");
        return sb.toString();
    }

    static void printTree(ParseTree node, int indent) {
        StringBuilder pad = new StringBuilder();
        for (int i = 0; i < indent; i++) pad.append(' ');
        if (node.getChildCount() == 0) {
            String text = node.getText();
            if (!text.trim().isEmpty()) {
                System.out.println(pad + "\"" + text + "\"");
            }
            return;
        }
        System.out.println(pad + nodeLabel(node));
        for (int i = 0; i < node.getChildCount(); i++) {
            printTree(node.getChild(i), indent + 2);
        }
    }

    static int buildAscendente(ParseTree node, List<String> pasos, int nextNum) {
        if (node.getChildCount() == 0) {
            String text = node.getText();
            if (!text.trim().isEmpty()) {
                pasos.add(String.format("  %2d. Leer token: \"%s\"", nextNum++, text));
            }
            return nextNum;
        }
        StringBuilder rhs = new StringBuilder();
        for (int i = 0; i < node.getChildCount(); i++) {
            ParseTree child = node.getChild(i);
            if (child.getChildCount() == 0) {
                String text = child.getText();
                if (!text.trim().isEmpty()) {
                    if (rhs.length() > 0) rhs.append(" ");
                    rhs.append("\"").append(text).append("\"");
                }
            } else {
                nextNum = buildAscendente(child, pasos, nextNum);
                if (rhs.length() > 0) rhs.append(" ");
                rhs.append(nodeLabel(child));
            }
        }
        pasos.add(String.format("  %2d. Reducir: %s <- %s", nextNum++, nodeLabel(node), rhs));
        return nextNum;
    }
}
