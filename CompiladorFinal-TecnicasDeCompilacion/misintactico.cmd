@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"

if "%~1"=="" goto help

REM Compilar si es necesario
if not exist "target\classes\" (
    echo => Compilando proyecto...
    call mvn clean compile -q
    if errorlevel 1 (echo Error de compilacion & exit /b 1)
)

REM Classpath con todas las dependencias (incluye TreeViewer grafico)
set M2=%USERPROFILE%\.m2\repository
set CP=target\classes
set CP=%CP%;%M2%\org\antlr\antlr4-runtime\4.9.3\antlr4-runtime-4.9.3.jar
set CP=%CP%;%M2%\org\antlr\antlr4\4.9.3\antlr4-4.9.3.jar
set CP=%CP%;%M2%\org\antlr\antlr-runtime\3.5.2\antlr-runtime-3.5.2.jar
set CP=%CP%;%M2%\org\antlr\ST4\4.3.1\ST4-4.3.1.jar
set CP=%CP%;%M2%\org\abego\treelayout\org.abego.treelayout.core\1.0.3\org.abego.treelayout.core-1.0.3.jar

REM Usar JAVA_HOME si existe, si no usar java del PATH
if not "%JAVA_HOME%"=="" (
    "%JAVA_HOME%\bin\java" -cp "%CP%" com.compilador.MiSintacticoApp "%~1"
) else (
    java -cp "%CP%" com.compilador.MiSintacticoApp "%~1"
)
goto end

:help
echo.
echo ============================================================
echo   MiSintactico - Analizador Sintactico de Expresiones
echo ============================================================
echo.
echo USO: misintactico "<expresion>"
echo      misintactico <archivo.txt>
echo.
echo Ejemplos:
echo   misintactico "a + b * c"
echo   misintactico "(a + b) * c - d"
echo.
echo Se abrira una ventana con el arbol sintactico grafico.
echo ============================================================

:end
endlocal
