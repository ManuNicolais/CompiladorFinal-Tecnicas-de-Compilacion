package com.compilador;

public class SemanticoVisitor extends MiLenguajeBaseVisitor<String> {

    private TablaSimbolos tabla;
    private String ambitoActual = "global";
    private String tipoFuncionActual = "";
    private int nivelBreak = 0;

    public SemanticoVisitor(TablaSimbolos tabla) {
        this.tabla = tabla;
    }

    private boolean esNumerico(String tipo) {
        return "int".equals(tipo) || "double".equals(tipo) || "char".equals(tipo);
    }

    private boolean esCompatible(String lhs, String rhs) {
        if (lhs == null || rhs == null) return false;
        if (lhs.equals(rhs)) return true;
        if ("int".equals(lhs) && "char".equals(rhs)) return true;
        if ("double".equals(lhs) && ("int".equals(rhs) || "char".equals(rhs))) return true;
        return false;
    }

    private String promoverTipos(String t1, String t2) {
        if (t1 == null || t2 == null) return "error";
        if ("double".equals(t1) || "double".equals(t2)) return "double";
        if ("int".equals(t1) || "int".equals(t2)) return "int";
        if ("char".equals(t1) || "char".equals(t2)) return "int";
        return "error";
    }

    @Override
    public String visitPrograma(MiLenguajeParser.ProgramaContext ctx) {
        for (MiLenguajeParser.DeclaracionGlobalContext d : ctx.declaracionGlobal()) {
            visit(d);
        }
        return null;
    }

    @Override
    public String visitDeclFuncion(MiLenguajeParser.DeclFuncionContext ctx) {
        String nombre = ctx.ID().getText();
        String tipoRet = ctx.tipo().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int col = ctx.ID().getSymbol().getCharPositionInLine();

        tabla.agregar(nombre, tipoRet, "funcion", linea, col);

        TablaSimbolos.Simbolo func = tabla.buscar(nombre);
        if (func != null && ctx.listaParams() != null) {
            func.cantParams = ctx.listaParams().param().size();
        }

        tipoFuncionActual = tipoRet;
        ambitoActual = nombre;
        tabla.pushScope(nombre);
        tabla.setAmbitoActual(nombre);

        if (ctx.listaParams() != null) {
            for (MiLenguajeParser.ParamContext p : ctx.listaParams().param()) {
                String pNombre = p.ID().getText();
                String pTipo = p.tipo().getText();
                int pLinea = p.ID().getSymbol().getLine();
                int pCol = p.ID().getSymbol().getCharPositionInLine();
                tabla.agregar(pNombre, pTipo, "parametro", pLinea, pCol);
                if (func != null) {
                    func.tiposParams.add(pTipo);
                }
            }
        }

        for (MiLenguajeParser.SentenciaContext s : ctx.bloque().sentencia()) {
            visit(s);
        }

        tabla.popScope();
        tipoFuncionActual = "";
        ambitoActual = "global";
        return null;
    }

    @Override
    public String visitDeclVariable(MiLenguajeParser.DeclVariableContext ctx) {
        String tipo = ctx.tipo().getText();
        String nombre = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int col = ctx.ID().getSymbol().getCharPositionInLine();
        tabla.agregar(nombre, tipo, "variable", linea, col);
        return null;
    }

    @Override
    public String visitDeclArray(MiLenguajeParser.DeclArrayContext ctx) {
        String tipo = ctx.tipo().getText();
        String nombre = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int col = ctx.ID().getSymbol().getCharPositionInLine();
        String tamano = ctx.INTEGER().getText();
        tabla.agregar(nombre, tipo, "variable", linea, col, tamano);
        return null;
    }

    @Override
    public String visitSentDecl(MiLenguajeParser.SentDeclContext ctx) {
        return visit(ctx.variableDecl());
    }

