# Analisis Sintactico - Explicacion del Codigo

## Que hace esta fase?

Verifica que los tokens formen una estructura valida segun la gramatica y construye un arbol sintactico.

**Ejemplo concreto:**
```
Entrada: [INT, ID("sumar"), PA, INT, ID("a"), COMA, INT, ID("b"), PC, LLAVE_A, ...]
Salida: Arbol sintactico que representa la estructura del programa
```

## Codigo en App.java (lineas 59-83)

```java
// === 2. ANALISIS SINTACTICO ===
System.out.println("\n=== 2. ANÁLISIS SINTÁCTICO ===");
tokens.seek(0);
```

**Que hace:** Reinicia el stream de tokens al inicio. Despues del analisis lexico, el stream esta al final. `seek(0)` lo vuelve a posicionar en el primer token para que el parser pueda leerlo desde el principio.

```java
MiLenguajeParser parser = new MiLenguajeParser(tokens);
```

**Que hace:** Crea el parser. `MiLenguajeParser` es una clase que ANTLR genera automaticamente desde `MiLenguaje.g4`. Sabe como verificar la estructura del programa usando las reglas sintacticas que definimos.

```java
List<String> erroresSintacticos = new ArrayList<>();
parser.removeErrorListeners();
parser.addErrorListener(new BaseErrorListener() {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                          int line, int charPositionInLine, String msg, RecognitionException e) {
        erroresSintacticos.add("ERROR SINTÁCTICO en línea " + line + ":" + charPositionInLine + " - " + msg);
    }
});
```

**Que hace:** Configura el manejo de errores sintacticos (igual que en el lexico). Cuando el parser encuentra algo que no cumple la gramatica (ej: falta un `;`, hay un `)` de mas), llama a `syntaxError()` y nosotros guardamos el error en la lista.

```java
ParseTree tree = parser.programa();
```

**Que hace:** **Esta es la linea mas importante.** Le dice al parser que empiece a parsear desde la regla `programa` (la regla inicial de la gramatica). El parser:
1. Lee tokens del stream
2. Verifica que cumplan la gramatica
3. Construye un arbol (ParseTree) que representa la estructura

Si el codigo es valido, `tree` contiene el arbol completo. Si hay errores, el parser los reporta pero igual intenta construir un arbol parcial.

```java
if (!erroresSintacticos.isEmpty()) {
    for (String err : erroresSintacticos) {
        System.out.println("   ❌ " + err);
    }
    System.out.println("\n❌ Compilación abortada por errores sintácticos.");
    return;
}
```

**Que hace:** Si hubo errores sintacticos, los muestra y aborta. No tiene sentido hacer analisis semantico si la estructura del programa es invalida.

```java
System.out.println("✅ Análisis sintáctico completado sin errores.");
System.out.println("   📊 Árbol sintáctico generado correctamente");
```

**Que hace:** Confirma que el programa es sintacticamente valido y que se genero el arbol.

## Visualizacion del arbol (lineas 86-88)

```java
// === 3. VISUALIZACION DEL AST ===
System.out.println("\n=== 3. VISUALIZACIÓN DEL AST ===");
mostrarArbolSintactico(tree, parser);
System.out.println("   📊 Ventana del árbol sintáctico abierta");
```

**Que hace:** Llama a la funcion `mostrarArbolSintactico()` que abre una ventana grafica con el arbol.

### Funcion mostrarArbolSintactico() (lineas 125-153)

```java
private static void mostrarArbolSintactico(ParseTree tree, MiLenguajeParser parser) {
    SwingUtilities.invokeLater(() -> {
```

**Que hace:** `SwingUtilities.invokeLater()` ejecuta el codigo dentro de la lambda en el hilo de la interfaz grafica (EDT - Event Dispatch Thread). Esto es necesario porque Swing no es thread-safe y todas las operaciones de UI deben hacerse en el EDT.

```java
        JFrame frame = new JFrame("Árbol Sintáctico");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
```

**Que hace:** Crea una ventana (JFrame) con titulo "Árbol Sintáctico". `DISPOSE_ON_CLOSE` significa que cuando se cierra la ventana, se libera la memoria pero el programa sigue corriendo (no se cierra la consola).

```java
        TreeViewer viewer = new TreeViewer(Arrays.asList(parser.getRuleNames()), tree);
        viewer.setScale(1.5);
```

**Que hace:** 
- `TreeViewer` es una clase de ANTLR que dibuja arboles sintacticos
- `parser.getRuleNames()` devuelve los nombres de las reglas de la gramatica (programa, funcionDecl, sentencia, etc.)
- `tree` es el arbol que genero el parser
- `setScale(1.5)` hace zoom inicial del 150%

```java
        JScrollPane scrollPane = new JScrollPane(viewer);
        frame.add(scrollPane);
```

**Que hace:** Mete el viewer en un JScrollPane para que se pueda hacer scroll si el arbol es muy grande. Luego agrega el scroll pane a la ventana.

```java
        JPanel controls = new JPanel();
        JButton zoomIn = new JButton("+");
        JButton zoomOut = new JButton("-");
        JButton reset = new JButton("Reset");
```

