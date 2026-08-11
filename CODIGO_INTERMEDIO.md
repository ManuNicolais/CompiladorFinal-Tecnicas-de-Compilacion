# Codigo Intermedio - Explicacion del Codigo

## Que hace esta fase?

Transforma el arbol sintactico en codigo de tres direcciones (TAC - Three Address Code), una representacion intermedia entre el codigo fuente y el codigo maquina.

**Ejemplo concreto:**
```
Entrada: resultado = a + b;
Salida:
  t0 = a + b
  resultado = t0
```

## Codigo en App.java (lineas 120-135)

```java
// === 5. GENERACION DE CODIGO INTERMEDIO ===
System.out.println("\n=== 5. GENERACIÓN DE CÓDIGO INTERMEDIO ===");
System.out.println("   🎯 Iniciando recorrido del AST con CodigoVisitor...");
CodigoVisitor codigo = new CodigoVisitor();
```

**Que hace:** Crea el visitor de codigo intermedio. Este visitor recorre el arbol y genera instrucciones TAC.

```java
codigo.visit(tree);
```

**Que hace:** Recorre el arbol sintactico y genera instrucciones. Cada metodo `visitXxx()` genera una o mas instrucciones y las guarda en una lista interna.

```java
System.out.println("   📝 Código de tres direcciones generado:\n");
codigo.imprimir();
```

**Que hace:** Muestra las instrucciones generadas en pantalla.

```java
String nombreBase = nombreArchivo.replaceFirst("[.][^.]+$", "");
String archivoIntermedio = nombreBase + "_codigo_intermedio.txt";

codigo.guardarEnArchivo(archivoIntermedio);

System.out.println("\n✅ Código intermedio guardado en: " + archivoIntermedio);
```

**Que hace:** 
1. Extrae el nombre del archivo sin extension (ej: "ejemplo_correcto")
2. Le agrega "_codigo_intermedio.txt" (ej: "ejemplo_correcto_codigo_intermedio.txt")
3. Guarda las instrucciones en ese archivo
4. Confirma que se guardo

## CodigoVisitor.java - Explicacion linea por linea

### Campos (lineas 7-10)

```java
public class CodigoVisitor extends MiLenguajeBaseVisitor<String> {

    private List<String> codigo = new ArrayList<>();
    private int tempCount = 0;
    private int labelCount = 0;
```

**Que hace:**
- `codigo`: lista de instrucciones generadas
- `tempCount`: contador para generar nombres de temporales (t0, t1, t2...)
- `labelCount`: contador para generar etiquetas (THEN_0, ELSE_1, END_IF_2...)

**Por que retorna `String`?** Porque cada metodo `visitXxx()` retorna el nombre del temporal donde quedo el resultado, para que el nodo padre pueda usarlo.

### nuevaTemp() (lineas 12-14)

```java
private String nuevaTemp() {
    return "t" + (tempCount++);
}
```

**Que hace:** Genera un nombre de temporal unico.

**Ejemplo:**
```
1ra llamada: retorna "t0", tempCount queda en 1
2da llamada: retorna "t1", tempCount queda en 2
3ra llamada: retorna "t2", tempCount queda en 3
```

### nuevaLabel() (lineas 16-18)

```java
private String nuevaLabel(String prefijo) {
    return prefijo + "_" + (labelCount++);
}
```

**Que hace:** Genera una etiqueta unica con un prefijo.

**Ejemplo:**
```
nuevaLabel("THEN"): retorna "THEN_0", labelCount queda en 1
nuevaLabel("ELSE"): retorna "ELSE_1", labelCount queda en 2
nuevaLabel("END_IF"): retorna "END_IF_2", labelCount queda en 3
```

### emitir() (lineas 20-22)

```java
private void emitir(String instruccion) {
    codigo.add(instruccion);
}
```

**Que hace:** Agrega una instruccion a la lista de codigo.

### getCodigo() (lineas 24-26)

```java
public List<String> getCodigo() {
    return codigo;
}
```

**Que hace:** Devuelve la lista de instrucciones generadas.

### imprimir() (lineas 28-32)

```java
public void imprimir() {
    for (int i = 0; i < codigo.size(); i++) {
        System.out.printf("%3d: %s\n", i, codigo.get(i));
    }
}
```

