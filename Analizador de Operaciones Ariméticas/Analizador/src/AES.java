public class AES {
    // Atributos - Maneja numero de linea, contenido erroneo y tipo de error sintactico
    private int lineaError;
    private String contenidoError;
    private String tipoError;
    private String contexto;  // contexto adicional (ej: tipo esperado vs encontrado)

    // Constructor basico
    public AES(int linea, String contenido, String tipo) {
        this.lineaError = linea;
        this.contenidoError = contenido;
        this.tipoError = tipo;
        this.contexto = null;
    }

    // Constructor con contexto adicional
    public AES(int linea, String contenido, String tipo, String ctx) {
        this.lineaError = linea;
        this.contenidoError = contenido;
        this.tipoError = tipo;
        this.contexto = ctx;
    }

    @Override
    public String toString() {
        String descripcion = contexto != null ? tipoError + " - " + contexto : tipoError;
        String reporte = String.format("Error encontrado en la linea - %d, Lexema - '%s', token - %s", 
            lineaError, contenidoError, descripcion);
        return "Error{" + reporte + "}";
    }

    // Getters y setters
    public int getLinea() {
        return this.lineaError;
    }

    public String getContenido() {
        return this.contenidoError;
    }

    public String getTipo() {
        return this.tipoError;
    }

    public String getContexto() {
        return this.contexto;
    }

    public void setLinea(int linea) {
        this.lineaError = linea;
    }

    public void setContenido(String contenido) {
        this.contenidoError = contenido;
    }

    public void setTipo(String tipo) {
        this.tipoError = tipo;
    }

    public void setContexto(String ctx) {
        this.contexto = ctx;
    }
}
