package org.example.Parser;

/**
 * Almacena Error Sintactico/Semantico
 */
public class AES {
    private int lineaError;
    private String contenido;
    private String tipo;
    private String contexto;

    public AES(int linea, String contenido, String tipo) {
        this(linea, contenido, tipo, null);
    }

    public AES(int linea, String contenido, String tipo, String contexto) {
        this.lineaError = linea;
        this.contenido = contenido;
        this.tipo = tipo;
        this.contexto = contexto;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[Linea %d] %s: '%s'", lineaError, tipo, contenido));

        if (contexto != null && !contexto.isEmpty()) {
            sb.append(" → ").append(contexto);
        }

        return sb.toString();
    }

    // Getters y Setters
    public int getLinea() { return lineaError; }
    public void setLinea(int linea) { this.lineaError = linea; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getContexto() { return contexto; }
    public void setContexto(String contexto) { this.contexto = contexto; }
}
