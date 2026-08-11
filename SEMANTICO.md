# Analisis Semantico - Explicacion del Codigo

## Que hace esta fase?

Verifica que el programa tenga sentido logico:
- Variables declaradas antes de usarse
- No hay variables duplicadas en el mismo ambito
- Funciones existen y se llaman con la cantidad correcta de argumentos
- No se asigna valor a una funcion
- Detecta variables no usadas (warnings)

## Codigo en App.java (lineas 91-117)

```java
// === 4. ANALISIS SEMANTICO ===
System.out.println("\n=== 4. ANÁLISIS SEMÁNTICO ===");
TablaSimbolos tabla = new TablaSimbolos();
```

**Que hace:** Crea la tabla de simbolos. Es una estructura de datos que guarda informacion de todas las variables, funciones y parametros del programa.

```java
SemanticoVisitor semantico = new SemanticoVisitor(tabla);
semantico.visit(tree);
```

**Que hace:** 
- Crea un visitor semantico y le pasa la tabla de simbolos
- `visit(tree)` recorre el arbol sintactico y va llenando la tabla
- Cada vez que encuentra una declaracion, agrega un simbolo
- Cada vez que encuentra un uso de variable, verifica que exista

```java
System.out.println("   📋 Tabla de símbolos construida:");
tabla.imprimir();
```

**Que hace:** Muestra la tabla de simbolos en pantalla con formato de tabla.

```java
if (!tabla.getErrores().isEmpty()) {
    System.out.println("\n❌ ERRORES SEMÁNTICOS:");
    for (String err : tabla.getErrores()) {
        System.out.println("   ❌ " + err);
    }
    
    if (!tabla.getWarnings().isEmpty()) {
        System.out.println("\n⚠️ WARNINGS SEMÁNTICOS:");
        for (String w : tabla.getWarnings()) {
            System.out.println("   ⚠️ " + w);
        }
        System.out.println("   ⚠️ El código tiene warnings, pero se puede continuar.");
    }
    
    System.out.println("\n❌ Compilación detenida debido a errores semánticos.");
    return;
}
```

**Que hace:** Si hay errores semanticos, los muestra junto con los warnings (si los hay) y aborta la compilacion. Los warnings no detienen la compilacion, pero se muestran igual.

```java
if (!tabla.getWarnings().isEmpty()) {
    System.out.println("\n⚠️ WARNINGS SEMÁNTICOS:");
    for (String w : tabla.getWarnings()) {
        System.out.println("   ⚠️ " + w);
    }
    System.out.println("   ⚠️ El código tiene warnings, pero se puede continuar.");
}

System.out.println("\n✅ Análisis semántico completado sin errores.");
```

**Que hace:** Si no hay errores pero si warnings, los muestra y continua. Si no hay ni errores ni warnings, simplemente confirma que todo esta bien.

## TablaSimbolos.java - Explicacion linea por linea

### Clase interna Simbolo (lineas 7-31)

```java
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
```

**Que hace:** Define la estructura de un simbolo. Cada variable, funcion o parametro tiene:
- `nombre`: "contador", "sumar", "x"
- `tipo`: "int", "double", "void"
- `categoria`: "variable", "funcion", "parametro"
- `linea`: numero de linea donde se declaro
- `columna`: columna donde se declaro
- `ambito`: "global", "sumar", "main" (donde vive)
- `usada`: true si se uso en alguna expresion
- `cantParams`: cuantos parametros tiene (solo funciones)
- `tiposParams`: lista de tipos de parametros (solo funciones)
- `tamanoArray`: tamano del array (solo arrays, ej: "3")

```java
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
```

**Que hace:** Constructor que inicializa un simbolo con valores por defecto.

### Campos de la tabla (lineas 33-37)

```java
private List<Map<String, Simbolo>> scopes = new ArrayList<>();
private List<Simbolo> todosSimbolos = new ArrayList<>();
private List<String> errores = new ArrayList<>();
private List<String> warnings = new ArrayList<>();
private String ambitoActual = "global";
```

