grammar MiSintactico;

// ==============================================================
//  REGLAS SINTACTICAS (EDITAR para cada ejercicio)
// ==============================================================
// Editar las reglas de abajo segun el ejercicio.
// programa : (token)* EOF  acepta cualquier secuencia de tokens.
// Permite que el lexer procese todo y el test usa la regla 'expr'.
// ==============================================================

programa : (token)* EOF ;

// --- Reglas de expresion (EDITAR para cada ejercicio) ---

// Gramatica completa: E -> E+T | E-T | T
expr : expr SUM term     # Suma
     | expr RES term     # Resta
     | term              # Termino
     ;

// T -> T*F | T/F | F
term : term MUL factor   # Multiplicacion
     | term DIV factor   # Division
     | factor            # SoloFactor
     ;

// F -> id | num | (E)  (con numeros y parentesis)
factor : ID              # Ident
       | INTEGER         # Entero
       | DECIMAL         # Decimal
       | PA expr PC      # Paren
       ;

// --- Reglas auxiliares para ejercicios del parcial ---
// s : IF c THEN s ELSE s  # IfStmt
//    | a                  # AssignStmt
//    ;
// c : e RELOP e ;
// a : ID ASIG e ;

// ==============================================================
//  TOKENS LEXICOS (NO EDITAR, ya cubren todo)
// ==============================================================

token : PA | PC | CA | CC | LA | LC | PYC | COMA
      | IGUAL | ASIG | MAYOR | MAYOR_IGUAL | MENOR | MENOR_IGUAL
      | EQL | DISTINTO | SUM | RES | MUL | DIV | MOD
      | OR | AND | NOT
      | FOR | WHILE | IF | ELSE
      | INT | CHAR | DOUBLE | VOID | STRING
      | RETURN | BREAK | CONTINUE | TRUE | FALSE
      | ID | INTEGER | DECIMAL | CHARACTER | STR_LIT
      | RELOP
      | OTRO
      ;

fragment LETRA : [A-Za-z];
fragment DIGITO : [0-9];

// Delimitadores
PA   : '(' ; PC   : ')' ;
CA   : '[' ; CC   : ']' ;
LA   : '{' ; LC   : '}' ;
PYC  : ';' ; COMA : ',' ;

// Asignacion
IGUAL : '=' ;
ASIG  : ':=' ;

// Relacionales
MAYOR       : '>' ;
MAYOR_IGUAL : '>=' ;
MENOR       : '<' ;
MENOR_IGUAL : '<=' ;
EQL         : '==' ;
DISTINTO    : '!=' ;
RELOP       : MAYOR | MENOR | MAYOR_IGUAL | MENOR_IGUAL | EQL | DISTINTO ;

// Aritmeticos
SUM : '+' ; RES : '-' ; MUL : '*' ; DIV : '/' ; MOD : '%' ;

// Logicos
OR  : 'or' ;
AND : 'and' ;
NOT : 'not' ;

// Palabras reservadas
FOR     : 'for' ;
WHILE   : 'while' ;
IF      : 'if' ;
ELSE    : 'else' ;
INT     : 'int' ;
CHAR    : 'char' ;
DOUBLE  : 'double' ;
VOID    : 'void' ;
STRING  : 'string' ;
RETURN  : 'return' ;
BREAK   : 'break' ;
CONTINUE: 'continue' ;
TRUE    : 'true' ;
FALSE   : 'false' ;

// Identificadores
ID : (LETRA | '_') (LETRA | DIGITO | '_')* ;

// Literales
INTEGER   : DIGITO+ ;
DECIMAL   : INTEGER '.' INTEGER ;
CHARACTER : '\'' (~['\r\n] | '\\' .) '\'' ;
STR_LIT   : '"' (~["\r\n\\] | '\\' .)* '"' ;

// Comentarios
COMENTARIO_LINEA  : '//' ~[\r\n]* -> skip ;
COMENTARIO_BLOQUE : '/*' .*? '*/' -> skip ;

// Whitespace
WS : [ \r\n\t] -> skip ;

// Error
OTRO : . ;
