package com.compilador;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.gui.TreeViewer;
import javax.swing.*;
import java.awt.*;
import java.nio.file.*;
import java.util.*;

public class MiSintacticoApp {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("USO: java MiSintacticoApp \"<expresion>\"");
            System.out.println("     java MiSintacticoApp <archivo.txt>");
            System.out.println("");
            System.out.println("Ejemplos:");
            System.out.println("  java MiSintacticoApp \"a + b * c\"");
            System.out.println("  java MiSintacticoApp \"(a + b) * c - d\"");
            return;
        }

        String entrada;
        try {
            Path path = Paths.get(args[0]);
            if (Files.exists(path) && !Files.isDirectory(path)) {
                entrada = new String(Files.readAllBytes(path)).trim();
                System.out.println("Archivo: " + path.toAbsolutePath());
            } else {
                entrada = args[0];
            }
        } catch (Exception e) {
            entrada = args[0];
        }

        CharStream input = CharStreams.fromString(entrada);

        // 1. LEXER
        MiSintacticoLexer lexer = new MiSintacticoLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();

        System.out.println("Expresion: \"" + entrada + "\"");
        System.out.println("");

        // 2. TABLA DE TOKENS
        System.out.println("--- TABLA DE TOKENS ---");
        Vocabulary vocab = lexer.getVocabulary();
        int idx = 0;
        for (Token t : tokens.getTokens()) {
            if (t.getType() == Token.EOF) continue;
            String nombre = vocab.getSymbolicName(t.getType());
            if (nombre == null) nombre = "'" + t.getText() + "'";
            System.out.printf("  %2d: %-8s \"%s\"%n", idx++, nombre, t.getText());
        }
        System.out.println("");

        // 3. PARSER
        tokens.seek(0);
        MiSintacticoParser parser = new MiSintacticoParser(tokens);
        java.util.List<String> errores = new ArrayList<>();
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                    int line, int charPositionInLine, String msg, RecognitionException e) {
                errores.add("ERROR en linea " + line + ":" + charPositionInLine + " - " + msg);
            }
        });

        java.lang.reflect.Method metodo = MiSintacticoParser.class.getMethod("expr");
        ParseTree tree = (ParseTree) metodo.invoke(parser);

        // 4. RESULTADO
        System.out.println("--- RESULTADO ---");
        System.out.print("  " + entrada + "  =>  ");
        if (errores.isEmpty()) {
            System.out.println("VALIDA");
        } else {
            System.out.println("INVALIDA:");
            for (String err : errores) {
                System.out.println("     " + err);
            }
        }
        System.out.println("");

        // 5. ARBOL (texto)
        System.out.println("--- ARBOL SINTACTICO (texto) ---");
        printTree(tree, 2);
        System.out.println("");

        // 6. REDUCCIONES
        System.out.println("--- REDUCCIONES (ascendente) ---");
        java.util.List<String> pasos = new ArrayList<>();
        buildAscendente(tree, pasos, 0);
        for (String paso : pasos) {
            System.out.println("  " + paso);
        }

        // 7. ARBOL GRAFICO (ventana Swing como MiLenguaje)
        if (errores.isEmpty()) {
            mostrarArbolGrafico(tree, parser);
        }
    }

    static void mostrarArbolGrafico(ParseTree tree, MiSintacticoParser parser) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Arbol Sintactico - MiSintactico");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            TreeViewer viewer = new TreeViewer(Arrays.asList(parser.getRuleNames()), tree);
            viewer.setScale(1.5);

            JScrollPane scrollPane = new JScrollPane(viewer);
            frame.add(scrollPane);

            JPanel controls = new JPanel();
            JButton zoomIn = new JButton("+");
            JButton zoomOut = new JButton("-");
            JButton reset = new JButton("Reset");

            zoomIn.addActionListener(e -> viewer.setScale(viewer.getScale() * 1.2));
            zoomOut.addActionListener(e -> viewer.setScale(viewer.getScale() / 1.2));
            reset.addActionListener(e -> viewer.setScale(1.5));

            controls.add(zoomIn);
            controls.add(zoomOut);
            controls.add(reset);
            frame.add(controls, BorderLayout.SOUTH);

            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    static String nodeLabel(ParseTree node) {
        if (node instanceof RuleNode) {
            String name = node.getClass().getSimpleName();
            if (name.endsWith("Context")) return name.substring(0, name.length() - 7);
            return name;
        }
        String text = node.getText();
        return text;
    }

    static void printTree(ParseTree node, int indent) {
        StringBuilder pad = new StringBuilder();
        for (int i = 0; i < indent; i++) pad.append(' ');
        if (node.getChildCount() == 0) {
            String text = node.getText();
            if (!text.trim().isEmpty()) System.out.println(pad + "\"" + text + "\"");
            return;
        }
        System.out.println(pad + nodeLabel(node));
        for (int i = 0; i < node.getChildCount(); i++) printTree(node.getChild(i), indent + 2);
    }

    static int buildAscendente(ParseTree node, java.util.List<String> pasos, int nextNum) {
        if (node.getChildCount() == 0) {
            String text = node.getText();
            if (!text.trim().isEmpty()) pasos.add(String.format("  %2d. leer \"%s\"", nextNum++, text));
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
        pasos.add(String.format("  %2d. %s <- %s", nextNum++, nodeLabel(node), rhs));
        return nextNum;
    }
}
