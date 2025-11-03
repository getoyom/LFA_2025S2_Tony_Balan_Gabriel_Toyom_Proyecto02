package org.example.Lexer;
/*
 * Almacena Error de Reconocimiento (lexico)
 */
public class AER {

    private int lineNumber;
    private String lex;
    private String tipo;

    //Constructor
    public AER(int numLinea, String lexito, String tokensito){
        this.lineNumber = numLinea;
        this.lex = lexito;
        this.tipo = tokensito;
    }

    @Override
    public String toString(){
        String reporte = String.format("Error encontrado en la linea - %d, Lexema - '%s', token - %s", lineNumber, lex, tipo);

        return "Error{" + reporte + "}";
    }

    //Getters y setters
    public int getLN(){return this.lineNumber;}

    public String getLex(){return this.lex;}

    public String getToken(){return this.tipo;}

    public void setLN(int lineN){this.lineNumber = lineN;}

    public void setLex(String lexema){this.lex = lexema;}

    public void setToken(String tokenN){this.tipo = tokenN;}

}