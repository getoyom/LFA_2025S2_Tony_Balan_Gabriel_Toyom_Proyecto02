import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack; //Pila para ver sintaxis

public class sintactico {
    
    // Lista de tokens validos después del parseo
    private ArrayList<Token> filtro1; //Solo tiene los que se analizan para sintaxis
    private ArrayList<ArrayList<Token>> filtro2; //Tendrá las operaciones válidas sintácticamente
    private ArrayList<AES> erroresSintacticos;
    private HashMap<String, Float> opRe;
    private ArrayList<ArrayList<Token>> validos;

    //Orden jerárjico: Inverso, Potencia - Raíz, Multiplicación - División, Suma - Resta
    //Operaciones con reestricción de 0: Inverso, División
    //Operaciones con reestricción de negativos

    //Constructor
    public sintactico() {
        this.filtro1 = new ArrayList<>();
        this.filtro2 = new ArrayList<>();
        this.erroresSintacticos = new ArrayList<>();
        this.opRe = new HashMap<>();
        this.validos = new ArrayList<>();
    }

    //Primer paso, limpiar todo lo que no sirva
    public void filtrarTokens(ArrayList<Token> tokensValidos){ //Recibe los tokens válidos lexicamente

        System.out.println("Filtrando los tokens validos lexicamente a sintacticamente\n");
        //Ver que sí cumplan con la sintaxis - Solo es por si algo no se limpio bien anteriormente
        verLexico(tokensValidos);
        System.out.println("Filtrando operaciones validas sintacticamente...\n");
        //Ver que sí cumplan con la sintaxis adecuada
        verSintaxis(filtro1);

        System.out.println("Validando semántica de operaciones...\n");
        //Ver semántica - Que las operaciones matemáticas tengan lógica
        verSemantica(filtro2);

        //Mostrar los resultados finales
        printES();
        printResultados();
    }

    //Ver operador
    private boolean verificarOperador(String operador){
        return operador.equals("SUMA") || operador.equals("RESTA") || operador.equals("MULTIPLICACION") ||
                operador.equals("DIVISION") || operador.equals("POTENCIA") || operador.equals("RAIZ") ||
                operador.equals("INVERSO") || operador.equals("MOD");
    }

    //Ver que las etiquetas cumplan con apertura, cierre o número
    private void verLexico(ArrayList<Token> tokensLexicos){
        for (Token token : tokensLexicos) {
            if (token.getTokens() == Token.Tokencitos.APERTURA_OPERACION) {
                if(verificarOperador(token.getOperador())){
                    filtro1.add(token);
                }
                else {
                    erroresSintacticos.add(new AES(token.getLinea(), token.getValor(), "Operador inválido", "El operador '" + token.getOperador() + "' no es reconocido"));
                }
            } else if (token.getTokens() == Token.Tokencitos.CIERRE_OPERACION) {
                filtro1.add(token);
            } else if (token.getTokens() == Token.Tokencitos.APERTURA_NUMERO) {
                filtro1.add(token);
            } else if (token.getTokens() == Token.Tokencitos.CIERRE_NUMERO) {
                filtro1.add(token);
            } else if (token.getTokens() == Token.Tokencitos.NOMBRE_OPERACION) {
                filtro1.add(token);
            } else if (token.getTokens() == Token.Tokencitos.NUMERO) {
                filtro1.add(token);
            } else {
                erroresSintacticos.add(new AES(token.getLinea(), token.getValor(), "Token inválido", "No se reconoce el token"));
            }
        }
    }

