package org.example.Lexer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Scanner {
    private final ArrayList<Token> tokensList;
    private final ArrayList<AER> errores;

    private int numeroLinea;

    public Scanner() {
        this.tokensList = new ArrayList<>();
        this.errores = new ArrayList<>();
        this.numeroLinea = 0;
    }

    public void scanFile(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String lineaOriginal;

           /*Leer linea por linea el archivo*/
            while ((lineaOriginal = reader.readLine()) != null) {
                numeroLinea++;
                procesarLinea(lineaOriginal);
            }

            System.out.printf("Archivo leido exitosamente: %s\n", fileName);
            System.out.printf("Total de tokens reconocidos: %d\n", tokensList.size());

            mostrarTokens();
            if (errores.isEmpty()) {
                System.out.println("No hay errores lexicos registrados.");
            } else {
                mostrarErrores();
            }

        } catch (FileNotFoundException e) {
            System.err.println("ERROR: No se encontro el archivo: " + fileName);
        } catch (IOException e) {
            System.err.println("ERROR: No se pudo leer el archivo: " + fileName);
        }
    }

    /* Procesa una linea extrayendo todos sus lexemas */
    private void procesarLinea(String linea) {
        if (linea == null || linea.trim().isEmpty()) {
            return;
        }

        /*Eliminar espacios en blanco excepto dentro de valores*/
        linea = linea.trim();

        /*Extraer todos los lexemas de esta linea*/
        ArrayList<String> lexemas = extraerLexemas(linea);

        /*Analizar cada lexema*/
        for (String lexema : lexemas) {
            if (!lexema.isEmpty()) {
                verificacionLineas(lexema);
            }
        }
    }

    private ArrayList<String> extraerLexemas(String linea) {
        ArrayList<String> lexemas = new ArrayList<>();
        StringBuilder lexemaActual = new StringBuilder();
        boolean dentroEtiqueta = false;

        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);

            if (c == '<') {
                if (!lexemaActual.isEmpty()) {
                    String temp = lexemaActual.toString().trim();
                    if (!temp.isEmpty()) {
                        lexemas.add(temp);
                    }
                    lexemaActual = new StringBuilder();
                }

                dentroEtiqueta = true;
                lexemaActual.append(c);

            } else if (c == '>') {
                lexemaActual.append(c);
                lexemas.add(lexemaActual.toString());
                lexemaActual = new StringBuilder();
                dentroEtiqueta = false;

            } else if (Character.isWhitespace(c) && !dentroEtiqueta) {
                if (!lexemaActual.isEmpty()) {
                    String temp = lexemaActual.toString().trim();
                    if (!temp.isEmpty()) {
                        lexemas.add(temp);
                    }
                    lexemaActual = new StringBuilder();
                }

            } else {
                lexemaActual.append(c);
            }
        }
        if (!lexemaActual.isEmpty()) {
            String temp = lexemaActual.toString().trim();
            if (!temp.isEmpty()) {
                lexemas.add(temp);
            }
        }
        return lexemas;
    }

    private void verificacionLineas(String lineaActual) {
        if (lineaActual == null || lineaActual.trim().isEmpty()) {
            return;
        }

        if (lineaActual.startsWith("<") && lineaActual.endsWith(">")) {
            /*Agregar error en caso exista una etiqueta vacia*/
            if (lineaActual.length() <= 2) {
                errores.add(new AER(numeroLinea, lineaActual, "Etiqueta vacia"));
                return;
            }

            char[] actual = lineaActual.toCharArray();
            if (actual[1] == '/') {
                if (lineaActual.length() <= 3) {
                    errores.add(new AER(numeroLinea, lineaActual, "Etiqueta de cierre vacia"));
                    return;
                }
                verificarCierre(lineaActual);
            } else {
                verificarApertura(lineaActual);
            }

        } else if (esNumero(lineaActual)) {
            tokensList.add(new Token(lineaActual, Token.Tokencitos.NUMERO, numeroLinea));
        } else {
            errores.add(new AER(numeroLinea, lineaActual, "Lexema no reconocido"));
        }
    }

    private void verificarCierre(String lineaActual) {
        /*Limpiar linea actual para mejor analisis*/
        String contenido = lineaActual.substring(2, lineaActual.length() - 1).trim();

        switch (contenido) {
            case "Operacion" -> tokensList.add(new Token(lineaActual, Token.Tokencitos.CIERRE_OPERACION, numeroLinea));
            case "Numero" -> tokensList.add(new Token(lineaActual, Token.Tokencitos.CIERRE_NUMERO, numeroLinea));
            case "P" -> tokensList.add(new Token(lineaActual, Token.Tokencitos.CIERRE_POTENCIA, numeroLinea));
            case "R" -> tokensList.add(new Token(lineaActual, Token.Tokencitos.CIERRE_RAIZ, numeroLinea));
            default -> errores.add(new AER(numeroLinea, contenido, "Etiqueta de cierre desconocida"));
        }
    }

    private void verificarApertura(String lineaActual) {
        /*Limpiar linea actual para mejor analisis*/
        String contenido = lineaActual.substring(1, lineaActual.length() - 1).trim();

        if (contenido.startsWith("Operacion=")) {
            /*Separar lexema Operacion= y el nombre de la operacion para analizar este ultimo*/
            String[] partes = contenido.split("=", 2);
            /*Verificar que solo exista un signo =*/
            if (partes.length == 2) {
                /*Tomar el nombre de la operacion*/
                String operacion = partes[1].trim();

                if (verificarOperacion(operacion)) {
                    Token token = new Token(lineaActual, Token.Tokencitos.APERTURA_OPERACION, numeroLinea);
                    /*Configurar parametro extra para operaciones*/
                    token.setOperador(operacion);
                    tokensList.add(token);
                    /*Si la operacion es valida, el lexema es valido*/
                    tokensList.add(new Token(operacion, Token.Tokencitos.NOMBRE_OPERACION, numeroLinea));
                } else {
                    errores.add(new AER(numeroLinea, operacion, "Operador invalido"));
                }
            } else {
                errores.add(new AER(numeroLinea, contenido, "Formato incorrecto en apertura de operacion"));
            }

        } else if (contenido.equals("Numero")) {
            tokensList.add(new Token(lineaActual, Token.Tokencitos.APERTURA_NUMERO, numeroLinea));

        } else if (contenido.equals("P")) {
            tokensList.add(new Token(lineaActual, Token.Tokencitos.APERTURA_POTENCIA, numeroLinea));

        } else if (contenido.equals("R")) {
            tokensList.add(new Token(lineaActual, Token.Tokencitos.APERTURA_RAIZ, numeroLinea));

        } else {
            errores.add(new AER(numeroLinea, contenido, "Etiqueta de apertura desconocida"));
        }
    }

    private boolean verificarOperacion(String op) {
        return op.equals("SUMA") || op.equals("RESTA") || op.equals("MULTIPLICACION") ||
                op.equals("DIVISION") || op.equals("POTENCIA") || op.equals("RAIZ") ||
                op.equals("INVERSO") || op.equals("MOD");
    }

    private boolean esNumero(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        int inicio = 0;
        if (str.charAt(0) == '-') {
            /*El string solo es una signo -*/
            if (str.length() == 1) {
                return false;
            }
            inicio = 1;
        }

        boolean puntoEncontrado = false;
        boolean digitoEncontrado = false;

        for (int i = inicio; i < str.length(); i++) {
            char c = str.charAt(i);

            if (Character.isDigit(c)) {
                digitoEncontrado = true;
            } else if (c == '.') {
                /*Si previamente se encontro un punto*/
                if (puntoEncontrado) {
                    return false;
                }
                puntoEncontrado = true;
            } else {
                return false;
            }
        }

        return digitoEncontrado &&
                !str.endsWith(".") &&
                !(str.startsWith(".") || str.startsWith("-."));
    }

    private void mostrarTokens() {
        System.out.println("----------LISTA DE TOKENS----------");
        for (Token t : tokensList) {
            System.out.println(t.toString());
        }
        System.out.println("-----------------------------------");
    }

    private void mostrarErrores() {
        System.out.println("---------LISTA DE ERRRORES LEXICOS---------");
        if (errores.isEmpty()) {
            System.out.println("No hay errores registrados");
        } else {
            for (AER err : errores) {
                System.out.println(err.toString());
            }
        }
        System.out.println("-----------------------------------");
    }

    /*Getters*/
    public ArrayList<Token> getTokens() {
        return tokensList;
    }

    public ArrayList<AER> getErrores() {
        return errores;
    }

}