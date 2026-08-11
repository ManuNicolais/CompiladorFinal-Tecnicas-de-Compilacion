// Archivo de prueba SIN errores
// Demuestra todas las fases del compilador + optimizaciones

// Variables globales
int contadorGlobal;
double valorPi;
char inicial;
bool activo;

// Funcion con codigo muerto intencional (para optimizacion)
int calcular(int x) {
    int res;
    res = x + 10;
    return res;          // a partir de aca nada se ejecuta
    res = res * 2;       // CODIGO MUERTO
    res = res / 3;       // CODIGO MUERTO
}

int main() {
    int a;
    int b;
    int c;
    int d;
    int i;
    int resultado;
    int numeros[3];

    // ---- Inicializar globales ----
    contadorGlobal = 0;
    valorPi = 3.14;
    inicial = 'M';
    activo = true;

    // ---- OPT 1: Simplificacion de expresiones ----
    a = 5 + 3;           // constant folding: debe quedar a = 8
    b = a + 0;           // identidad: debe quedar b = a
    c = b * 1;           // identidad: debe quedar c = b
    d = c * 0;           // identidad: debe quedar d = 0

    resultado = a + b + c + d;

    // ---- OPT 2: Propagacion de constantes ----
    a = 10;              // a es constante = 10
    b = a + 5;           // debe propagarse: b = 15
    c = b;               // debe propagarse: c = 15

    resultado = resultado + c;

    // ---- OPT 3: Eliminacion de subexpresiones comunes ----
    a = resultado + 5;   // t0 = resultado + 5
    b = resultado + 5;   // debe reusar t0, no genera nuevo temporal

    resultado = a + b;

    // ---- Arrays ----
    numeros[0] = 10;
    numeros[1] = 20;
    numeros[2] = 30;
    resultado = resultado + numeros[0] + numeros[1];

    // ---- OPT 4: Codigo muerto ya aplicado en calcular() ----

    // ---- OPT 5: Optimizacion de bucles ----
    // contadorGlobal no cambia dentro del while (loop invariant)
    // Se usa limite = numeros[0] (10) para evitar que el propagador
    // simplifique incorrectamente la condicion del bucle
    i = 0;
    while (i < numeros[0]) {
        resultado = resultado + contadorGlobal;
        i = i + 1;
    }

    // ---- For con invariante ----
    for (i = 0; i < numeros[0]; i = i + 1) {
        resultado = resultado + contadorGlobal;
    }

    // ---- if-else ----
    if (resultado > 0) {
        int aux;
        aux = resultado + 10;
        resultado = aux;
    } else {
        resultado = 0;
    }

    // ---- Llamada a funcion ----
    resultado = calcular(resultado);

    // ---- Usar globales ----
    contadorGlobal = resultado;
    valorPi = resultado;
    inicial = 'X';

    return resultado;
}
