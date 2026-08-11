package com.compilador;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.gui.TreeViewer;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class App {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java -jar compiladorFinal-1.0-jar-with-dependencies.jar <archivo.txt>");
            System.exit(1);
        }

        String nombreArchivo = args[0];

        try {
            System.out.println("ðŸš€ Iniciando compilaciÃ³n de: " + nombreArchivo);
            System.out.println("============================================================");

            CharStream input = CharStreams.fromFileName(nombreArchivo);

            // === 1. ANALISIS LEXICO ===
            //captura errores lÃ©xicos.
            System.out.println("\n=== 1. ANÃLISIS LÃ‰XICO ===");
            MiLenguajeLexer lexer = new MiLenguajeLexer(input);
            List<String> erroresLexicos = new ArrayList<>();
            lexer.removeErrorListeners();
            lexer.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                      int line, int charPositionInLine, String msg, RecognitionException e) {
                    erroresLexicos.add("ERROR LÃ‰XICO en lÃ­nea " + line + ":" + charPositionInLine + " - " + msg);
                }
            });

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();

            if (!erroresLexicos.isEmpty()) {
                for (String err : erroresLexicos) {
                    System.out.println("   âŒ " + err);
                }
                System.out.println("\nâŒ CompilaciÃ³n abortada por errores lÃ©xicos.");
                return;
            }

            System.out.println("âœ… AnÃ¡lisis lÃ©xico completado sin errores.");
            System.out.println("   ðŸ“Š Tokens procesados: " + (tokens.size() - 1));

            mostrarTablaTokens(tokens, lexer);

            // === 2. ANALISIS SINTACTICO ===
            //captura errores sintÃ¡cticos.
            System.out.println("\n=== 2. ANÃLISIS SINTÃCTICO ===");
            tokens.seek(0);
            MiLenguajeParser parser = new MiLenguajeParser(tokens);
            List<String> erroresSintacticos = new ArrayList<>();
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                      int line, int charPositionInLine, String msg, RecognitionException e) {
                    erroresSintacticos.add("ERROR SINTÃCTICO en lÃ­nea " + line + ":" + charPositionInLine + " - " + msg);
                }
            });

            ParseTree tree = parser.programa();

            if (!erroresSintacticos.isEmpty()) {
                for (String err : erroresSintacticos) {
                    System.out.println("   âŒ " + err);
                }
                System.out.println("\nâŒ CompilaciÃ³n abortada por errores sintÃ¡cticos.");
                return;
            }

            System.out.println("âœ… AnÃ¡lisis sintÃ¡ctico completado sin errores.");
            System.out.println("   ðŸ“Š Ãrbol sintÃ¡ctico generado correctamente");

            // === 3. VISUALIZACION DEL AST ===
            System.out.println("\n=== 3. VISUALIZACIÃ“N DEL AST ===");
            mostrarArbolSintactico(tree, parser);
            System.out.println("   ðŸ“Š Ventana del Ã¡rbol sintÃ¡ctico abierta");

            // === 4. ANALISIS SEMANTICO ===
            System.out.println("\n=== 4. ANÃLISIS SEMÃNTICO ===");
            TablaSimbolos tabla = new TablaSimbolos();
            SemanticoVisitor semantico = new SemanticoVisitor(tabla);
            semantico.visit(tree);

            System.out.println("   ðŸ“‹ Tabla de sÃ­mbolos construida:");
            tabla.imprimir();

            if (!tabla.getErrores().isEmpty()) {
                System.out.println("\nâŒ ERRORES SEMÃNTICOS:");
                for (String err : tabla.getErrores()) {
                    System.out.println("   âŒ " + err);
                }

                if (!tabla.getWarnings().isEmpty()) {
                    System.out.println("\nâš ï¸ WARNINGS SEMÃNTICOS:");
                    for (String w : tabla.getWarnings()) {
                        System.out.println("   âš ï¸ " + w);
                    }
                    System.out.println("   âš ï¸ El cÃ³digo tiene warnings, pero se puede continuar.");
                }

                System.out.println("\nâŒ CompilaciÃ³n detenida debido a errores semÃ¡nticos.");
                return;
            }

            if (!tabla.getWarnings().isEmpty()) {
                System.out.println("\nâš ï¸ WARNINGS SEMÃNTICOS:");
                for (String w : tabla.getWarnings()) {
                    System.out.println("   âš ï¸ " + w);
                }
                System.out.println("   âš ï¸ El cÃ³digo tiene warnings, pero se puede continuar.");
            }

            System.out.println("\nâœ… AnÃ¡lisis semÃ¡ntico completado sin errores.");

            // === 5. GENERACION DE CODIGO INTERMEDIO ===
            System.out.println("\n=== 5. GENERACIÃ“N DE CÃ“DIGO INTERMEDIO ===");
            System.out.println("   ðŸŽ¯ Iniciando recorrido del AST con CodigoVisitor...");
            CodigoVisitor codigo = new CodigoVisitor();
            codigo.visit(tree);

            String nombreBase = nombreArchivo.replaceFirst("[.][^.]+$", "");

            // Guardar cÃ³digo sin optimizar
            String archivoNoOptimizado = nombreBase + "_codigo_sin_optimizar.txt";
            codigo.guardarEnArchivo(archivoNoOptimizado);
            int instOriginales = codigo.getCodigo().size();
            System.out.println("\n   ðŸ“ CÃ³digo de tres direcciones SIN optimizar (" + instOriginales + " instrucciones):\n");
            codigo.imprimir();
            System.out.println("\nâœ… CÃ³digo sin optimizar guardado en: " + archivoNoOptimizado);

            // === 6. OPTIMIZACIONES PASO A PASO ===
            System.out.println("\n=== 6. OPTIMIZACIONES ===");
            List<String> codigoActual = new ArrayList<>(codigo.getCodigo());

            // 6.1 SimplificaciÃ³n de expresiones
            System.out.println("\n   6.1 Aplicando simplificaciÃ³n de expresiones...");
            List<String> paso1 = OptimizadorCodigo.aplicarSimplificacionExpresiones(codigoActual);
            String archivoP1 = nombreBase + "_optimizacion_01_simplificacion.txt";
            codigo.setCodigo(paso1);
            codigo.guardarEnArchivo(archivoP1);
            System.out.println("   âœ… SimplificaciÃ³n aplicada (" + paso1.size() + " instrucciones). Guardado en: " + archivoP1);
            codigoActual = new ArrayList<>(paso1);

            // 6.2 PropagaciÃ³n de constantes
            System.out.println("\n   6.2 Aplicando propagaciÃ³n de constantes...");
            List<String> paso2 = OptimizadorCodigo.aplicarPropagacionConstantes(codigoActual);
            String archivoP2 = nombreBase + "_optimizacion_02_propagacion_constantes.txt";
            codigo.setCodigo(paso2);
            codigo.guardarEnArchivo(archivoP2);
            System.out.println("   âœ… PropagaciÃ³n aplicada (" + paso2.size() + " instrucciones). Guardado en: " + archivoP2);
            codigoActual = new ArrayList<>(paso2);

            // 6.3 EliminaciÃ³n de subexpresiones comunes
            System.out.println("\n   6.3 Aplicando eliminaciÃ³n de subexpresiones comunes...");
            List<String> paso3 = OptimizadorCodigo.aplicarEliminacionSubexpresiones(codigoActual);
            String archivoP3 = nombreBase + "_optimizacion_03_subexpresiones_comunes.txt";
            codigo.setCodigo(paso3);
            codigo.guardarEnArchivo(archivoP3);
            System.out.println("   âœ… Subexpresiones eliminadas (" + paso3.size() + " instrucciones). Guardado en: " + archivoP3);
            codigoActual = new ArrayList<>(paso3);

            // 6.4 EliminaciÃ³n de cÃ³digo muerto
            System.out.println("\n   6.4 Aplicando eliminaciÃ³n de cÃ³digo muerto...");
            List<String> paso4 = OptimizadorCodigo.aplicarEliminacionCodigoMuerto(codigoActual);
            String archivoP4 = nombreBase + "_optimizacion_04_codigo_muerto.txt";
            codigo.setCodigo(paso4);
            codigo.guardarEnArchivo(archivoP4);
            System.out.println("   âœ… CÃ³digo muerto eliminado (" + paso4.size() + " instrucciones). Guardado en: " + archivoP4);
            codigoActual = new ArrayList<>(paso4);

            // 6.5 OptimizaciÃ³n de bucles
            System.out.println("\n   6.5 Aplicando optimizaciÃ³n de bucles...");
            List<String> paso5 = OptimizadorCodigo.aplicarOptimizacionBucles(codigoActual);
            String archivoP5 = nombreBase + "_optimizacion_05_bucles.txt";
            codigo.setCodigo(paso5);
            codigo.guardarEnArchivo(archivoP5);
            System.out.println("   âœ… Bucles optimizados (" + paso5.size() + " instrucciones). Guardado en: " + archivoP5);
            codigoActual = new ArrayList<>(paso5);

            // Aplicar optimizaciÃ³n completa (varias iteraciones) y guardar resultado final
            System.out.println("\n   6.6 Aplicando optimizaciÃ³n completa (iterativa)...");
            List<String> codigoFinal = OptimizadorCodigo.optimizar(codigoActual);
            String archivoFinal = nombreBase + "_codigo_optimizado.txt";
            codigo.setCodigo(codigoFinal);
            codigo.guardarEnArchivo(archivoFinal);
            System.out.println("   âœ… OptimizaciÃ³n completa aplicada (" + codigoFinal.size() + " instrucciones). Guardado en: " + archivoFinal);

            System.out.println("\n   ðŸ“ CÃ³digo de tres direcciones OPTIMIZADO:\n");
            codigo.imprimir();

            // === 7. RESUMEN DE COMPILACION ===
            System.out.println("\n=== 7. RESUMEN DE COMPILACIÃ“N ===");
            System.out.println("   ðŸ“ Archivo procesado: " + nombreArchivo);
            System.out.println("   ðŸ”¤ Tokens analizados: " + (tokens.size() - 1));
            System.out.println("   ðŸ“Š SÃ­mbolos en tabla: " + tabla.getTodosSimbolos().size());
            System.out.println("\n   ðŸ“Š ProgresiÃ³n de optimizaciones:");
            System.out.println("      ðŸ“„ Sin optimizar          : " + String.format("%3d", instOriginales) + " instrucciones â†’ " + archivoNoOptimizado);
            System.out.println("      ðŸ“„ 1. SimplificaciÃ³n       : " + String.format("%3d", paso1.size()) + " instrucciones â†’ " + archivoP1);
            System.out.println("      ðŸ“„ 2. PropagaciÃ³n constantes: " + String.format("%3d", paso2.size()) + " instrucciones â†’ " + archivoP2);
            System.out.println("      ðŸ“„ 3. Subexpresiones comunes: " + String.format("%3d", paso3.size()) + " instrucciones â†’ " + archivoP3);
            System.out.println("      ðŸ“„ 4. CÃ³digo muerto        : " + String.format("%3d", paso4.size()) + " instrucciones â†’ " + archivoP4);
            System.out.println("      ðŸ“„ 5. Bucles               : " + String.format("%3d", paso5.size()) + " instrucciones â†’ " + archivoP5);
            System.out.println("      ðŸ“„ 6. OptimizaciÃ³n completa : " + String.format("%3d", codigoFinal.size()) + " instrucciones â†’ " + archivoFinal);
            int eliminadas = instOriginales - codigoFinal.size();
            double pct = instOriginales > 0 ? (eliminadas * 100.0 / instOriginales) : 0;
            System.out.println("\n   ðŸ“Š Instrucciones originales: " + instOriginales);
            System.out.println("   ðŸ“Š Instrucciones finales: " + codigoFinal.size());
            System.out.println("   ðŸ“Š Instrucciones eliminadas: " + eliminadas);
            System.out.println(String.format("   ðŸ“Š ReducciÃ³n de cÃ³digo: %.2f%%", pct));

            System.out.println("\nðŸŽ‰ Â¡COMPILACIÃ“N Y OPTIMIZACIÃ“N EXITOSA! ðŸŽ‰");

        } catch (IOException e) {
            System.err.println("âŒ Error al leer el archivo: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("âŒ Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void mostrarTablaTokens(CommonTokenStream tokens, MiLenguajeLexer lexer) {
        System.out.println("\n   ðŸ“‹ TABLA DE TOKENS:");
        System.out.println("   " + String.format("%-5s %-6s %-6s %-28s %s", "NÂ°", "LÃNEA", "COLUMNA", "TIPO", "VALOR"));
        System.out.println("   " + "--------------------------------------------------------------------");
        List<Token> lista = tokens.getTokens();
        Vocabulary vocab = lexer.getVocabulary();
        int idx = 0;
        for (Token t : lista) {
            int tipo = t.getType();
            if (tipo == Token.EOF) continue;
            String nombre = vocab.getSymbolicName(tipo);
            if (nombre == null) nombre = "OTRO";
            String valor = t.getText();
            valor = valor.replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t");
            System.out.println("   " + String.format("%-5d %-6d %-6d %-28s %s", idx, t.getLine(), t.getCharPositionInLine(), nombre, '"' + valor + '"'));
            idx++;
        }
        System.out.println();
    }

    private static void mostrarArbolSintactico(ParseTree tree, MiLenguajeParser parser) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Ãrbol SintÃ¡ctico");
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

            frame.setSize(1200, 800);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
