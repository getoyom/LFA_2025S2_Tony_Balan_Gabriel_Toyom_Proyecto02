public class Token{
    
    private String valor;
    private Tokencitos tipo;
    private int linea;
    private String operador; // Para APERTURA_OPERACION

    public Token(String valor, Tokencitos tipo, int linea) {
        this.valor = valor;
        this.tipo = tipo;
        this.linea = linea;
    }

    // Getters y Setters
    public String getValor() {return valor;}

    public void setValor(String valor) {this.valor = valor;}

    public Tokencitos getTokens() {return tipo;}

    public void setTokens(Tokencitos tipo) {this.tipo = tipo; }

    public int getLinea() {return linea;}

    public void setLinea(int linea) {this.linea = linea;}

    public String getOperador() {return operador;}

    public void setOperador(String operador) {this.operador = operador;}

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
        String resultado = String.format("Tipo: %s, Valor:'%s', Linea: %d", tipo, valor, linea);
        if (operador != null) {
            resultado += ", operador:'" + operador + "'";
        }
        return resultado + "}";
    }
}
