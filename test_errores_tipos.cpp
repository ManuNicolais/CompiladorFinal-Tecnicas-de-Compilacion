// Test de errores de tipo

// Error: retorno incorrecto
string funcionRetornaString() {
    return 42;
}

int funcionRetornaInt() {
    return "hola";
}

double funcionRetornaDouble() {
    return true;
}

void funcionVoidRetorno() {
    return 10;
}

void funcionVoidOk() {
    return;
}

int funcionOk() {
    return 10;
}

// Error: break/continue fuera de bucle
int testBreakFuera() {
    break;
    continue;
    return 0;
}

int testBucles() {
    int i;
    i = 0;
    while (i < 10) {
        if (i > 5) {
            break;
        }
        i = i + 1;
    }

    for (i = 0; i < 10; i = i + 1) {
        continue;
    }
    return 0;
}

// Error: condicion if no booleana
int testIfCond(int x) {
    if (x) {
        x = 1;
    }
    if (x + 1) {
        x = 2;
    }
    if ("texto") {
        x = 3;
    }
    return x;
}

// Error: condicion while no booleana
int testWhileCond(int x) {
    while (x) {
        x = x - 1;
    }
    while (x + 1) {
        x = x - 1;
    }
    return x;
}

// Error: condicion for no booleana
int testForCond(int x) {
    int i;
    for (i = 0; x; i = i + 1) {
        i = 0;
    }
    return x;
}

// Error: asignacion tipos incompatibles
int testAsignacion() {
    int a;
    bool b;
    string s;
    char c;

    a = "texto";
    b = 10;
    a = b;
    s = 3.14;
    b = c;
    a = true;
    return a;
}

// Error: argumentos de funcion incorrectos
int testArgs(int a, double b) {
    return a;
}

int testLlamadaArgs() {
    int x;
    x = testArgs("texto", 3.14);
    x = testArgs(10, false);
    x = testArgs(10, "otro");
    return x;
}

// Error: operacion entre tipos incompatibles
int testOperaciones() {
    int a;
    a = 10 + true;
    a = 20 * false;
    a = 30 - "hola";
    a = 40 / "mundo";
    return a;
}

// Error: comparacion entre tipos incompatibles
int testComparaciones() {
    int a;
    bool r;
    r = a > "texto";
    r = a == "hola";
    return a;
}

// Error: operadores logicos con no-bool
int testLogicos() {
    int a;
    int b;
    bool r;
    r = a && b;
    r = a || "texto";
    return a;
}

int main() {
    int x;
    x = 0;
    return x;
}
