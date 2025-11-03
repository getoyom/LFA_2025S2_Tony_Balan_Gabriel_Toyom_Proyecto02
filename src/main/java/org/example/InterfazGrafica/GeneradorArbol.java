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
    private static final int ANCHO_NODO = 100;  // Tamaño de los nodos ajustado para mayor claridad
    private static final int ALTO_NODO = 50;  // Tamaño de los nodos ajustado para mayor claridad
    private static final int ESPACIO_HORIZONTAL = 100;  // Espacio horizontal más grande para mejorar distribución
    private static final int ESPACIO_VERTICAL = 80;  // Menos espacio vertical para evitar que el árbol se estire demasiado
    private static final int ESPACIO_ENTRE_ARBOLES = 120;  // Ajustado para separar los árboles de manera adecuada
    private static final Color COLOR_OPERACION = new Color(102, 126, 234);
    private static final Color COLOR_NUMERO = new Color(39, 174, 96);
    private static final Color COLOR_ESPECIAL = new Color(230, 126, 34);

    // Tamaño de la fuente para los diferentes tipos de nodos
    private static final int TAM_FUENTE_NUMERO = 10;  // Fuente más pequeña para los números
    private static final int TAM_FUENTE_OPERACION = 12;  // Fuente para las operaciones
    private static final int TAM_FUENTE_ESPECIAL = 10;  // Fuente para exponentes y raíces

    /**
     * Clase interna para almacenar información de un nodo del árbol
     */
    private static class NodoArbol {
        String valor;
        String tipo;
        ArrayList<NodoArbol> hijos;

        NodoArbol(String valor, String tipo) {
            this.valor = valor;
            this.tipo = tipo;
            this.hijos = new ArrayList<>();
        }

        void agregarHijo(NodoArbol hijo) {
            hijos.add(hijo);
        }
    }

    /**
     * Genera el árbol de jerarquía para todas las operaciones válidas, cada una como un árbol independiente
     * @param operacionesValidas Lista de operaciones a visualizar
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

        // Calcular dimensiones totales
        int anchoMaximo = 0;
        int altoTotal = 0;
        for (NodoArbol arbol : arboles) {
            int anchoArbol = calcularAnchoArbol(arbol) * (ANCHO_NODO + ESPACIO_HORIZONTAL);
            anchoMaximo = Math.max(anchoMaximo, anchoArbol);
            int altoArbol = calcularAltoArbol(arbol) * (ALTO_NODO + ESPACIO_VERTICAL);
            altoTotal += altoArbol + ESPACIO_ENTRE_ARBOLES;
        }

        // Asegurar dimensiones mínimas
        anchoMaximo = Math.max(anchoMaximo, 900);  // Ajustado para un tamaño más amplio
        altoTotal = Math.max(altoTotal, 800);  // Ajustado para mayor altura

        // Crear imagen y dibujar árboles
        BufferedImage imagen = new BufferedImage(anchoMaximo, altoTotal, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagen.createGraphics();

        // Configurar calidad de renderizado
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Fondo blanco
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, anchoMaximo, altoTotal);

        // Dibujar cada árbol
        int posicionYActual = 100;  // Ajustado para comenzar más abajo
        for (NodoArbol arbol : arboles) {
            dibujarArbol(g2d, arbol, anchoMaximo / 2, posicionYActual, anchoMaximo / 2);
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

        // Encontrar el token de apertura de operación principal
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

        // Crear nodo raíz de la operación
        NodoArbol raiz = new NodoArbol(tokenPrincipal.getOperador(), "operacion");

        // Construir sub-árbol recursivamente
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
                        // Sub-operación anidada
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
                        // Número directo de esta operación
                        NodoArbol nodoNumero = new NodoArbol(token.getValor(), "numero");
                        nodoActual.agregarHijo(nodoNumero);
                    }
                    break;

                case APERTURA_POTENCIA:
                    if (nivel == 1) {
                        NodoArbol nodoPotencia = new NodoArbol("EXPONENTE", "especial");
                        nodoActual.agregarHijo(nodoPotencia);
                        // Evitar agregar el mismo exponente de nuevo
                        if (i + 1 < tokens.size() && tokens.get(i + 1).getTokens() == Token.Tokencitos.NUMERO) {
                            NodoArbol nodoValor = new NodoArbol(tokens.get(i + 1).getValor(), "numero");
                            nodoPotencia.agregarHijo(nodoValor);
                            i++; // Avanzar el índice para evitar que el número se procese más tarde
                        }
                    }
                    break;

                case APERTURA_RAIZ:
                    if (nivel == 1) {
                        NodoArbol nodoRaiz = new NodoArbol("INDICE", "especial");
                        nodoActual.agregarHijo(nodoRaiz);
                        // Evitar agregar el mismo índice de nuevo
                        if (i + 1 < tokens.size() && tokens.get(i + 1).getTokens() == Token.Tokencitos.NUMERO) {
                            // Aquí solo agregamos el valor "5" una vez como parte del "INDICE"
                            NodoArbol nodoValor = new NodoArbol(tokens.get(i + 1).getValor(), "numero");
                            nodoRaiz.agregarHijo(nodoValor);
                            i++; // Avanzar el índice para evitar que el número se procese más tarde
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
     * Calcula el ancho necesario para dibujar el árbol
     */
    private int calcularAnchoArbol(NodoArbol nodo) {
        if (nodo.hijos.isEmpty()) {
            return 1;
        }

        int anchoTotal = 0;
        for (NodoArbol hijo : nodo.hijos) {
            anchoTotal += calcularAnchoArbol(hijo);
        }

        return Math.max(1, anchoTotal);
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
     * Dibuja el árbol de forma recursiva en el lienzo
     */
    private void dibujarArbol(Graphics2D g2d, NodoArbol nodo, int x, int y, int espacioDisponible) {
        // Dibujar el nodo actual
        dibujarNodo(g2d, nodo, x, y);

        // Dibujar hijos
        if (!nodo.hijos.isEmpty()) {
            int numHijos = nodo.hijos.size();
            int espacioPorHijo = espacioDisponible / numHijos;
            int inicioX = x - espacioDisponible / 2 + espacioPorHijo / 2;

            for (int i = 0; i < numHijos; i++) {
                NodoArbol hijo = nodo.hijos.get(i);
                int hijoX = inicioX + (i * espacioPorHijo);
                int hijoY = y + ESPACIO_VERTICAL;

                // Dibujar línea de conexión
                g2d.setColor(new Color(200, 200, 200));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(x, y + ALTO_NODO / 2, hijoX, hijoY - ALTO_NODO / 2);

                // Dibujar hijo recursivamente
                dibujarArbol(g2d, hijo, hijoX, hijoY, espacioPorHijo);
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
                g2d.setFont(new Font("Arial", Font.BOLD, TAM_FUENTE_OPERACION));  // Título de la operación
                break;
            case "numero":
                colorFondo = COLOR_NUMERO;
                g2d.setFont(new Font("Arial", Font.BOLD, TAM_FUENTE_NUMERO));  // Números más pequeños
                break;
            case "especial":
                colorFondo = COLOR_ESPECIAL;
                g2d.setFont(new Font("Arial", Font.BOLD, TAM_FUENTE_ESPECIAL));  // Exponentes y raíces más pequeños
                break;
            default:
                colorFondo = new Color(149, 165, 166);
                g2d.setFont(new Font("Arial", Font.BOLD, 11));
                break;
        }

        // Dibujar sombra
        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.fillRoundRect(nodoCentroX + 3, nodoCentroY + 3, ANCHO_NODO, ALTO_NODO, 15, 15);

        // Dibujar fondo del nodo
        g2d.setColor(colorFondo);
        g2d.fillRoundRect(nodoCentroX, nodoCentroY, ANCHO_NODO, ALTO_NODO, 15, 15);

        // Dibujar borde
        g2d.setColor(colorFondo.darker());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(nodoCentroX, nodoCentroY, ANCHO_NODO, ALTO_NODO, 15, 15);

        // Dibujar texto
        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();
        String texto = nodo.valor;

        // Truncar texto si es muy largo
        if (fm.stringWidth(texto) > ANCHO_NODO - 10) {
            while (fm.stringWidth(texto + "...") > ANCHO_NODO - 10 && !texto.isEmpty()) {
                texto = texto.substring(0, texto.length() - 1);
            }
            texto += "...";
        }

        int textoX = x - fm.stringWidth(texto) / 2;
        int textoY = y + fm.getAscent() / 2 - 2;

        g2d.drawString(texto, textoX, textoY);
    }
}
