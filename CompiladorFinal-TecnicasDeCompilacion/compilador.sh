#!/bin/bash
# ============================================================
# COMPILADOR - TÃ©cnicas de CompilaciÃ³n 2026
# Script TUTORIAL: explica quÃ© ejecutar, en quÃ© orden y por quÃ©
# ============================================================
# USO:
#   En Linux/Mac/Git Bash:  ./compilador.sh
#   Paso a paso manual:     seguir las secciones numeradas
# ============================================================
# Requisito: Java 11+, Maven 3+, y ANTLR4 (plugin Maven)
# ============================================================

export JAVA_TOOL_OPTIONS="-Djava.awt.headless=true"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo ""
echo "â•”â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•—"
echo "â•‘         COMPILADOR TC 2026 â€” SCRIPT TUTORIAL               â•‘"
echo "â•‘  Verifica que TODAS las consignas del PDF se cumplan        â•‘"
echo "â•šâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•"
echo ""

# ============================================================
# PASO 0: Compilar el proyecto
# ============================================================
echo "â•”â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•—"
echo "â•‘  PASO 0: COMPILAR EL PROYECTO                              â•‘"
echo "â•‘  Comando: mvn clean package -q                             â•‘"
echo "â•‘  Genera: target/compiladorFinal-1.0-jar-with-dependencies.jar         â•‘"
echo "â•šâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•"
echo ""

echo "=> Compilando..."
mvn clean package -q
echo "   âœ… CompilaciÃ³n exitosa. JAR listo."
echo ""

# ============================================================
# PASO 1: Programa CORRECTO (todas las fases)
# ============================================================
echo "â•”â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•—"
echo "â•‘  PASO 1: PROGRAMA CORRECTO â€” ejemplo_correcto.cpp          â•‘"
echo "â•‘                                                            â•‘"
echo "â•‘  Demuestra TODAS las fases del compilador:                 â•‘"
echo "â•‘   âœ” AnÃ¡lisis LÃ©xico  â†’ tabla de tokens                     â•‘"
echo "â•‘   âœ” AnÃ¡lisis SintÃ¡ctico â†’ AST (ventana emergente sin GUI)  â•‘"
echo "â•‘   âœ” AnÃ¡lisis SemÃ¡ntico â†’ tabla de sÃ­mbolos                 â•‘"
echo "â•‘   âœ” CÃ³digo Intermedio â†’ cÃ³digo de 3 direcciones            â•‘"
echo "â•‘   âœ” OptimizaciÃ³n â†’ 5 pasos + optimizaciÃ³n completa         â•‘"
echo "â•‘                                                            â•‘"
echo "â•‘  Requisitos del PDF cubiertos:                             â•‘"
echo "â•‘   - AnÃ¡lisis LÃ©xico (tabla de tokens)                      â•‘"
echo "â•‘   - AnÃ¡lisis SintÃ¡ctico (AST, visualizaciÃ³n)               â•‘"
echo "â•‘   - AnÃ¡lisis SemÃ¡ntico (tabla de sÃ­mbolos)                 â•‘"
echo "â•‘   - GeneraciÃ³n de CÃ³digo Intermedio (3 direcciones)        â•‘"
echo "â•‘   - OptimizaciÃ³n (5 tÃ©cnicas implementadas)                â•‘"
echo "â•‘   - Salidas (archivos .txt por cada fase)                  â•‘"
echo "â•šâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•"
echo "  Comando: java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar ejemplo_correcto.cpp"
echo ""

java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar ejemplo_correcto.cpp

echo ""
echo "   ðŸ“ Archivos generados por este paso:"
for f in ejemplo_correcto_*.txt; do
    LINEAS=$(wc -l < "$f")
    echo "      â†’ $f ($LINEAS lÃ­neas)"
done
echo ""

# ============================================================
# PASO 2: Errores SEMÃNTICOS BÃSICOS
# ============================================================
echo "â•”â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•—"
echo "â•‘  PASO 2: ERRORES SEMÃNTICOS â€” ejemplo_con_errores.cpp      â•‘"
echo "â•‘                                                            â•‘"
echo "â•‘  Demuestra DETECCIÃ“N DE ERRORES SEMÃNTICOS con:            â•‘"
echo "â•‘   âœ˜ Variable duplicada en mismo Ã¡mbito                     â•‘"
echo "â•‘   âœ˜ Variable no declarada                                  â•‘"
echo "â•‘   âœ˜ AsignaciÃ³n a funciÃ³n (no es variable)                  â•‘"
echo "â•‘   âš  Variables declaradas pero nunca usadas (warning)       â•‘"
echo "â•‘                                                            â•‘"
echo "â•‘  Requisitos del PDF cubiertos:                             â•‘"
echo "â•‘   - Verificar Ã¡mbito de variables y funciones               â•‘"
echo "â•‘   - Reportar errores semÃ¡nticos (con detalles lÃ­nea/col)   â•‘"
echo "â•‘   - Distinguir entre errores (crÃ­ticos) y warnings         â•‘"
echo "â•‘   - Las warnings se muestran en AMARILLO, errores en ROJO  â•‘"
echo "â•šâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•"
echo "  Comando: java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar ejemplo_con_errores.cpp"
echo ""