**Que hace:** Imprime cada instruccion con su numero de linea. `%3d` formatea el numero con ancho minimo de 3 caracteres (alineado a la derecha).

**Ejemplo:**
```
  0: PROGRAMA_INICIO:
  1: DECLARE contadorGlobal int
  2: func_sumar:
```

### guardarEnArchivo() (lineas 34-43)

```java
public void guardarEnArchivo(String nombreArchivo) {
    try (FileWriter writer = new FileWriter(nombreArchivo)) {
        for (int i = 0; i < codigo.size(); i++) {
            writer.write(String.format("%3d: %s\n", i, codigo.get(i)));
        }
    } catch (IOException e) {
        System.err.println("Error al guardar código intermedio: " + e.getMessage());
    }
}
```

**Que hace:** Guarda las instrucciones en un archivo de texto. `try (FileWriter writer = ...)` usa try-with-resources para cerrar automaticamente el archivo al terminar.

### visitPrograma() (lineas 45-55)

```java
@Override
public String visitPrograma(MiLenguajeParser.ProgramaContext ctx) {
    emitir("// Código de tres direcciones generado");
    emitir("PROGRAMA_INICIO:");
    emitir("// Declaración de variables globales");
    for (MiLenguajeParser.DeclaracionGlobalContext d : ctx.declaracionGlobal()) {
        visit(d);
    }
    emitir("PROGRAMA_FIN:");
    return null;
}
```

**Que hace:** 
1. Emite comentario inicial
2. Emite etiqueta `PROGRAMA_INICIO:`
3. Emite comentario para variables globales
4. Visita cada declaracion global
5. Emite etiqueta `PROGRAMA_FIN:`

**Ejemplo de salida:**
```
  0: // Código de tres direcciones generado
  1: PROGRAMA_INICIO:
  2: // Declaración de variables globales
  3: DECLARE contadorGlobal int
  ...
 53: PROGRAMA_FIN:
```

### visitDeclFuncion() (lineas 57-70)

```java
@Override
public String visitDeclFuncion(MiLenguajeParser.DeclFuncionContext ctx) {
    String nombre = ctx.ID().getText();
    emitir("func_" + nombre + ":");
```

**Que hace:** Emite una etiqueta para la funcion con prefijo "func_".

**Ejemplo:**
```
int sumar(int a, int b) { ... }
-> emite: "func_sumar:"
```

```java
    if (ctx.listaParams() != null) {
        for (MiLenguajeParser.ParamContext p : ctx.listaParams().param()) {
            emitir("PARAM " + p.ID().getText() + " " + p.tipo().getText());
        }
    }
```

**Que hace:** Para cada parametro, emite una instruccion `PARAM nombre tipo`.

**Ejemplo:**
```
int sumar(int a, int b)
-> emite:
   PARAM a int
   PARAM b int
```

```java
    for (MiLenguajeParser.SentenciaContext s : ctx.bloque().sentencia()) {
        visit(s);
    }
    return null;
}
```

**Que hace:** Visita cada sentencia del cuerpo de la funcion.

### visitDeclVariable() (lineas 72-75)

```java
@Override
public String visitDeclVariable(MiLenguajeParser.DeclVariableContext ctx) {
    emitir("DECLARE " + ctx.ID().getText() + " " + ctx.tipo().getText());
    return null;
}
```

**Que hace:** Emite una instruccion `DECLARE nombre tipo`.

**Ejemplo:**
```
int contador;
-> emite: "DECLARE contador int"
```

### visitDeclArray() (lineas 77-80)

```java
@Override
public String visitDeclArray(MiLenguajeParser.DeclArrayContext ctx) {
    emitir("DECLARE " + ctx.ID().getText() + "[" + ctx.INTEGER().getText() + "] " + ctx.tipo().getText());
    return null;
}
```

**Que hace:** Emite una instruccion `DECLARE nombre[tamano] tipo`.

**Ejemplo:**
```
int numeros[3];
-> emite: "DECLARE numeros[3] int"
```

### visitSentDecl() (lineas 82-85)

```java
@Override
public String visitSentDecl(MiLenguajeParser.SentDeclContext ctx) {
    return visit(ctx.variableDecl());
}
```

**Que hace:** Delega a `visitDeclVariable()` o `visitDeclArray()`.

### visitSentAsignacion() (lineas 87-103)