**Que hace:** Crea un panel con 3 botones para controlar el zoom.

```java
        zoomIn.addActionListener(e -> viewer.setScale(viewer.getScale() * 1.2));
        zoomOut.addActionListener(e -> viewer.setScale(viewer.getScale() / 1.2));
        reset.addActionListener(e -> viewer.setScale(1.5));
```

**Que hace:** 
- Boton `+`: multiplica el scale actual por 1.2 (acerca 20%)
- Boton `-`: divide el scale actual por 1.2 (aleja 20%)
- Boton `Reset`: vuelve al scale original de 1.5

```java
        controls.add(zoomIn);
        controls.add(zoomOut);
        controls.add(reset);
        frame.add(controls, BorderLayout.SOUTH);
```

**Que hace:** Agrega los botones al panel de controles y luego agrega ese panel a la parte inferior (SOUTH) de la ventana.

```java
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    });
}
```

**Que hace:**
- `setSize(1200, 800)`: tamano de la ventana (1200px ancho, 800px alto)
- `setLocationRelativeTo(null)`: centra la ventana en la pantalla
- `setVisible(true)`: hace visible la ventana

## Archivo MiLenguaje.g4 - Seccion de reglas sintacticas

### Regla programa (linea 58)

```antlr
programa : declaracionGlobal* EOF ;
```

**Que hace:** Un programa es cero o mas declaraciones globales seguidas de EOF (end of file).

**Ejemplo:**
```
int x;              <- declaracionGlobal
int sumar(...) {}   <- declaracionGlobal
int main() {}       <- declaracionGlobal
EOF                 <- fin del archivo
```

### Regla declaracionGlobal (linea 60)

```antlr
declaracionGlobal : funcionDecl | variableDecl ;
```

**Que hace:** Una declaracion global puede ser una funcion O una variable.

### Regla funcionDecl (linea 62)

```antlr
funcionDecl : tipo ID PA listaParams? PC bloque    # DeclFuncion ;
```

**Que hace:** Una funcion tiene:
- `tipo`: tipo de retorno (int, void, etc.)
- `ID`: nombre de la funcion
- `PA`: parentesis de apertura `(`
- `listaParams?`: lista opcional de parametros (el `?` significa 0 o 1 vez)
- `PC`: parentesis de cierre `)`
- `bloque`: cuerpo de la funcion entre `{ }`

**Ejemplo:**
```
int sumar(int a, int b) { ... }
 ^    ^    ^  ^^^^^^^^^^   ^^^
 |    |    |      |         |
tipo  ID   PA  listaParams  bloque
```

**Por que `# DeclFuncion`?** Es una etiqueta que ANTLR usa para generar el metodo `visitDeclFuncion()` en el Visitor.

### Regla variableDecl (lineas 64-67)

```antlr
variableDecl
    : tipo ID PYC                # DeclVariable
    | tipo ID CO INTEGER CC PYC  # DeclArray
    ;
```

**Que hace:** Una declaracion de variable puede ser:
- Variable simple: `int x;` (tipo + nombre + punto y coma)
- Array: `int numeros[3];` (tipo + nombre + `[` + tamano + `]` + punto y coma)

**Ejemplos:**
```
int contador;       <- DeclVariable
double pi;          <- DeclVariable
int numeros[10];    <- DeclArray
```

### Regla bloque (linea 69)

```antlr
bloque : LLAVE_A sentencia* LLAVE_C ;
```

**Que hace:** Un bloque es `{` seguido de cero o mas sentencias seguido de `}`.

**Ejemplo:**
```
{
    int x;           <- sentencia
    x = 10;          <- sentencia
    if (...) { }     <- sentencia
}
```

### Regla sentencia (lineas 71-80)

```antlr
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
```

**Que hace:** Una sentencia puede ser:

1. **SentDecl**: declaracion de variable dentro de una funcion/bloque
   ```
   int resultado;
   ```

2. **SentAsignacion**: asignacion simple
   ```
   x = 10;
   ```

3. **SentAsignacionArray**: asignacion a elemento de array
   ```
   numeros[0] = 10;
   ```

4. **SentIf**: if con else opcional
   ```
   if (x > 0) { ... } else { ... }
   ```

5. **SentWhile**: bucle while
   ```
   while (x < 10) { ... }
   ```

6. **SentReturn**: retorno de funcion con expresion opcional
   ```
   return resultado;
   return;  // en funciones void
   ```

7. **SentBloque**: bloque anidado
   ```
   { int temp; temp = x; }
   ```

8. **SentExpr**: expresion como sentencia (ej: llamada a funcion)
   ```
   sumar(5, 3);
   ```

### Regla expr (lineas 82-88)

```antlr
expr
    : expr opLogico expr         # ExprLogica
    | expr opRelacional expr     # ExprRelacional
    | expr (SUM | RES) expr      # ExprAritmetica
    | expr (MUL | DIV | MOD) expr  # ExprMulDiv
    | primaria                   # ExprPrimaria
    ;
```

**Que hace:** Define expresiones con precedencia de operadores (de menor a mayor):

