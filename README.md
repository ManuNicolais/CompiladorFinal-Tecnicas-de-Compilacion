# CompiladorFinal — Técnicas de Compilación

> Compilador académico para un subconjunto del lenguaje C++, desarrollado como
> **Trabajo Final** de la materia *Técnicas de Compilación* (TC 2026).
> Implementa las cinco fases clásicas de un compilador: **análisis léxico,
> sintáctico, semántico, generación de código intermedio y optimización**.

---

## Tabla de contenidos

1. [Descripción del proyecto](#descripción-del-proyecto)
2. [Requisitos](#requisitos)
3. [Estructura del repositorio](#estructura-del-repositorio)
4. [¿Cómo funciona el compilador?](#cómo-funciona-el-compilador)
5. [Guía de uso paso a paso](#guía-de-uso-paso-a-paso)
6. [El lenguaje soportado](#el-lenguaje-soportado)
7. [Ejemplos](#ejemplos)
8. [Documentación técnica](#documentación-técnica)
9. [Posibles extensiones](#posibles-extensiones)
10. [Créditos](#créditos)

---

## Descripción del proyecto

Este proyecto construye un **compilador completo** para un subconjunto del
lenguaje C++, tomando como entrada un archivo fuente `*.cpp` y produciendo como
salida:

- La **tabla de tokens** (análisis léxico).
- El **árbol sintáctico** (AST) con visualización gráfica (análisis sintáctico).
- La **tabla de símbolos** con verificación de tipos y ámbitos (análisis semántico).
- El **código de tres direcciones** sin optimizar (generación de código intermedio).
- El **código optimizado** aplicando 5 técnicas de optimización paso a paso.

La herramienta utilizada para generar el analizador léxico y el parser es
**[ANTLR 4](https://www.antlr.org/)**, a partir de una gramática propia
(`MiLenguaje.g4`). El proyecto se gestiona con **Maven** y está escrito en
**Java 8+**.

---

## Requisitos

| Herramienta | Versión mínima | Notas |
|---|---|---|
| [JDK / Java](https://www.oracle.com/java/technologies/downloads/) | 8 | Recomendado 11+ |
| [Maven](https://maven.apache.org/) | 3.6 | Gestión de dependencias y build |
| [ANTLR](https://www.antlr.org/) | 4.9.3 | Se descarga automáticamente con Maven |

> **Nota:** no es necesario instalar ANTLR manualmente. Maven lo descarga e
> integra al compilar el proyecto. Solo se necesita Java y Maven.

---

## Estructura del repositorio

```
CompiladorFinal-TecnicasDeCompilacion/
│
├── pom.xml                          # Configuración Maven (dependencias y plugins)
├── README.md                        # Este documento
│
├── src/
│   └── main/
│       ├── antlr4/
│       │   └── com/compilador/
│       │       ├── MiLenguaje.g4    # ⭐ Gramática ANTLR del lenguaje (léxico + sintáctico)
│       │       └── MiSintactico.g4  # Gramática auxiliar para ejercicios sintácticos
│       │
│       └── java/com/compilador/
│           ├── App.java             # ⭐ Punto de entrada: ejecuta el pipeline completo
│           ├── MiLenguajeLexer.java #  (generado por ANTLR en tiempo de build)
│           ├── MiLenguajeParser.java#  (generado por ANTLR en tiempo de build)
│           ├── TablaSimbolos.java   # Tabla de símbolos + mensajes de error/warning
│           ├── SemanticoVisitor.java# Análisis semántico (tipos, ámbitos, retornos)
│           ├── CodigoVisitor.java   # Generación de código de tres direcciones
│           ├── OptimizadorCodigo.java# 5 técnicas de optimización de código intermedio
│           ├── MiSintacticoApp.java # Visor gráfico del árbol sintáctico (Swing)
│           └── TestSintactico.java  # Utilidad para probar reglas sintácticas
│
├── compilador.ps1                   # Script tutorial (PowerShell): prueba todas las fases
├── compilador.sh                    # Script tutorial (Bash/Linux): prueba todas las fases
├── misintactico.cmd                 # Analizador sintáctico interactivo (Windows)
│
├── ejemplo_correcto.cpp             # Archivo fuente de prueba SIN errores
├── ejemplo_con_errores.cpp          # Archivo fuente de prueba CON errores semánticos
├── test_errores_tipos.cpp           # Tests exhaustivos de verificación de tipos
├── test_optimizaciones.cpp          # Tests de las 5 técnicas de optimización
├── ejemplos.txt                     # Expresiones de ejemplo
│
├── LEXICO.md                        # Documentación del análisis léxico
├── SINTACTICO.md                    # Documentación del análisis sintáctico
├── SEMANTICO.md                     # Documentación del análisis semántico
├── CODIGO_INTERMEDIO.md             # Documentación de la generación de código
└── INFORME_TECNICO.md               # Informe técnico completo del proyecto
```

### Descripción de los archivos principales

| Archivo | Rol |
|---|---|
| `MiLenguaje.g4` | Define los **tokens** (lexer) y las **reglas gramaticales** (parser). Es la fuente de verdad del lenguaje. |
| `App.java` | **Clase principal.** Orquesta las 7 etapas: léxico, sintáctico, AST gráfico, semántico, código intermedio, optimización y resumen. |
| `TablaSimbolos.java` | Modela la tabla de símbolos y acumula errores y warnings semánticos con línea y columna. |
| `SemanticoVisitor.java` | Recorre el AST y verifica tipos, ámbitos, declaraciones duplicadas, `break`/`continue` en bucles, etc. |
| `CodigoVisitor.java` | Recorre el AST y emite **código de tres direcciones** (temporales `t1`, `t2`, … y etiquetas). |
| `OptimizadorCodigo.java` | Aplica las 5 optimizaciones y reporta métricas de reducción. |

---

## ¿Cómo funciona el compilador?

El compilador implementa el **modelo clásico de compiladores** en dos etapas:
**análisis** (front-end) y **síntesis** (back-end). Todas las etapas operan sobre
el archivo de entrada y producen salidas visibles en consola y en archivos `.txt`.

```
                   archivo.cpp
                        │
                        ▼
   ┌──────────────────────────────────────┐
   │  1. ANÁLISIS LÉXICO                  │  → tabla de tokens
   │  (MiLenguajeLexer, ANTLR)            │
   └──────────────────────────────────────┘
                        │ tokens
                        ▼
   ┌──────────────────────────────────────┐
   │  2. ANÁLISIS SINTÁCTICO              │  → árbol de análisis (AST)
   │  (MiLenguajeParser, ANTLR)           │
   └──────────────────────────────────────┘
                        │ AST
                        ▼
   ┌──────────────────────────────────────┐
   │  3. VISUALIZACIÓN DEL AST            │  → ventana gráfica (Swing)
   │  (TreeViewer de ANTLR)               │
   └──────────────────────────────────────┘
                        │ AST
                        ▼
   ┌──────────────────────────────────────┐
   │  4. ANÁLISIS SEMÁNTICO               │  → tabla de símbolos
   │  (SemanticoVisitor)                  │     + errores/warnings
   └──────────────────────────────────────┘
                        │ AST validado
                        ▼
   ┌──────────────────────────────────────┐
   │  5. CÓDIGO INTERMEDIO                │  → código de 3 direcciones
   │  (CodigoVisitor)                     │
   └──────────────────────────────────────┘
                        │ código de 3 direcciones
                        ▼
   ┌──────────────────────────────────────┐
   │  6. OPTIMIZACIÓN (5 técnicas)        │  → código optimizado
   │  (OptimizadorCodigo)                 │
   └──────────────────────────────────────┘
                        │
                        ▼
   ┌──────────────────────────────────────┐
   │  7. RESUMEN DE COMPILACIÓN           │  → métricas y archivos de salida
   └──────────────────────────────────────┘
```

### 1. Análisis léxico

El lexer generado por ANTLR a partir de `MiLenguaje.g4` reconoce:

- **Palabras reservadas**: `if`, `else`, `while`, `for`, `break`, `continue`,
  `return`, tipos (`int`, `double`, `char`, `bool`, `void`, `string`), `true`, `false`.
- **Operadores**: aritméticos (`+ - * / %`), relacionales (`> < >= <= == !=`),
  lógicos (`&& ||`), asignación (`=`).
- **Delimitadores**: `( ) { } [ ] , ;`.
- **Literales**: números enteros y decimales, caracteres y cadenas.
- **Comentarios** de línea (`//`) y de bloque (`/* */`), que se descartan.

Los errores léxicos (caracteres inválidos) se capturan con un `ErrorListener`
personalizado y detienen la compilación. La salida es una **tabla de tokens**
con número, línea, columna, tipo y valor.

### 2. Análisis sintáctico

El parser aplica las reglas gramaticales definidas en `MiLenguaje.g4`
(recursión por la izquierda, que ANTLR 4 resuelve automáticamente). Construye el
**árbol de análisis sintáctico** a partir de la regla inicial `programa`.

La gramática modela:
- Declaraciones globales (funciones y variables).
- Bloques de sentencias `{ ... }`.
- Declaración de variables (simples y arreglos unidimensionales).
- Asignaciones, `if/else`, `while`, `for`, `return`, `break`, `continue`.
- Expresiones con precedencia de operadores (lógicos < relacionales < `+`/`-` < `*`/`/`/`%`).
- Llamadas a funciones y acceso a arreglos.

### 3. Visualización del AST

Usando `TreeViewer` de ANTLR se abre una **ventana Swing interactiva** con el
árbol sintáctico, con controles de zoom (`+`, `-`) y restablecimiento (`Reset`).

### 4. Análisis semántico

`SemanticoVisitor` recorre el AST y construye la **tabla de símbolos**, verificando:

- **Declaraciones duplicadas** en el mismo ámbito.
- **Variables no declaradas**.
- **Asignaciones a funciones** (inválidas).
- **Verificación de tipos**: asignaciones incompatibles, condiciones no booleanas,
  retornos incorrectos, argumentos con tipo erróneo, operaciones con tipos no numéricos.
- **`break`/`continue` fuera de un bucle**.
- **Warnings**: variables declaradas pero nunca utilizadas.

Los errores se reportan con **línea y columna** y distinguen entre errores
críticos (rojo, detienen la compilación) y warnings (amarillo).

### 5. Generación de código intermedio

`CodigoVisitor` recorre el AST y emite **código de tres direcciones**:
instrucciones con un operador y hasta tres operandos, usando temporales
(`t0`, `t1`, …), etiquetas (`THEN_1`, `END_IF_1`, …), `goto`, `CALL`,
`PARAM`, `DECLARE` y `return`.

### 6. Optimización de código intermedio

`OptimizadorCodigo` aplica **5 técnicas** en pasos sucesivos, guardando un
archivo por cada paso:

1. **Simplificación de expresiones** — evalúa expresiones constantes en tiempo
   de compilación (`5 + 3` → `8`) e identidades.
2. **Propagación de constantes** — reemplaza variables cuyo valor constante se
   conoce por el valor mismo.
3. **Eliminación de subexpresiones comunes** — evita recalcular subexpresiones
   repetidas.
4. **Eliminación de código muerto** — elimina sentencias inalcanzables o
   redundantes (p. ej. `temp = temp`).
5. **Optimización de bucles** — extrae cálculos invariantes fuera del bucle.

Finalmente se aplica una **optimización completa iterativa** y se reporta la
reducción porcentual de instrucciones.

---

## Guía de uso paso a paso

### Paso 1 — Compilar el proyecto

Desde la carpeta raíz del proyecto:

```bash
mvn clean package
```

Este comando:
1. Descarga las dependencias (ANTLR runtime, JUnit, etc.).
2. Genera el lexer y el parser a partir de `MiLenguaje.g4`.
3. Compila las clases Java.
4. Empaqueta un **JAR ejecutable con todas las dependencias**:
   `target/compiladorFinal-1.0-jar-with-dependencies.jar`.

### Paso 2 — Ejecutar el compilador sobre un archivo

```bash
java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar ejemplo_correcto.cpp
```

Reemplaza `ejemplo_correcto.cpp` por cualquier archivo fuente del subconjunto
de C++. El compilador ejecutará todas las fases y generará los archivos de
salida con el mismo nombre base:

| Salida | Contenido |
|---|---|
| `ejemplo_correcto_codigo_sin_optimizar.txt` | Código de tres direcciones original |
| `ejemplo_correcto_optimizacion_01_simplificacion.txt` | Tras la técnica 1 |
| `ejemplo_correcto_optimizacion_02_propagacion_constantes.txt` | Tras la técnica 2 |
| `ejemplo_correcto_optimizacion_03_subexpresiones_comunes.txt` | Tras la técnica 3 |
| `ejemplo_correcto_optimizacion_04_codigo_muerto.txt` | Tras la técnica 4 |
| `ejemplo_correcto_optimizacion_05_bucles.txt` | Tras la técnica 5 |
| `ejemplo_correcto_codigo_optimizado.txt` | Código final optimizado |

### Paso 3 — Probar con los archivos incluidos

El repositorio incluye varios casos de prueba:

```bash
# Programa correcto (todas las fases)
java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar ejemplo_correcto.cpp

# Programa con errores semánticos (duplicados, variables no declaradas)
java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar ejemplo_con_errores.cpp

# Tests exhaustivos de verificación de tipos
java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar test_errores_tipos.cpp

# Tests de las 5 técnicas de optimización
java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar test_optimizaciones.cpp
```

### Paso 4 — Script tutorial (recomendado para entrega)

**PowerShell (Windows):**

```powershell
.\compilador.ps1
```

**Bash (Linux/macOS):**

```bash
chmod +x compilador.sh
./compilador.sh
```

El script compila el proyecto, ejecuta los cuatro casos de prueba y muestra un
**resumen de verificación de consignas**, comparando manualmente las salidas de
cada fase de optimización.

### Paso 5 — Analizador sintáctico interactivo (opcional)

```cmd
misintactico.cmd "a + b * c"
misintactico.cmd (a + b) * c - d
```

Abre una ventana con el árbol sintáctico gráfico de la expresión indicada.

---

## El lenguaje soportado

El compilador reconoce el siguiente subconjunto de C++:

### Tipos de datos
`int`, `double`, `char`, `bool`, `void`, `string`

### Estructuras de control
`if`, `if/else`, `while`, `for`, `break`, `continue`, `return`

### Elementos del lenguaje
- Funciones con parámetros y retorno.
- Variables locales y globales.
- Arreglos unidimensionales.
- Operadores aritméticos, relacionales y lógicos.
- Comentarios de línea y de bloque.

### Ejemplo válido

```cpp
// Variables globales
int contadorGlobal;
double valorPi;

// Función que retorna valor
int sumar(int a, int b) {
    int resultado;
    resultado = a + b;
    contadorGlobal = contadorGlobal + 1;
    return resultado;
}

int main() {
    int estado;
    int numeros[3];

    contadorGlobal = 0;
    numeros[0] = 10;
    numeros[1] = 20;

    estado = sumar(numeros[0], numeros[1]);

    if (estado > 0) {
        estado = estado + 10;
    }

    return estado;
}
```

---

## Ejemplos

| Archivo | Qué demuestra |
|---|---|
| `ejemplo_correcto.cpp` | Programa válido que recorre las 5 fases sin errores. |
| `ejemplo_con_errores.cpp` | Errores semánticos: duplicados, variables no declaradas, asignación a función. |
| `test_errores_tipos.cpp` | Verificación de tipos: incompatibilidades, condiciones, retornos y argumentos. |
| `test_optimizaciones.cpp` | Las 5 técnicas de optimización aplicadas paso a paso. |
| `ejemplo_loops.cpp` | Estructuras `while` y `for` con su código intermedio. |
| `ejemplo_expresiones.txt` | Expresiones aritméticas de distinta complejidad. |

> Los archivos `*.txt` con sufijos como `_codigo_intermedio`, `_codigo_optimizado`
> o `_optimizacion_*` son **salidas de ejemplo** ya generadas por el compilador,
> para comparar resultados.

---

## Documentación técnica

Cada fase del compilador tiene su propia guía técnica en este repositorio:

- **`LEXICO.md`** — Tokens, reglas léxicas y manejo de errores léxicos.
- **`SINTACTICO.md`** — Gramática, análisis ascendente/descendente y AST.
- **`SEMANTICO.md`** — Tabla de símbolos, verificación de tipos y ámbitos.
- **`CODIGO_INTERMEDIO.md`** — Código de tres direcciones y generación.
- **`INFORME_TECNICO.md`** — Informe técnico general del trabajo final.

---

## Posibles extensiones

Ideas para continuar el desarrollo:

- [ ] Soporte de **generación de código máquina** o ensamblador.
- [ ] Tipos **`float`**, `long`, `unsigned` y arreglos multidimensionales.
- [ ] **Structs / clases** y manejo de objetos.
- [ ] **Paso de parámetros por referencia**.
- [ ] Optimizaciones adicionales: *loop unrolling*, *inlining*, análisis de flujo de datos.
- [ ] Un **entorno de desarrollo integrado (IDE)** simple en JavaFX.

---

## Créditos

Proyecto final de la materia **Técnicas de Compilación (2026)**.

- **David Abril Perrig**
- **Manuel Nicolais**