**Que hace:**
- `scopes`: pila de ambitos. Cada ambito es un mapa nombre -> simbolo. Permite tener variables locales con el mismo nombre que globales.
- `todosSimbolos`: lista plana de todos los simbolos (para imprimir en orden)
- `errores`: lista de errores semanticos encontrados
- `warnings`: lista de advertencias (variables no usadas)
- `ambitoActual`: nombre del ambito donde estamos ("global", "sumar", "main")

### Constructor (lineas 39-41)

```java
public TablaSimbolos() {
    scopes.add(new LinkedHashMap<>());
}
```

**Que hace:** Crea el ambito global (primer scope). `LinkedHashMap` mantiene el orden de insercion, asi los simbolos se imprimen en el orden en que fueron declarados.

### pushScope() (lineas 43-46)

```java
public void pushScope(String ambito) {
    scopes.add(new LinkedHashMap<>());
    ambitoActual = ambito;
}
```

**Que hace:** Crea un nuevo ambito (scope) y lo agrega a la pila. Se usa cuando entramos a una funcion o bloque.

**Ejemplo:**
```
int x;                    <- scope global
int sumar(int a, int b) { <- pushScope("sumar")
    int resultado;        <- scope sumar
    ...
}                         <- popScope()
```

### popScope() (lineas 48-58)

```java
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
```

**Que hace:**
1. Verifica que haya mas de un scope (no podemos eliminar el global)
2. Elimina el scope actual de la pila
3. Para cada simbolo del scope eliminado:
   - Si no fue usado Y no es una funcion, genera un warning
4. Actualiza `ambitoActual` al scope padre

**Por que no generamos warning para funciones?** Porque las funciones no se "usan" en el mismo sentido que las variables. Una funcion puede ser declarada pero nunca llamada, y eso no es un error.

### setAmbitoActual() (lineas 60-62)

```java
public void setAmbitoActual(String ambito) {
    this.ambitoActual = ambito;
}
```

**Que hace:** Cambia el nombre del ambito actual. Lo usa el visitor cuando entra a una funcion.

### agregar() con tamanoArray (lineas 64-77)

```java
public void agregar(String nombre, String tipo, String categoria, int linea, int columna, String tamanoArray) {
    Map<String, Simbolo> currentScope = scopes.get(scopes.size() - 1);
```

**Que hace:** Obtiene el scope actual (el ultimo de la pila).

```java
    if (currentScope.containsKey(nombre)) {
        Simbolo existente = currentScope.get(nombre);
        errores.add("Variable '" + nombre + "' ya declarada en el ámbito '" + ambitoActual + "' (línea " + existente.linea + ", columna " + existente.columna + ")");
```

**Que hace:** Si ya existe un simbolo con ese nombre en el scope actual, es un error de variable duplicada.

**Ejemplo:**
```
int x;  <- agrega x al scope global
int x;  <- ERROR: x ya existe en el scope global
```

```java
    } else {
        Simbolo nuevo = new Simbolo(nombre, tipo, categoria, linea, columna, ambitoActual);
        if (tamanoArray != null) {
            nuevo.tamanoArray = tamanoArray;
        }
        currentScope.put(nombre, nuevo);
        todosSimbolos.add(nuevo);
    }
}
```

**Que hace:** Si no existe, crea un nuevo simbolo y lo agrega al scope actual y a la lista plana.

### agregar() sin tamanoArray (lineas 79-81)

```java
public void agregar(String nombre, String tipo, String categoria, int linea, int columna) {
    agregar(nombre, tipo, categoria, linea, columna, null);
}
```

**Que hace:** Sobrecarga del metodo anterior para variables que no son arrays.

### buscar() (lineas 83-89)

```java
public Simbolo buscar(String nombre) {
    for (int i = scopes.size() - 1; i >= 0; i--) {
        Simbolo s = scopes.get(i).get(nombre);
        if (s != null) return s;
    }
    return null;
}
```

