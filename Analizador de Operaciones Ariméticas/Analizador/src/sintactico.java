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
        
        for(Token tokensito: filtro1){
            // Agregar todos los tokens al bloque actual
            bloqueActual.add(tokensito);
            
            if(tokensito.getTokens() == Token.Tokencitos.APERTURA_OPERACION){
                // Verificar si el operador es válido antes de procesar
                if(!verificarOperador(tokensito.getOperador())){
                    // Descartar todo el bloque actual si hay operador inválido
                    bloqueActual.clear();
                    continue;
                }
                // Empujar token de apertura a la pila de balanceo
                pila.push(tokensito);
                // Inicializar contador de operandos para este nivel
                contadorOperandos.push(0); 
                // Incrementar nivel de anidación
                nivelAnidacion++;
            } else if(tokensito.getTokens() == Token.Tokencitos.NUMERO){
                // Incrementar contador de operandos del nivel actual
                if(!contadorOperandos.isEmpty()){
                    int operandos = contadorOperandos.pop();
                    contadorOperandos.push(operandos + 1);
                }
            } else if(tokensito.getTokens() == Token.Tokencitos.CIERRE_OPERACION){
                if(pila.isEmpty()){
                    // Error: cierre sin apertura correspondiente
                    erroresSintacticos.add(new AES(tokensito.getLinea(), tokensito.getValor(), "Error de sintaxis", "Etiqueta de cierre sin apertura correspondiente"));
                } else {
                    // Obtener token de apertura y contador de operandos
                    Token apertura = pila.pop();
                    int operandos = contadorOperandos.pop();
                    
                    // Validar que tenga al menos 2 operandos
                    if(operandos < 2){
                        erroresSintacticos.add(new AES(apertura.getLinea(), apertura.getValor(), "Error semántico", "La operación debe tener al menos 2 operandos, encontrados: " + operandos));
                    }
                    
                    // Decrementar nivel de anidación
                    nivelAnidacion--;
                    
                    // Si hay un nivel superior, contar esta operación como 1 operando
                    if(!contadorOperandos.isEmpty()){
                        int operandosSuperiores = contadorOperandos.pop();
                        contadorOperandos.push(operandosSuperiores + 1);
                    }
                    
                    // Si volvemos al nivel 0, completamos una operación principal
                    if(nivelAnidacion == 0){
                        filtro2.add(new ArrayList<>(bloqueActual));
                        bloqueActual.clear();
                    }
                }
            }
        }
        
        // Agregar el último bloque si existe
        if(!bloqueActual.isEmpty()){
            filtro2.add(new ArrayList<>(bloqueActual));
        }
        
        // Verificar que todas las etiquetas estén cerradas
        if (!pila.isEmpty()) {
            while(!pila.isEmpty()){
                Token tokenMalo = pila.pop();
                erroresSintacticos.add(new AES(tokenMalo.getLinea(), tokenMalo.getValor(), "Error de sintaxis", "Etiquetas de operación sin cerrar"));
            }
        }
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
     * Usa una pila para rastrear qué operador está activo para cada número
     * Si cualquier número es inválido, descarta toda la operación
     */
    private boolean validarOperacion(ArrayList<Token> operacion){
        Stack<Token> operadores = new Stack<>();  // Pila de operadores activos
        
        for(Token token : operacion){
            if(token.getTokens() == Token.Tokencitos.APERTURA_OPERACION){
                // Nuevo operador activo - agregarlo a la pila
                operadores.push(token);
            } else if(token.getTokens() == Token.Tokencitos.CIERRE_OPERACION){
                // Operación terminada - quitar operador de la pila
                if(!operadores.isEmpty()){
                    operadores.pop();
                }
            } else if(token.getTokens() == Token.Tokencitos.NUMERO){
                // Validar número contra el operador activo actual
                if(!operadores.isEmpty()){
                    Token operador = operadores.peek();  // Operador más reciente
                    if(!validarNumero(token, operador)){
                        return false; // UN error invalida TODA la operación
                    }
                }
            }
            // Ignorar otros tipos de tokens (APERTURA_NUMERO, etc.)
        }
        return true;  // Todos los números son válidos
    }
    
    /**
     * Valida un número específico según las restricciones del operador activo
     * Retorna false si el número viola alguna restricción matemática
     */
    private boolean validarNumero(Token numero, Token operador){
        String valor = numero.getValor();
        String op = operador.getOperador();
        
        switch(op){
            case "DIVISION":
            case "MOD":
                // No se puede dividir entre 0 ni hacer módulo de 0
                if(valor.equals("0")){
                    erroresSintacticos.add(new AES(numero.getLinea(), valor, "Error semántico", op + " entre 0"));
                    return false;
                }
                break;
            case "INVERSO":
                // No se puede calcular 1/0
                if(valor.equals("0")){
                    erroresSintacticos.add(new AES(numero.getLinea(), valor, "Error semántico", "Inverso de 0"));
                    return false;
                }
                break;
            case "RAIZ":
                // No se puede calcular raíz de números negativos
                if(valor.startsWith("-")){
                    erroresSintacticos.add(new AES(numero.getLinea(), valor, "Error semántico", "Raíz de número negativo"));
                    return false;
                }
                break;
            // SUMA, RESTA, MULTIPLICACION, POTENCIA no tienen restricciones
        }
        return true;  // Número válido para este operador
    }
    
    /**
     * Convierte una lista de tokens en una expresión string evaluable
     * Ejemplo: [APERTURA_SUMA, NUMERO(5), NUMERO(3), CIERRE] → "sum(5,3)"
     */
    private String construirExpresion(ArrayList<Token> operacion){
        StringBuilder sb = new StringBuilder();
        
        for(Token token : operacion){
            if(token.getTokens() == Token.Tokencitos.APERTURA_OPERACION){
                // Convertir operador y abrir paréntesis: "SUMA" → "sum("
                sb.append(convertirOperador(token.getOperador())).append("(");
            } else if(token.getTokens() == Token.Tokencitos.CIERRE_OPERACION){
                // Cerrar paréntesis de la operación
                sb.append(")");
            } else if(token.getTokens() == Token.Tokencitos.NUMERO){
                // Agregar número seguido de coma: "5,"
                sb.append(token.getValor()).append(",");
            }
            // Ignorar APERTURA_NUMERO, CIERRE_NUMERO, etc.
        }
        
        // Limpiar comas antes de paréntesis de cierre: "sum(5,3,)" → "sum(5,3)"
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
                    resultado /= operandos.get(i);
                }
                return resultado;
            case "pow":
                resultado = operandos.get(0);
                for(int i = 1; i < operandos.size(); i++){
                    resultado = (float) Math.pow(resultado, operandos.get(i));
                }
                return resultado;
            case "sqrt":
                return (float) Math.sqrt(operandos.get(0));
            case "inv":
                return 1f / operandos.get(0);
            case "mod":
                resultado = operandos.get(0);
                for(int i = 1; i < operandos.size(); i++){
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
        for(HashMap.Entry<String, Float> entry : opRe.entrySet()){
            System.out.println("Operación - " + entry.getKey() + ", Resultado: " + entry.getValue());
        }
        System.out.println("-----------------------------------------------\n");
    }
}