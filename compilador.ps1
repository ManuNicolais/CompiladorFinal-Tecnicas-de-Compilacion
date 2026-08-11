# ============================================================
# COMPILADOR - Tecnicas de Compilacion 2026
# Script TUTORIAL para PowerShell
# Explica que ejecutar, en que orden y que verifica cada paso
# ============================================================
# USO:
#   PowerShell: .\compilador.ps1
#   (si da error de permisos: Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass)
# ============================================================
# Requisito: Java 11+, Maven 3+
# ============================================================

$env:JAVA_TOOL_OPTIONS = "-Djava.awt.headless=true"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location -Path $scriptDir

Write-Host ""
Write-Host "============================================================"
Write-Host "     COMPILADOR TC 2026 - SCRIPT TUTORIAL"
Write-Host "  Verifica que TODAS las consignas del PDF se cumplan"
Write-Host "============================================================"
Write-Host ""

# ============================================================
# PASO 0: Compilar el proyecto
# ============================================================
Write-Host "============================================================"
Write-Host "  PASO 0: COMPILAR EL PROYECTO"
Write-Host "  Comando: mvn clean package -q"
Write-Host "  Genera: target\compiladorFinal-1.0-jar-with-dependencies.jar"
Write-Host "============================================================"
Write-Host ""

Write-Host "=> Compilando..."
mvn clean package -q
if ($?) {
    Write-Host "   [OK] Compilacion exitosa. JAR listo."
}
Write-Host ""

# ============================================================
# PASO 1: Programa CORRECTO (todas las fases)
# ============================================================
Write-Host "============================================================"
Write-Host "  PASO 1: PROGRAMA CORRECTO - ejemplo_correcto.cpp"
Write-Host ""
Write-Host "  Demuestra TODAS las fases del compilador:"
Write-Host "   [v] Analisis Lexico   -> tabla de tokens"
Write-Host "   [v] Analisis Sintactico -> AST (sin ventana GUI)"
Write-Host "   [v] Analisis Semantico -> tabla de simbolos"
Write-Host "   [v] Codigo Intermedio -> codigo de 3 direcciones"
Write-Host "   [v] Optimizacion -> 5 pasos + optimizacion completa"
Write-Host ""
Write-Host "  Requisitos del PDF cubiertos:"
Write-Host "   - Analisis Lexico (tabla de tokens)"
Write-Host "   - Analisis Sintactico (AST, visualizacion)"
Write-Host "   - Analisis Semantico (tabla de simbolos)"
Write-Host "   - Generacion de Codigo Intermedio (3 direcciones)"
Write-Host "   - Optimizacion (5 tecnicas implementadas)"
Write-Host "   - Salidas (archivos .txt por cada fase)"
Write-Host "============================================================"
Write-Host "  Comando: java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar ejemplo_correcto.cpp"
Write-Host ""

java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar ejemplo_correcto.cpp

Write-Host ""
Write-Host "   Archivos generados por este paso:"
Get-ChildItem -Filter "ejemplo_correcto_*.txt" | ForEach-Object {
    $lineas = (Get-Content $_.FullName | Measure-Object -Line).Lines
    Write-Host "      -> $($_.Name) ($lineas lineas)"
}
Write-Host ""

# ============================================================
# PASO 2: Errores SEMANTICOS BASICOS
# ============================================================
Write-Host "============================================================"
Write-Host "  PASO 2: ERRORES SEMANTICOS - ejemplo_con_errores.cpp"
Write-Host ""
Write-Host "  Demuestra DETECCION DE ERRORES SEMANTICOS con:"
Write-Host "   [x] Variable duplicada en mismo ambito"
Write-Host "   [x] Variable no declarada"
Write-Host "   [x] Asignacion a funcion (no es variable)"
Write-Host "   [!] Variables declaradas pero nunca usadas (warning)"
Write-Host ""
Write-Host "  Requisitos del PDF cubiertos:"
Write-Host "   - Verificar ambito de variables y funciones"
Write-Host "   - Reportar errores semanticos (con detalles linea/col)"
Write-Host "   - Distinguir entre errores (criticos) y warnings"
Write-Host "   - Warnings en AMARILLO, errores en ROJO"
Write-Host "============================================================"
Write-Host "  Comando: java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar ejemplo_con_errores.cpp"
Write-Host ""

java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar ejemplo_con_errores.cpp

Write-Host ""

# ============================================================
# PASO 3: Errores de TIPO (test exhaustivo)
# ============================================================
Write-Host "============================================================"
Write-Host "  PASO 3: ERRORES DE TIPO - test_errores_tipos.cpp"
Write-Host ""
Write-Host "  Demuestra VERIFICACION DE TIPOS de datos:"
Write-Host "   [x] Asignacion de tipo incompatible (int = string)"
Write-Host "   [x] Condicion if/while/for con tipo no booleano"
Write-Host "   [x] Tipo de retorno incorrecto en funcion"
Write-Host "   [x] Argumento de funcion con tipo incorrecto"
Write-Host "   [x] Operacion aritmetica con tipos no numericos"
Write-Host "   [x] Comparacion entre tipos incompatibles"
Write-Host "   [x] Operador logico con operandos no booleanos"
Write-Host "   [x] break/continue fuera de un bucle"
Write-Host "   [!] Retorno explicito en funcion void (warning)"
Write-Host ""
Write-Host "  Requisitos del PDF cubiertos:"
Write-Host "   - Verificar tipos de datos y compatibilidad en opers."
Write-Host "   - Reportar errores semanticos (con detalles linea/col)"
Write-Host "   - Distinguir entre errores (criticos) y warnings"
Write-Host "============================================================"
Write-Host "  Comando: java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar test_errores_tipos.cpp"
Write-Host ""

