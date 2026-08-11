package com.compilador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OptimizadorCodigo {

    private enum Tipo {
        LABEL,
        GOTO,
        IF_GOTO,
        RETURN,
        DECLARE,
        PARAM,
        CALL,
        BINARY,
        ASSIGN,
        ARRAY_STORE,
        ARRAY_LOAD,
        COMMENT,
        OTHER
    }

    private static final Pattern LABEL_PATTERN = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)[:]");
    private static final Pattern IF_GOTO_PATTERN = Pattern.compile("^if\\s+(.+)\\s+goto\\s+([A-Za-z_][A-Za-z0-9_]*)$");
    private static final Pattern GOTO_PATTERN = Pattern.compile("^goto\\s+([A-Za-z_][A-Za-z0-9_]*)$");
    private static final Pattern CALL_PATTERN = Pattern.compile("^([A-Za-z0-9_]+)\\s*=\\s*CALL\\s+([A-Za-z0-9_]+),\\s*(.*)$");
    private static final Pattern ASSIGN_PATTERN = Pattern.compile("^(.+?)\\s*=\\s*(.+)$");
    private static final Pattern BINARY_PATTERN = Pattern.compile("^(.+)\\s+([+\\-*/<>=!]+)\\s+(.+)$");
    private static final Pattern CONSTANT_PATTERN = Pattern.compile("^(?:-?\\d+|true|false|'.'|\".*\")$");
    private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private static class Instr {
        Tipo tipo;
        String original;
        String label;
        String target;
        String cond;
        String dest;
        String rhs;
        String op;
        String arg1;
        String arg2;
        String funcName;
        List<String> args;

        Instr(String original) {
            this.original = original;
            this.args = new ArrayList<>();
        }

        void actualizarTexto() {
            switch (tipo) {
                case LABEL:
                    original = label + ":";
                    break;
                case GOTO:
                    original = "goto " + target;
                    break;
                case IF_GOTO:
                    original = "if " + cond + " goto " + target;
                    break;
                case RETURN:
                    original = (rhs == null || rhs.isEmpty()) ? "return" : "return " + rhs;
                    break;
                case DECLARE:
                    original = "DECLARE " + rhs;
                    break;
                case PARAM:
                    original = "PARAM " + rhs;
                    break;
                case CALL:
                    original = dest + " = CALL " + funcName;
                    if (!args.isEmpty()) {
                        original += ", " + String.join(", ", args);
                    }
                    break;
                case ARRAY_STORE:
                    original = dest + " = " + rhs;
                    break;
                case ARRAY_LOAD:
                    original = dest + " = " + rhs;
                    break;
                case BINARY:
                    original = dest + " = " + arg1 + " " + op + " " + arg2;
                    break;
                case ASSIGN:
                    original = dest + " = " + rhs;
                    break;
                case COMMENT:
                case OTHER:
                default:
                    break;
            }
        }

        boolean esConstante(String token) {
            if (token == null) return false;
            return CONSTANT_PATTERN.matcher(token.trim()).matches();
        }

        boolean isPure() {
            return tipo == Tipo.ASSIGN || tipo == Tipo.BINARY || tipo == Tipo.ARRAY_LOAD;
        }

        Set<String> nombresUsados() {
            Set<String> result = new HashSet<>();
            if (tipo == Tipo.BINARY) {
                result.addAll(nombresEnTexto(arg1));
                result.addAll(nombresEnTexto(arg2));
            } else if (tipo == Tipo.ASSIGN || tipo == Tipo.ARRAY_STORE || tipo == Tipo.ARRAY_LOAD) {
                result.addAll(nombresEnTexto(rhs));
            } else if (tipo == Tipo.IF_GOTO) {
                result.addAll(nombresEnTexto(cond));
            } else if (tipo == Tipo.RETURN) {
                result.addAll(nombresEnTexto(rhs));
            } else if (tipo == Tipo.CALL) {
                for (String arg : args) {
                    result.addAll(nombresEnTexto(arg));
                }
            }
            return result;
        }

        private Set<String> nombresEnTexto(String texto) {
            Set<String> nombres = new HashSet<>();
            if (texto == null) return nombres;
            Matcher matcher = NAME_PATTERN.matcher(texto);
            while (matcher.find()) {
                String nombre = matcher.group();
                if (!"true".equals(nombre) && !"false".equals(nombre) && !"goto".equals(nombre) && !"if".equals(nombre) && !"CALL".equals(nombre)) {
                    nombres.add(nombre);
                }
            }
            return nombres;
        }
    }

    public static List<String> optimizar(List<String> codigoOriginal) {
        if (codigoOriginal == null) return null;
        List<Instr> instrs = parse(codigoOriginal);
        boolean cambiado;
        int iter = 0;
        do {
            cambiado = false;
            cambiado |= simplificarExpresiones(instrs);
            cambiado |= propagacionConstantes(instrs);
            cambiado |= eliminarSubexpresionesComunes(instrs);
            cambiado |= eliminarCodigoMuerto(instrs);
            cambiado |= optimizacionBucles(instrs);
            iter++;
        } while (cambiado && iter < 6);
        return formatear(instrs);
    }

    public static List<String> aplicarSimplificacionExpresiones(List<String> codigo) {
        List<Instr> instrs = parse(codigo);
        simplificarExpresiones(instrs);
        return formatear(instrs);
    }

    public static List<String> aplicarPropagacionConstantes(List<String> codigo) {
        List<Instr> instrs = parse(codigo);
        propagacionConstantes(instrs);
        return formatear(instrs);
    }

    public static List<String> aplicarEliminacionSubexpresiones(List<String> codigo) {
        List<Instr> instrs = parse(codigo);
        eliminarSubexpresionesComunes(instrs);
        return formatear(instrs);
    }

    public static List<String> aplicarEliminacionCodigoMuerto(List<String> codigo) {
        List<Instr> instrs = parse(codigo);
        eliminarCodigoMuerto(instrs);
        return formatear(instrs);
    }

    public static List<String> aplicarOptimizacionBucles(List<String> codigo) {
        List<Instr> instrs = parse(codigo);
        optimizacionBucles(instrs);
        return formatear(instrs);
    }

    public static List<Instr> parse(List<String> codigoOriginal) {
        List<Instr> instrs = new ArrayList<>();
        for (String linea : codigoOriginal) {
            String texto = linea.trim();
            Instr instr = new Instr(texto);

            if (texto.isEmpty()) {
                instr.tipo = Tipo.OTHER;
                instrs.add(instr);
                continue;
            }
            if (texto.startsWith("//")) {
                instr.tipo = Tipo.COMMENT;
                instrs.add(instr);
                continue;
            }
            Matcher m;
            m = LABEL_PATTERN.matcher(texto);
            if (m.matches()) {
                instr.tipo = Tipo.LABEL;
                instr.label = m.group(1);
                instrs.add(instr);
                continue;
            }
            m = IF_GOTO_PATTERN.matcher(texto);
            if (m.matches()) {
                instr.tipo = Tipo.IF_GOTO;
                instr.cond = m.group(1).trim();
                instr.target = m.group(2);
                instrs.add(instr);
                continue;
            }
            m = GOTO_PATTERN.matcher(texto);
            if (m.matches()) {
                instr.tipo = Tipo.GOTO;
                instr.target = m.group(1);
                instrs.add(instr);
                continue;
            }
            if (texto.startsWith("return")) {
                instr.tipo = Tipo.RETURN;
                String[] parts = texto.split("\\s+", 2);
                instr.rhs = parts.length > 1 ? parts[1].trim() : null;
                instrs.add(instr);
                continue;
            }
            if (texto.startsWith("DECLARE ")) {
                instr.tipo = Tipo.DECLARE;
                instr.rhs = texto.substring("DECLARE ".length()).trim();
                instrs.add(instr);
                continue;
            }
            if (texto.startsWith("PARAM ")) {
                instr.tipo = Tipo.PARAM;
                instr.rhs = texto.substring("PARAM ".length()).trim();
                instrs.add(instr);
                continue;
            }
            m = ASSIGN_PATTERN.matcher(texto);
            if (m.matches()) {
                String lhs = m.group(1).trim();
                String rhs = m.group(2).trim();
                if (rhs.startsWith("CALL ")) {
                    instr.tipo = Tipo.CALL;
                    instr.dest = lhs;
                    String callExpr = rhs.substring("CALL ".length()).trim();
                    int comma = callExpr.indexOf(',');
                    if (comma > 0) {
                        instr.funcName = callExpr.substring(0, comma).trim();
                        String rest = callExpr.substring(comma + 1).trim();
                        for (String arg : rest.split(",")) {
                            instr.args.add(arg.trim());
                        }
                    } else {
                        instr.funcName = callExpr;
                    }
                } else if (lhs.contains("[")) {
                    instr.tipo = Tipo.ARRAY_STORE;
                    instr.dest = lhs;
                    instr.rhs = rhs;
                } else {
                    Matcher binary = BINARY_PATTERN.matcher(rhs);
                    if (binary.matches()) {
                        instr.tipo = Tipo.BINARY;
                        instr.dest = lhs;
                        instr.arg1 = binary.group(1).trim();
                        instr.op = binary.group(2).trim();
                        instr.arg2 = binary.group(3).trim();
                    } else {
                        instr.tipo = Tipo.ASSIGN;
                        instr.dest = lhs;
                        instr.rhs = rhs;
                        if (rhs.contains("[")) {
                            instr.tipo = Tipo.ARRAY_LOAD;
                        }
                    }
                }
                instrs.add(instr);
                continue;
            }
            instr.tipo = Tipo.OTHER;
            instrs.add(instr);
        }
        return instrs;
    }

    public static List<String> formatear(List<Instr> instrs) {
        List<String> resultado = new ArrayList<>();
        for (Instr instr : instrs) {
            resultado.add(instr.original);
        }
        return resultado;
    }

    private static boolean simplificarExpresiones(List<Instr> instrs) { //Elimina operaciones indefinidas(a+0, a*1, a*0, etc) y evalúa operaciones constantes (2+3, 4*5, etc).
        boolean cambiado = false;
        for (Instr instr : instrs) {
            if (instr.tipo == Tipo.BINARY) {
                String a = instr.arg1;
                String b = instr.arg2;
                if (instr.esConstante(a) && instr.esConstante(b)) {
                    String valor = evaluarConstante(a, instr.op, b);
                    if (valor != null) {
                        instr.tipo = Tipo.ASSIGN;
                        instr.rhs = valor;
                        instr.op = null;
                        instr.arg1 = null;
                        instr.arg2 = null;
                        instr.actualizarTexto();
                        cambiado = true;
                        continue;
                    }
                }
                String simplificado = simplificarIdentidad(instr.op, a, b);
                if (!simplificado.equals(a + " " + instr.op + " " + b)) {
                    if (simplificado != null && !simplificado.isEmpty()) {
                        if (simplificado.contains(" ")) {
                            Matcher m = BINARY_PATTERN.matcher(simplificado);
                            if (m.matches()) {
                                instr.arg1 = m.group(1).trim();
                                instr.op = m.group(2).trim();
                                instr.arg2 = m.group(3).trim();
                            }
                        } else {
                            instr.tipo = Tipo.ASSIGN;
                            instr.rhs = simplificado;
                            instr.op = null;
                            instr.arg1 = null;
                            instr.arg2 = null;
                        }
                        instr.actualizarTexto();
                        cambiado = true;
                    }
                }
            } else if (instr.tipo == Tipo.ASSIGN && instr.rhs != null && instr.rhs.equals(instr.dest)) {
                instr.tipo = Tipo.COMMENT;
                instr.original = "// Eliminada asignación redundante";
                cambiado = true;
            }
        }
        return cambiado;
    }

    private static String simplificarIdentidad(String op, String a, String b) {
        if ("+".equals(op)) {
            if ("0".equals(a)) return b;
            if ("0".equals(b)) return a;
        }
        if ("-".equals(op)) {
            if ("0".equals(b)) return a;
            if (a.equals(b)) return "0";
        }
        if ("*".equals(op)) {
            if ("1".equals(a)) return b;
            if ("1".equals(b)) return a;
            if ("0".equals(a) || "0".equals(b)) return "0";
        }
        if ("/".equals(op)) {
            if ("1".equals(b)) return a;
        }
        if ("&&".equals(op)) {
            if ("true".equals(a)) return b;
            if ("true".equals(b)) return a;
            if ("false".equals(a) || "false".equals(b)) return "false";
        }
        if ("||".equals(op)) {
            if ("false".equals(a)) return b;
            if ("false".equals(b)) return a;
            if ("true".equals(a) || "true".equals(b)) return "true";
        }
        if (("==".equals(op) || "!=".equals(op)) && a.equals(b)) {
            return "true".equals(op) ? "true" : "false";
        }
        return a + " " + op + " " + b;
    }

    private static String evaluarConstante(String a, String op, String b) {
        try {
            if (a.startsWith("\"") || a.startsWith("'")) {
                return null;
            }
            boolean aBool = "true".equals(a) || "false".equals(a);
            boolean bBool = "true".equals(b) || "false".equals(b);
            if (aBool && bBool) {
                boolean va = Boolean.parseBoolean(a);
                boolean vb = Boolean.parseBoolean(b);
                switch (op) {
                    case "&&": return Boolean.toString(va && vb);
                    case "||": return Boolean.toString(va || vb);
                    case "==": return Boolean.toString(va == vb);
                    case "!=": return Boolean.toString(va != vb);
                }
                return null;
            }
            long va = Long.parseLong(a);
            long vb = Long.parseLong(b);
            switch (op) {
                case "+": return Long.toString(va + vb);
                case "-": return Long.toString(va - vb);
                case "*": return Long.toString(va * vb);
                case "/": return vb == 0 ? null : Long.toString(va / vb);
                case "==": return Boolean.toString(va == vb);
                case "!=": return Boolean.toString(va != vb);
                case ">": return Boolean.toString(va > vb);
                case ">=": return Boolean.toString(va >= vb);
                case "<": return Boolean.toString(va < vb);
                case "<=": return Boolean.toString(va <= vb);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    private static boolean propagacionConstantes(List<Instr> instrs) { // Reemplaza variables por constantes conocidas y evalúa condiciones en IF_GOTO.
        boolean cambiado = false;
        Map<String, String> constantes = new HashMap<>();
        for (Instr instr : instrs) {
            if (instr.tipo == Tipo.BINARY) {
                instr.arg1 = reemplazarConstante(instr.arg1, constantes);
                instr.arg2 = reemplazarConstante(instr.arg2, constantes);
                instr.actualizarTexto();
                if (instr.esConstante(instr.arg1) && instr.esConstante(instr.arg2)) {
                    String valor = evaluarConstante(instr.arg1, instr.op, instr.arg2);
                    if (valor != null) {
                        instr.tipo = Tipo.ASSIGN;
                        instr.rhs = valor;
                        instr.op = null;
                        instr.arg1 = null;
                        instr.arg2 = null;
                        instr.actualizarTexto();
                        constantes.put(instr.dest, valor);
                        cambiado = true;
                        continue;
                    }
                }
                constantes.remove(instr.dest);
            } else if (instr.tipo == Tipo.ASSIGN || instr.tipo == Tipo.ARRAY_LOAD) {
                instr.rhs = reemplazarConstante(instr.rhs, constantes);
                instr.actualizarTexto();
                if (instr.esConstante(instr.rhs)) {
                    constantes.put(instr.dest, instr.rhs);
                    cambiado = true;
                } else {
                    constantes.remove(instr.dest);
                }
            } else if (instr.tipo == Tipo.IF_GOTO) {
                String nuevaCond = reemplazarConstante(instr.cond, constantes);
                if (!nuevaCond.equals(instr.cond)) {
                    instr.cond = nuevaCond;
                    instr.actualizarTexto();
                    cambiado = true;
                }
            } else if (instr.tipo == Tipo.RETURN) {
                if (instr.rhs != null) {
                    String nuevo = reemplazarConstante(instr.rhs, constantes);
                    if (!nuevo.equals(instr.rhs)) {
                        instr.rhs = nuevo;
                        instr.actualizarTexto();
                        cambiado = true;
                    }
                }
            } else if (instr.tipo == Tipo.CALL) {
                boolean any = false;
                for (int i = 0; i < instr.args.size(); i++) {
                    String reemplazo = reemplazarConstante(instr.args.get(i), constantes);
                    if (!reemplazo.equals(instr.args.get(i))) {
                        instr.args.set(i, reemplazo);
                        any = true;
                    }
                }
                if (any) {
                    instr.actualizarTexto();
                    cambiado = true;
                }
                constantes.remove(instr.dest);
            } else {
                if (instr.dest != null) {
                    constantes.remove(instr.dest);
                }
                // Limpiar constantes al entrar a una nueva funcion
                if (instr.tipo == Tipo.LABEL && instr.label != null && instr.label.startsWith("func_")) {
                    constantes.clear();
                }
                // Eliminar parametros del mapa de constantes
                if (instr.tipo == Tipo.PARAM && instr.rhs != null) {
                    String paramName = instr.rhs.split(" ")[0];
                    constantes.remove(paramName);
                }
            }
        }
        return cambiado;
    }

    private static String reemplazarConstante(String operand, Map<String, String> constantes) {
        if (operand == null) return null;
        String trimmed = operand.trim();
        if (constantes.containsKey(trimmed)) {
            return constantes.get(trimmed);
        }
        return operand;
    }

    private static boolean eliminarSubexpresionesComunes(List<Instr> instrs) { //Detecta a+b repetido y lo reemplaza por una variable temporal.
        boolean cambiado = false;
        Map<String, String> expToTemp = new HashMap<>();
        for (Instr instr : instrs) {
            if (instr.tipo == Tipo.BINARY) {
                String key = generarClave(instr.op, instr.arg1, instr.arg2);
                if (expToTemp.containsKey(key)) {
                    String existing = expToTemp.get(key);
                    instr.tipo = Tipo.ASSIGN;
                    instr.rhs = existing;
                    instr.arg1 = null;
                    instr.arg2 = null;
                    instr.op = null;
                    instr.actualizarTexto();
                    cambiado = true;
                } else {
                    expToTemp.put(key, instr.dest);
                }
            }
        }
        return cambiado;
    }

    private static String generarClave(String op, String arg1, String arg2) {
        if ("+".equals(op) || "*".equals(op) || "==".equals(op) || "!=".equals(op)) {
            List<String> partes = new ArrayList<>();
            partes.add(arg1);
            partes.add(arg2);
            partes.sort(String::compareTo);
            return op + "|" + partes.get(0) + "|" + partes.get(1);
        }
        return op + "|" + arg1 + "|" + arg2;
    }

    private static boolean eliminarCodigoMuerto(List<Instr> instrs) {
        boolean changed = false;

        // 1) Eliminar codigo inalcanzable despues de return o goto incondicional
        int prevLabel = -1;
        for (int i = 0; i < instrs.size(); i++) {
            Instr instr = instrs.get(i);
            if (instr.tipo == Tipo.LABEL) {
                prevLabel = i;
            }
            if ((instr.tipo == Tipo.RETURN || instr.tipo == Tipo.GOTO) && i + 1 < instrs.size()) {
                Instr next = instrs.get(i + 1);
                if (next.tipo != Tipo.LABEL && next.tipo != Tipo.COMMENT) {
                    // Marcar como comentario para mantener el numero de linea
                    next.tipo = Tipo.COMMENT;
                    next.original = "// CODIGO MUERTO: " + next.original;
                    changed = true;
                }
            }
        }

        // 2) Eliminar asignaciones a variables/temporales que nunca se usan despues
        Set<String> used = new HashSet<>();
        ListIterator<Instr> iter = instrs.listIterator(instrs.size());
        while (iter.hasPrevious()) {
            Instr instr = iter.previous();
            if (instr.tipo == Tipo.COMMENT && instr.original.startsWith("// CODIGO MUERTO:")) {
                iter.remove();
                changed = true;
                continue;
            }
            if ((instr.tipo == Tipo.ASSIGN || instr.tipo == Tipo.BINARY || instr.tipo == Tipo.ARRAY_LOAD) && instr.dest != null && !used.contains(instr.dest) && instr.isPure()) {
                if (!instr.dest.startsWith("t") && instr.tipo == Tipo.ASSIGN) {
                    addUses(instr, used);
                } else {
                    iter.remove();
                    changed = true;
                    continue;
                }
            } else {
                addUses(instr, used);
            }
        }
        return changed;
    }

    private static void addUses(Instr instr, Set<String> used) {
        for (String nombre : instr.nombresUsados()) {
            used.add(nombre);
        }
    }

    private static boolean optimizacionBucles(List<Instr> instrs) { //mueve codigo constante fuera del loop
        boolean changed = false;
        for (int i = 0; i < instrs.size(); i++) {
            Instr inicio = instrs.get(i);
            if (inicio.tipo != Tipo.LABEL || !inicio.label.startsWith("WHILE_INICIO_")) {
                continue;
            }
            if (i + 2 >= instrs.size()) continue;
            Instr ifInstr = instrs.get(i + 1);
            Instr gotoInstr = instrs.get(i + 2);
            if (ifInstr.tipo != Tipo.IF_GOTO || gotoInstr.tipo != Tipo.GOTO) continue;
            String bodyLabel = ifInstr.target;
            int bodyIndex = encontrarLabel(instrs, bodyLabel);
            if (bodyIndex < 0) continue;
            int loopBackIndex = -1;
            for (int j = bodyIndex + 1; j < instrs.size(); j++) {
                Instr candidate = instrs.get(j);
                if (candidate.tipo == Tipo.GOTO && bodyLabel.equals(candidate.target)) {
                    loopBackIndex = j;
                    break;
                }
            }
            if (loopBackIndex < 0) continue;
            Set<String> assignedEnBucle = new HashSet<>();
            for (int j = bodyIndex + 1; j < loopBackIndex; j++) {
                Instr candidate = instrs.get(j);
                if (candidate.dest != null) {
                    assignedEnBucle.add(candidate.dest);
                }
            }
            List<Instr> invariantes = new ArrayList<>();
            for (int j = bodyIndex + 1; j < loopBackIndex; j++) {
                Instr candidate = instrs.get(j);
                if (candidate.isPure() && candidate.dest != null && candidate.dest.startsWith("t")) {
                    Set<String> usados = candidate.nombresUsados();
                    boolean puedeHoist = true;
                    for (String nombre : usados) {
                        if (assignedEnBucle.contains(nombre)) {
                            puedeHoist = false;
                            break;
                        }
                    }
                    if (puedeHoist) {
                        invariantes.add(candidate);
                    }
                }
            }
            if (!invariantes.isEmpty()) {
                for (Instr instr : invariantes) {
                    instrs.remove(instr);
                }
                instrs.addAll(i, invariantes);
                changed = true;
            }
        }
        return changed;
    }

    private static int encontrarLabel(List<Instr> instrs, String label) {
        for (int i = 0; i < instrs.size(); i++) {
            Instr instr = instrs.get(i);
            if (instr.tipo == Tipo.LABEL && label.equals(instr.label)) {
                return i;
            }
        }
        return -1;
    }
}
