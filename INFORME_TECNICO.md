# Trabajo Final - Técnicas de Compilación 2026

## Compilador para Subconjunto de C++

---

**Integrantes:**
- David Abril Perrig
- Manuel Nicolais

**Materia:** Técnicas de Compilación

**Año:** 2026

**Fecha de entrega:** 23/06/2026

---

## Índice

1. Introducción
2. Desarrollo
   - 2.1. Análisis Léxico
   - 2.2. Análisis Sintáctico
   - 2.3. Análisis Semántico (en desarrollo)
   - 2.4. Generación de Código Intermedio (pendiente)
   - 2.5. Optimización (pendiente)
3. Conclusiones
4. Bibliografía

---

## 1. Introducción

El presente trabajo consiste en el desarrollo de un compilador para un subconjunto del lenguaje C++, implementado en Java utilizando ANTLR (Another Tool for Language Recognition) para las fases de análisis léxico y sintáctico.

El compilador implementa las siguientes fases:
- **Análisis Léxico:** Reconocimiento de tokens del lenguaje
- **Análisis Sintáctico:** Construcción del árbol sintáctico abstracto (AST)
- **Análisis Semántico:** Verificación de tipos y declaraciones (en desarrollo)
- **Generación de Código Intermedio:** Código de tres direcciones (pendiente)
- **Optimización:** Técnicas de optimización de código (pendiente)

### Subconjunto de C++ Implementado

El compilador soporta los siguientes elementos del lenguaje:

**Tipos de datos:**
- `int`, `double`, `char`, `bool`, `void`, `string`

**Estructuras de control:**
- `if`, `if-else`
- `while`
- `for` (pendiente)

**Elementos del lenguaje:**
- Funciones con parámetros y retorno
- Variables locales y globales
- Arreglos unidimensionales
- Operadores aritméticos (`+`, `-`, `*`, `/`, `%`)
- Operadores relacionales (`==`, `!=`, `<`, `>`, `<=`, `>=`)
- Operadores lógicos (`&&`, `||`)
- Comentarios de línea (`//`) y bloque (`/* */`)

---

## 2. Desarrollo

### 2.1. Análisis Léxico

El análisis léxico es la primera fase del compilador. Su función es leer el código fuente carácter por carácter y agruparlo en **tokens** (unidades léxicas con significado).

#### Implementación

El análisis léxico se implementó utilizando ANTLR 4.9.3, que genera automáticamente un lexer a partir de la gramática definida en `MiLenguaje.g4`.

#### Tokens Reconocidos

**Palabras reservadas:**
```antlr
IF       : 'if' ;
ELSE     : 'else' ;
WHILE    : 'while' ;
RETURN   : 'return' ;
INT      : 'int' ;
DOUBLE   : 'double' ;
CHAR     : 'char' ;
BOOL     : 'bool' ;
VOID     : 'void' ;
STRING   : 'string' ;
TRUE     : 'true' ;
FALSE    : 'false' ;
```

**Operadores:**
```antlr
// Relacionales
EQL          : '==' ;
DISTINTO     : '!=' ;
MAYOR_IGUAL  : '>=' ;
MENOR_IGUAL  : '<=' ;
MAYOR        : '>' ;
MENOR        : '<' ;

// Lógicos
AND : '&&' ;
OR  : '||' ;

// Aritméticos
SUM : '+' ;
RES : '-' ;
MUL : '*' ;
DIV : '/' ;
MOD : '%' ;

// Asignación
ASIG : '=' ;
```

**Delimitadores:**
```antlr
PA      : '(' ;
PC      : ')' ;
LLAVE_A : '{' ;
LLAVE_C : '}' ;
CO      : '[' ;
CC      : ']' ;
COMA    : ',' ;
PYC     : ';' ;
```

**Literales:**
```antlr
CHARACTER : '\'' (~['\r\n\\] | '\\' .) '\'' ;
STR_LIT   : '"' (~["\r\n\\] | '\\' .)* '"' ;
ID        : [a-zA-Z_][a-zA-Z0-9_]* ;
INTEGER   : [0-9]+ ;
DECIMAL   : [0-9]+ '.' [0-9]+ ;
```

**Tokens ignorados:**
```antlr
COMENTARIO_LINEA  : '//' ~[\r\n]* -> skip ;
COMENTARIO_BLOQUE : '/*' .*? '*/' -> skip ;
WS                : [ \r\n\t]+ -> skip ;
```

#### Prioridad de Tokens

ANTLR utiliza las siguientes reglas de prioridad:
1. **El token más largo gana:** `>=` se reconoce como `MAYOR_IGUAL`, no como `MAYOR` + `ASIG`
2. **Si tienen la misma longitud, el primero en el archivo gana:** `if` se reconoce como `IF`, no como `ID`

