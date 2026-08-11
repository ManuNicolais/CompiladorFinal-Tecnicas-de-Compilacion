package com.compilador;

import java.util.*;

public class TablaSimbolos {

    public static class Simbolo {
        public String nombre;
        public String tipo;
        public String categoria;
        public int linea;
        public int columna;
        public String ambito;
        public boolean usada;
        public int cantParams;
        public List<String> tiposParams;
        public String tamanoArray;

        public Simbolo(String nombre, String tipo, String categoria, int linea, int columna, String ambito) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.categoria = categoria;
            this.linea = linea;
            this.columna = columna;
            this.ambito = ambito;
            this.usada = false;
            this.cantParams = 0;
            this.tiposParams = new ArrayList<>();
            this.tamanoArray = null;
        }
    }

    private List<Map<String, Simbolo>> scopes = new ArrayList<>();
    private List<Simbolo> todosSimbolos = new ArrayList<>();
    private List<String> errores = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private String ambitoActual = "global";

    public TablaSimbolos() {
        scopes.add(new LinkedHashMap<>());
    }

    public void pushScope(String ambito) {
        scopes.add(new LinkedHashMap<>());
        ambitoActual = ambito;
    }

    public void popScope() {
        if (scopes.size() > 1) {
            Map<String, Simbolo> scope = scopes.remove(scopes.size() - 1);
            for (Simbolo s : scope.values()) {
                if (!s.usada && !"funcion".equals(s.categoria)) {
                    warnings.add("Variable '" + s.nombre + "' declarada en línea " + s.linea + " pero nunca usada (ámbito: " + s.ambito + ")");
                }
            }
            ambitoActual = scopes.size() > 1 ? "desconocido" : "global";
        }
    }

    public void setAmbitoActual(String ambito) {
        this.ambitoActual = ambito;
    }

    public void agregar(String nombre, String tipo, String categoria, int linea, int columna, String tamanoArray) {
        Map<String, Simbolo> currentScope = scopes.get(scopes.size() - 1);
        if (currentScope.containsKey(nombre)) {
            Simbolo existente = currentScope.get(nombre);
            errores.add("Variable '" + nombre + "' ya declarada en el ámbito '" + ambitoActual + "' (línea " + existente.linea + ", columna " + existente.columna + ")");
        } else {
            Simbolo nuevo = new Simbolo(nombre, tipo, categoria, linea, columna, ambitoActual);
            if (tamanoArray != null) {
                nuevo.tamanoArray = tamanoArray;
            }
            currentScope.put(nombre, nuevo);
            todosSimbolos.add(nuevo);
        }
    }

    public void agregar(String nombre, String tipo, String categoria, int linea, int columna) {
        agregar(nombre, tipo, categoria, linea, columna, null);
    }

    public Simbolo buscar(String nombre) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            Simbolo s = scopes.get(i).get(nombre);
            if (s != null) return s;
        }
        return null;
    }

    public boolean existe(String nombre) {
        return buscar(nombre) != null;
    }

    public void marcarUsada(String nombre) {
        Simbolo s = buscar(nombre);
        if (s != null) s.usada = true;
    }

    public void agregarError(String error) {
        errores.add(error);
    }

    public void agregarWarning(String warning) {
        warnings.add(warning);
    }

    public List<String> getErrores() {
        return errores;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void imprimir() {
        System.out.println("\n=== TABLA DE SÍMBOLOS ===");
        System.out.printf("%-16s %-11s %-16s %-11s %-12s %-16s %s\n",
                "NOMBRE", "TIPO", "CATEGORÍA", "LÍNEA", "COLUMNA", "ÁMBITO", "DETALLES");
        System.out.println("--------------------------------------------------------------------------------------------");
        for (Simbolo s : todosSimbolos) {
            String detalles = "";
            if ("parametro".equals(s.categoria)) {
                detalles = "";
            } else if ("funcion".equals(s.categoria)) {
                String params = String.join(", ", s.tiposParams);
                detalles = "[private] [" + params + "]";
            } else if (s.tamanoArray != null) {
                detalles = "[arr:" + s.tamanoArray + "] [private]";
            } else {
                detalles = "[private]";
            }
            System.out.printf("%-16s %-11s %-16s %-11d %-12d %-16s %s\n",
                    s.nombre, s.tipo, s.categoria, s.linea, s.columna, s.ambito, detalles);
        }
    }

    public List<Map<String, Simbolo>> getScopes() {
        return scopes;
    }

    public List<Simbolo> getTodosSimbolos() {
        return todosSimbolos;
    }
}