    // Validar sintaxis y semántica de las operaciones
    private void verSintaxis(ArrayList<Token> filtro1){
        // Pila para validar balanceo de etiquetas
        Stack<Token> pila = new Stack<>();
        // Pila para contar operandos por cada nivel de anidación
        Stack<Integer> contadorOperandos = new Stack<>();
        // Lista temporal para acumular tokens de una operación completa
        ArrayList<Token> bloqueActual = new ArrayList<>();
        // Contador de profundidad de anidación
        int nivelAnidacion = 0;
        // Flag para marcar si la operación actual tiene errores
        boolean operacionConError = false;
        
        for(int i = 0; i < filtro1.size(); i++){
            Token tokensito = filtro1.get(i);
            
            if(tokensito.getTokens() == Token.Tokencitos.APERTURA_OPERACION){
                // Verificar si el operador es válido antes de procesar
                if(!verificarOperador(tokensito.getOperador())){
                    operacionConError = true;
                    erroresSintacticos.add(new AES(tokensito.getLinea(), tokensito.getValor(), "Error de sintaxis", "Operador inválido: " + tokensito.getOperador()));
                }
                
                // Validar estructura de números en esta operación
                if(!validarEstructuraNumeros(filtro1, i)){
                    operacionConError = true;
                }
                
                // Validar estructura específica por operación
                if(!validarEstructuraEspecifica(filtro1, i)){
                    operacionConError = true;
                }
                
                bloqueActual.add(tokensito);
                pila.push(tokensito);
                contadorOperandos.push(0);
                nivelAnidacion++;
                
            } else if(tokensito.getTokens() == Token.Tokencitos.NUMERO){
                bloqueActual.add(tokensito);
                if(!contadorOperandos.isEmpty()){
                    int operandos = contadorOperandos.pop();
                    contadorOperandos.push(operandos + 1);
                }
            } else if(tokensito.getTokens() == Token.Tokencitos.CIERRE_OPERACION){
                bloqueActual.add(tokensito);
                
                if(pila.isEmpty()){
                    erroresSintacticos.add(new AES(tokensito.getLinea(), tokensito.getValor(), "Error de sintaxis", "Etiqueta de cierre sin apertura correspondiente"));
                    operacionConError = true;
                } else {
                    Token apertura = pila.pop();
                    int operandos = contadorOperandos.pop();
                    
                    String operador = apertura.getOperador();
                    int operandosRequeridos = operador.equals("INVERSO") ? 1 : 2;
                    
                    if(operandos < operandosRequeridos){
                        erroresSintacticos.add(new AES(apertura.getLinea(), apertura.getValor(), "Error semántico", "La operación " + operador + " debe tener al menos " + operandosRequeridos + " operando(s), encontrados: " + operandos));
                        operacionConError = true;
                    }
                    
                    nivelAnidacion--;
                    
                    if(!contadorOperandos.isEmpty()){
                        int operandosSuperiores = contadorOperandos.pop();
                        contadorOperandos.push(operandosSuperiores + 1);
                    }
                    
                    // Si volvemos al nivel 0, completamos una operación principal
                    if(nivelAnidacion == 0){
                        // Solo agregar si no hay errores
                        if(!operacionConError){
                            filtro2.add(new ArrayList<>(bloqueActual));
                        }
                        bloqueActual.clear();
                        operacionConError = false;
                    }
                }
            } else {
                bloqueActual.add(tokensito);
            }
        }
        
        // Verificar que todas las etiquetas estén cerradas
        if (!pila.isEmpty()) {
            while(!pila.isEmpty()){
                Token tokenMalo = pila.pop();
                erroresSintacticos.add(new AES(tokenMalo.getLinea(), tokenMalo.getValor(), "Error de sintaxis", "Etiquetas de operación sin cerrar"));
            }
        }
    }
    
