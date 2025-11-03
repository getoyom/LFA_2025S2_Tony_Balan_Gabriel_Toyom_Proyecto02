package org.example.InterfazGrafica;

import org.example.Lexer.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/**
 * Clase encargada de generar un arbol de jerarquia visual de las operaciones aritmeticas
 * El arbol se representa como un grafico PNG que muestra la estructura de las operaciones
 */
public class GeneradorArbol {

    private static final String ARCHIVO_SALIDA = "ArbolJerarquia.png";
    private static final int ANCHO_NODO = 120;  // Aumentado
    private static final int ALTO_NODO = 50;    // Aumentado
    private static final int ESPACIO_HORIZONTAL = 40;  // Más espacio
    private static final int ESPACIO_VERTICAL = 100;   // Más espacio vertical
    private static final int ESPACIO_ENTRE_ARBOLES = 150;  // Mayor separación entre árboles
    private static final int PADDING_HORIZONTAL = 80;
    private static final int PADDING_VERTICAL = 100;    // Padding superior e inferior
    private static final Color COLOR_OPERACION = new Color(102, 126, 234);
    private static final Color COLOR_NUMERO = new Color(39, 174, 96);
    private static final Color COLOR_ESPECIAL = new Color(230, 126, 34);

    private static final int TAMAÑO_FUENTE_NUMERO = 12;      // Aumentado
    private static final int TAMAÑO_FUENTE_OPERACION = 14;   // Aumentado
    private static final int TAMAÑO_FUENTE_ESPECIAL = 11;    // Aumentado

    /**
     * Clase interna para almacenar información de un nodo del árbol
     */
    private static class NodoArbol {
        String valor;
        String tipo;
        ArrayList<NodoArbol> hijos;
        int anchoCalculado;

        NodoArbol(String valor, String tipo) {
            this.valor = valor;
            this.tipo = tipo;
            this.hijos = new ArrayList<>();
            this.anchoCalculado = 0;
        }

        void agregarHijo(NodoArbol hijo) {
            hijos.add(hijo);
        }
    }

    /**
     * Genera el árbol de jerarquía para todas las operaciones válidas
     */
    public void generarArbolJerarquia(ArrayList<ArrayList<Token>> operacionesValidas) throws IOException {
        if (operacionesValidas.isEmpty()) {
            return;
        }

        // Construir árboles para cada operación
        ArrayList<NodoArbol> arboles = new ArrayList<>();
        int numeroOperacion = 1;
        for (ArrayList<Token> operacion : operacionesValidas) {
            NodoArbol nodoOperacion = construirArbolOperacion(operacion);
            if (nodoOperacion != null) {
                nodoOperacion.valor = "OP-" + numeroOperacion + ": " + nodoOperacion.valor;
                arboles.add(nodoOperacion);
                numeroOperacion++;
            }
        }

        // Calcular anchos reales para cada árbol
        for (NodoArbol arbol : arboles) {
            calcularAnchosReales(arbol);
        }

        // Calcular dimensiones totales
        int anchoMaximo = 0;
        int altoTotal = PADDING_VERTICAL;  // Padding inicial

        for (NodoArbol arbol : arboles) {
            int anchoArbol = arbol.anchoCalculado + PADDING_HORIZONTAL * 2;
            anchoMaximo = Math.max(anchoMaximo, anchoArbol);
            int altoArbol = calcularAltoArbol(arbol) * (ALTO_NODO + ESPACIO_VERTICAL);
            altoTotal += altoArbol + ESPACIO_ENTRE_ARBOLES;
        }

        altoTotal += PADDING_VERTICAL;  // Padding final

        // Dimensiones mínimas más grandes
        anchoMaximo = Math.max(anchoMaximo, 1200);
        altoTotal = Math.max(altoTotal, 800);

        // Crear imagen y dibujar árboles
        BufferedImage imagen = new BufferedImage(anchoMaximo, altoTotal, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagen.createGraphics();

        // Configurar calidad de renderizado
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Fondo blanco
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, anchoMaximo, altoTotal);

        // Dibujar cada árbol
        int posicionYActual = PADDING_VERTICAL;
        for (NodoArbol arbol : arboles) {
            dibujarArbolOptimizado(g2d, arbol, anchoMaximo / 2, posicionYActual);
            int altoArbol = calcularAltoArbol(arbol) * (ALTO_NODO + ESPACIO_VERTICAL);
            posicionYActual += altoArbol + ESPACIO_ENTRE_ARBOLES;
        }

        g2d.dispose();

        // Guardar imagen
        ImageIO.write(imagen, "PNG", new File(ARCHIVO_SALIDA));
    }

