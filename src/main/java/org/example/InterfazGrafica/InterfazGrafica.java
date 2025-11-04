package org.example.InterfazGrafica;

import org.example.Lexer.Scanner;
import org.example.Parser.Sintactico;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/*Interfaz grafica principal del Analizador de Operaciones Aritmeticas
 Proporciona funcionalidad para cargar, editar, analizar archivos y generar reportes*/
public class InterfazGrafica extends JFrame {

    /*Componentes principales*/
    private JTextArea areaTexto;
    private File archivoActual;

    /*Modulos de analisis*/
    private Scanner scanner;
    private Sintactico analizadorSintactico;

    /*Generadores de reportes*/
    private GeneradorHTML generadorHTML;
    private GeneradorArbol generadorArbol;

    // Constantes de interfaz
    private static final String TITULO_APLICACION = "Analizador de Operaciones Aritmeticas - LFYA";
    private static final int ANCHO_VENTANA = 900;
    private static final int ALTO_VENTANA = 700;

    /* Constructor principal de la interfaz grafica */
    public InterfazGrafica() {
        inicializarComponentes();
        configurarVentana();
        crearMenuBar();
        crearAreaTexto();

        setVisible(true);
    }

    /* Inicializa los componentes y modulos del analizador */
    private void inicializarComponentes() {
        scanner = new Scanner();
        analizadorSintactico = new Sintactico();
        generadorHTML = new GeneradorHTML();
        generadorArbol = new GeneradorArbol();
        archivoActual = null;
    }

    /*Configura las propiedades principales de la ventana*/
    private void configurarVentana() {
        setTitle(TITULO_APLICACION);
        setSize(ANCHO_VENTANA, ALTO_VENTANA);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    /*Crea la barra de menu con todas las opciones disponibles*/
    private void crearMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        /*Menu Archivo*/
        JMenu menuArchivo = new JMenu("Archivo");
        menuArchivo.setMnemonic(KeyEvent.VK_A);

        JMenuItem itemAbrir = crearMenuItem("Abrir", KeyEvent.VK_A,
                KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        itemAbrir.addActionListener(e -> abrirArchivo());

        JMenuItem itemGuardar = crearMenuItem("Guardar", KeyEvent.VK_G,
                KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        itemGuardar.addActionListener(e -> guardarArchivo());

        JMenuItem itemGuardarComo = crearMenuItem("Guardar Como...", KeyEvent.VK_C,
                KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        itemGuardarComo.addActionListener(e -> guardarArchivoComo());

        JMenuItem itemSalir = crearMenuItem("Salir", KeyEvent.VK_S,
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        itemSalir.addActionListener(e -> salirAplicacion());

        menuArchivo.add(itemAbrir);
        menuArchivo.add(itemGuardar);
        menuArchivo.add(itemGuardarComo);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);

        /* Menu Analisis*/
        JMenu menuAnalisis = new JMenu("Analisis");
        menuAnalisis.setMnemonic(KeyEvent.VK_N);

        JMenuItem itemAnalizar = crearMenuItem("Analizar", KeyEvent.VK_A,
                KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        itemAnalizar.addActionListener(e -> analizarTexto());

        menuAnalisis.add(itemAnalizar);

        /*Menu Ayuda*/
        JMenu menuAyuda = new JMenu("Ayuda");
        menuAyuda.setMnemonic(KeyEvent.VK_Y);

        JMenuItem itemManualUsuario = crearMenuItem("Manual de Usuario", KeyEvent.VK_U, null);
        itemManualUsuario.addActionListener(e -> abrirManual("Manual de Usuario.pdf"));

        JMenuItem itemManualTecnico = crearMenuItem("Manual Tecnico", KeyEvent.VK_T, null);
        itemManualTecnico.addActionListener(e -> abrirManual("ManualTecnico.pdf"));

        menuAyuda.add(itemManualUsuario);
        menuAyuda.add(itemManualTecnico);
        menuAyuda.addSeparator();


        /*Agregar menus a la barra*/
        menuBar.add(menuArchivo);
        menuBar.add(menuAnalisis);
        menuBar.add(menuAyuda);

        setJMenuBar(menuBar);
    }

    /*Crea un item de menu con sus propiedades*/
    private JMenuItem crearMenuItem(String texto, int mnemonic, KeyStroke acelerador) {
        JMenuItem item = new JMenuItem(texto);
        item.setMnemonic(mnemonic);
        if (acelerador != null) {
            item.setAccelerator(acelerador);
        }
        return item;
    }

    /*Crea el area de texto principal con numeracion de lineas*/
    private void crearAreaTexto() {
        areaTexto = new JTextArea();
        areaTexto.setFont(new Font("Monospaced", Font.PLAIN, 14));
        areaTexto.setTabSize(4);
        areaTexto.setLineWrap(false);

        /*Panel con numeracion de lineas*/
        JTextArea numeracionLineas = new JTextArea("1");
        numeracionLineas.setFont(new Font("Monospaced", Font.PLAIN, 14));
        numeracionLineas.setBackground(new Color(240, 240, 240));
        numeracionLineas.setEditable(false);
        numeracionLineas.setFocusable(false);

        /*Actualizar numeracion al escribir*/
        areaTexto.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarNumeracion(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarNumeracion(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarNumeracion(); }

            private void actualizarNumeracion() {
                int lineas = areaTexto.getLineCount();
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= lineas; i++) {
                    sb.append(i).append("\n");
                }
                numeracionLineas.setText(sb.toString());
            }
        });

        JScrollPane scrollPane = new JScrollPane(areaTexto);
        scrollPane.setRowHeaderView(numeracionLineas);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        add(scrollPane, BorderLayout.CENTER);
    }

    /*Abre un archivo y carga su contenido en el area de texto*/
    private void abrirArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de texto (*.txt)", "txt"));
        fileChooser.setCurrentDirectory(new File("."));

        int resultado = fileChooser.showOpenDialog(this);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            archivoActual = fileChooser.getSelectedFile();
            cargarContenidoArchivo(archivoActual);
            setTitle(TITULO_APLICACION + " - " + archivoActual.getName());
        }
    }