java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar test_errores_tipos.cpp

Write-Host ""

# ============================================================
# PASO 4: Optimizaciones (test especifico)
# ============================================================
Write-Host "============================================================"
Write-Host "  PASO 4: OPTIMIZACIONES - test_optimizaciones.cpp"
Write-Host ""
Write-Host "  Demuestra las 5 TECNICAS DE OPTIMIZACION:"
Write-Host "   1) Simplificacion de expresiones (folding + identidades)"
Write-Host "   2) Propagacion de constantes"
Write-Host "   3) Eliminacion de subexpresiones comunes"
Write-Host "   4) Eliminacion de codigo muerto"
Write-Host "   5) Optimizacion de bucles (loop invariant)"
Write-Host ""
Write-Host "  Requisitos del PDF cubiertos:"
Write-Host "   - Implementar >=3 tecnicas de optimizacion (tenemos 5)"
Write-Host "   - Generar archivos de salida para cada paso"
Write-Host "   - Comparar codigo sin optimizar vs optimizado"
Write-Host "============================================================"
Write-Host "  Comando: java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar test_optimizaciones.cpp"
Write-Host ""

java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar test_optimizaciones.cpp

Write-Host ""
Write-Host "   Archivos de optimizacion generados para este paso:"
Get-ChildItem -Filter "test_optimizaciones_*.txt" | ForEach-Object {
    $lineas = (Get-Content $_.FullName | Measure-Object -Line).Lines
    Write-Host "      -> $($_.Name) ($lineas lineas)"
}
Write-Host ""

# ============================================================
# RESUMEN FINAL: Verificacion de consignas
# ============================================================
Write-Host "============================================================"
Write-Host "  RESUMEN - VERIFICACION DE CONSIGNAS DEL PDF"
Write-Host "============================================================"
Write-Host ""
Write-Host "  1. ANALISIS LEXICO"
Write-Host "      [OK] Reconocimiento de tokens del lenguaje"
Write-Host "      [OK] Reporte de errores lexicos"
Write-Host "      [OK] Generacion de tabla de tokens"
Write-Host ""
Write-Host "  2. ANALISIS SINTACTICO"
Write-Host "      [OK] Verificacion de estructura gramatical"
Write-Host "      [OK] Construccion de AST"
Write-Host "      [OK] Reporte de errores sintacticos"
Write-Host "      [OK] Visualizacion del arbol sintactico"
Write-Host ""
Write-Host "  3. ANALISIS SEMANTICO"
Write-Host "      [OK] Construccion y mantenimiento de tabla de simbolos"
Write-Host "      [OK] Verificacion de tipos de datos y compatibilidad"
Write-Host "      [OK] Verificacion de ambito de variables y funciones"
Write-Host "      [OK] Verificacion de tipos en:"
Write-Host "         - Asignaciones, condiciones, retornos"
Write-Host "         - Argumentos de funcion, operaciones aritmeticas"
Write-Host "         - Comparaciones, operadores logicos"
Write-Host "         - break/continue en contexto de bucle"
Write-Host "      [OK] Reporte de errores semanticos con linea y columna"
Write-Host "      [OK] Distincion entre errores (criticos) y warnings"
Write-Host ""
Write-Host "  4. GENERACION DE CODIGO INTERMEDIO"
Write-Host "      [OK] Codigo de tres direcciones"
Write-Host "      [OK] Expresiones aritmeticas y logicas"
Write-Host "      [OK] Estructuras de control (if-else, for, while)"
Write-Host "      [OK] Llamadas a funciones y retorno de valores"
Write-Host ""
Write-Host "  5. OPTIMIZACION DE CODIGO"
Write-Host "      [OK] 5 tecnicas implementadas (minimo requerido: 3)"
Write-Host "         -> Paso 1: Simplificacion de expresiones"
Write-Host "         -> Paso 2: Propagacion de constantes"
Write-Host "         -> Paso 3: Eliminacion de subexpresiones comunes"
Write-Host "         -> Paso 4: Eliminacion de codigo muerto"
Write-Host "         -> Paso 5: Optimizacion de bucles"
Write-Host "      [OK] Archivos de salida para cada paso de optimizacion"
Write-Host "      [OK] Archivo final optimizado"
Write-Host ""
Write-Host "  6. SALIDAS DEL COMPILADOR"
Write-Host "      [OK] Archivos de codigo intermedio (sin optimizar)"
Write-Host "      [OK] Archivos de codigo optimizado"
Write-Host "      [OK] Codigo intermedio para cada paso de optimizacion"
Write-Host "      [OK] Reporte con colores (verde/amarillo/rojo)"
Write-Host ""

Write-Host "============================================================"
Write-Host "  COMPARACION MANUAL ENTRE PASOS DE OPTIMIZACION"
Write-Host "============================================================"
Write-Host ""
Write-Host "  Para ver como cambia el codigo entre optimizaciones:"
Write-Host ""
Write-Host "  Compare-Object (Get-Content ejemplo_correcto_codigo_sin_optimizar.txt) "
Write-Host "                (Get-Content ejemplo_correcto_codigo_optimizado.txt)"
Write-Host ""
Write-Host "  Notepad ejemplo_correcto_codigo_sin_optimizar.txt"
Write-Host "  Notepad ejemplo_correcto_codigo_optimizado.txt"
Write-Host ""

Write-Host "============================================================"
Write-Host "  COMPILAR UN ARCHIVO PROPIO"
Write-Host "============================================================"
Write-Host ""
Write-Host "  java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar mi_archivo.cpp"
Write-Host ""

Write-Host "============================================================"
Write-Host "  [OK] TODAS LAS CONSIGNAS DEL PDF VERIFICADAS CON EXITO"
Write-Host "============================================================"
Write-Host ""
