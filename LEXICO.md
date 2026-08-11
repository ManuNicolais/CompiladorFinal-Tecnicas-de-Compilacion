# Analisis Lexico - Explicacion del Codigo

## Que hace esta fase?

Lee el archivo de entrada caracter por caracter y lo convierte en una lista de tokens.

**Ejemplo concreto:**
```
Entrada: "int x = 10;"
Salida: [INT("int"), ID("x"), ASIG("="), INTEGER("10"), PYC(";")]
```

## Codigo en App.java (lineas 23-56)

```java
// === 1. ANALISIS LEXICO ===
System.out.println("\n=== 1. ANÁLISIS LÉXICO ===");
MiLenguajeLexer lexer = new MiLenguajeLexer(input);
```

**Que hace:** Crea el lexer. `MiLenguajeLexer` es una clase que ANTLR genera automaticamente desde `MiLenguaje.g4`. Sabe reconocer todos los tokens que definimos en la gramatica.

```java
List<String> erroresLexicos = new ArrayList<>();
lexer.removeErrorListeners();
lexer.addErrorListener(new BaseErrorListener() {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                          int line, int charPositionInLine, String msg, RecognitionException e) {
        erroresLexicos.add("ERROR LÉXICO en línea " + line + ":" + charPositionInLine + " - " + msg);
    }
});
```

**Que hace:** Configura el manejo de errores. Por defecto ANTLR imprime errores a stderr. Aca lo cambiamos para que guarde los errores en una lista y podamos decidir que hacer con ellos despues.

- `removeErrorListeners()` - Saca el listener por defecto
- `addErrorListener()` - Agrega nuestro listener personalizado que guarda errores en `erroresLexicos`

```java
CommonTokenStream tokens = new CommonTokenStream(lexer);
tokens.fill();
```

**Que hace:** 
- `CommonTokenStream` crea un stream (lista) de tokens
- `fill()` le dice al lexer que procese TODO el archivo y llene el stream con tokens
- Sin `fill()`, el lexer procesaria tokens de a uno (lazy evaluation)

```java
if (!erroresLexicos.isEmpty()) {
    for (String err : erroresLexicos) {
        System.out.println("   ❌ " + err);
    }
    System.out.println("\n❌ Compilación abortada por errores léxicos.");
    return;
}
```

**Que hace:** Si hubo errores lexicos (caracteres invalidos como `@`, `#`), los muestra y aborta la compilacion. No tiene sentido seguir si el codigo tiene caracteres que no reconocemos.

```java
System.out.println("✅ Análisis léxico completado sin errores.");
System.out.println("   📊 Tokens procesados: " + (tokens.size() - 1));
```

**Que hace:** Muestra que todo salio bien. `tokens.size() - 1` porque el ultimo token siempre es `EOF` (end of file) y no lo contamos.

## Archivo MiLenguaje.g4 - Seccion de tokens

### Palabras reservadas (lineas 1-12)

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

**Que hace:** Define las palabras clave del lenguaje. Cuando el lexer ve `if`, genera un token de tipo `IF`.

**Por que van ANTES que `ID`?** Porque ANTLR usa la regla "primero en el archivo gana" cuando hay ambiguedad. Si `ID` estuviera antes, `if` se reconoceria como `ID("if")` en vez de `IF`.

### Operadores relacionales (lineas 15-21)

```antlr
EQL          : '==' ;
DISTINTO     : '!=' ;
MAYOR_IGUAL  : '>=' ;
MENOR_IGUAL  : '<=' ;
MAYOR        : '>' ;
MENOR        : '<' ;
```

**Que hace:** Define operadores de comparacion.

**Por que `>=` va antes que `>`?** Porque ANTLR usa la regla "token mas largo gana". Cuando ve `>=`, primero intenta matchear `>=` (2 caracteres) y solo si no puede, intenta `>` (1 caracter). Pero por seguridad, los ponemos en orden de mayor a menor longitud.

### Operadores logicos (lineas 24-25)

```antlr
AND : '&&' ;
OR  : '||' ;
```

**Que hace:** Define `&&` (y logico) y `||` (o logico).

### Operadores aritmeticos (lineas 28)

```antlr
SUM : '+' ;  RES : '-' ;  MUL : '*' ;  DIV : '/' ;  MOD : '%' ;
```

**Que hace:** Define suma, resta, multiplicacion, division y modulo.

### Asignacion (linea 31)

```antlr
ASIG : '=' ;
```

**Que hace:** Define el operador de asignacion. Ojo: es `=` (uno solo), no `==` (que es comparacion).

### Delimitadores (lineas 34-35)

```antlr
PA : '(' ;  PC : ')' ;  LLAVE_A : '{' ;  LLAVE_C : '}' ;
CO : '[' ;  CC : ']' ;  COMA : ',' ;  PYC : ';' ;
```

**Que hace:** Define simbolos de puntuacion:
- `PA`/`PC`: parentesis de apertura/cierre
- `LLAVE_A`/`LLAVE_C`: llaves de apertura/cierre
- `CO`/`CC`: corchetes (para arrays)
- `COMA`: separador de parametros
- `PYC`: punto y coma (fin de sentencia)