    @Override
    public String visitSentAsignacion(MiLenguajeParser.SentAsignacionContext ctx) {
        MiLenguajeParser.ExprContext lhs = ctx.expr(0);
        int linea = ctx.getStart().getLine();

        if (lhs instanceof MiLenguajeParser.ExprPrimariaContext) {
            MiLenguajeParser.PrimariaContext primaria = ((MiLenguajeParser.ExprPrimariaContext) lhs).primaria();
            if (primaria instanceof MiLenguajeParser.IdentificadorContext) {
                String id = ((MiLenguajeParser.IdentificadorContext) primaria).ID().getText();
                int l = ((MiLenguajeParser.IdentificadorContext) primaria).ID().getSymbol().getLine();
                TablaSimbolos.Simbolo simbolo = tabla.buscar(id);
                if (simbolo != null && "funcion".equals(simbolo.categoria)) {
                    tabla.agregarError("No se puede asignar valor a '" + id + "' porque no es una variable (línea " + l + ")");
                }
            }
        }

        String tipoLHS = visit(ctx.expr(0));
        String tipoRHS = visit(ctx.expr(1));

        if (tipoLHS != null && !"error".equals(tipoLHS) && tipoRHS != null && !"error".equals(tipoRHS)) {
            if (!esCompatible(tipoLHS, tipoRHS)) {
                tabla.agregarError("Error de tipo: no se puede asignar valor de tipo '" + tipoRHS + "' a variable de tipo '" + tipoLHS + "' (línea " + linea + ")");
            }
        }
        return null;
    }

    @Override
    public String visitSentAsignacionArray(MiLenguajeParser.SentAsignacionArrayContext ctx) {
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        TablaSimbolos.Simbolo arr = tabla.buscar(id);
        if (arr == null) {
            tabla.agregarError("Variable '" + id + "' no declarada (línea " + linea + ")");
        } else {
            tabla.marcarUsada(id);
            String tipoRHS = visit(ctx.expr(1));
            if (tipoRHS != null && !"error".equals(tipoRHS) && !esCompatible(arr.tipo, tipoRHS)) {
                tabla.agregarError("Error de tipo: no se puede asignar valor de tipo '" + tipoRHS + "' a variable de tipo '" + arr.tipo + "' (línea " + linea + ")");
            }
        }
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        return null;
    }

    @Override
    public String visitSentIf(MiLenguajeParser.SentIfContext ctx) {
        String tipoCond = visit(ctx.expr());
        if (tipoCond != null && !"error".equals(tipoCond) && !"bool".equals(tipoCond)) {
            tabla.agregarError("Error de tipo: condición del if debe ser de tipo 'bool', se obtuvo '" + tipoCond + "' (línea " + ctx.getStart().getLine() + ")");
        }
        visit(ctx.bloque(0));
        if (ctx.bloque().size() > 1) {
            visit(ctx.bloque(1));
        }
        return null;
    }

    @Override
    public String visitSentWhile(MiLenguajeParser.SentWhileContext ctx) {
        String tipoCond = visit(ctx.expr());
        if (tipoCond != null && !"error".equals(tipoCond) && !"bool".equals(tipoCond)) {
            tabla.agregarError("Error de tipo: condición del while debe ser de tipo 'bool', se obtuvo '" + tipoCond + "' (línea " + ctx.getStart().getLine() + ")");
        }
        nivelBreak++;
        visit(ctx.bloque());
        nivelBreak--;
        return null;
    }

    @Override
    public String visitSentFor(MiLenguajeParser.SentForContext ctx) {
        if (ctx.sentenciaOpcional(0) != null) {
            visit(ctx.sentenciaOpcional(0));
        }
        if (ctx.expr() != null) {
            String tipoCond = visit(ctx.expr());
            if (tipoCond != null && !"error".equals(tipoCond) && !"bool".equals(tipoCond)) {
                tabla.agregarError("Error de tipo: condición del for debe ser de tipo 'bool', se obtuvo '" + tipoCond + "' (línea " + ctx.getStart().getLine() + ")");
            }
        }
        if (ctx.sentenciaOpcional(1) != null) {
            visit(ctx.sentenciaOpcional(1));
        }
        nivelBreak++;
        visit(ctx.bloque());
        nivelBreak--;
        return null;
    }

    @Override
    public String visitSentBreak(MiLenguajeParser.SentBreakContext ctx) {
        if (nivelBreak <= 0) {
            tabla.agregarError("Error de tipo: break solo puede usarse dentro de un bucle (línea " + ctx.getStart().getLine() + ")");
        }
        return null;
    }