**Que hace:** Busca un simbolo en todos los scopes, desde el mas interno hacia el mas externo.

**Ejemplo:**
```
int x;                    <- scope global: {x}
int sumar(int a) {        <- scope sumar: {a}
    int x;                <- scope sumar: {a, x}
    return x + a;         <- busca x: lo encuentra en scope sumar
}
```

**Por que busca de adentro hacia afuera?** Porque las variables locales tienen prioridad sobre las globales (shadowing).

### existe() (lineas 91-93)

```java
public boolean existe(String nombre) {
    return buscar(nombre) != null;
}
```

**Que hace:** Verifica si un simbolo existe en algun scope.

### marcarUsada() (lineas 95-98)

```java
public void marcarUsada(String nombre) {
    Simbolo s = buscar(nombre);
    if (s != null) s.usada = true;
}
```

**Que hace:** Marca un simbolo como usado. Se llama cada vez que encontramos una variable en una expresion.

### agregarError() (lineas 100-102)

```java
public void agregarError(String error) {
    errores.add(error);
}
```

**Que hace:** Agrega un error semantico a la lista.

### getErrores() y getWarnings() (lineas 104-110)

```java
public List<String> getErrores() {
    return errores;
}

public List<String> getWarnings() {
    return warnings;
}
```

**Que hace:** Devuelven las listas de errores y warnings.

### imprimir() (lineas 112-132)

```java
public void imprimir() {
    System.out.println("\n=== TABLA DE SÍMBOLOS ===");
    System.out.printf("%-16s %-11s %-16s %-11s %-12s %-16s %s\n",
            "NOMBRE", "TIPO", "CATEGORÍA", "LÍNEA", "COLUMNA", "ÁMBITO", "DETALLES");
    System.out.println("--------------------------------------------------------------------------------------------");
```

**Que hace:** Imprime el encabezado de la tabla con formato. `%-16s` significa "string alineado a la izquierda con ancho minimo de 16 caracteres".

```java
    for (Simbolo s : todosSimbolos) {
        String detalles = "";
        if ("parametro".equals(s.categoria)) {
            detalles = "";
```

**Que hace:** Para parametros, la columna DETALLES esta vacia.

```java
        } else if ("funcion".equals(s.categoria)) {
            String params = String.join(", ", s.tiposParams);
            detalles = "[private] [" + params + "]";
```

**Que hace:** Para funciones, muestra `[private]` y los tipos de parametros entre corchetes.

**Ejemplo:**
```
sumar  int  funcion  6  4  global  [private] [int, int]
```

```java
        } else if (s.tamanoArray != null) {
            detalles = "[arr:" + s.tamanoArray + "] [private]";
```

**Que hace:** Para arrays, muestra `[arr:tamano] [private]`.

**Ejemplo:**
```
numeros  int  variable  16  8  main  [arr:3] [private]
```

```java
        } else {
            detalles = "[private]";
        }
        System.out.printf("%-16s %-11s %-16s %-11d %-12d %-16s %s\n",
                s.nombre, s.tipo, s.categoria, s.linea, s.columna, s.ambito, detalles);
    }
}
```

**Que hace:** Para variables normales, muestra solo `[private]`.

### getScopes() y getTodosSimbolos() (lineas 134-140)

```java
public List<Map<String, Simbolo>> getScopes() {
    return scopes;
}

public List<Simbolo> getTodosSimbolos() {
    return todosSimbolos;
}
```

**Que hace:** Devuelven la pila de scopes y la lista plana de simbolos.

## SemanticoVisitor.java - Explicacion linea por linea

### Campos y constructor (lineas 5-11)

```java
public class SemanticoVisitor extends MiLenguajeBaseVisitor<Void> {

    private TablaSimbolos tabla;
    private String ambitoActual = "global";

    public SemanticoVisitor(TablaSimbolos tabla) {
        this.tabla = tabla;
    }
```

