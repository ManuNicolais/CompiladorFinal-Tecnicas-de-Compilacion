// ============================================================
// TEST DE OPTIMIZACIONES - TÃ©cnicas de CompilaciÃ³n 2026
// ============================================================
// Este archivo contiene patrones que activan CADA UNA de
// las 5 optimizaciones implementadas. Ejecutar con:
//   java -jar target/compiladorFinal-1.0-jar-with-dependencies.jar test_optimizaciones.cpp
// ============================================================

// -----------------------------------------------------------
// OPTIMIZACIÃ“N 1: SIMPLIFICACIÃ“N DE EXPRESIONES
// Patrones: constant folding (5+3), identidades (a+0, a*1, a*0, a/1)
// -----------------------------------------------------------
int testSimplificacion(int x) {
    int a;
    int b;
    int c;
    int d;

    a = 5 + 3;        // constant folding: debe quedar a = 8
    b = a + 0;        // identidad: debe quedar b = a
    c = b * 1;        // identidad: debe quedar c = b
    d = c * 0;        // identidad: debe quedar d = 0

    return a + b + c + d;
}

// -----------------------------------------------------------
// OPTIMIZACIÃ“N 2: PROPAGACIÃ“N DE CONSTANTES
// Patrones: y=10 â†’ luego y se reemplaza por 10 donde aparezca
// -----------------------------------------------------------
int testPropagacion(int x) {
    int a;
    int b;
    int c;

    a = 10;            // a es constante = 10
    b = a + 5;         // debe propagarse: b = 10 + 5 â†’ b = 15
    c = b;             // debe propagarse: c = 15

    return c;
}

// -----------------------------------------------------------
// OPTIMIZACIÃ“N 3: ELIMINACIÃ“N DE SUBEXPRESIONES COMUNES
// Patrones: (a+b) calculado dos veces
// -----------------------------------------------------------
int testSubexpresiones(int a, int b) {
    int x;
    int y;
    int z;

    x = a + b;         // t0 = a + b
    y = a + b;         // debe reusar: y = t0  (no genera nuevo temporal)
    z = x + y;

    return z;
}

// -----------------------------------------------------------
// OPTIMIZACIÃ“N 4: ELIMINACIÃ“N DE CÃ“DIGO MUERTO
// Patrones: cÃ³digo despuÃ©s de return (inalcanzable), temporales no usados
// -----------------------------------------------------------
int testCodigoMuerto(int x) {
    int a;
    int b;
    int c;
    int resultado;

    resultado = x + 10;
    return resultado;  // a partir de acÃ¡ nada se ejecuta

    a = 20;            // CÃ“DIGO MUERTO: nunca se ejecuta
    b = 30;            // CÃ“DIGO MUERTO: nunca se ejecuta
    c = a + b;         // CÃ“DIGO MUERTO: nunca se ejecuta
    resultado = c;     // CÃ“DIGO MUERTO: nunca se ejecuta
}

// -----------------------------------------------------------
// OPTIMIZACIÃ“N 5: OPTIMIZACIÃ“N DE BUCLES (loop invariant)
// Patrones: invariantes dentro de while que pueden moverse fuera
// -----------------------------------------------------------
int testBucles(int n) {
    int i;
    int resultado;
    int limite;
    int factor;

    factor = 3;                    // constante fuera del bucle
    limite = n * 2;                // calculado fuera del bucle

    i = 0;
    while (i < limite) {
        resultado = resultado + factor;  // factor no cambia dentro del bucle
        i = i + 1;
    }

    return resultado;
}

// -----------------------------------------------------------
// OPTIMIZACIÃ“N 4 Y 5 COMBINADAS: cÃ³digo muerto + bucles
// -----------------------------------------------------------
int main() {
    int resultado;
    int tempNoUsado;

    resultado = testSimplificacion(1);
    resultado = testPropagacion(resultado);
    resultado = testSubexpresiones(resultado, 5);
    resultado = testCodigoMuerto(resultado);
    resultado = testBucles(resultado);

    tempNoUsado = 999;             // CÃ“DIGO MUERTO: tempNoUsado nunca se usa despuÃ©s

    return resultado;
}