1. **ExprLogica**: operadores logicos `&&`, `||` (menor precedencia)
   ```
   x > 0 && y < 10
   ```

2. **ExprRelacional**: operadores relacionales `==`, `!=`, `>`, `<`, `>=`, `<=`
   ```
   x > 0
   ```

3. **ExprAritmetica**: suma y resta
   ```
   x + y - z
   ```

4. **ExprMulDiv**: multiplicacion, division, modulo (mayor precedencia)
   ```
   x * y / z
   ```

5. **ExprPrimaria**: expresion atomica (sin operadores)
   ```
   x, 10, sumar(5, 3), (x + y)
   ```

**Como funciona la precedencia:**
```
Entrada: 2 + 3 * 4

El parser ve:
- 2 + 3 * 4
- Intenta matchear ExprAritmetica: expr + expr
  - expr izquierda: 2 (ExprPrimaria)
  - expr derecha: 3 * 4
    - Intenta matchear ExprMulDiv: expr * expr
      - expr izquierda: 3 (ExprPrimaria)
      - expr derecha: 4 (ExprPrimaria)

Resultado: 2 + (3 * 4) = 14
NO: (2 + 3) * 4 = 20
```

### Regla primaria (lineas 90-99)

```antlr
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
```

**Que hace:** Define expresiones atomicas (sin operadores binarios):

1. **LlamadaFuncion**: `sumar(5, 3)`
2. **AccesoArray**: `numeros[0]`
3. **Identificador**: `x`, `contador`
4. **Numero**: `10`, `42`
5. **NumeroDecimal**: `3.14`
6. **LiteralChar**: `'A'`, `'\n'`
7. **LiteralString**: `"hola"`
8. **LiteralTrue**: `true`
9. **LiteralFalse**: `false`
10. **Paren**: `(x + y)` (expresion entre parentesis)

**Por que el orden importa?**
- `LlamadaFuncion` va antes que `Identificador` porque `sumar(5, 3)` es mas especifico que solo `sumar`
- `AccesoArray` va antes que `Identificador` porque `numeros[0]` es mas especifico que solo `numeros`

### Regla listaArgs (linea 101)

```antlr
listaArgs : expr (COMA expr)* ;
```

**Que hace:** Lista de argumentos separados por comas.
- `expr`: primera expresion
- `(COMA expr)*`: cero o mas repeticiones de `,` + expresion

**Ejemplo:**
```
sumar(5, 3, x + y)
      ^  ^  ^^^^^
      |  |     |
    expr |   expr
         |
       COMA
```

## Flujo completo del analisis sintactico

```
1. App.java reinicia el stream: tokens.seek(0)

2. Crea MiLenguajeParser(tokens)
   -> El parser sabe como verificar la gramatica

3. Configura listener de errores

4. Llama a parser.programa()
   -> El parser empieza desde la regla "programa"
   -> Lee tokens y verifica que cumplan:
      programa : declaracionGlobal* EOF
   
   -> Para cada declaracionGlobal, verifica:
      declaracionGlobal : funcionDecl | variableDecl
   
   -> Si es funcionDecl, verifica:
      funcionDecl : tipo ID PA listaParams? PC bloque
      
      -> Lee "int" -> tipo valido
      -> Lee "sumar" -> ID
      -> Lee "(" -> PA
      -> Lee "int a, int b" -> listaParams
      -> Lee ")" -> PC
      -> Lee "{ ... }" -> bloque
      
      -> Si todo coincide, crea nodo DeclFuncion en el arbol
   
   -> Si algo no coincide, llama a syntaxError()

5. Si no hay errores, tree contiene el arbol completo

6. App.java verifica si erroresSintacticos esta vacio
   -> Si no esta vacio, muestra errores y aborta
   -> Si esta vacio, abre ventana con el arbol y continua al semantico
```

## Preguntas frecuentes

**P: Que es un ParseTree?**
R: Es un arbol que representa la estructura del programa. Cada nodo es una regla de la gramatica o un token. Por ejemplo:
```
programa
  └── declaracionGlobal (DeclFuncion)
        ├── tipo: "int"
        ├── ID: "sumar"
        ├── PA: "("
        ├── listaParams
        │     ├── param: int a
        │     └── param: int b
        ├── PC: ")"
        └── bloque
              └── ...
```

**P: Por que usamos `#` etiquetas en las reglas?**
R: Para que ANTLR genere metodos `visitXxx()` especificos en el Visitor. Sin etiquetas, todas las alternativas de `sentencia` generarian un solo metodo `visitSentencia()` y tendriamos que usar `instanceof` para distinguir que tipo de sentencia es.

**P: Que pasa si el parser encuentra un error?**
R: ANTLR intenta recuperarse del error (error recovery) y seguir parseando. Por eso puede reportar multiples errores de una sola vez. Pero nosotros abortamos en cuanto hay al menos un error.

**P: Por que `tokens.seek(0)`?**
R: Porque despues del analisis lexico, el stream de tokens esta al final (despues de hacer `tokens.fill()`). El parser necesita leer desde el principio, asi que lo reiniciamos.