**Que hace:**
- Extiende `MiLenguajeBaseVisitor<Void>` (clase generada por ANTLR)
- `Void` significa que los metodos visit no retornan valor
- Guarda referencia a la tabla de simbolos
- `ambitoActual` trackea en que funcion/bloque estamos

### visitPrograma() (lineas 13-19)

```java
@Override
public Void visitPrograma(MiLenguajeParser.ProgramaContext ctx) {
    for (MiLenguajeParser.DeclaracionGlobalContext d : ctx.declaracionGlobal()) {
        visit(d);
    }
    return null;
}
```

**Que hace:** Visita cada declaracion global del programa. `ctx.declaracionGlobal()` devuelve la lista de declaraciones.

### visitDeclFuncion() (lineas 21-58)

```java
@Override
public Void visitDeclFuncion(MiLenguajeParser.DeclFuncionContext ctx) {
    String nombre = ctx.ID().getText();
    String tipoRet = ctx.tipo().getText();
    int linea = ctx.ID().getSymbol().getLine();
    int col = ctx.ID().getSymbol().getCharPositionInLine();
```

**Que hace:** Extrae informacion de la funcion:
- `ctx.ID().getText()`: nombre de la funcion
- `ctx.tipo().getText()`: tipo de retorno
- `ctx.ID().getSymbol().getLine()`: linea donde se declaro
- `ctx.ID().getSymbol().getCharPositionInLine()`: columna donde se declaro

```java
    tabla.agregar(nombre, tipoRet, "funcion", linea, col);
```

**Que hace:** Agrega la funcion a la tabla de simbolos en el scope global.

```java
    TablaSimbolos.Simbolo func = tabla.buscar(nombre);
    if (func != null && ctx.listaParams() != null) {
        func.cantParams = ctx.listaParams().param().size();
    }
```

**Que hace:** Busca el simbolo recien agregado y le setea la cantidad de parametros.

```java
    ambitoActual = nombre;
    tabla.pushScope(nombre);
    tabla.setAmbitoActual(nombre);
```

**Que hace:** 
- Cambia el ambito actual al nombre de la funcion
- Crea un nuevo scope para la funcion
- Actualiza el ambito en la tabla

```java
    if (ctx.listaParams() != null) {
        for (MiLenguajeParser.ParamContext p : ctx.listaParams().param()) {
            String pNombre = p.ID().getText();
            String pTipo = p.tipo().getText();
            int pLinea = p.ID().getSymbol().getLine();
            int pCol = p.ID().getSymbol().getCharPositionInLine();
            tabla.agregar(pNombre, pTipo, "parametro", pLinea, pCol);
            if (func != null) {
                func.tiposParams.add(pTipo);
            }
        }
    }
```

**Que hace:** Para cada parametro:
1. Extrae nombre, tipo, linea, columna
2. Lo agrega al scope de la funcion como "parametro"
3. Agrega el tipo a la lista de tipos de parametros de la funcion

**Ejemplo:**
```
int sumar(int a, int b)
          ^^^^  ^^^^
           |      |
           |      └─ parametro b, tipo int
           └─ parametro a, tipo int

func.tiposParams = ["int", "int"]
```

```java
    for (MiLenguajeParser.SentenciaContext s : ctx.bloque().sentencia()) {
        visit(s);
    }
```

**Que hace:** Visita cada sentencia del cuerpo de la funcion.

```java
    tabla.popScope();
    ambitoActual = "global";
    return null;
}
```

**Que hace:** Al salir de la funcion:
1. Elimina el scope de la funcion (y genera warnings de variables no usadas)
2. Vuelve al ambito global

### visitDeclVariable() (lineas 60-67)

```java
@Override
public Void visitDeclVariable(MiLenguajeParser.DeclVariableContext ctx) {
    String tipo = ctx.tipo().getText();
    String nombre = ctx.ID().getText();
    int linea = ctx.ID().getSymbol().getLine();
    int col = ctx.ID().getSymbol().getCharPositionInLine();
    tabla.agregar(nombre, tipo, "variable", linea, col);
    return null;
}
```

