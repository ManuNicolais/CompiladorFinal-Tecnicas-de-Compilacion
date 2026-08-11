package com.compilador;

import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

public class CodigoVisitor extends MiLenguajeBaseVisitor<String> {

    private List<String> codigo = new ArrayList<>();
    private int tempCount = 0;
    private int labelCount = 0;

    private String nuevaTemp() {
        return "t" + (tempCount++);
    }

    private String nuevaLabel(String prefijo) {
        return prefijo + "_" + (labelCount++);
    }

    private void emitir(String instruccion) {
        codigo.add(instruccion);
    }

    public List<String> getCodigo() {
        return codigo;
    }

    public void imprimir() {
        for (int i = 0; i < codigo.size(); i++) {
            System.out.printf("%3d: %s\n", i, codigo.get(i));
        }
    }

    public void guardarEnArchivo(String nombreArchivo) {
        try (FileWriter writer = new FileWriter(nombreArchivo)) {
            for (int i = 0; i < codigo.size(); i++) {
                writer.write(String.format("%3d: %s\n", i, codigo.get(i)));
            }
        } catch (IOException e) {
            System.err.println("Error al guardar código intermedio: " + e.getMessage());
        }
    }

    public void setCodigo(List<String> codigo) {
        this.codigo = codigo;
    }

    public void aplicarOptimizaciones() {
        List<String> optimizado = OptimizadorCodigo.optimizar(codigo);
        if (optimizado != null) {
            this.codigo = optimizado;
        }
    }

    @Override
    public String visitPrograma(MiLenguajeParser.ProgramaContext ctx) {
        emitir("// Código de tres direcciones generado");
        emitir("PROGRAMA_INICIO:");
        emitir("// Declaración de variables globales");
        for (MiLenguajeParser.DeclaracionGlobalContext d : ctx.declaracionGlobal()) {
            visit(d);
        }
        emitir("PROGRAMA_FIN:");
        return null;
    }

    @Override
    public String visitDeclFuncion(MiLenguajeParser.DeclFuncionContext ctx) {
        String nombre = ctx.ID().getText();
        emitir("func_" + nombre + ":");
        if (ctx.listaParams() != null) {
            for (MiLenguajeParser.ParamContext p : ctx.listaParams().param()) {
                emitir("PARAM " + p.ID().getText() + " " + p.tipo().getText());
            }
        }
        for (MiLenguajeParser.SentenciaContext s : ctx.bloque().sentencia()) {
            visit(s);
        }
        return null;
    }

    @Override
    public String visitDeclVariable(MiLenguajeParser.DeclVariableContext ctx) {
        emitir("DECLARE " + ctx.ID().getText() + " " + ctx.tipo().getText());
        return null;
    }

    @Override
    public String visitDeclArray(MiLenguajeParser.DeclArrayContext ctx) {
        emitir("DECLARE " + ctx.ID().getText() + "[" + ctx.INTEGER().getText() + "] " + ctx.tipo().getText());
        return null;
    }

    @Override
    public String visitSentDecl(MiLenguajeParser.SentDeclContext ctx) {
        return visit(ctx.variableDecl());
    }

    @Override
    public String visitSentAsignacion(MiLenguajeParser.SentAsignacionContext ctx) {
        MiLenguajeParser.ExprContext lhs = ctx.expr(0);
        String valor = visit(ctx.expr(1));

        if (lhs instanceof MiLenguajeParser.ExprPrimariaContext
            && ((MiLenguajeParser.ExprPrimariaContext) lhs).primaria() instanceof MiLenguajeParser.AccesoArrayContext) {
            MiLenguajeParser.AccesoArrayContext arr = (MiLenguajeParser.AccesoArrayContext) ((MiLenguajeParser.ExprPrimariaContext) lhs).primaria();
            String id = arr.ID().getText();
            String indice = visit(arr.expr());
            emitir(id + "[" + indice + "] = " + valor);
        } else {
            String target = visit(lhs);
            emitir(target + " = " + valor);
        }
        return null;
    }

    @Override
    public String visitSentAsignacionArray(MiLenguajeParser.SentAsignacionArrayContext ctx) {
        String id = ctx.ID().getText();
        String indice = visit(ctx.expr(0));
        String valor = visit(ctx.expr(1));
        emitir(id + "[" + indice + "] = " + valor);
        return null;
    }

    @Override
    public String visitSentIf(MiLenguajeParser.SentIfContext ctx) {
        String condicion = visit(ctx.expr());
        String thenLabel = nuevaLabel("THEN");
        String elseLabel = nuevaLabel("ELSE");
        String endLabel = nuevaLabel("END_IF");

        emitir("if " + condicion + " goto " + thenLabel);
        emitir("goto " + elseLabel);

        emitir(thenLabel + ":");
        visit(ctx.bloque(0));
        emitir("goto " + endLabel);

        emitir(elseLabel + ":");
        if (ctx.bloque().size() > 1) {
            visit(ctx.bloque(1));
        }

        emitir(endLabel + ":");
        return null;
    }

    @Override
    public String visitSentWhile(MiLenguajeParser.SentWhileContext ctx) {
        String inicioLabel = nuevaLabel("WHILE_INICIO");
        String cuerpoLabel = nuevaLabel("WHILE_CUERPO");
        String finLabel = nuevaLabel("WHILE_FIN");

        emitir(inicioLabel + ":");
        String condicion = visit(ctx.expr());
        emitir("if " + condicion + " goto " + cuerpoLabel);
        emitir("goto " + finLabel);

        emitir(cuerpoLabel + ":");
        visit(ctx.bloque());
        emitir("goto " + inicioLabel);

        emitir(finLabel + ":");
        return null;
    }

