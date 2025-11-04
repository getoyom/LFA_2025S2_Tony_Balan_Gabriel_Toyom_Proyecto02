package org.example.Lexer;

public class Token {
    private final String valor;
    private final Tokencitos tipo;
    private int linea;
    private String operador;

    public Token(String valor, Tokencitos tipo, int linea) {
        this.valor = valor;
        this.tipo = tipo;
        this.linea = linea;
    }
    /*Diccionario de Tokens aceptados por el programa*/
    public enum Tokencitos {
        APERTURA_OPERACION,
        CIERRE_OPERACION,
        APERTURA_NUMERO,
        CIERRE_NUMERO,
        APERTURA_POTENCIA,
        CIERRE_POTENCIA,
        APERTURA_RAIZ,
        CIERRE_RAIZ,
        NUMERO,
        NOMBRE_OPERACION
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-25s | %-20s | Linea: %d",
                tipo, "'" + valor + "'", linea));

        if (operador != null) {
            sb.append(" | Operador: ").append(operador);
        }

        return sb.toString();
    }

    /* Getters y Setters*/
    public String getValor() { return valor; }
    public Tokencitos getTokens() { return tipo; }

    public int getLinea() { return linea; }
    public void setLinea(int linea) { this.linea = linea; }

    public String getOperador() { return operador; }
    public void setOperador(String operador) { this.operador = operador; }
}