### Literales (lineas 38-39)

```antlr
CHARACTER : '\'' (~['\r\n\\] | '\\' .) '\'' ;
STR_LIT   : '"' (~["\r\n\\] | '\\' .)* '"' ;
```

**Que hace:**
- `CHARACTER`: reconoce caracteres entre comillas simples como `'A'`, `'\n'`
  - `~['\r\n\\]` = cualquier caracter EXCEPTO comilla simple, salto de linea o backslash
  - `'\\' .` = o un caracter escapado como `\n`, `\t`
- `STR_LIT`: reconoce strings entre comillas dobles como `"hola"`
  - Misma logica pero con `*` (cero o mas caracteres)

### Identificadores (linea 42)

```antlr
ID : [a-zA-Z_][a-zA-Z0-9_]* ;
```

**Que hace:** Reconoce nombres de variables, funciones, etc.
- `[a-zA-Z_]` = empieza con letra o guion bajo
- `[a-zA-Z0-9_]*` = seguido de cero o mas letras, digitos o guiones bajos

**Ejemplos:** `contador`, `_temp`, `x1`, `MI_CONSTANTE`

### Numeros (lineas 45-46)

```antlr
INTEGER : [0-9]+ ;
DECIMAL : [0-9]+ '.' [0-9]+ ;
```

**Que hace:**
- `INTEGER`: uno o mas digitos (ej: `123`, `0`, `999`)
- `DECIMAL`: digitos, punto, digitos (ej: `3.14`, `0.5`)

**Por que `DECIMAL` va antes que `INTEGER`?** Porque cuando ANTLR ve `3.14`, primero intenta matchear `DECIMAL` (que es mas largo: `3.14` = 4 caracteres) y solo si no puede, intenta `INTEGER` (que seria solo `3` = 1 caracter).

### Tokens que se ignoran (lineas 49-51)

```antlr
COMENTARIO_LINEA  : '//' ~[\r\n]* -> skip ;
COMENTARIO_BLOQUE : '/*' .*? '*/' -> skip ;
WS                : [ \r\n\t]+ -> skip ;
```

**Que hace:**
- `COMENTARIO_LINEA`: reconoce `// comentario hasta fin de linea` y lo descarta (`-> skip`)
- `COMENTARIO_BLOQUE`: reconoce `/* comentario multilínea */` y lo descarta
- `WS`: reconoce espacios, tabs, saltos de linea y los descarta

**Por que `-> skip`?** Porque los comentarios y espacios no son tokens que el parser necesite. Si no los descartaramos, el parser veria tokens `WS` entre cada token real y tendria que ignorarlos manualmente.

### Token de error (linea 54)

```antlr
OTRO : . ;
```

**Que hace:** Captura cualquier caracter que no coincida con las reglas anteriores. El `.` significa "cualquier caracter".

**Para que sirve?** Si el usuario escribe `@` o `#`, el lexer genera un token `OTRO` y nuestro listener de errores lo reporta. Sin esto, ANTLR lanzaria una excepcion.

## Flujo completo del analisis lexico

```
1. App.java llama a CharStreams.fromFileName("ejemplo.txt")
   -> Lee el archivo y lo convierte en un stream de caracteres

2. Crea MiLenguajeLexer(input)
   -> El lexer sabe como reconocer tokens (definidos en MiLenguaje.g4)

3. Crea CommonTokenStream(lexer)
   -> Wrapper que almacena tokens en una lista

4. Llama a tokens.fill()
   -> El lexer procesa TODO el archivo:
      - Lee "int" -> genera token INT
      - Lee " " -> genera token WS -> lo descarta (skip)
      - Lee "x" -> genera token ID
      - Lee " " -> genera token WS -> lo descarta
      - Lee "=" -> genera token ASIG
      - Lee " " -> genera token WS -> lo descarta
      - Lee "10" -> genera token INTEGER
      - Lee ";" -> genera token PYC
      - Lee EOF -> genera token EOF

5. tokens ahora contiene: [INT, ID, ASIG, INTEGER, PYC, EOF]

6. Si hay errores (caracteres invalidos), se guardaron en erroresLexicos

7. App.java verifica si erroresLexicos esta vacio
   -> Si no esta vacio, muestra errores y aborta
   -> Si esta vacio, continua al analisis sintactico
```

## Preguntas frecuentes

**P: Por que no mostramos los tokens en pantalla?**
R: En versiones anteriores si los mostrabamos, pero el formato del Ejemplo_Final solo muestra la cantidad de tokens procesados, no cada uno individualmente.

**P: Que pasa si el archivo esta vacio?**
R: El lexer genera solo el token `EOF` y `tokens.size() - 1` da 0. El parser despues fallara porque espera al menos una declaracion.

**P: Que diferencia hay entre `MiLenguajeLexer` y `MiLenguajeParser`?**
R: 
- `MiLenguajeLexer`: reconoce tokens (analisis lexico)
- `MiLenguajeParser`: verifica estructura (analisis sintactico)
- Ambos son generados por ANTLR desde `MiLenguaje.g4`
