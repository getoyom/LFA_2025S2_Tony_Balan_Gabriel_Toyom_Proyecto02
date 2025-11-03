package org.example.Parser;

import org.example.Lexer.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * Analizador Sintactico y Semantico
 *
 * Responsabilidades:
 * - Validar la estructura sintactica de las operaciones
 * - Validar la semantica de las operaciones matematicas
 * - Calcular resultados de operaciones validas
 * - Gestionar errores encontrados
 */
public class Sintactico {

    private ArrayList<Token> filtro1;
    private ArrayList<ArrayList<Token>> filtro2;
    private ArrayList<AES> erroresSintacticos;
    private HashMap<String, Float> operacionesResultados;
    private ArrayList<ArrayList<Token>> operacionesValidas;

    public Sintactico() {
        this.filtro1 = new ArrayList<>();
        this.filtro2 = new ArrayList<>();
        this.erroresSintacticos = new ArrayList<>();
        this.operacionesResultados = new HashMap<>();
        this.operacionesValidas = new ArrayList<>();
    }

    /**
     * Proceso principal de analisis
     */
    public void filtrarTokens(ArrayList<Token> tokensValidos) {
        System.out.println("\n=== INICIANDO ANÁLISIS ===");
        System.out.println("Fase 1: Filtrado lexico...");
        verificarLexico(tokensValidos);

        System.out.println("Fase 2: Analisis sintactico...");
        verificarSintaxis(filtro1);

        System.out.println("Fase 3: Analisis semantico...");
        verificarSemantica(filtro2);

        imprimirErrores();
        imprimirResultados();
        System.out.println("=== ANALISIS COMPLETADO ===\n");
    }

    /**
     * Fase 1: Verificacion lexica basica
     */
    private void verificarLexico(ArrayList<Token> tokensLexicos) {
        for (Token token : tokensLexicos) {
            if (esTokenValido(token)) {
                filtro1.add(token);
            } else {
                registrarErrorSintactico(token.getLinea(), token.getValor(),
                        "Token invalido", "El token no es reconocido");
            }
        }
    }

    private boolean esTokenValido(Token token) {
        switch (token.getTokens()) {
            case APERTURA_OPERACION:
                if (!esOperadorValido(token.getOperador())) {
                    registrarErrorSintactico(token.getLinea(), token.getValor(),
                            "Operador invalido", "El operador '" + token.getOperador() + "' no existe");
                    return false;
                }
                return true;
            case CIERRE_OPERACION:
            case APERTURA_NUMERO:
            case CIERRE_NUMERO:
            case APERTURA_POTENCIA:
            case CIERRE_POTENCIA:
            case APERTURA_RAIZ:
            case CIERRE_RAIZ:
            case NOMBRE_OPERACION:
            case NUMERO:
                return true;
            default:
                return false;
        }
    }

    private boolean esOperadorValido(String operador) {
        return operador.equals("SUMA") || operador.equals("RESTA") ||
                operador.equals("MULTIPLICACION") || operador.equals("DIVISION") ||
                operador.equals("POTENCIA") || operador.equals("RAIZ") ||
                operador.equals("INVERSO") || operador.equals("MOD");
    }

    /**
     * Fase 2: Analisis sintactico con validacion de estructura
     */
    private void verificarSintaxis(ArrayList<Token> tokens) {
        Stack<Token> pilaOperaciones = new Stack<>();
        Stack<Integer> contadorOperandos = new Stack<>();
        ArrayList<Token> bloqueActual = new ArrayList<>();
        int nivelAnidacion = 0;
        boolean hayError = false;

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            if (token.getTokens() == Token.Tokencitos.APERTURA_OPERACION) {
                if (nivelAnidacion == 0 && !bloqueActual.isEmpty()) {
                    procesarBloquePendiente(pilaOperaciones, bloqueActual);
                    contadorOperandos.clear();
                    hayError = false;
                }

                if (!validarEstructuraOperacion(tokens, i)) {
                    hayError = true;
                }

                bloqueActual.add(token);
                pilaOperaciones.push(token);
                contadorOperandos.push(0);
                nivelAnidacion++;

            } else if (token.getTokens() == Token.Tokencitos.NUMERO) {
                bloqueActual.add(token);
                if (!contadorOperandos.isEmpty()) {
                    contadorOperandos.push(contadorOperandos.pop() + 1);
                }

            } else if (token.getTokens() == Token.Tokencitos.CIERRE_OPERACION) {
                bloqueActual.add(token);

                if (pilaOperaciones.isEmpty()) {
                    registrarErrorSintactico(token.getLinea(), token.getValor(),
                            "Error de sintaxis", "Cierre sin apertura correspondiente");
                    hayError = true;
                } else {
                    Token apertura = pilaOperaciones.pop();
                    int operandos = contadorOperandos.pop();

                    if (!validarCantidadOperandos(apertura, operandos)) {
                        hayError = true;
                    }

                    nivelAnidacion--;

                    if (!contadorOperandos.isEmpty()) {
                        contadorOperandos.push(contadorOperandos.pop() + 1);
                    }

                    if (nivelAnidacion == 0) {
                        if (!hayError) {
                            filtro2.add(new ArrayList<>(bloqueActual));
                        }
                        bloqueActual.clear();
                        hayError = false;
                    }
                }
            } else {
                bloqueActual.add(token);
            }
        }