Por esta razón, las palabras reservadas se definen antes que `ID`, y los operadores compuestos (`>=`, `<=`) antes que los simples (`>`, `<`).

#### Manejo de Errores Léxicos

El lexer detecta caracteres inválidos y los reporta como errores:

```java
lexer.removeErrorListeners();
lexer.addErrorListener(new BaseErrorListener() {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                          int line, int charPositionInLine, String msg, RecognitionException e) {
        erroresLexicos.add("ERROR LÉXICO en línea " + line + ":" + charPositionInLine + " - " + msg);
    }
});
```

Si se detectan errores léxicos, la compilación se aborta inmediatamente.

#### Ejemplo de Análisis Léxico

**Entrada:**
```c
int x = 10;
```

**Tokens generados:**
```
INT("int"), ID("x"), ASIG("="), INTEGER("10"), PYC(";")
```

### 2.2. Análisis Sintáctico

El análisis sintáctico es la segunda fase del compilador. Su función es verificar que los tokens formen una estructura válida según la gramática del lenguaje, construyendo un **árbol sintáctico abstracto (AST)**.

#### Gramática Implementada

La gramática se define en `MiLenguaje.g4` utilizando la notación de ANTLR:

```antlr
programa : declaracionGlobal* EOF ;

declaracionGlobal
    : funcionDecl
    | variableDecl
    ;

funcionDecl
    : tipo ID PA listaParams? PC bloque    # DeclFuncion
    ;

listaParams
    : param (COMA param)*
    ;

param
    : tipo ID
    ;

variableDecl
    : tipo ID PYC                # DeclVariable
    | tipo ID CO INTEGER CC PYC  # DeclArray
    ;

bloque
    : LLAVE_A sentencia* LLAVE_C
    ;

sentencia
    : variableDecl                    # SentDecl
    | expr ASIG expr PYC              # SentAsignacion
    | ID CO expr CC ASIG expr PYC     # SentAsignacionArray
    | IF PA expr PC bloque (ELSE bloque)?  # SentIf
    | WHILE PA expr PC bloque         # SentWhile
    | RETURN expr? PYC                # SentReturn
    | bloque                          # SentBloque
    | expr PYC                        # SentExpr
    ;

expr
    : expr opLogico expr         # ExprLogica
    | expr opRelacional expr     # ExprRelacional
    | expr (SUM | RES) expr      # ExprAritmetica
    | expr (MUL | DIV | MOD) expr  # ExprMulDiv
    | primaria                   # ExprPrimaria
    ;

primaria
    : ID PA listaArgs? PC    # LlamadaFuncion
    | ID CO expr CC          # AccesoArray
    | ID                     # Identificador
    | INTEGER                # Numero
    | DECIMAL                # NumeroDecimal
    | CHARACTER              # LiteralChar
    | STR_LIT                # LiteralString
    | TRUE                   # LiteralTrue
    | FALSE                  # LiteralFalse
    | PA expr PC             # Paren
    ;

tipo : INT | DOUBLE | CHAR | BOOL | VOID | STRING ;
```

#### Precedencia de Operadores

La gramática garantiza la precedencia correcta mediante el orden de las reglas:

1. **Operadores lógicos** (`&&`, `||`) - menor precedencia
2. **Operadores relacionales** (`==`, `!=`, `>`, `<`, `>=`, `<=`)
3. **Operadores aritméticos aditivos** (`+`, `-`)
4. **Operadores aritméticos multiplicativos** (`*`, `/`, `%`) - mayor precedencia
5. **Expresiones primarias** (identificadores, literales, paréntesis)

**Ejemplo:**
```c
2 + 3 * 4
```

El parser construye:
```
ExprAritmetica (+)
  ├── primaria -> 2
  └── ExprMulDiv (*)
        ├── primaria -> 3
        └── primaria -> 4
```

Resultado: `2 + (3 * 4) = 14`, NO `(2 + 3) * 4 = 20`.

#### Etiquetas (# Nombre)

Las etiquetas al final de cada alternativa (ej: `# DeclFuncion`, `# SentIf`) permiten que ANTLR genere métodos `visitXxx()` específicos en el Visitor, facilitando el recorrido del árbol en las fases posteriores.

#### Visualización del Árbol Sintáctico

El compilador incluye una visualización gráfica del AST utilizando `TreeViewer` de ANTLR:

```java
private static void mostrarArbolSintactico(ParseTree tree, MiLenguajeParser parser) {
    SwingUtilities.invokeLater(() -> {
        JFrame frame = new JFrame("Árbol Sintáctico");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        TreeViewer viewer = new TreeViewer(Arrays.asList(parser.getRuleNames()), tree);
        viewer.setScale(1.5);

        JScrollPane scrollPane = new JScrollPane(viewer);
        frame.add(scrollPane);

        // Controles de zoom
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
```