**Que hace:** Agrega una variable simple a la tabla de simbolos.

**Ejemplo:**
```
int contador;
    ^^^^^^^^
        |
    nombre="contador", tipo="int", categoria="variable"
```

### visitDeclArray() (lineas 69-77)

```java
@Override
public Void visitDeclArray(MiLenguajeParser.DeclArrayContext ctx) {
    String tipo = ctx.tipo().getText();
    String nombre = ctx.ID().getText();
    int linea = ctx.ID().getSymbol().getLine();
    int col = ctx.ID().getSymbol().getCharPositionInLine();
    String tamano = ctx.INTEGER().getText();
    tabla.agregar(nombre, tipo, "variable", linea, col, tamano);
    return null;
}
```

**Que hace:** Agrega un array a la tabla de simbolos. La diferencia con `visitDeclVariable()` es que extrae el tamano del array y lo pasa como parametro adicional.

**Ejemplo:**
```
int numeros[3];
    ^^^^^^^  ^
       |     |
       |     └─ tamano="3"
       └─ nombre="numeros", tipo="int"
```

### visitSentDecl() (lineas 79-82)

```java
@Override
public Void visitSentDecl(MiLenguajeParser.SentDeclContext ctx) {
    return visit(ctx.variableDecl());
}
```

**Que hace:** Delega a `visitDeclVariable()` o `visitDeclArray()` segun corresponda.

### visitSentAsignacion() (lineas 84-101)

```java
@Override
public Void visitSentAsignacion(MiLenguajeParser.SentAsignacionContext ctx) {
    MiLenguajeParser.ExprContext lhs = ctx.expr(0);
```

**Que hace:** Obtiene el lado izquierdo de la asignacion (primera expresion).

```java
    if (lhs instanceof MiLenguajeParser.ExprPrimariaContext) {
        MiLenguajeParser.PrimariaContext primaria = ((MiLenguajeParser.ExprPrimariaContext) lhs).primaria();
        if (primaria instanceof MiLenguajeParser.IdentificadorContext) {
            String id = ((MiLenguajeParser.IdentificadorContext) primaria).ID().getText();
            int linea = ((MiLenguajeParser.IdentificadorContext) primaria).ID().getSymbol().getLine();
            TablaSimbolos.Simbolo simbolo = tabla.buscar(id);
            if (simbolo != null && "funcion".equals(simbolo.categoria)) {
                tabla.agregarError("No se puede asignar valor a '" + id + "' porque no es una variable (línea " + linea + ")");
            }
        }
    }
```

**Que hace:** Verifica que no estemos asignando un valor a una funcion.

**Ejemplo de error:**
```
int sumar(int a, int b) { ... }
sumar = 10;  <- ERROR: sumar es una funcion, no una variable
```

**Como funciona:**
1. Verifica que el lado izquierdo sea una expresion primaria
2. Verifica que sea un identificador (no una llamada a funcion o acceso a array)
3. Busca el simbolo en la tabla
4. Si es una funcion, genera error

```java
    visit(ctx.expr(0));
    visit(ctx.expr(1));
    return null;
}
```

**Que hace:** Visita ambas expresiones (lado izquierdo y derecho) para detectar variables no declaradas.

### visitSentAsignacionArray() (lineas 103-115)

```java
@Override
public Void visitSentAsignacionArray(MiLenguajeParser.SentAsignacionArrayContext ctx) {
    String id = ctx.ID().getText();
    if (!tabla.existe(id)) {
        tabla.agregarError("Variable '" + id + "' no declarada (línea " + ctx.ID().getSymbol().getLine() + ")");
    } else {
        tabla.marcarUsada(id);
    }
    visit(ctx.expr(0));
    visit(ctx.expr(1));
    return null;
}
```

**Que hace:** Verifica que el array exista antes de asignarle un valor.

