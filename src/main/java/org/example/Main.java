package org.example;

import org.example.InterfazGrafica.*;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/*Clase principal para ejecutar la aplicacion con interfaz grafica.*/
public class Main {

    /* Punto de entrada principal de la aplicacion.*/
    public static void main(String[] args) {
        configurarLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            try {
                new InterfazGrafica();
            } catch (Exception e) {
                System.err.println("Error al iniciar la interfaz grafica: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /* Configura el look and feel de la aplicacion.*/
    private static void configurarLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.out.println("Look and Feel configurado: " + UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No se pudo establecer el Look and Feel del sistema, usando predeterminado.");
            try {
                // Intenta usar Nimbus si no se puede usar el look and feel del sistema
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                System.out.println("Look and Feel configurado: Nimbus");
            } catch (Exception ex) {
                //Usar el LookAndFeel predeterminado si falla Nimbus
            }
        }
    }
}