    // Validar que los números estén correctamente estructurados
    private boolean validarEstructuraNumeros(ArrayList<Token> tokens, int inicioOperacion){
        if(inicioOperacion >= tokens.size()) return true;
        
        String operador = tokens.get(inicioOperacion).getOperador();
        int nivel = 0;
        boolean estructuraValida = true;
        
        for(int i = inicioOperacion; i < tokens.size(); i++){
            Token token = tokens.get(i);
            
            if(token.getTokens() == Token.Tokencitos.APERTURA_OPERACION){
                nivel++;
            } else if(token.getTokens() == Token.Tokencitos.CIERRE_OPERACION){
                nivel--;
                if(nivel == 0) break;
            } else if(token.getTokens() == Token.Tokencitos.NUMERO && nivel == 1){
                if(i > 0 && i < tokens.size() - 1){
                    Token anterior = tokens.get(i - 1);
                    Token siguiente = tokens.get(i + 1);
                    
                    boolean esValido = false;
                    
                    // Validar según el tipo de operación
                    if(operador.equals("POTENCIA") || operador.equals("RAIZ")){
                        esValido = (anterior.getTokens() == Token.Tokencitos.APERTURA_NUMERO && siguiente.getTokens() == Token.Tokencitos.CIERRE_NUMERO) ||
                                  (anterior.getTokens() == Token.Tokencitos.APERTURA_POTENCIA && siguiente.getTokens() == Token.Tokencitos.CIERRE_POTENCIA) ||
                                  (anterior.getTokens() == Token.Tokencitos.APERTURA_RAIZ && siguiente.getTokens() == Token.Tokencitos.CIERRE_RAIZ);
                    } else {
                        esValido = anterior.getTokens() == Token.Tokencitos.APERTURA_NUMERO && siguiente.getTokens() == Token.Tokencitos.CIERRE_NUMERO;
                    }
                    
                    if(!esValido){
                        erroresSintacticos.add(new AES(token.getLinea(), token.getValor(), "Error de sintaxis", "Número sin etiquetas correctas"));
                        estructuraValida = false;
                    }
                }
            }
        }
        
        return estructuraValida;
    }
    
    // Validar estructura específica por tipo de operación
    private boolean validarEstructuraEspecifica(ArrayList<Token> tokens, int inicioOperacion){
        if(inicioOperacion >= tokens.size()) return true;
        
        String operador = tokens.get(inicioOperacion).getOperador();
        int nivel = 0;
        int contadorP = 0, contadorR = 0, contadorNumero = 0;
        boolean estructuraValida = true;
        
        for(int i = inicioOperacion; i < tokens.size(); i++){
            Token token = tokens.get(i);
            
            if(token.getTokens() == Token.Tokencitos.APERTURA_OPERACION){
                nivel++;
            } else if(token.getTokens() == Token.Tokencitos.CIERRE_OPERACION){
                nivel--;
                if(nivel == 0) break;
            } else if(nivel == 1){
                // Solo contar en el nivel principal de la operación
                if(token.getTokens() == Token.Tokencitos.APERTURA_POTENCIA){
                    contadorP++;
                    if(!operador.equals("POTENCIA")){
                        erroresSintacticos.add(new AES(token.getLinea(), token.getValor(), "Error de sintaxis", "Etiqueta <P> solo válida en operaciones POTENCIA"));
                        estructuraValida = false;
                    }
                } else if(token.getTokens() == Token.Tokencitos.APERTURA_RAIZ){
                    contadorR++;
                    if(!operador.equals("RAIZ")){
                        erroresSintacticos.add(new AES(token.getLinea(), token.getValor(), "Error de sintaxis", "Etiqueta <R> solo válida en operaciones RAIZ"));
                        estructuraValida = false;
                    }
                } else if(token.getTokens() == Token.Tokencitos.APERTURA_NUMERO){
                    contadorNumero++;
                }
            }
        }
        
        // Validar estructura específica por operación
        if(operador.equals("POTENCIA")){
            if(contadorP != 1 || contadorNumero != 1){
                erroresSintacticos.add(new AES(tokens.get(inicioOperacion).getLinea(), operador, "Error de sintaxis", "POTENCIA debe tener exactamente 1 <P> y 1 <Numero>"));
                estructuraValida = false;
            }
        } else if(operador.equals("RAIZ")){
            if(contadorR != 1 || contadorNumero != 1){
                erroresSintacticos.add(new AES(tokens.get(inicioOperacion).getLinea(), operador, "Error de sintaxis", "RAIZ debe tener exactamente 1 <R> y 1 <Numero>"));
                estructuraValida = false;
            }
        }
        
        return estructuraValida;
    }

    // Getter para acceder a los errores sintácticos
    public ArrayList<AES> getErroresSintacticos() {
        return erroresSintacticos;
    }
    