**Ejemplo:**
```
numeros[0] = 10;  <- verifica que "numeros" exista
```

### visitSentIf() (lineas 117-125)

```java
@Override
public Void visitSentIf(MiLenguajeParser.SentIfContext ctx) {
    visit(ctx.expr());
    visit(ctx.bloque(0));
    if (ctx.bloque().size() > 1) {
        visit(ctx.bloque(1));
    }
    return null;
}
```

**Que hace:** Visita la condicion, el bloque then y (si existe) el bloque else.

### visitSentWhile() (lineas 127-132)

```java
@Override
public Void visitSentWhile(MiLenguajeParser.SentWhileContext ctx) {
    visit(ctx.expr());
    visit(ctx.bloque());
    return null;
}
```

**Que hace:** Visita la condicion y el cuerpo del while.

### visitSentReturn() (lineas 134-140)

```java
@Override
public Void visitSentReturn(MiLenguajeParser.SentReturnContext ctx) {
    if (ctx.expr() != null) {
        visit(ctx.expr());
    }
    return null;
}
```

**Que hace:** Si el return tiene expresion, la visita.

**Ejemplo:**
```
return resultado;  <- visita la expresion "resultado"
return;            <- no hay expresion, no visita nada
```

### visitSentBloque() (lineas 142-145)

```java
@Override
public Void visitSentBloque(MiLenguajeParser.SentBloqueContext ctx) {
    return visit(ctx.bloque());
}
```

**Que hace:** Delega a `visitBloque()`.

### visitSentExpr() (lineas 147-150)

```java
@Override
public Void visitSentExpr(MiLenguajeParser.SentExprContext ctx) {
    return visit(ctx.expr());
}
```

**Que hace:** Visita la expresion (ej: llamada a funcion).

### visitBloque() (lineas 152-160)

```java
@Override
public Void visitBloque(MiLenguajeParser.BloqueContext ctx) {
    tabla.pushScope(ambitoActual);
    tabla.setAmbitoActual(ambitoActual);
    for (MiLenguajeParser.SentenciaContext s : ctx.sentencia()) {
        visit(s);
    }
    tabla.popScope();
    return null;
}
```

**Que hace:** 
1. Crea un nuevo scope para el bloque
2. Visita cada sentencia del bloque
3. Elimina el scope al salir

**Por que usamos `ambitoActual` en vez de "bloque_X"?** Para que las variables declaradas dentro de un if tengan el ambito de la funcion padre, no "bloque_37".

**Ejemplo:**
```
int main() {              <- ambitoActual = "main"
    if (x > 0) {          <- pushScope("main")
        int auxiliar;     <- auxiliar tiene ambito "main"
    }                     <- popScope()
}
```

### visitExprLogica() (lineas 162-167)

```java
@Override
public Void visitExprLogica(MiLenguajeParser.ExprLogicaContext ctx) {
    visit(ctx.expr(0));
    visit(ctx.expr(1));
    return null;
}
```

**Que hace:** Visita ambas expresiones de la operacion logica.

**Ejemplo:**
```
x > 0 && y < 10
^^^^^    ^^^^^^
  |        |
expr(0)  expr(1)
```

### visitExprRelacional() (lineas 169-174)

```java
@Override
public Void visitExprRelacional(MiLenguajeParser.ExprRelacionalContext ctx) {
    visit(ctx.expr(0));
    visit(ctx.expr(1));
    return null;
}
```

**Que hace:** Visita ambas expresiones de la operacion relacional.

### visitExprAritmetica() (lineas 176-181)

```java
@Override
public Void visitExprAritmetica(MiLenguajeParser.ExprAritmeticaContext ctx) {
    visit(ctx.expr(0));
    visit(ctx.expr(1));
    return null;
}
```

**Que hace:** Visita ambas expresiones de la operacion aritmetica (+, -).

### visitExprMulDiv() (lineas 183-188)