        procesarBloquePendiente(pilaOperaciones, bloqueActual);
    }

    private void procesarBloquePendiente(Stack<Token> pila, ArrayList<Token> bloque) {
        while (!pila.isEmpty()) {
            Token token = pila.pop();
            registrarErrorSintactico(token.getLinea(), token.getValor(),
                    "Error de sintaxis", "Operacion sin cerrar");
        }
        bloque.clear();
    }

    private boolean validarEstructuraOperacion(ArrayList<Token> tokens, int inicio) {
        return validarEstructuraNumeros(tokens, inicio) &&
                validarEstructuraEspecifica(tokens, inicio);
    }

    private boolean validarEstructuraNumeros(ArrayList<Token> tokens, int inicio) {
        int nivel = 0;
        boolean valido = true;

        for (int i = inicio; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            if (token.getTokens() == Token.Tokencitos.APERTURA_OPERACION) {
                nivel++;
            } else if (token.getTokens() == Token.Tokencitos.CIERRE_OPERACION) {
                nivel--;
                if (nivel == 0) break;
            } else if (token.getTokens() == Token.Tokencitos.NUMERO && nivel == 1) {
                if (!estaEnEtiquetaCorrecta(tokens, i)) {
                    registrarErrorSintactico(token.getLinea(), token.getValor(),
                            "Error de sintaxis", "Numero sin etiquetas correctas");
                    valido = false;
                }
            }
        }

        return valido;
    }

    private boolean estaEnEtiquetaCorrecta(ArrayList<Token> tokens, int pos) {
        if (pos <= 0 || pos >= tokens.size() - 1) return false;

        Token anterior = tokens.get(pos - 1);
        Token siguiente = tokens.get(pos + 1);

        return (anterior.getTokens() == Token.Tokencitos.APERTURA_NUMERO &&
                siguiente.getTokens() == Token.Tokencitos.CIERRE_NUMERO) ||
                (anterior.getTokens() == Token.Tokencitos.APERTURA_POTENCIA &&
                        siguiente.getTokens() == Token.Tokencitos.CIERRE_POTENCIA) ||
                (anterior.getTokens() == Token.Tokencitos.APERTURA_RAIZ &&
                        siguiente.getTokens() == Token.Tokencitos.CIERRE_RAIZ);
    }

    private boolean validarEstructuraEspecifica(ArrayList<Token> tokens, int inicio) {
        String operador = tokens.get(inicio).getOperador();
        int operandos = contarOperandosDirectos(tokens, inicio);
        boolean valido = true;

        if (operador.equals("POTENCIA") && operandos != 1) {
            registrarErrorSintactico(tokens.get(inicio).getLinea(), operador,
                    "Error de sintaxis",
                    "POTENCIA requiere 1 operando (base), encontrados: " + operandos);
            valido = false;
        } else if (operador.equals("RAIZ") && operandos != 1) {
            registrarErrorSintactico(tokens.get(inicio).getLinea(), operador,
                    "Error de sintaxis",
                    "RAIZ requiere 1 operando, encontrados: " + operandos);
            valido = false;
        }

        return valido;
    }

    private int contarOperandosDirectos(ArrayList<Token> tokens, int inicio) {
        int nivel = 0;
        int operandos = 0;

        for (int i = inicio; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            if (token.getTokens() == Token.Tokencitos.APERTURA_OPERACION) {
                if (nivel == 1) operandos++;
                nivel++;
            } else if (token.getTokens() == Token.Tokencitos.CIERRE_OPERACION) {
                nivel--;
                if (nivel == 0) break;
            } else if (nivel == 1 && token.getTokens() == Token.Tokencitos.NUMERO) {
                if (i > 0 && tokens.get(i - 1).getTokens() == Token.Tokencitos.APERTURA_NUMERO) {
                    operandos++;
                }
            }
        }

        return operandos;
    }

    private boolean validarCantidadOperandos(Token apertura, int operandos) {
        String operador = apertura.getOperador();
        int requeridos = (operador.equals("INVERSO") || operador.equals("RAIZ")) ? 1 : 2;

        if (operandos < requeridos) {
            registrarErrorSintactico(apertura.getLinea(), apertura.getValor(),
                    "Error semantico",
                    String.format("%s requiere al menos %d operando(s), encontrados: %d",
                            operador, requeridos, operandos));
            return false;
        }

        return true;
    }

    /**
     * Fase 3: Analisis semantico y calculo de resultados
     */
    private void verificarSemantica(ArrayList<ArrayList<Token>> operaciones) {
        for (ArrayList<Token> operacion : operaciones) {
            try {
                String expresion = construirExpresion(operacion);
                float resultado = calcularExpresion(expresion);

                operacionesValidas.add(operacion);
                operacionesResultados.put(expresion, resultado);

            } catch (ArithmeticException e) {
                registrarErrorSemantico(operacion, e.getMessage());
            } catch (Exception e) {
                int linea = operacion.isEmpty() ? 0 : operacion.get(0).getLinea();
                registrarErrorSintactico(linea, "Operacion",
                        "Error de procesamiento", "No se pudo procesar la operacion");
            }
        }
    }

    private String construirExpresion(ArrayList<Token> operacion) {
        StringBuilder expresion = new StringBuilder();

        for (Token token : operacion) {
            switch (token.getTokens()) {
                case APERTURA_OPERACION:
                    expresion.append(nombreOperador(token.getOperador())).append("(");
                    break;
                case CIERRE_OPERACION:
                    expresion.append(")");
                    break;
                case NUMERO:
                    if (token.getValor().trim().isEmpty()) {
                        throw new IllegalArgumentException("Numero vacio detectado");
                    }
                    expresion.append(token.getValor()).append(",");
                    break;
                default:
                    // Ignorar otros tokens
                    break;
            }
        }

        return expresion.toString().replaceAll(",\\)", ")");
    }

    private String nombreOperador(String operador) {
        switch (operador) {
            case "SUMA": return "sum";
            case "RESTA": return "sub";
            case "MULTIPLICACION": return "mul";
            case "DIVISION": return "div";
            case "POTENCIA": return "pow";
            case "RAIZ": return "sqrt";
            case "INVERSO": return "inv";
            case "MOD": return "mod";
            default: return operador.toLowerCase();
        }
    }

    private float calcularExpresion(String expresion) {
        if (esNumero(expresion)) {
            return Float.parseFloat(expresion);
        }

        int inicio = expresion.indexOf('(');
        int fin = expresion.lastIndexOf(')');

        if (inicio == -1 || fin == -1 || fin <= inicio) {
            throw new IllegalArgumentException("Formato de expresion invalido");
        }

        String operacion = expresion.substring(0, inicio);
        String contenido = expresion.substring(inicio + 1, fin);

        ArrayList<Float> operandos = extraerOperandos(contenido);
        return ejecutarOperacion(operacion, operandos);
    }

    private boolean esNumero(String texto) {
        try {
            Float.parseFloat(texto);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private ArrayList<Float> extraerOperandos(String contenido) {
        ArrayList<Float> operandos = new ArrayList<>();
        StringBuilder operandoActual = new StringBuilder();
        int nivel = 0;

        for (char c : contenido.toCharArray()) {
            if (c == '(') {
                nivel++;
                operandoActual.append(c);
            } else if (c == ')') {
                nivel--;
                operandoActual.append(c);
            } else if (c == ',' && nivel == 0) {
                String op = operandoActual.toString().trim();
                operandos.add(calcularExpresion(op));
                operandoActual.setLength(0);
            } else {
                operandoActual.append(c);
            }
        }

        if (operandoActual.length() > 0) {
            String op = operandoActual.toString().trim();
            operandos.add(calcularExpresion(op));
        }

        return operandos;
    }

    private float ejecutarOperacion(String operacion, ArrayList<Float> operandos) {
        switch (operacion) {
            case "sum":
                return operandos.stream().reduce(0f, Float::sum);

            case "sub":
                float resultado = operandos.get(0);
                for (int i = 1; i < operandos.size(); i++) {
                    resultado -= operandos.get(i);
                }
                return resultado;

            case "mul":
                return operandos.stream().reduce(1f, (a, b) -> a * b);

            case "div":
                resultado = operandos.get(0);
                for (int i = 1; i < operandos.size(); i++) {
                    if (operandos.get(i) == 0.0f) {
                        throw new ArithmeticException("Division por cero");
                    }
                    resultado /= operandos.get(i);
                }
                return resultado;

            case "pow":
                if (operandos.size() >= 2) {
                    return (float) Math.pow(operandos.get(1), operandos.get(0));
                }
                return 0f;

            case "sqrt":
                if (operandos.size() >= 2) {
                    float indice = operandos.get(0);
                    float radicando = operandos.get(1);

                    if (indice == 0.0f) {
                        throw new ArithmeticException("Raiz con indice cero");
                    }
                    if (radicando < 0 && indice % 2 == 0) {
                        throw new ArithmeticException("Raiz par de numero negativo");
                    }

                    return (float) Math.pow(radicando, 1.0f / indice);
                } else {
                    if (operandos.get(0) < 0) {
                        throw new ArithmeticException("Raiz cuadrada de numero negativo");
                    }
                    return (float) Math.sqrt(operandos.get(0));
                }

            case "inv":
                if (operandos.get(0) == 0.0f) {
                    throw new ArithmeticException("Inverso de cero");
                }
                return 1f / operandos.get(0);

            case "mod":
                resultado = operandos.get(0);
                for (int i = 1; i < operandos.size(); i++) {
                    if (operandos.get(i) == 0.0f) {
                        throw new ArithmeticException("Modulo por cero");
                    }
                    resultado %= operandos.get(i);
                }
                return resultado;

            default:
                return 0f;
        }
    }

    /**
     * Gestion de errores
     */
    private void registrarErrorSintactico(int linea, String contenido, String tipo, String contexto) {
        erroresSintacticos.add(new AES(linea, contenido, tipo, contexto));
    }

    private void registrarErrorSemantico(ArrayList<Token> operacion, String mensaje) {
        int linea = operacion.isEmpty() ? 0 : operacion.get(0).getLinea();
        String tipo = "";

        if (mensaje.contains("indice cero")) {
            tipo = "Raiz con indice cero es indefinida";
        } else if (mensaje.contains("par de numero negativo")) {
            tipo = "Raiz par de numero negativo no es real";
        } else if (mensaje.contains("cuadrada de numero negativo")) {
            tipo = "Raiz cuadrada de numero negativo no es real";
        } else if (mensaje.contains("Division por cero")) {
            tipo = "Division por cero detectada";
        } else if (mensaje.contains("Inverso de cero")) {
            tipo = "Inverso de cero es indefinido";
        } else if (mensaje.contains("Modulo por cero")) {
            tipo = "Modulo por cero es indefinido";
        } else {
            tipo = "Error matematico: " + mensaje;
        }

        registrarErrorSintactico(linea, "Operacion", "Error semantico", tipo);
    }

    /**
     * Impresion de resultados
     */
    private void imprimirErrores() {
        System.out.println("\n========== ERRORES ENCONTRADOS ==========");
        if (erroresSintacticos.isEmpty()) {
            System.out.println("No se encontraron errores");
        } else {
            erroresSintacticos.forEach(System.out::println);
        }
        System.out.println("=========================================\n");
    }

    private void imprimirResultados() {
        System.out.println("\n========== RESULTADOS ==========");
        if (operacionesResultados.isEmpty()) {
            System.out.println("No hay operaciones validas");
        } else {
            operacionesResultados.forEach((expr, res) ->
                    System.out.printf("Operacion: %s -> Resultado: %.2f%n", expr, res)
            );
        }
        System.out.println("================================\n");
    }

    // Getters
    public ArrayList<AES> getErroresSintacticos() {
        return erroresSintacticos;
    }

    public HashMap<String, Float> getResultados() {
        return operacionesResultados;
    }

    public ArrayList<ArrayList<Token>> getOperacionesValidas() {
        return operacionesValidas;
    }
}