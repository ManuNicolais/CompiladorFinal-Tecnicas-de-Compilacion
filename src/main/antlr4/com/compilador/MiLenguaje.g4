grammar MiLenguaje;

// ============================================
// REGLAS SINTACTICAS (Parser)
// ============================================

//lista de declaraciones globales.
programa : declaracionGlobal* EOF ;

declaracionGlobal
    : funcionDecl
    | variableDecl
    ;

//declaración de función con tipo, nombre, parámetros opcionales y bloque de sentencias.
funcionDecl
    : tipo ID PA listaParams? PC bloque    # DeclFuncion
    ;

listaParams
    : param (COMA param)*
    ;

param
    : tipo ID (CO INTEGER CC)?
    ;

//declaración de variable simple o arreglo.
variableDecl
    : tipo ID PYC                                             # DeclVariable
    | tipo ID CO INTEGER CC PYC                               # DeclArray
    ;

//un conjunto de sentencias entre {}
bloque
    : LLAVE_A sentencia* LLAVE_C
    ;

//declaración, asignación, asignación a arreglo, if, while, for, return, break, continue, bloque o expresión.
sentencia
    : variableDecl                                            # SentDecl
    | expr ASIG expr PYC                                      # SentAsignacion
    | ID CO expr CC ASIG expr PYC                             # SentAsignacionArray
    | IF PA expr PC bloque (ELSE bloque)?                     # SentIf
    | WHILE PA expr PC bloque                                 # SentWhile
    | FOR PA sentenciaOpcional? PYC expr? PYC sentenciaOpcional? PC bloque  # SentFor
    | BREAK PYC                                               # SentBreak
    | CONTINUE PYC                                            # SentContinue
    | RETURN expr? PYC                                        # SentReturn
    | bloque                                                  # SentBloque
    | expr PYC                                                # SentExpr
    ;

sentenciaOpcional
    : expr ASIG expr                                          # SentOpcExpr
    | variableDecl                                            # SentOpcDecl
    ;

// Expresiones con precedencia (de menor a mayor)
expr
    : expr opLogico expr        # ExprLogica
    | expr opRelacional expr    # ExprRelacional
    | expr (SUM | RES) expr     # ExprAritmetica
    | expr (MUL | DIV | MOD) expr  # ExprMulDiv
    | primaria                  # ExprPrimaria
    ;

opRelacional
    : MAYOR | MENOR | MAYOR_IGUAL | MENOR_IGUAL | EQL | DISTINTO
    ;

opLogico
    : AND | OR
    ;

//identificador, acceso a arreglo, llamada a función, literales y paréntesis.
primaria
    : ID PA listaArgs? PC       # LlamadaFuncion
    | ID CO expr CC             # AccesoArray
    | ID                        # Identificador
    | INTEGER                   # Numero
    | DECIMAL                   # NumeroDecimal
    | CHARACTER                 # LiteralChar
    | STR_LIT                   # LiteralString
    | TRUE                      # LiteralTrue
    | FALSE                     # LiteralFalse
    | PA expr PC                # Paren
    ;

listaArgs
    : expr (COMA expr)*
    ;

// ============================================
// TOKENS LEXICOS (Lexer)
// ============================================

// Palabras reservadas (ANTES que ID)
IF       : 'if' ;
ELSE     : 'else' ;
WHILE    : 'while' ;
FOR      : 'for' ;
BREAK    : 'break' ;
CONTINUE : 'continue' ;
RETURN   : 'return' ;
INT      : 'int' ;
DOUBLE   : 'double' ;
CHAR     : 'char' ;
BOOL     : 'bool' ;
VOID     : 'void' ;
STRING   : 'string' ;
TRUE     : 'true' ;
FALSE    : 'false' ;

// Operadores relacionales (compuestos ANTES que simples)
EQL          : '==' ;
DISTINTO     : '!=' ;
MAYOR_IGUAL  : '>=' ;
MENOR_IGUAL  : '<=' ;
MAYOR        : '>' ;
MENOR        : '<' ;

// Operadores logicos
AND : '&&' ;
OR  : '||' ;

// Operadores aritmeticos
SUM : '+' ;
RES : '-' ;
MUL : '*' ;
DIV : '/' ;
MOD : '%' ;

// Asignacion
ASIG : '=' ;

// Delimitadores
PA      : '(' ;
PC      : ')' ;
LLAVE_A : '{' ;
LLAVE_C : '}' ;
CO      : '[' ;
CC      : ']' ;
COMA    : ',' ;
PYC     : ';' ;

// Literales
CHARACTER : '\'' (~['\r\n\\] | '\\' .) '\'' ;
STR_LIT : '"' (~["\r\n\\] | '\\' .)* '"' ;

// Identificadores
ID : [a-zA-Z_][a-zA-Z0-9_]* ;

// Numeros
INTEGER : [0-9]+ ;
DECIMAL : [0-9]+ '.' [0-9]+ ;

// Comentarios (se ignoran)
COMENTARIO_LINEA  : '//' ~[\r\n]* -> skip ;
COMENTARIO_BLOQUE : '/*' .*? '*/' -> skip ;

// Espacios en blanco (se ignoran)
WS : [ \r\n\t]+ -> skip ;

// Captura caracteres invalidos
OTRO : . ;

// Reglas de tipo (parser, no lexer)
tipo : INT | DOUBLE | CHAR | BOOL | VOID | STRING ;