```java
@Override
public String visitSentAsignacion(MiLenguajeParser.SentAsignacionContext ctx) {
    MiLenguajeParser.ExprContext lhs = ctx.expr(0);
    String valor = visit(ctx.expr(1));
```

**Que hace:** 
1. Obtiene el lado izquierdo de la asignacion
2. Visita el lado derecho y obtiene el nombre del temporal donde quedo el resultado

```java
    if (lhs instanceof MiLenguajeParser.ExprPrimariaContext
        && ((MiLenguajeParser.ExprPrimariaContext) lhs).primaria() instanceof MiLenguajeParser.AccesoArrayContext) {
        MiLenguajeParser.AccesoArrayContext arr = (MiLenguajeParser.AccesoArrayContext) ((MiLenguajeParser.ExprPrimariaContext) lhs).primaria();
        String id = arr.ID().getText();
        String indice = visit(arr.expr());
        emitir(id + "[" + indice + "] = " + valor);
```

**Que hace:** Si el lado izquierdo es un acceso a array (ej: `numeros[0] = 10`):
1. Extrae el nombre del array
2. Visita la expresion del indice y obtiene el temporal
3. Emite `array[indice] = valor`

**Ejemplo:**
```
numeros[0] = 10;
-> visita expr(1): "10" -> retorna "10"
-> emite: "numeros[0] = 10"
```

```java
    } else {
        String target = visit(lhs);
        emitir(target + " = " + valor);
    }
    return null;
}
```

**Que hace:** Si es una asignacion simple (ej: `x = 10`):
1. Visita el lado izquierdo y obtiene el nombre de la variable
2. Emite `variable = valor`

**Ejemplo:**
```
resultado = a + b;
-> visita expr(0): "resultado" -> retorna "resultado"
-> visita expr(1): "a + b" -> emite "t0 = a + b", retorna "t0"
-> emite: "resultado = t0"
```

### visitSentAsignacionArray() (lineas 105-113)

```java
@Override
public String visitSentAsignacionArray(MiLenguajeParser.SentAsignacionArrayContext ctx) {
    String id = ctx.ID().getText();
    String indice = visit(ctx.expr(0));
    String valor = visit(ctx.expr(1));
    emitir(id + "[" + indice + "] = " + valor);
    return null;
}
```

**Que hace:** Maneja asignaciones a arrays con la sintaxis explicita `ID[expr] = expr`.

**Ejemplo:**
```
numeros[0] = 10;
-> id = "numeros"
-> visita expr(0): "0" -> retorna "0"
-> visita expr(1): "10" -> retorna "10"
-> emite: "numeros[0] = 10"
```

### visitSentIf() (lineas 115-135)

```java
@Override
public String visitSentIf(MiLenguajeParser.SentIfContext ctx) {
    String condicion = visit(ctx.expr());
```

**Que hace:** Visita la condicion y obtiene el temporal donde quedo el resultado.

**Ejemplo:**
```
if (estado > 0) { ... }
-> visita expr: "estado > 0"
   -> emite: "t9 = estado > 0"
   -> retorna "t9"
```

```java
    String thenLabel = nuevaLabel("THEN");
    String elseLabel = nuevaLabel("ELSE");
    String endLabel = nuevaLabel("END_IF");
```

**Que hace:** Genera tres etiquetas unicas.

**Ejemplo:**
```
thenLabel = "THEN_0"
elseLabel = "ELSE_1"
endLabel = "END_IF_2"
```

```java
    emitir("if " + condicion + " goto " + thenLabel);
    emitir("goto " + elseLabel);
```

**Que hace:** Emite el salto condicional y el salto incondicional al else.

**Ejemplo:**
```
if t9 goto THEN_0
goto ELSE_1
```

```java
    emitir(thenLabel + ":");
    visit(ctx.bloque(0));
    emitir("goto " + endLabel);
```

**Que hace:** 
1. Emite la etiqueta del then
2. Visita el bloque then
3. Emite salto al final del if

**Ejemplo:**
```
THEN_0:
  ... (codigo del then)
  goto END_IF_2
```

```java
    emitir(elseLabel + ":");
    if (ctx.bloque().size() > 1) {
        visit(ctx.bloque(1));
    }
```

**Que hace:** 
1. Emite la etiqueta del else
2. Si hay bloque else, lo visita

**Ejemplo:**
```
ELSE_1:
  ... (codigo del else, si existe)
```