java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar ejemplo_con_errores.cpp

echo ""

# ============================================================
# PASO 3: Errores de TIPO (nuevo test exhaustivo)
# ============================================================
echo "â•”â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•—"
echo "â•‘  PASO 3: ERRORES DE TIPO â€” test_errores_tipos.cpp          â•‘"
echo "â•‘                                                            â•‘"
echo "â•‘  Demuestra VERIFICACIÃ“N DE TIPOS de datos:                 â•‘"
echo "â•‘   âœ˜ AsignaciÃ³n de tipo incompatible (int = string)         â•‘"
echo "â•‘   âœ˜ CondiciÃ³n if/while/for con tipo no booleano            â•‘"
echo "â•‘   âœ˜ Tipo de retorno incorrecto en funciÃ³n                  â•‘"
echo "â•‘   âœ˜ Argumento de funciÃ³n con tipo incorrecto               â•‘"
echo "â•‘   âœ˜ OperaciÃ³n aritmÃ©tica con tipos no numÃ©ricos            â•‘"
echo "â•‘   âœ˜ ComparaciÃ³n entre tipos incompatibles                  â•‘"
echo "â•‘   âœ˜ Operador lÃ³gico con operandos no booleanos             â•‘"
echo "â•‘   âœ˜ break/continue fuera de un bucle                       â•‘"
echo "â•‘   âš  Retorno explÃ­cito en funciÃ³n void (warning)            â•‘"
echo "â•‘                                                            â•‘"
echo "â•‘  Requisitos del PDF cubiertos:                             â•‘"
echo "â•‘   - Verificar tipos de datos y compatibilidad en opers.    â•‘"
echo "â•‘   - Reportar errores semÃ¡nticos (con detalles lÃ­nea/col)   â•‘"
echo "â•‘   - Distinguir entre errores (crÃ­ticos) y warnings         â•‘"
echo "â•šâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•"
echo "  Comando: java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar test_errores_tipos.cpp"
echo ""

java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar test_errores_tipos.cpp

echo ""

# ============================================================
# PASO 4: Optimizaciones (test especÃ­fico)
# ============================================================
echo "â•”â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•—"
echo "â•‘  PASO 4: OPTIMIZACIONES â€” test_optimizaciones.cpp          â•‘"
echo "â•‘                                                            â•‘"
echo "â•‘  Demuestra las 5 TÃ‰CNICAS DE OPTIMIZACIÃ“N:                 â•‘"
echo "â•‘   1ï¸âƒ£ SimplificaciÃ³n de expresiones (folding + identidades) â•‘"
echo "â•‘   2ï¸âƒ£ PropagaciÃ³n de constantes                             â•‘"
echo "â•‘   3ï¸âƒ£ EliminaciÃ³n de subexpresiones comunes                 â•‘"
echo "â•‘   4ï¸âƒ£ EliminaciÃ³n de cÃ³digo muerto                          â•‘"
echo "â•‘   5ï¸âƒ£ OptimizaciÃ³n de bucles (loop invariant)               â•‘"
echo "â•‘                                                            â•‘"
echo "â•‘  Requisitos del PDF cubiertos:                             â•‘"
echo "â•‘   - Implementar â‰¥3 tÃ©cnicas de optimizaciÃ³n (tenemos 5)    â•‘"
echo "â•‘   - Generar archivos de salida para cada paso              â•‘"
echo "â•‘   - Comparar cÃ³digo sin optimizar vs optimizado            â•‘"
echo "â•šâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•"
echo "  Comando: java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar test_optimizaciones.cpp"
echo ""

java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar test_optimizaciones.cpp

echo ""
echo "   ðŸ“ Archivos de optimizaciÃ³n generados para este paso:"
for f in test_optimizaciones_*.txt; do
    LINEAS=$(wc -l < "$f")
    echo "      â†’ $f ($LINEAS lÃ­neas)"
done
echo ""