    /**
     * Analiza cada bloque de operación completa del filtro2 y valida semánticamente
     * Si la operación es válida, la guarda en 'validos' y construye su expresión string
     */
    private void verSemantica(ArrayList<ArrayList<Token>> filtro2){
        // Procesar cada operación completa (desde apertura hasta cierre principal)
        for(ArrayList<Token> operacion : filtro2){
            // Validar que todos los números cumplan restricciones semánticas
            boolean operacionValida = validarOperacion(operacion);
            
            // Si no hay errores semánticos, procesar la operación
            if(operacionValida){
                try {
                    validos.add(operacion);  // Guardar operación válida
                    String expresion = construirExpresion(operacion);  // Convertir a string
                    float resultado = calcularOperacion(expresion);    // Calcular resultado
                    opRe.put(expresion, resultado);  // Almacenar en HashMap
                } catch (Exception e) {
                    // Si llega aquí, hay un error en la construcción de la expresión
                    int linea = operacion.isEmpty() ? 0 : operacion.get(0).getLinea();
                    erroresSintacticos.add(new AES(linea, "Operación", "Error de procesamiento", "No se pudo procesar la operación"));
                }
            }
            // Si hay error semántico, la operación ya fue descartada
        }
    }
    
    /**
     * Valida semánticamente una operación completa
     * Primero construye y evalúa la expresión para detectar divisiones por cero
     */
    private boolean validarOperacion(ArrayList<Token> operacion){
        try {
            // Construir la expresión para evaluarla
            String expresion = construirExpresion(operacion);
            // Intentar calcular - si hay división por cero, se detectará aquí
            calcularOperacion(expresion);
            return true;
        } catch (ArithmeticException e) {
            // Error de división por cero u otra operación matemática inválida
            int linea = operacion.isEmpty() ? 0 : operacion.get(0).getLinea();
            erroresSintacticos.add(new AES(linea, "Operación", "Error semántico", "División por cero detectada"));
            return false;
        } catch (Exception e) {
            // Otros errores matemáticos
            int linea = operacion.isEmpty() ? 0 : operacion.get(0).getLinea();
            erroresSintacticos.add(new AES(linea, "Operación", "Error semántico", "Error en cálculo matemático"));
            return false;
        }
    }
    
    /**
     * Convierte una lista de tokens en una expresión string evaluable
     * Ejemplo: [APERTURA_SUMA, NUMERO(5), NUMERO(3), CIERRE] → "sum(5,3)"
     */
    private String construirExpresion(ArrayList<Token> operacion){
        StringBuilder sb = new StringBuilder();
        
        for(Token token : operacion){
            if(token.getTokens() == Token.Tokencitos.APERTURA_OPERACION){
                sb.append(convertirOperador(token.getOperador())).append("(");
            } else if(token.getTokens() == Token.Tokencitos.CIERRE_OPERACION){
                sb.append(")");
            } else if(token.getTokens() == Token.Tokencitos.NUMERO){
                // Validar que no sea número vacío
                if(token.getValor().trim().isEmpty()){
                    throw new IllegalArgumentException("Número vacío detectado");
                }
                sb.append(token.getValor()).append(",");
            }
        }
        
        return sb.toString().replaceAll(",\\)", ")");
    }
    