    /**
     * Construye el árbol de una operación a partir de sus tokens
     */
    private NodoArbol construirArbolOperacion(ArrayList<Token> tokens) {
        if (tokens.isEmpty()) {
            return null;
        }

        Token tokenPrincipal = null;
        for (Token token : tokens) {
            if (token.getTokens() == Token.Tokencitos.APERTURA_OPERACION) {
                tokenPrincipal = token;
                break;
            }
        }

        if (tokenPrincipal == null) {
            return null;
        }

        NodoArbol raiz = new NodoArbol(tokenPrincipal.getOperador(), "operacion");
        construirSubArbol(raiz, tokens, 0);

        return raiz;
    }

    /**
     * Construye recursivamente el sub-árbol de una operación
     */
    private int construirSubArbol(NodoArbol nodoActual, ArrayList<Token> tokens, int indice) {
        int nivel = 0;
        int i = indice;

        while (i < tokens.size()) {
            Token token = tokens.get(i);

            switch (token.getTokens()) {
                case APERTURA_OPERACION:
                    if (nivel > 0) {
                        NodoArbol nodoHijo = new NodoArbol(token.getOperador(), "operacion");
                        nodoActual.agregarHijo(nodoHijo);
                        i = construirSubArbol(nodoHijo, tokens, i);
                    }
                    nivel++;
                    break;

                case CIERRE_OPERACION:
                    nivel--;
                    if (nivel == 0) {
                        return i;
                    }
                    break;

                case NUMERO:
                    if (nivel == 1) {
                        NodoArbol nodoNumero = new NodoArbol(token.getValor(), "numero");
                        nodoActual.agregarHijo(nodoNumero);
                    }
                    break;

                case APERTURA_POTENCIA:
                    if (nivel == 1) {
                        NodoArbol nodoPotencia = new NodoArbol("EXPONENTE", "especial");
                        nodoActual.agregarHijo(nodoPotencia);
                        if (i + 1 < tokens.size() && tokens.get(i + 1).getTokens() == Token.Tokencitos.NUMERO) {
                            NodoArbol nodoValor = new NodoArbol(tokens.get(i + 1).getValor(), "numero");
                            nodoPotencia.agregarHijo(nodoValor);
                            i++;
                        }
                    }
                    break;

                case APERTURA_RAIZ:
                    if (nivel == 1) {
                        NodoArbol nodoRaiz = new NodoArbol("INDICE", "especial");
                        nodoActual.agregarHijo(nodoRaiz);
                        if (i + 1 < tokens.size() && tokens.get(i + 1).getTokens() == Token.Tokencitos.NUMERO) {
                            NodoArbol nodoValor = new NodoArbol(tokens.get(i + 1).getValor(), "numero");
                            nodoRaiz.agregarHijo(nodoValor);
                            i++;
                        }
                    }
                    break;

                default:
                    break;
            }

            i++;
        }

        return i;
    }

    /**
     * Calcula los anchos reales que necesita cada subárbol
     */
    private int calcularAnchosReales(NodoArbol nodo) {
        if (nodo.hijos.isEmpty()) {
            nodo.anchoCalculado = ANCHO_NODO;
            return ANCHO_NODO;
        }

        int anchoTotal = 0;
        for (NodoArbol hijo : nodo.hijos) {
            anchoTotal += calcularAnchosReales(hijo);
        }

        // Agregar espacio entre hijos
        anchoTotal += (nodo.hijos.size() - 1) * ESPACIO_HORIZONTAL;

        // El ancho del nodo es el máximo entre su ancho propio y el de sus hijos
        nodo.anchoCalculado = Math.max(ANCHO_NODO, anchoTotal);

        return nodo.anchoCalculado;
    }