```java
    emitir(endLabel + ":");
    return null;
}
```

**Que hace:** Emite la etiqueta de fin del if.

**Ejemplo completo:**
```
if (estado > 0) {
    auxiliar = estado + 10;
} else {
    auxiliar = 0;
}

Genera:
  t9 = estado > 0
  if t9 goto THEN_0
  goto ELSE_1
THEN_0:
  t10 = estado + 10
  auxiliar = t10
  goto END_IF_2
ELSE_1:
  auxiliar = 0
END_IF_2:
```

### visitSentWhile() (lineas 137-155)

```java
@Override
public String visitSentWhile(MiLenguajeParser.SentWhileContext ctx) {
    String inicioLabel = nuevaLabel("WHILE_INICIO");
    String cuerpoLabel = nuevaLabel("WHILE_CUERPO");
    String finLabel = nuevaLabel("WHILE_FIN");
```

**Que hace:** Genera tres etiquetas para el while.

```java
    emitir(inicioLabel + ":");
    String condicion = visit(ctx.expr());
    emitir("if " + condicion + " goto " + cuerpoLabel);
    emitir("goto " + finLabel);
```

**Que hace:** 
1. Emite etiqueta de inicio
2. Visita la condicion
3. Emite salto al cuerpo si la condicion es verdadera
4. Emite salto al fin si la condicion es falsa

```java
    emitir(cuerpoLabel + ":");
    visit(ctx.bloque());
    emitir("goto " + inicioLabel);
```

**Que hace:** 
1. Emite etiqueta del cuerpo
2. Visita el cuerpo del while
3. Emite salto al inicio para volver a evaluar la condicion

```java
    emitir(finLabel + ":");
    return null;
}
```

**Que hace:** Emite etiqueta de fin del while.

**Ejemplo:**
```
while (x < 10) {
    x = x + 1;
}

Genera:
WHILE_INICIO_0:
  t0 = x < 10
  if t0 goto WHILE_CUERPO_1
  goto WHILE_FIN_2
WHILE_CUERPO_1:
  t1 = x + 1
  x = t1
  goto WHILE_INICIO_0
WHILE_FIN_2:
```

### visitSentReturn() (lineas 157-166)

```java
@Override
public String visitSentReturn(MiLenguajeParser.SentReturnContext ctx) {
    if (ctx.expr() != null) {
        String valor = visit(ctx.expr());
        emitir("return " + valor);
    } else {
        emitir("return");
    }
    return null;
}
```

**Que hace:** 
- Si el return tiene expresion, la visita y emite `return valor`
- Si no tiene expresion, emite `return` solo

**Ejemplo:**
```
return resultado;
-> visita expr: "resultado" -> retorna "resultado"
-> emite: "return resultado"

return;
-> emite: "return"
```

### visitSentBloque() (lineas 168-171)

```java
@Override
public String visitSentBloque(MiLenguajeParser.SentBloqueContext ctx) {
    return visit(ctx.bloque());
}
```

**Que hace:** Delega a `visitBloque()`.

### visitSentExpr() (lineas 173-176)

```java
@Override
public String visitSentExpr(MiLenguajeParser.SentExprContext ctx) {
    return visit(ctx.expr());
}
```

**Que hace:** Visita la expresion (ej: llamada a funcion).

### visitBloque() (lineas 178-184)

```java
@Override
public String visitBloque(MiLenguajeParser.BloqueContext ctx) {
    for (MiLenguajeParser.SentenciaContext s : ctx.sentencia()) {
        visit(s);
    }
    return null;
}
```

**Que hace:** Visita cada sentencia del bloque.

### visitExprLogica() (lineas 186-194)

```java
@Override
public String visitExprLogica(MiLenguajeParser.ExprLogicaContext ctx) {
    String izq = visit(ctx.expr(0));
    String der = visit(ctx.expr(1));
    String op = ctx.opLogico().getText();
    String temp = nuevaTemp();
    emitir(temp + " = " + izq + " " + op + " " + der);
    return temp;
}
```

**Que hace:** 
1. Visita expresion izquierda y obtiene temporal
2. Visita expresion derecha y obtiene temporal
3. Obtiene el operador (`&&` o `||`)
4. Crea un nuevo temporal
5. Emite `temp = izq op der`
6. Retorna el nombre del temporal