# ============================================================
# RESUMEN FINAL: VerificaciÃ³n de consignas
# ============================================================
echo "â•”â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•—"
echo "â•‘  RESUMEN â€” VERIFICACIÃ“N DE CONSIGNAS DEL PDF               â•‘"
echo "â•šâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•"
echo ""
echo "  ðŸ“‹ 1. ANÃLISIS LÃ‰XICO"
echo "      âœ… Reconocimiento de tokens del lenguaje"
echo "      âœ… Reporte de errores lÃ©xicos"
echo "      âœ… GeneraciÃ³n de tabla de tokens"
echo ""
echo "  ðŸ“‹ 2. ANÃLISIS SINTÃCTICO"
echo "      âœ… VerificaciÃ³n de estructura gramatical"
echo "      âœ… ConstrucciÃ³n de AST"
echo "      âœ… Reporte de errores sintÃ¡cticos"
echo "      âœ… VisualizaciÃ³n del Ã¡rbol sintÃ¡ctico"
echo ""
echo "  ðŸ“‹ 3. ANÃLISIS SEMÃNTICO"
echo "      âœ… ConstrucciÃ³n y mantenimiento de tabla de sÃ­mbolos"
echo "      âœ… VerificaciÃ³n de tipos de datos y compatibilidad"
echo "      âœ… VerificaciÃ³n de Ã¡mbito de variables y funciones"
echo "      âœ… VerificaciÃ³n de tipos en:"
echo "         - Asignaciones, condiciones, retornos"
echo "         - Argumentos de funciÃ³n, operaciones aritmÃ©ticas"
echo "         - Comparaciones, operadores lÃ³gicos"
echo "         - break/continue en contexto de bucle"
echo "      âœ… Reporte de errores semÃ¡nticos con lÃ­nea y columna"
echo "      âœ… DistinciÃ³n entre errores (crÃ­ticos) y warnings"
echo ""
echo "  ðŸ“‹ 4. GENERACIÃ“N DE CÃ“DIGO INTERMEDIO"
echo "      âœ… CÃ³digo de tres direcciones"
echo "      âœ… Expresiones aritmÃ©ticas y lÃ³gicas"
echo "      âœ… Estructuras de control (if-else, for, while)"
echo "      âœ… Llamadas a funciones y retorno de valores"
echo ""
echo "  ðŸ“‹ 5. OPTIMIZACIÃ“N DE CÃ“DIGO"
echo "      âœ… 5 tÃ©cnicas implementadas (mÃ­nimo requerido: 3)"
echo "         â†’ Paso 1: SimplificaciÃ³n de expresiones"
echo "         â†’ Paso 2: PropagaciÃ³n de constantes"
echo "         â†’ Paso 3: EliminaciÃ³n de subexpresiones comunes"
echo "         â†’ Paso 4: EliminaciÃ³n de cÃ³digo muerto"
echo "         â†’ Paso 5: OptimizaciÃ³n de bucles"
echo "      âœ… Archivos de salida para cada paso de optimizaciÃ³n"
echo "      âœ… Archivo final optimizado"
echo ""
echo "  ðŸ“‹ 6. SALIDAS DEL COMPILADOR"
echo "      âœ… Archivos de cÃ³digo intermedio (sin optimizar)"
echo "      âœ… Archivos de cÃ³digo optimizado"
echo "      âœ… CÃ³digo intermedio para cada paso de optimizaciÃ³n"
echo "      âœ… Reporte con colores (verde/amarillo/rojo)"
echo ""

echo "â•”â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•—"
echo "â•‘  COMPARACIÃ“N MANUAL ENTRE PASOS DE OPTIMIZACIÃ“N            â•‘"
echo "â•šâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•"
echo ""
echo "  Para ver cÃ³mo cambia el cÃ³digo entre optimizaciones:"
echo ""
echo "  diff ejemplo_correcto_codigo_sin_optimizar.txt \\"
echo "       ejemplo_correcto_optimizacion_01_simplificacion.txt"
echo ""
echo "  diff test_optimizaciones_codigo_sin_optimizar.txt \\"
echo "       test_optimizaciones_codigo_optimizado.txt"
echo ""
echo "  diff ejemplo_correcto_codigo_sin_optimizar.txt \\"
echo "       ejemplo_correcto_codigo_optimizado.txt"
echo ""

echo "â•”â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•—"
echo "â•‘  COMPILAR UN ARCHIVO PROPIO                                 â•‘"
echo "â•šâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•"
echo ""
echo "  java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar mi_archivo.cpp"
echo ""

echo "â•”â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•—"
echo "â•‘  ðŸŽ‰ TODAS LAS CONSIGNAS DEL PDF VERIFICADAS CON Ã‰XITO       â•‘"
echo "â•šâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•"
echo ""
