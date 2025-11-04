package org.example.Parser;

/* Representa un error sintactico o semantico encontrado durante el analisis */
public class AES {
    private int lineaError;
    private final String contenido;
    private final String tipo;
    private final String contexto;

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

    /*Getters*/
    public int getLinea() { return lineaError; }
    public void setLinea(int linea) { this.lineaError = linea; }
    public String getContenido() { return contenido; }
    public String getTipo() { return tipo; }
    public String getContexto() { return contexto; }
}