**Ejemplo:**
```
x > 0 && y < 10
-> visita expr(0): "x > 0" -> emite "t0 = x > 0", retorna "t0"
-> visita expr(1): "y < 10" -> emite "t1 = y < 10", retorna "t1"
-> op = "&&"
-> temp = "t2"
-> emite: "t2 = t0 && t1"
-> retorna "t2"
```

### visitExprRelacional() (lineas 196-204)

```java
@Override
public String visitExprRelacional(MiLenguajeParser.ExprRelacionalContext ctx) {
    String izq = visit(ctx.expr(0));
    String der = visit(ctx.expr(1));
    String op = ctx.opRelacional().getText();
    String temp = nuevaTemp();
    emitir(temp + " = " + izq + " " + op + " " + der);
    return temp;
}
```

**Que hace:** Igual que `visitExprLogica()` pero para operadores relacionales (`==`, `!=`, `>`, `<`, `>=`, `<=`).

**Ejemplo:**
```
estado > 0
-> visita expr(0): "estado" -> retorna "estado"
-> visita expr(1): "0" -> retorna "0"
-> op = ">"
-> temp = "t9"
-> emite: "t9 = estado > 0"
-> retorna "t9"
```

### visitExprAritmetica() (lineas 206-214)

```java
@Override
public String visitExprAritmetica(MiLenguajeParser.ExprAritmeticaContext ctx) {
    String izq = visit(ctx.expr(0));
    String der = visit(ctx.expr(1));
    String op = ctx.getChild(1).getText();
    String temp = nuevaTemp();
    emitir(temp + " = " + izq + " " + op + " " + der);
    return temp;
}
```

**Que hace:** Igual que los anteriores pero para suma y resta.

**Por que `ctx.getChild(1).getText()`?** Porque el operador es el segundo hijo del nodo (el primero es `expr(0)`).

**Ejemplo:**
```
a + b
-> visita expr(0): "a" -> retorna "a"
-> visita expr(1): "b" -> retorna "b"
-> op = "+"
-> temp = "t0"
-> emite: "t0 = a + b"
-> retorna "t0"
```

### visitExprMulDiv() (lineas 216-224)

```java
@Override
public String visitExprMulDiv(MiLenguajeParser.ExprMulDivContext ctx) {
    String izq = visit(ctx.expr(0));
    String der = visit(ctx.expr(1));
    String op = ctx.getChild(1).getText();
    String temp = nuevaTemp();
    emitir(temp + " = " + izq + " " + op + " " + der);
    return temp;
}
```

**Que hace:** Igual que `visitExprAritmetica()` pero para multiplicacion, division y modulo.

### visitExprPrimaria() (lineas 226-229)

```java
@Override
public String visitExprPrimaria(MiLenguajeParser.ExprPrimariaContext ctx) {
    return visit(ctx.primaria());
}
```

**Que hace:** Delega a la primaria correspondiente.

### visitIdentificador() (lineas 231-234)

```java
@Override
public String visitIdentificador(MiLenguajeParser.IdentificadorContext ctx) {
    return ctx.ID().getText();
}
```

**Que hace:** Retorna el nombre del identificador.

**Ejemplo:**
```
contador
-> retorna "contador"
```

### visitNumero() (lineas 236-239)

```java
@Override
public String visitNumero(MiLenguajeParser.NumeroContext ctx) {
    return ctx.INTEGER().getText();
}
```

**Que hace:** Retorna el numero como string.

**Ejemplo:**
```
42
-> retorna "42"
```

### visitNumeroDecimal() (lineas 241-244)

```java
@Override
public String visitNumeroDecimal(MiLenguajeParser.NumeroDecimalContext ctx) {
    return ctx.DECIMAL().getText();
}
```

**Que hace:** Retorna el numero decimal como string.

### visitLiteralChar() (lineas 246-249)

```java
@Override
public String visitLiteralChar(MiLenguajeParser.LiteralCharContext ctx) {
    return ctx.CHARACTER().getText();
}
```

**Que hace:** Retorna el caracter literal (incluyendo comillas).

**Ejemplo:**
```
'A'
-> retorna "'A'"
```

### visitLiteralString() (lineas 251-254)

```java
@Override
public String visitLiteralString(MiLenguajeParser.LiteralStringContext ctx) {
    return ctx.STR_LIT().getText();
}
```

