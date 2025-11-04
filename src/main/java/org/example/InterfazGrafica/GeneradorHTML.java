package org.example.InterfazGrafica;

import org.example.Lexer.*;
import org.example.Parser.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GeneradorHTML {

    private static final String ARCHIVO_RESULTADOS = "RESULTADOS_EQUIPO#3.html.html";
    private static final String ARCHIVO_ERRORES = "ERRORES_EQUIPO#3.html.html";

    public void generarArchivoResultados(ArrayList<ArrayList<Token>> operacionesValidas,
                                         HashMap<String, Float> resultados) throws IOException {

        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"es\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Resultados de Operaciones</title>\n");
        html.append(generarEstilosResultados());
        html.append("</head>\n");
        html.append("<body>\n");

        html.append("    <div class=\"container\">\n");
        html.append("        <h1>Resultados de Operaciones Aritmeticas</h1>\n");
        html.append("        <div class=\"info-header\">\n");
        html.append("            <p><strong>Fecha:</strong> ").append(obtenerFechaActual()).append("</p>\n");
        html.append("            <p><strong>Total de operaciones validas:</strong> ").append(operacionesValidas.size()).append("</p>\n");
        html.append("        </div>\n");

        if (operacionesValidas.isEmpty()) {
            html.append("        <div class=\"no-results\">\n");
            html.append("            <p>No se encontraron operaciones validas para procesar.</p>\n");
            html.append("        </div>\n");
        } else {
            html.append("        <table>\n");
            html.append("            <thead>\n");
            html.append("                <tr>\n");
            html.append("                    <th>No.</th>\n");
            html.append("                    <th>Operacion</th>\n");
            html.append("                    <th>Resultado</th>\n");
            html.append("                    <th>Linea</th>\n");
            html.append("                </tr>\n");
            html.append("            </thead>\n");
            html.append("            <tbody>\n");

            int contador = 1;
            for (ArrayList<Token> operacion : operacionesValidas) {
                String expresion = construirExpresionLegible(operacion);
                Float resultado = buscarResultado(resultados, operacion);
                int linea = operacion.isEmpty() ? 0 : operacion.getFirst().getLinea();

                html.append("                <tr>\n");
                html.append("                    <td>").append(contador++).append("</td>\n");
                html.append("                    <td class=\"operacion\">").append(expresion).append("</td>\n");
                html.append("                    <td class=\"resultado\">").append(formatearResultado(resultado)).append("</td>\n");
                html.append("                    <td>").append(linea).append("</td>\n");
                html.append("                </tr>\n");
            }

            html.append("            </tbody>\n");
            html.append("        </table>\n");
        }

        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>\n");

        escribirArchivo(ARCHIVO_RESULTADOS, html.toString());
    }

    public void generarArchivoErrores(ArrayList<AER> erroresLexicos,
                                      ArrayList<AES> erroresSintacticos) throws IOException {

        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"es\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Errores Encontrados</title>\n");
        html.append(generarEstilosErrores());
        html.append("</head>\n");
        html.append("<body>\n");

        html.append("    <div class=\"container\">\n");
        html.append("        <h1>Errores Encontrados en el Analisis</h1>\n");
        html.append("        <div class=\"info-header\">\n");
        html.append("            <p><strong>Fecha:</strong> ").append(obtenerFechaActual()).append("</p>\n");
        html.append("            <p><strong>Total de errores:</strong> ").append(erroresLexicos.size() + erroresSintacticos.size()).append("</p>\n");
        html.append("        </div>\n");

        html.append("        <h2>Errores Lexicos</h2>\n");

        if (erroresLexicos.isEmpty()) {
            html.append("        <div class=\"no-errors\">\n");
            html.append("            <p>No se encontraron errores lexicos.</p>\n");
            html.append("        </div>\n");
        } else {
            html.append("        <table>\n");
            html.append("            <thead>\n");
            html.append("                <tr>\n");
            html.append("                    <th>No.</th>\n");
            html.append("                    <th>Linea</th>\n");
            html.append("                    <th>Lexema</th>\n");
            html.append("                    <th>Descripcion</th>\n");
            html.append("                </tr>\n");
            html.append("            </thead>\n");
            html.append("            <tbody>\n");

            int contador = 1;
            for (AER error : erroresLexicos) {
                html.append("                <tr class=\"error-lexico\">\n");
                html.append("                    <td>").append(contador++).append("</td>\n");
                html.append("                    <td>").append(error.getLN()).append("</td>\n");
                html.append("                    <td><code>").append(escaparHTML(error.getLex())).append("</code></td>\n");
                html.append("                    <td>").append(escaparHTML(error.getToken())).append("</td>\n");
                html.append("                </tr>\n");
            }

            html.append("            </tbody>\n");
            html.append("        </table>\n");
        }

        html.append("        <h2>Errores Sintacticos y Semanticos</h2>\n");

        if (erroresSintacticos.isEmpty()) {
            html.append("        <div class=\"no-errors\">\n");
            html.append("            <p>No se encontraron errores sintacticos o semanticos.</p>\n");
            html.append("        </div>\n");
        } else {
            html.append("        <table>\n");
            html.append("            <thead>\n");
            html.append("                <tr>\n");
            html.append("                    <th>No.</th>\n");
            html.append("                    <th>Linea</th>\n");
            html.append("                    <th>Tipo</th>\n");
            html.append("                    <th>Contenido</th>\n");
            html.append("                    <th>Contexto</th>\n");
            html.append("                </tr>\n");
            html.append("            </thead>\n");
            html.append("            <tbody>\n");

            int contador = 1;
            for (AES error : erroresSintacticos) {
                html.append("                <tr class=\"error-sintactico\">\n");
                html.append("                    <td>").append(contador++).append("</td>\n");
                html.append("                    <td>").append(error.getLinea()).append("</td>\n");
                html.append("                    <td>").append(escaparHTML(error.getTipo())).append("</td>\n");
                html.append("                    <td><code>").append(escaparHTML(error.getContenido())).append("</code></td>\n");
                html.append("                    <td>").append(escaparHTML(error.getContexto() != null ? error.getContexto() : "")).append("</td>\n");
                html.append("                </tr>\n");
            }

            html.append("            </tbody>\n");
            html.append("        </table>\n");
        }

        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>\n");

        escribirArchivo(ARCHIVO_ERRORES, html.toString());
    }

    private String generarEstilosResultados() {
        return """
                    <style>
                        * {
                            margin: 0;
                            padding: 0;
                            box-sizing: border-box;
                        }
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            padding: 20px;
                            min-height: 100vh;
                        }
                        .container {
                            max-width: 1200px;
                            margin: 0 auto;
                            background: white;
                            border-radius: 10px;
                            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
                            padding: 30px;
                        }
                        h1 {
                            color: #667eea;
                            text-align: center;
                            margin-bottom: 30px;
                            font-size: 2em;
                            border-bottom: 3px solid #667eea;
                            padding-bottom: 15px;
                        }
                        .info-header {
                            background: #f8f9fa;
                            padding: 15px;
                            border-radius: 5px;
                            margin-bottom: 20px;
                            border-left: 4px solid #667eea;
                        }
                        .info-header p {
                            margin: 5px 0;
                            color: #333;
                        }
                        table {
                            width: 100%;
                            border-collapse: collapse;
                            margin-top: 20px;
                        }
                        thead {
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            color: white;
                        }
                        th {
                            padding: 15px;
                            text-align: left;
                            font-weight: 600;
                        }
                        td {
                            padding: 12px 15px;
                            border-bottom: 1px solid #e0e0e0;
                        }
                        tbody tr:hover {
                            background: #f5f5f5;
                            transition: background 0.3s;
                        }
                        .operacion {
                            font-family: 'Courier New', monospace;
                            color: #2c3e50;
                        }
                        .resultado {
                            font-weight: bold;
                            color: #27ae60;
                            font-size: 1.1em;
                        }
                        .no-results {
                            text-align: center;
                            padding: 40px;
                            color: #888;
                            font-style: italic;
                        }
                    </style>
                """;
    }

    private String generarEstilosErrores() {
        return """
                    <style>
                        * {
                            margin: 0;
                            padding: 0;
                            box-sizing: border-box;
                        }
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
                            padding: 20px;
                            min-height: 100vh;
                        }
                        .container {
                            max-width: 1200px;
                            margin: 0 auto;
                            background: white;
                            border-radius: 10px;
                            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
                            padding: 30px;
                        }
                        h1 {
                            color: #e74c3c;
                            text-align: center;
                            margin-bottom: 30px;
                            font-size: 2em;
                            border-bottom: 3px solid #e74c3c;
                            padding-bottom: 15px;
                        }
                        h2 {
                            color: #2c3e50;
                            margin-top: 30px;
                            margin-bottom: 15px;
                            font-size: 1.5em;
                            border-left: 4px solid #e74c3c;
                            padding-left: 15px;
                        }
                        .info-header {
                            background: #fff3cd;
                            padding: 15px;
                            border-radius: 5px;
                            margin-bottom: 20px;
                            border-left: 4px solid #ffc107;
                        }
                        .info-header p {
                            margin: 5px 0;
                            color: #856404;
                        }
                        table {
                            width: 100%;
                            border-collapse: collapse;
                            margin-top: 20px;
                            margin-bottom: 30px;
                        }
                        thead {
                            background: #e74c3c;
                            color: white;
                        }
                        th {
                            padding: 15px;
                            text-align: left;
                            font-weight: 600;
                        }
                        td {
                            padding: 12px 15px;
                            border-bottom: 1px solid #e0e0e0;
                        }
                        tbody tr:hover {
                            background: #ffe6e6;
                            transition: background 0.3s;
                        }
                        .error-lexico {
                            background: #fff3f3;
                        }
                        .error-sintactico {
                            background: #fff9f3;
                        }
                        code {
                            background: #f4f4f4;
                            padding: 2px 6px;
                            border-radius: 3px;
                            font-family: 'Courier New', monospace;
                            color: #c7254e;
                        }
                        .no-errors {
                            text-align: center;
                            padding: 40px;
                            color: #27ae60;
                            font-weight: bold;
                            background: #d4edda;
                            border-radius: 5px;
                            margin: 20px 0;
                        }
                    </style>
                """;
    }

    private String construirExpresionLegible(ArrayList<Token> operacion) {
        StringBuilder expresion = new StringBuilder();

        for (Token token : operacion) {
            switch (token.getTokens()) {
                case APERTURA_OPERACION:
                    expresion.append(token.getOperador()).append("(");
                    break;
                case CIERRE_OPERACION:
                    expresion.append(")");
                    break;
                case NUMERO:
                    expresion.append(token.getValor()).append(", ");
                    break;
                default:
                    break;
            }
        }

        return expresion.toString().replaceAll(", \\)", ")");
    }

    private Float buscarResultado(HashMap<String, Float> resultados, ArrayList<Token> operacion) {
        StringBuilder expresion = new StringBuilder();

        for (Token token : operacion) {
            switch (token.getTokens()) {
                case APERTURA_OPERACION:
                    String nombreOp = nombreOperador(token.getOperador());
                    expresion.append(nombreOp).append("(");
                    break;
                case CIERRE_OPERACION:
                    expresion.append(")");
                    break;
                case NUMERO:
                    expresion.append(token.getValor()).append(",");
                    break;
                default:
                    break;
            }
        }

        String expr = expresion.toString().replaceAll(",\\)", ")");
        return resultados.get(expr);
    }

    private String nombreOperador(String operador) {
        return switch (operador) {
            case "SUMA" -> "sum";
            case "RESTA" -> "sub";
            case "MULTIPLICACION" -> "mul";
            case "DIVISION" -> "div";
            case "POTENCIA" -> "pow";
            case "RAIZ" -> "sqrt";
            case "INVERSO" -> "inv";
            case "MOD" -> "mod";
            default -> operador.toLowerCase();
        };
    }

    private String formatearResultado(Float resultado) {
        if (resultado == null) {
            return "N/A";
        }

        if (resultado == resultado.intValue()) {
            return String.valueOf(resultado.intValue());
        }

        return String.format("%.4f", resultado);
    }

    private String escaparHTML(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String obtenerFechaActual() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return formatter.format(new Date());
    }

    private void escribirArchivo(String nombreArchivo, String contenido) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivo))) {
            writer.write(contenido);
        }
    }
}