```java
@Override
public Void visitExprMulDiv(MiLenguajeParser.ExprMulDivContext ctx) {
    visit(ctx.expr(0));
    visit(ctx.expr(1));
    return null;
}
```

**Que hace:** Visita ambas expresiones de la operacion (*, /, %).

### visitExprPrimaria() (lineas 190-193)

```java
@Override
public Void visitExprPrimaria(MiLenguajeParser.ExprPrimariaContext ctx) {
    return visit(ctx.primaria());
}
```

**Que hace:** Delega a la primaria correspondiente.

### visitIdentificador() (lineas 195-206)

```java
@Override
public Void visitIdentificador(MiLenguajeParser.IdentificadorContext ctx) {
    String id = ctx.ID().getText();
    int linea = ctx.ID().getSymbol().getLine();
    if (!tabla.existe(id)) {
        tabla.agregarError("Variable '" + id + "' no declarada (línea " + linea + ")");
    } else {
        tabla.marcarUsada(id);
    }
    return null;
}
```

**Que hace:** Cuando encuentra un identificador en una expresion:
1. Verifica que exista en la tabla
2. Si no existe, genera error
3. Si existe, lo marca como usado

**Ejemplo:**
```
resultado = a + b;
            ^   ^
            |   └─ visita b: existe? si -> marcarUsada("b")
            └─ visita a: existe? si -> marcarUsada("a")
```

### visitLlamadaFuncion() (lineas 208-228)

```java
@Override
public Void visitLlamadaFuncion(MiLenguajeParser.LlamadaFuncionContext ctx) {
    String id = ctx.ID().getText();
    int linea = ctx.ID().getSymbol().getLine();
    TablaSimbolos.Simbolo func = tabla.buscar(id);
```

**Que hace:** Busca la funcion en la tabla de simbolos.

```java
    if (func == null) {
        tabla.agregarError("Función '" + id + "' no declarada (línea " + linea + ")");
```

**Que hace:** Si la funcion no existe, genera error.

**Ejemplo:**
```
resultado = inexistente(1, 2);  <- ERROR: funcion no declarada
```

```java
    } else {
        tabla.marcarUsada(id);
        int argsDado = ctx.listaArgs() != null ? ctx.listaArgs().expr().size() : 0;
        if (func.cantParams != argsDado) {
            tabla.agregarError("Función '" + id + "' espera " + func.cantParams + " argumentos, recibió " + argsDado + " (línea " + linea + ")");
        }
    }
```

**Que hace:** Si la funcion existe:
1. La marca como usada
2. Cuenta cuantos argumentos se le pasaron
3. Verifica que coincida con la cantidad de parametros

**Ejemplo:**
```
int sumar(int a, int b) { ... }  <- cantParams = 2
resultado = sumar(5);            <- ERROR: espera 2, recibio 1
```

```java
    if (ctx.listaArgs() != null) {
        for (MiLenguajeParser.ExprContext e : ctx.listaArgs().expr()) {
            visit(e);
        }
    }
    return null;
}
```

**Que hace:** Visita cada argumento (puede contener variables).

### visitAccesoArray() (lineas 230-241)

```java
@Override
public Void visitAccesoArray(MiLenguajeParser.AccesoArrayContext ctx) {
    String id = ctx.ID().getText();
    int linea = ctx.ID().getSymbol().getLine();
    if (!tabla.existe(id)) {
        tabla.agregarError("Variable '" + id + "' no declarada (línea " + linea + ")");
    } else {
        tabla.marcarUsada(id);
    }
    visit(ctx.expr());
    return null;
}
```

**Que hace:** Verifica que el array exista y visita la expresion del indice.

**Ejemplo:**
```
temp = numeros[0];
       ^^^^^^^  ^
          |     |
          |     └─ visita expr(0)
          └─ verifica que "numeros" exista
```

### Metodos para literales (lineas 243-258)