    @Override
    public String visitSentFor(MiLenguajeParser.SentForContext ctx) {
        String inicioLabel = nuevaLabel("FOR_INICIO");
        String cuerpoLabel = nuevaLabel("FOR_CUERPO");
        String actualizLabel = nuevaLabel("FOR_ACTUALIZ");
        String finLabel = nuevaLabel("FOR_FIN");

        // Inicialización (si existe)
        if (ctx.sentenciaOpcional(0) != null) {
            visit(ctx.sentenciaOpcional(0));
        }

        emitir(inicioLabel + ":");
        // Condición (si existe)
        if (ctx.expr() != null) {
            String condicion = visit(ctx.expr());
            emitir("if " + condicion + " goto " + cuerpoLabel);
            emitir("goto " + finLabel);
        }

        emitir(cuerpoLabel + ":");
        visit(ctx.bloque());
        
        emitir(actualizLabel + ":");
        // Actualización (si existe)
        if (ctx.sentenciaOpcional(1) != null) {
            visit(ctx.sentenciaOpcional(1));
        }
        emitir("goto " + inicioLabel);

        emitir(finLabel + ":");
        return null;
    }

    @Override
    public String visitSentBreak(MiLenguajeParser.SentBreakContext ctx) {
        emitir("break");
        return null;
    }

    @Override
    public String visitSentContinue(MiLenguajeParser.SentContinueContext ctx) {
        emitir("continue");
        return null;
    }

    @Override
    public String visitSentReturn(MiLenguajeParser.SentReturnContext ctx) {
        if (ctx.expr() != null) {
            String valor = visit(ctx.expr());
            emitir("return " + valor);
        } else {
            emitir("return");
        }
        return null;
    }

    @Override
    public String visitSentBloque(MiLenguajeParser.SentBloqueContext ctx) {
        return visit(ctx.bloque());
    }

    @Override
    public String visitSentExpr(MiLenguajeParser.SentExprContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public String visitBloque(MiLenguajeParser.BloqueContext ctx) {
        for (MiLenguajeParser.SentenciaContext s : ctx.sentencia()) {
            visit(s);
        }
        return null;
    }

    @Override
    public String visitExprLogica(MiLenguajeParser.ExprLogicaContext ctx) {
        String izq = visit(ctx.expr(0));
        String der = visit(ctx.expr(1));
        String op = ctx.opLogico().getText();
        String temp = nuevaTemp();
        emitir(temp + " = " + izq + " " + op + " " + der);
        return temp;
    }

    @Override
    public String visitExprRelacional(MiLenguajeParser.ExprRelacionalContext ctx) {
        String izq = visit(ctx.expr(0));
        String der = visit(ctx.expr(1));
        String op = ctx.opRelacional().getText();
        String temp = nuevaTemp();
        emitir(temp + " = " + izq + " " + op + " " + der);
        return temp;
    }

    @Override
    public String visitExprAritmetica(MiLenguajeParser.ExprAritmeticaContext ctx) {
        String izq = visit(ctx.expr(0));
        String der = visit(ctx.expr(1));
        String op = ctx.getChild(1).getText();
        String temp = nuevaTemp();
        emitir(temp + " = " + izq + " " + op + " " + der);
        return temp;
    }

    @Override
    public String visitExprMulDiv(MiLenguajeParser.ExprMulDivContext ctx) {
        String izq = visit(ctx.expr(0));
        String der = visit(ctx.expr(1));
        String op = ctx.getChild(1).getText();
        String temp = nuevaTemp();
        emitir(temp + " = " + izq + " " + op + " " + der);
        return temp;
    }

    @Override
    public String visitExprPrimaria(MiLenguajeParser.ExprPrimariaContext ctx) {
        return visit(ctx.primaria());
    }

    @Override
    public String visitIdentificador(MiLenguajeParser.IdentificadorContext ctx) {
        return ctx.ID().getText();
    }

    @Override
    public String visitNumero(MiLenguajeParser.NumeroContext ctx) {
        return ctx.INTEGER().getText();
    }

    @Override
    public String visitNumeroDecimal(MiLenguajeParser.NumeroDecimalContext ctx) {
        return ctx.DECIMAL().getText();
    }

    @Override
    public String visitLiteralChar(MiLenguajeParser.LiteralCharContext ctx) {
        return ctx.CHARACTER().getText();
    }

    @Override
    public String visitLiteralString(MiLenguajeParser.LiteralStringContext ctx) {
        return ctx.STR_LIT().getText();
    }

    @Override
    public String visitLiteralTrue(MiLenguajeParser.LiteralTrueContext ctx) {
        return "true";
    }

    @Override
    public String visitLiteralFalse(MiLenguajeParser.LiteralFalseContext ctx) {
        return "false";
    }

    @Override
    public String visitLlamadaFuncion(MiLenguajeParser.LlamadaFuncionContext ctx) {
        String nombre = ctx.ID().getText();
        List<String> args = new ArrayList<>();
        if (ctx.listaArgs() != null) {
            for (MiLenguajeParser.ExprContext e : ctx.listaArgs().expr()) {
                args.add(visit(e));
            }
        }
        String temp = nuevaTemp();
        emitir(temp + " = CALL func_" + nombre + ", " + String.join(", ", args));
        return temp;
    }

    @Override
    public String visitAccesoArray(MiLenguajeParser.AccesoArrayContext ctx) {
        String id = ctx.ID().getText();
        String indice = visit(ctx.expr());
        String temp = nuevaTemp();
        emitir(temp + " = " + id + "[" + indice + "]");
        return temp;
    }

    @Override
    public String visitParen(MiLenguajeParser.ParenContext ctx) {
        return visit(ctx.expr());
    }
}