**Que hace:** Retorna el string literal (incluyendo comillas).

### visitLiteralTrue() y visitLiteralFalse() (lineas 256-263)

```java
@Override
public String visitLiteralTrue(MiLenguajeParser.LiteralTrueContext ctx) {
    return "true";
}

@Override
public String visitLiteralFalse(MiLenguajeParser.LiteralFalseContext ctx) {
    return "false";
}
```

**Que hace:** Retornan "true" o "false" como strings.

### visitLlamadaFuncion() (lineas 265-278)

```java
@Override
public String visitLlamadaFuncion(MiLenguajeParser.LlamadaFuncionContext ctx) {
    String nombre = ctx.ID().getText();
    List<String> args = new ArrayList<>();
    if (ctx.listaArgs() != null) {
        for (MiLenguajeParser.ExprContext e : ctx.listaArgs().expr()) {
            args.add(visit(e));
        }
    }
```

**Que hace:** 
1. Obtiene el nombre de la funcion
2. Para cada argumento, lo visita y guarda el temporal donde quedo el resultado

**Ejemplo:**
```
sumar(temp, 5)
-> nombre = "sumar"
-> visita expr(0): "temp" -> retorna "temp"
-> visita expr(1): "5" -> retorna "5"
-> args = ["temp", "5"]
```

```java
    String temp = nuevaTemp();
    emitir(temp + " = CALL func_" + nombre + ", " + String.join(", ", args));
    return temp;
}
```

**Que hace:** 
1. Crea un temporal para guardar el resultado de la llamada
2. Emite `temp = CALL func_nombre, arg1, arg2, ...`
3. Retorna el temporal

**Ejemplo:**
```
estado = sumar(temp, 5);
-> temp = "t8"
-> emite: "t8 = CALL func_sumar, temp, 5"
-> retorna "t8"
-> el padre emite: "estado = t8"
```

### visitAccesoArray() (lineas 280-287)

```java
@Override
public String visitAccesoArray(MiLenguajeParser.AccesoArrayContext ctx) {
    String id = ctx.ID().getText();
    String indice = visit(ctx.expr());
    String temp = nuevaTemp();
    emitir(temp + " = " + id + "[" + indice + "]");
    return temp;
}
```

**Que hace:** 
1. Obtiene el nombre del array
2. Visita la expresion del indice y obtiene el temporal
3. Crea un temporal para guardar el valor leido
4. Emite `temp = array[indice]`
5. Retorna el temporal

**Ejemplo:**
```
temp = numeros[0] + numeros[1];
-> visita "numeros[0]":
   -> id = "numeros"
   -> visita expr: "0" -> retorna "0"
   -> temp = "t2"
   -> emite: "t2 = numeros[0]"
   -> retorna "t2"
-> visita "numeros[1]":
   -> id = "numeros"
   -> visita expr: "1" -> retorna "1"
   -> temp = "t3"
   -> emite: "t3 = numeros[1]"
   -> retorna "t3"
-> visita "+":
   -> emite: "t4 = t2 + t3"
   -> retorna "t4"
-> emite: "temp = t4"
```

### visitParen() (lineas 289-292)

```java
@Override
public String visitParen(MiLenguajeParser.ParenContext ctx) {
    return visit(ctx.expr());
}
```

**Que hace:** Visita la expresion entre parentesis y retorna su resultado.

## Flujo completo de generacion de codigo intermedio