    /*Carga el contenido de un archivo en el area de texto*/
    private void cargarContenidoArchivo(File archivo) {
        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            areaTexto.setText("");
            String linea;
            while ((linea = reader.readLine()) != null) {
                areaTexto.append(linea + "\n");
            }
            areaTexto.setCaretPosition(0);
        } catch (IOException e) {
            mostrarError("Error al abrir el archivo: " + e.getMessage());
        }
    }

    /*Guarda el contenido actual en el archivo*/
    private void guardarArchivo() {
        if (archivoActual == null) {
            guardarArchivoComo();
        } else {
            guardarContenidoArchivo(archivoActual);
        }
    }

    /* Guarda el contenido con un nuevo nombre de archivo*/
    private void guardarArchivoComo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de texto (*.txt)", "txt"));
        fileChooser.setCurrentDirectory(new File("."));

        if (archivoActual != null) {
            fileChooser.setSelectedFile(archivoActual);
        }

        int resultado = fileChooser.showSaveDialog(this);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            archivoActual = fileChooser.getSelectedFile();

            // Agregar extension .txt si no la tiene
            if (!archivoActual.getName().toLowerCase().endsWith(".txt")) {
                archivoActual = new File(archivoActual.getAbsolutePath() + ".txt");
            }

            guardarContenidoArchivo(archivoActual);
            setTitle(TITULO_APLICACION + " - " + archivoActual.getName());
        }
    }

    /*Guarda el contenido del area de texto en un archivo*/
    private void guardarContenidoArchivo(File archivo) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
            writer.write(areaTexto.getText());
            JOptionPane.showMessageDialog(this,
                    "Archivo guardado exitosamente",
                    "Guardar",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            mostrarError("Error al guardar el archivo: " + e.getMessage());
        }
    }

    /*Analiza el texto actual y genera los reportes*/
    private void analizarTexto() {
        if (areaTexto.getText().trim().isEmpty()) {
            mostrarError("No hay texto para analizar");
            return;
        }

        /*Guardar contenido temporal para analisis*/
        File archivoTemporal = new File("temp_analisis.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoTemporal))) {
            writer.write(areaTexto.getText());
        } catch (IOException e) {
            mostrarError("Error al crear archivo temporal: " + e.getMessage());
            return;
        }

        /*Mostrar dialogo de progreso*/
        JDialog dialogoProgreso = crearDialogoProgreso();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                /*Reiniciar los analizadores*/
                scanner = new Scanner();
                analizadorSintactico = new Sintactico();
                scanner.scanFile(archivoTemporal.getAbsolutePath());
                analizadorSintactico.filtrarTokens(scanner.getTokens(), scanner.getErrores());
                generarArchivosReporte();
                return null;
            }

            @Override
            protected void done() {
                dialogoProgreso.dispose();
                archivoTemporal.delete();

                try {
                    get(); /*Verifica si hubo excepciones*/
                    mostrarResultadosAnalisis();
                } catch (Exception e) {
                    mostrarError("Error durante el analisis: " + e.getMessage());
                }
            }
        };

        worker.execute();
        dialogoProgreso.setVisible(true);
    }

    /*Crea un dialogo de progreso para el analisis*/
    private JDialog crearDialogoProgreso() {
        JDialog dialogo = new JDialog(this, "Analizando...", true);
        dialogo.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialogo.setSize(300, 100);
        dialogo.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Analizando el codigo fuente...");
        label.setHorizontalAlignment(SwingConstants.CENTER);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        panel.add(label, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);

        dialogo.add(panel);
        return dialogo;
    }

    /*Genera los archivos de reporte HTML y el arbol de jerarquia*/
    private void generarArchivosReporte() {
        try {
            generadorHTML.generarArchivoResultados(
                    analizadorSintactico.getOperacionesValidas(),
                    analizadorSintactico.getResultados()
            );
            generadorHTML.generarArchivoErrores(
                    scanner.getErrores(),
                    analizadorSintactico.getErroresSintacticos()
            );

            if (!analizadorSintactico.getOperacionesValidas().isEmpty()) {
                generadorArbol.generarArbolJerarquia(
                        analizadorSintactico.getOperacionesValidas()
                );
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al generar reportes: " + e.getMessage());
        }
    }

    /*Muestra un resumen de los resultados del analisis */
    private void mostrarResultadosAnalisis() {
        int totalTokens = scanner.getTokens().size();
        int erroresLexicos = scanner.getErrores().size();
        int erroresSintacticos = analizadorSintactico.getErroresSintacticos().size();
        int operacionesValidas = analizadorSintactico.getOperacionesValidas().size();

        StringBuilder mensaje = new StringBuilder();
        mensaje.append("----ANALISIS COMPLETADO----\n\n");
        mensaje.append("Tokens reconocidos: ").append(totalTokens).append("\n");
        mensaje.append("Errores lexicos: ").append(erroresLexicos).append("\n");
        mensaje.append("Errores sintacticos/semanticos: ").append(erroresSintacticos).append("\n");
        mensaje.append("Operaciones validas: ").append(operacionesValidas).append("\n\n");
        mensaje.append("Archivos generados:\n");
        mensaje.append("  - RESULTADOS_EQUIPO#3.html\n");
        mensaje.append("  - ERRORES_EQUIPO#3.html\n");

        if (operacionesValidas > 0) {
            mensaje.append("  - ArbolJerarquia.png\n");
        }

        JOptionPane.showMessageDialog(this,
                mensaje.toString(),
                "Resultados del Analisis",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /*Abre un archivo PDF de manual*/
    private void abrirManual(String nombreArchivo) {
        File manual = new File(nombreArchivo);

        if (!manual.exists()) {
            mostrarError("El archivo " + nombreArchivo + " no se encuentra en el directorio");
            return;
        }

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(manual);
            } else {
                mostrarError("No se puede abrir el archivo PDF en este sistema");
            }
        } catch (IOException e) {
            mostrarError("Error al abrir el manual: " + e.getMessage());
        }
    }

    /*Muestra un dialogo de error*/
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    /*Cierra la aplicacion con confirmacion*/
    private void salirAplicacion() {
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Desea salir de la aplicacion?",
                "Confirmar salida",
                JOptionPane.YES_NO_OPTION);

        if (opcion == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}