    @Override
    public String visitSentContinue(MiLenguajeParser.SentContinueContext ctx) {
        if (nivelBreak <= 0) {
            tabla.agregarError("Error de tipo: continue solo puede usarse dentro de un bucle (línea " + ctx.getStart().getLine() + ")");
        }
        return null;
    }

    @Override
    public String visitSentReturn(MiLenguajeParser.SentReturnContext ctx) {
        int linea = ctx.getStart().getLine();
        if (ctx.expr() != null) {
            String tipoRet = visit(ctx.expr());
            if ("void".equals(tipoFuncionActual)) {
                tabla.agregarWarning("retorno explícito en función con tipo de retorno 'void' (línea " + linea + ")");
            } else if (tipoRet != null && !"error".equals(tipoRet) && !esCompatible(tipoFuncionActual, tipoRet)) {
                tabla.agregarError("Error de tipo: la función '" + ambitoActual + "' retorna tipo '" + tipoRet + "' pero se esperaba '" + tipoFuncionActual + "' (línea " + linea + ")");
            }
        } else {
            if (!"void".equals(tipoFuncionActual)) {
                tabla.agregarError("Error de tipo: la función '" + ambitoActual + "' debe retornar un valor de tipo '" + tipoFuncionActual + "' (línea " + linea + ")");
            }
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
        tabla.pushScope(ambitoActual);
        tabla.setAmbitoActual(ambitoActual);
        for (MiLenguajeParser.SentenciaContext s : ctx.sentencia()) {
            visit(s);
        }
        tabla.popScope();
        return null;
    }

    @Override
    public String visitExprLogica(MiLenguajeParser.ExprLogicaContext ctx) {
        String t1 = visit(ctx.expr(0));
        String t2 = visit(ctx.expr(1));
        int linea = ctx.getStart().getLine();
        if (t1 != null && !"error".equals(t1) && !"bool".equals(t1)) {
            tabla.agregarError("Error de tipo: operador lógico no compatible con tipo '" + t1 + "' (línea " + linea + ")");
        }
        if (t2 != null && !"error".equals(t2) && !"bool".equals(t2)) {
            tabla.agregarError("Error de tipo: operador lógico no compatible con tipo '" + t2 + "' (línea " + linea + ")");
        }
        return "bool";
    }

    @Override
    public String visitExprRelacional(MiLenguajeParser.ExprRelacionalContext ctx) {
        String t1 = visit(ctx.expr(0));
        String t2 = visit(ctx.expr(1));
        int linea = ctx.getStart().getLine();
        if (t1 != null && t2 != null && !"error".equals(t1) && !"error".equals(t2)) {
            if (!t1.equals(t2) && !(esNumerico(t1) && esNumerico(t2))) {
                tabla.agregarError("Error de tipo: no se puede comparar tipo '" + t1 + "' con tipo '" + t2 + "' (línea " + linea + ")");
            }
        }
        return "bool";
    }

    @Override
    public String visitExprAritmetica(MiLenguajeParser.ExprAritmeticaContext ctx) {
        String t1 = visit(ctx.expr(0));
        String t2 = visit(ctx.expr(1));
        String op = ctx.getChild(1).getText();
        int linea = ctx.getStart().getLine();

        if (t1 != null && t2 != null && !"error".equals(t1) && !"error".equals(t2)) {
            if ("string".equals(t1) && "string".equals(t2) && "+".equals(op)) {
                return "string";
            }
            if (!esNumerico(t1)) {
                tabla.agregarError("Error de tipo: operador '" + op + "' no compatible con tipo '" + t1 + "' (línea " + linea + ")");
                return "error";
            }
            if (!esNumerico(t2)) {
                tabla.agregarError("Error de tipo: operador '" + op + "' no compatible con tipo '" + t2 + "' (línea " + linea + ")");
                return "error";
            }
            return promoverTipos(t1, t2);
        }
        return "error";
    }

    @Override
    public String visitExprMulDiv(MiLenguajeParser.ExprMulDivContext ctx) {
        String t1 = visit(ctx.expr(0));
        String t2 = visit(ctx.expr(1));
        String op = ctx.getChild(1).getText();
        int linea = ctx.getStart().getLine();

        if (t1 != null && t2 != null && !"error".equals(t1) && !"error".equals(t2)) {
            if (!esNumerico(t1)) {
                tabla.agregarError("Error de tipo: operador '" + op + "' no compatible con tipo '" + t1 + "' (línea " + linea + ")");
                return "error";
            }
            if (!esNumerico(t2)) {
                tabla.agregarError("Error de tipo: operador '" + op + "' no compatible con tipo '" + t2 + "' (línea " + linea + ")");
                return "error";
            }
            return promoverTipos(t1, t2);
        }
        return "error";
    }

    @Override
    public String visitExprPrimaria(MiLenguajeParser.ExprPrimariaContext ctx) {
        return visit(ctx.primaria());
    }

    @Override
    public String visitIdentificador(MiLenguajeParser.IdentificadorContext ctx) {
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        TablaSimbolos.Simbolo simbolo = tabla.buscar(id);
        if (simbolo == null) {
            tabla.agregarError("Variable '" + id + "' no declarada (línea " + linea + ")");
            return "error";
        }
        tabla.marcarUsada(id);
        return simbolo.tipo;
    }

    @Override
    public String visitLlamadaFuncion(MiLenguajeParser.LlamadaFuncionContext ctx) {
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        TablaSimbolos.Simbolo func = tabla.buscar(id);
        if (func == null) {
            tabla.agregarError("Función '" + id + "' no declarada (línea " + linea + ")");
            return "error";
        }
        tabla.marcarUsada(id);

        int argsDado = ctx.listaArgs() != null ? ctx.listaArgs().expr().size() : 0;
        if (func.cantParams != argsDado) {
            tabla.agregarError("Función '" + id + "' espera " + func.cantParams + " argumentos, recibió " + argsDado + " (línea " + linea + ")");
        }

        if (ctx.listaArgs() != null) {
            for (int i = 0; i < ctx.listaArgs().expr().size(); i++) {
                String tipoArg = visit(ctx.listaArgs().expr(i));
                if (i < func.tiposParams.size() && tipoArg != null && !"error".equals(tipoArg)) {
                    String tipoParam = func.tiposParams.get(i);
                    if (!esCompatible(tipoParam, tipoArg)) {
                        tabla.agregarError("Error de tipo: argumento " + (i + 1) + " de '" + id + "' tiene tipo '" + tipoArg + "' pero se esperaba '" + tipoParam + "' (línea " + linea + ")");
                    }
                }
            }
        }
        return func.tipo;
    }

    @Override
    public String visitAccesoArray(MiLenguajeParser.AccesoArrayContext ctx) {
        String id = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        TablaSimbolos.Simbolo simbolo = tabla.buscar(id);
        if (simbolo == null) {
            tabla.agregarError("Variable '" + id + "' no declarada (línea " + linea + ")");
            return "error";
        }
        tabla.marcarUsada(id);
        visit(ctx.expr());
        return simbolo.tipo;
    }

    @Override
    public String visitNumero(MiLenguajeParser.NumeroContext ctx) {
        return "int";
    }

    @Override
    public String visitNumeroDecimal(MiLenguajeParser.NumeroDecimalContext ctx) {
        return "double";
    }

    @Override
    public String visitLiteralChar(MiLenguajeParser.LiteralCharContext ctx) {
        return "char";
    }

    @Override
    public String visitLiteralString(MiLenguajeParser.LiteralStringContext ctx) {
        return "string";
    }

    @Override
    public String visitLiteralTrue(MiLenguajeParser.LiteralTrueContext ctx) { return "bool"; }

    @Override
    public String visitLiteralFalse(MiLenguajeParser.LiteralFalseContext ctx) { return "bool"; }

    @Override
    public String visitParen(MiLenguajeParser.ParenContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public String visitSentOpcExpr(MiLenguajeParser.SentOpcExprContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        return null;
    }

    @Override
    public String visitSentOpcDecl(MiLenguajeParser.SentOpcDeclContext ctx) {
        return visit(ctx.variableDecl());
    }
}