    /**
     * Calcula la altura necesaria para dibujar el árbol
     */
    private int calcularAltoArbol(NodoArbol nodo) {
        if (nodo.hijos.isEmpty()) {
            return 1;
        }

        int alturaMaxima = 0;
        for (NodoArbol hijo : nodo.hijos) {
            int alturaHijo = calcularAltoArbol(hijo);
            alturaMaxima = Math.max(alturaMaxima, alturaHijo);
        }

        return alturaMaxima + 1;
    }

    /**
     * Dibuja el árbol de forma optimizada usando los anchos calculados
     */
    private void dibujarArbolOptimizado(Graphics2D g2d, NodoArbol nodo, int x, int y) {
        // Dibujar el nodo actual
        dibujarNodo(g2d, nodo, x, y);

        // Dibujar hijos
        if (!nodo.hijos.isEmpty()) {
            // Calcular el ancho total de los hijos
            int anchoTotalHijos = 0;
            for (NodoArbol hijo : nodo.hijos) {
                anchoTotalHijos += hijo.anchoCalculado;
            }
            anchoTotalHijos += (nodo.hijos.size() - 1) * ESPACIO_HORIZONTAL;

            // Posición inicial para el primer hijo
            int inicioX = x - anchoTotalHijos / 2;
            int posicionX = inicioX;

            for (NodoArbol hijo : nodo.hijos) {
                // Calcular posición centrada del hijo en su espacio asignado
                int hijoX = posicionX + hijo.anchoCalculado / 2;
                int hijoY = y + ESPACIO_VERTICAL;

                // Dibujar línea de conexión más gruesa
                g2d.setColor(new Color(160, 160, 160));
                g2d.setStroke(new BasicStroke(2.5f));
                g2d.drawLine(x, y + ALTO_NODO / 2, hijoX, hijoY - ALTO_NODO / 2);

                // Dibujar hijo recursivamente
                dibujarArbolOptimizado(g2d, hijo, hijoX, hijoY);

                // Avanzar a la siguiente posición
                posicionX += hijo.anchoCalculado + ESPACIO_HORIZONTAL;
            }
        }
    }

    /**
     * Dibuja un nodo individual con su estilo correspondiente
     */
    private void dibujarNodo(Graphics2D g2d, NodoArbol nodo, int x, int y) {
        int nodoCentroX = x - ANCHO_NODO / 2;
        int nodoCentroY = y - ALTO_NODO / 2;

        // Determinar color según tipo
        Color colorFondo;
        switch (nodo.tipo) {
            case "operacion":
                colorFondo = COLOR_OPERACION;
                g2d.setFont(new Font("Arial", Font.BOLD, TAMAÑO_FUENTE_OPERACION));
                break;
            case "numero":
                colorFondo = COLOR_NUMERO;
                g2d.setFont(new Font("Arial", Font.BOLD, TAMAÑO_FUENTE_NUMERO));
                break;
            case "especial":
                colorFondo = COLOR_ESPECIAL;
                g2d.setFont(new Font("Arial", Font.BOLD, TAMAÑO_FUENTE_ESPECIAL));
                break;
            default:
                colorFondo = new Color(149, 165, 166);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                break;
        }

        // Dibujar sombra más pronunciada
        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.fillRoundRect(nodoCentroX + 4, nodoCentroY + 4, ANCHO_NODO, ALTO_NODO, 15, 15);

        // Dibujar fondo del nodo
        g2d.setColor(colorFondo);
        g2d.fillRoundRect(nodoCentroX, nodoCentroY, ANCHO_NODO, ALTO_NODO, 15, 15);

        // Dibujar borde más grueso
        g2d.setColor(colorFondo.darker());
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawRoundRect(nodoCentroX, nodoCentroY, ANCHO_NODO, ALTO_NODO, 15, 15);

        // Dibujar texto
        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();
        String texto = nodo.valor;

        // Truncar texto si es muy largo
        if (fm.stringWidth(texto) > ANCHO_NODO - 15) {
            while (fm.stringWidth(texto + "...") > ANCHO_NODO - 15 && texto.length() > 0) {
                texto = texto.substring(0, texto.length() - 1);
            }
            texto += "...";
        }

        int textoX = x - fm.stringWidth(texto) / 2;
        int textoY = y + fm.getAscent() / 2 - 2;

        g2d.drawString(texto, textoX, textoY);
    }
}