```
1. App.java crea CodigoVisitor

2. Llama a codigo.visit(tree)
   -> Empieza desde visitPrograma()

3. visitPrograma():
   - emite: "// Código de tres direcciones generado"
   - emite: "PROGRAMA_INICIO:"
   - emite: "// Declaración de variables globales"
   - visita cada declaracion global:
   
   a) visitDeclFuncion("sumar"):
      - emite: "func_sumar:"
      - emite: "PARAM a int"
      - emite: "PARAM b int"
      - visita cuerpo:
        - visitDeclVariable("resultado")
          -> emite: "DECLARE resultado int"
        - visitSentAsignacion("resultado = a + b")
          -> visita expr(1): "a + b"
             -> visitExprAritmetica:
                -> visita expr(0): "a" -> retorna "a"
                -> visita expr(1): "b" -> retorna "b"
                -> emite: "t0 = a + b"
                -> retorna "t0"
          -> visita expr(0): "resultado" -> retorna "resultado"
          -> emite: "resultado = t0"
        - visitSentReturn("resultado")
          -> visita expr: "resultado" -> retorna "resultado"
          -> emite: "return resultado"
   
   b) visitDeclFuncion("main"):
      - emite: "func_main:"
      - visita cuerpo:
        - visitDeclVariable("estado")
          -> emite: "DECLARE estado int"
        - visitDeclVariable("temp")
          -> emite: "DECLARE temp int"
        - visitDeclArray("numeros[3]")
          -> emite: "DECLARE numeros[3] int"
        - visitSentAsignacion("contadorGlobal = 0")
          -> visita expr(1): "0" -> retorna "0"
          -> visita expr(0): "contadorGlobal" -> retorna "contadorGlobal"
          -> emite: "contadorGlobal = 0"
        - ... (mas sentencias)
        - visitSentAsignacion("estado = sumar(temp, 5)")
          -> visita expr(1): "sumar(temp, 5)"
             -> visitLlamadaFuncion:
                -> visita arg(0): "temp" -> retorna "temp"
                -> visita arg(1): "5" -> retorna "5"
                -> temp = "t8"
                -> emite: "t8 = CALL func_sumar, temp, 5"
                -> retorna "t8"
          -> visita expr(0): "estado" -> retorna "estado"
          -> emite: "estado = t8"
        - visitSentIf("estado > 0"):
          -> visita expr: "estado > 0"
             -> visitExprRelacional:
                -> visita expr(0): "estado" -> retorna "estado"
                -> visita expr(1): "0" -> retorna "0"
                -> emite: "t9 = estado > 0"
                -> retorna "t9"
          -> thenLabel = "THEN_0"
          -> elseLabel = "ELSE_1"
          -> endLabel = "END_IF_2"
          -> emite: "if t9 goto THEN_0"
          -> emite: "goto ELSE_1"
          -> emite: "THEN_0:"
          -> visita bloque then:
             - visitDeclVariable("auxiliar")
               -> emite: "DECLARE auxiliar int"
             - visitSentAsignacion("auxiliar = estado + 10")
               -> visita expr(1): "estado + 10"
                  -> emite: "t10 = estado + 10"
                  -> retorna "t10"
               -> emite: "auxiliar = t10"
             - visitSentAsignacion("estado = auxiliar")
               -> emite: "estado = auxiliar"
          -> emite: "goto END_IF_2"
          -> emite: "ELSE_1:"
          -> emite: "END_IF_2:"
        - visitSentReturn("estado")
          -> emite: "return estado"
   
   - emite: "PROGRAMA_FIN:"

4. App.java llama a codigo.imprimir()
   -> Muestra todas las instrucciones numeradas

5. App.java llama a codigo.guardarEnArchivo()
   -> Guarda las instrucciones en ejemplo_correcto_codigo_intermedio.txt
```

## Preguntas frecuentes

**P: Por que usamos temporales (t0, t1, t2...)?**
R: Porque el codigo de tres direcciones solo permite 3 operandos por instruccion. Si tenemos `a + b + c`, necesitamos un temporal para guardar el resultado intermedio: `t0 = a + b`, `t1 = t0 + c`.

**P: Por que las etiquetas tienen numeros (THEN_0, ELSE_1)?**
R: Para que sean unicas. Si hay multiples if/else, cada uno necesita etiquetas diferentes.

**P: Que significa `CALL func_sumar, temp, 5`?**
R: Es una llamada a funcion. `CALL` es la instruccion, `func_sumar` es el nombre de la funcion (con prefijo "func_"), y `temp, 5` son los argumentos.

**P: Por que no generamos codigo de maquina directamente?**
R: Porque el codigo intermedio nos permite hacer optimizaciones antes de generar codigo final. Ademas, es mas facil de entender y depurar.

**P: Que pasa si una funcion no tiene return?**
R: No emitimos instruccion `return`. El codigo intermedio simplemente termina la funcion.

**P: Por que `visitSentAsignacion()` verifica si es acceso a array?**
R: Porque la gramatica permite `numeros[0] = 10` como una asignacion normal (el lado izquierdo es una expresion primaria que es un acceso a array). Necesitamos detectarlo para generar `numeros[0] = 10` en vez de `t0 = numeros[0]` y luego `t0 = 10`.