La ventana incluye:
- Visualización completa del árbol sintáctico
- Botones de zoom in (+), zoom out (-) y reset
- Scroll para árboles grandes

#### Manejo de Errores Sintácticos

El parser detecta estructuras inválidas y las reporta:

```java
parser.removeErrorListeners();
parser.addErrorListener(new BaseErrorListener() {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                          int line, int charPositionInLine, String msg, RecognitionException e) {
        erroresSintacticos.add("ERROR SINTÁCTICO en línea " + line + ":" + charPositionInLine + " - " + msg);
    }
});
```

Si se detectan errores sintácticos, la compilación se aborta.

#### Ejemplo de Análisis Sintáctico

**Entrada:**
```c
int sumar(int a, int b) {
    return a + b;
}
```

**Árbol sintáctico (simplificado):**
```
programa
  └── declaracionGlobal (DeclFuncion)
        ├── tipo: "int"
        ├── ID: "sumar"
        ├── listaParams
        │     ├── param: int a
        │     └── param: int b
        └── bloque
              └── sentencia (SentReturn)
                    └── expr (ExprAritmetica)
                          ├── primaria (Identificador): "a"
                          ├── SUM: "+"
                          └── primaria (Identificador): "b"
```

### 2.3. Análisis Semántico (en desarrollo)

El análisis semántico verifica que el programa tenga sentido lógico más allá de la sintaxis correcta.

**Funcionalidades implementadas:**
- Tabla de símbolos con ámbitos (scopes)
- Detección de variables duplicadas
- Detección de variables no declaradas
- Detección de funciones no declaradas
- Verificación de cantidad de argumentos en llamadas a funciones
- Warnings de variables no utilizadas

**Funcionalidades pendientes:**
- Verificación de tipos en asignaciones
- Verificación de tipos en operaciones
- Verificación de tipos en retornos de funciones

### 2.4. Generación de Código Intermedio (pendiente)

La generación de código intermedio transformará el AST en código de tres direcciones (TAC - Three Address Code).

**Instrucciones a generar:**
- Operaciones aritméticas: `t0 = a + b`
- Asignaciones: `x = t0`
- Acceso a arrays: `t1 = numeros[0]`
- Llamadas a funciones: `t2 = CALL func_sumar, arg1, arg2`
- Saltos condicionales: `if t3 goto THEN_0`
- Saltos incondicionales: `goto END_IF_1`
- Etiquetas: `THEN_0:`, `END_IF_1:`
- Declaraciones: `DECLARE x int`
- Parámetros: `PARAM a int`
- Retornos: `return resultado`

### 2.5. Optimización (pendiente)

Se implementarán al menos dos técnicas de optimización:

**Técnicas a implementar:**
1. **Propagación de constantes:** Reemplazar variables con valores constantes conocidos
2. **Eliminación de código muerto:** Eliminar código inalcanzable después de saltos incondicionales
3. **Simplificación de expresiones:** Evaluar expresiones constantes en tiempo de compilación
4. **Eliminación de sentencias redundantes:** Eliminar asignaciones innecesarias como `x = x`

---

## 3. Conclusiones

El desarrollo del compilador se encuentra en una etapa inicial, con las fases de análisis léxico y sintáctico completamente implementadas y funcionales.

**Logros hasta el momento:**
- ✅ Gramática completa del subconjunto de C++ definida en ANTLR
- ✅ Analizador léxico funcional con reconocimiento de todos los tokens
- ✅ Analizador sintáctico funcional con construcción de AST
- ✅ Visualización gráfica del árbol sintáctico
- ✅ Manejo de errores léxicos y sintácticos
- ✅ Ejemplos de prueba funcionales

**Próximos pasos:**
- Completar el análisis semántico (verificación de tipos)
- Implementar la generación de código intermedio
- Implementar técnicas de optimización
- Agregar soporte para bucles `for`
- Ampliar la suite de pruebas

---

## 4. Bibliografía

1. **ANTLR (Another Tool for Language Recognition)**
   - Sitio oficial: https://www.antlr.org/
   - Documentación: https://github.com/antlr/antlr4/blob/master/doc/index.md

2. **The Definitive ANTLR 4 Reference**
   - Autor: Terence Parr
   - Editorial: Pragmatic Bookshelf
   - Año: 2013

3. **Compilers: Principles, Techniques, and Tools (Dragon Book)**
   - Autores: Alfred V. Aho, Monica S. Lam, Ravi Sethi, Jeffrey D. Ullman
   - Editorial: Addison-Wesley
   - Año: 2006 (2da edición)

4. **Modern Compiler Implementation in Java**
   - Autor: Andrew W. Appel
   - Editorial: Cambridge University Press
   - Año: 2004

---

**Documento generado:** 05/06/2026

**Estado del proyecto:** En desarrollo (40% completado)