```java
@Override
public Void visitNumero(MiLenguajeParser.NumeroContext ctx) { return null; }

@Override
public Void visitNumeroDecimal(MiLenguajeParser.NumeroDecimalContext ctx) { return null; }

@Override
public Void visitLiteralChar(MiLenguajeParser.LiteralCharContext ctx) { return null; }

@Override
public Void visitLiteralString(MiLenguajeParser.LiteralStringContext ctx) { return null; }

@Override
public Void visitLiteralTrue(MiLenguajeParser.LiteralTrueContext ctx) { return null; }

@Override
public Void visitLiteralFalse(MiLenguajeParser.LiteralFalseContext ctx) { return null; }
```

**Que hace:** Los literales (numeros, strings, etc.) no requieren analisis semantico, asi que retornan null.

### visitParen() (lineas 260-263)

```java
@Override
public Void visitParen(MiLenguajeParser.ParenContext ctx) {
    return visit(ctx.expr());
}
```

**Que hace:** Visita la expresion entre parentesis.

## Flujo completo del analisis semantico

```
1. App.java crea TablaSimbolos y SemanticoVisitor

2. Llama a semantico.visit(tree)
   -> Empieza desde visitPrograma()

3. visitPrograma() visita cada declaracion global:

   a) visitDeclFuncion("sumar"):
      - Agrega "sumar" al scope global como funcion
      - pushScope("sumar")
      - Agrega parametros "a" y "b" al scope sumar
      - Visita cuerpo de la funcion:
        - visitDeclVariable("resultado") -> agrega al scope sumar
        - visitSentAsignacion("resultado = a + b")
          - visitIdentificador("resultado") -> marcarUsada
          - visitIdentificador("a") -> marcarUsada
          - visitIdentificador("b") -> marcarUsada
        - visitSentReturn("resultado")
          - visitIdentificador("resultado") -> marcarUsada
      - popScope() -> genera warnings si hay variables no usadas
   
   b) visitDeclFuncion("main"):
      - Agrega "main" al scope global como funcion
      - pushScope("main")
      - Visita cuerpo:
        - visitDeclVariable("estado") -> agrega al scope main
        - visitDeclVariable("temp") -> agrega al scope main
        - visitDeclArray("numeros[3]") -> agrega al scope main con tamanoArray="3"
        - visitSentAsignacion("contadorGlobal = 0")
          - visitIdentificador("contadorGlobal") -> buscar en scopes
            -> no esta en main, busca en global -> encontrado -> marcarUsada
        - ... (mas sentencias)
        - visitSentIf("estado > 0"):
          - visitExprRelacional:
            - visitIdentificador("estado") -> marcarUsada
            - visitNumero("0") -> no hace nada
          - visitBloque():
            - pushScope("main")
            - visitDeclVariable("auxiliar") -> agrega al scope main
            - visitSentAsignacion("auxiliar = estado + 10")
              - visitIdentificador("auxiliar") -> marcarUsada
              - visitIdentificador("estado") -> marcarUsada
              - visitNumero("10") -> no hace nada
            - popScope()
      - popScope()

4. App.java verifica si hay errores
   -> Si hay, muestra y aborta
   -> Si no hay, muestra warnings (si los hay) y continua
```

## Preguntas frecuentes

**P: Por que usamos una pila de scopes?**
R: Para soportar variables locales con el mismo nombre que globales (shadowing). Cada funcion/bloque tiene su propio scope.

**P: Por que los parametros no tienen `[private]` en la tabla?**
R: Porque el formato del Ejemplo_Final lo muestra asi. Los parametros no tienen visibilidad "private", son accesibles dentro de la funcion.

**P: Que pasa si declaro una variable dentro de un if?**
R: Se agrega al scope del bloque, pero con el ambito de la funcion padre (gracias a que usamos `ambitoActual` en `visitBloque()`).

**P: Por que no verificamos tipos?**
R: El Ejemplo_Final tampoco lo hace. Solo verifica que las variables existan y no esten duplicadas. La verificacion de tipos seria un analisis semantico mas completo.
