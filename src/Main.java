import java.util.ArrayList;

public class Main {

    private final static Scanner scanner = new Scanner();
    private final static sintactico filtro = new sintactico();

    public static void main(String[] args) throws Exception {

        String testInput = "Entrada2.txt";
        scanner.scanFile(testInput);
        System.out.println("\n------ACA EMPIEZA LO NUEVOOOOOOOO------------\n");
        
        // Filtrar solo tokens válidos (sin errores léxicos)
        ArrayList<Token> tokensValidos = new ArrayList<>();
        for(Token token : scanner.getTokens()){
            // Solo agregar tokens que no sean de etiquetas problemáticas
            if(token.getTokens() == Token.Tokencitos.APERTURA_OPERACION ||
               token.getTokens() == Token.Tokencitos.CIERRE_OPERACION ||
               token.getTokens() == Token.Tokencitos.APERTURA_NUMERO ||
               token.getTokens() == Token.Tokencitos.CIERRE_NUMERO ||
               token.getTokens() == Token.Tokencitos.APERTURA_POTENCIA ||
               token.getTokens() == Token.Tokencitos.CIERRE_POTENCIA ||
               token.getTokens() == Token.Tokencitos.APERTURA_RAIZ ||
               token.getTokens() == Token.Tokencitos.CIERRE_RAIZ ||
               token.getTokens() == Token.Tokencitos.NUMERO ||
               token.getTokens() == Token.Tokencitos.NOMBRE_OPERACION){
                tokensValidos.add(token);
            }
        }
        
        filtro.filtrarTokens(tokensValidos);
    }
}