    /**
     * Convierte nombres de operadores a formato de función
     * Esto facilita la evaluación posterior de la expresión
     */
    private String convertirOperador(String operador){
        switch(operador){
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
    
    /**
     * Calcula el resultado de una expresión respetando la jerarquía:
     * Inverso > Potencia-Raíz > Multiplicación-División > Suma-Resta
     */
    private float calcularOperacion(String expresion){
        return evaluarExpresion(expresion);
    }
    
    /**
     * Evalúa recursivamente una expresión desde las operaciones más internas
     */
    private float evaluarExpresion(String expr){
        // Si es solo un número, retornarlo
        if(esNumero(expr)){
            return Float.parseFloat(expr);
        }
        
        // Encontrar la operación principal (más externa)
        int inicio = expr.indexOf('(');
        int fin = expr.lastIndexOf(')');
        
        // Validar que la expresión tenga formato válido
        if(inicio == -1 || fin == -1 || fin <= inicio){
            throw new IllegalArgumentException("Operador no reconocido en la expresión");
        }
        
        String operacion = expr.substring(0, inicio);
        String contenido = expr.substring(inicio + 1, fin);
        
        // Separar operandos
        ArrayList<Float> operandos = obtenerOperandos(contenido);
        
        // Ejecutar operación según tipo
        return ejecutarOperacion(operacion, operandos);
    }
    
    /**
     * Verifica si una cadena es un número
     */
    private boolean esNumero(String str){
        try {
            Float.parseFloat(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Extrae y evalúa operandos de una expresión
     */
    private ArrayList<Float> obtenerOperandos(String contenido){
        ArrayList<Float> operandos = new ArrayList<>();
        StringBuilder operandoActual = new StringBuilder();
        int nivelParentesis = 0;
        
        for(int i = 0; i < contenido.length(); i++){
            char c = contenido.charAt(i);
            
            if(c == '('){
                nivelParentesis++;
                operandoActual.append(c);
            } else if(c == ')'){
                nivelParentesis--;
                operandoActual.append(c);
            } else if(c == ',' && nivelParentesis == 0){
                // Fin de operando
                String op = operandoActual.toString().trim();
                operandos.add(evaluarExpresion(op));
                operandoActual.setLength(0);
            } else {
                operandoActual.append(c);
            }
        }
        
        // Agregar último operando
        if(operandoActual.length() > 0){
            String op = operandoActual.toString().trim();
            operandos.add(evaluarExpresion(op));
        }
        
        return operandos;
    }
    
    /**
     * Ejecuta la operación matemática específica
     */
    private float ejecutarOperacion(String operacion, ArrayList<Float> operandos){
        switch(operacion){
            case "sum":
                return operandos.stream().reduce(0f, Float::sum);
            case "sub":
                float resultado = operandos.get(0);
                for(int i = 1; i < operandos.size(); i++){
                    resultado -= operandos.get(i);
                }
                return resultado;
            case "mul":
                return operandos.stream().reduce(1f, (a, b) -> a * b);
            case "div":
                resultado = operandos.get(0);
                for(int i = 1; i < operandos.size(); i++){
                    if(operandos.get(i) == 0.0f){
                        throw new ArithmeticException("División por cero");
                    }
                    resultado /= operandos.get(i);
                }
                return resultado;
            case "pow":
                if(operandos.size() >= 2){
                    float exponente = operandos.get(0);
                    float base = operandos.get(1);
                    return (float) Math.pow(base, exponente);
                }
                return 0f;
            case "sqrt":
                if(operandos.size() == 1){
                    return (float) Math.sqrt(operandos.get(0));
                } else {
                    float indice = operandos.get(0);
                    float radicando = operandos.get(1);
                    return (float) Math.pow(radicando, 1.0f / indice);
                }
            case "inv":
                if(operandos.get(0) == 0.0f){
                    throw new ArithmeticException("Inverso de cero");
                }
                return 1f / operandos.get(0);
            case "mod":
                resultado = operandos.get(0);
                for(int i = 1; i < operandos.size(); i++){
                    if(operandos.get(i) == 0.0f){
                        throw new ArithmeticException("Módulo por cero");
                    }
                    resultado %= operandos.get(i);
                }
                return resultado;
            default:
                return 0f;
        }
    }

    private void printES(){

        System.out.println("----------------ERRORES SINTACTICOS/SEMANTICOS-------------");
        for(AES error : erroresSintacticos){
            System.out.println(error.toString());
        }
        System.out.println("-----------------------------------------------\n");
    }

    private void printResultados(){
        System.out.println("----------------RESULTADOS----------------------");
        if(opRe.isEmpty()){
            System.out.println("No hay operaciones válidas para mostrar resultados."); //Se estaban duplicando algunas cosas
        } else {
            java.util.Set<String> operacionesMostradas = new java.util.HashSet<>();
            for(HashMap.Entry<String, Float> entry : opRe.entrySet()){
                String operacion = entry.getKey();
                if(!operacionesMostradas.contains(operacion)){
                    System.out.println("Operación - " + operacion + ", Resultado: " + entry.getValue());
                    operacionesMostradas.add(operacion);
                }
            }
        }
        System.out.println("-----------------------------------------------\n");
    }
}