package practica_5_algoritmos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

final class VisorCartas extends JFrame {
    private static final Color FONDO = new Color(0x151420);
    private static final Color PANEL = new Color(0x211E30);
    private static final Color BORDE = new Color(0x423850);
    private static final Color TEXTO = new Color(0xFAF3E9);
    private static final Color SECUNDARIO = new Color(0xC9BED4);
    private static final Color ACENTO = new Color(0xF3C582);

    private enum Filtro {
        TODAS("Todas"), NIVEL("Nivel"), ATRIBUTO("Atributo"), ATAQUE("Ataque"), DEFENSA("Defensa"),
        TIPO_MONSTRUO("Tipo de monstruo");

        private final String etiqueta;

        Filtro(String etiqueta) {
            this.etiqueta = etiqueta;
        }
    }

    private final ListaDobleCircular<Carta> cartas;
    private final CatalogoCartas catalogo;
    private final JComboBox<Object> valores = new JComboBox<>();
    private final JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private final JLabel nombre = texto("", 25, Font.BOLD, TEXTO);
    private final JLabel detalle = texto("", 15, Font.PLAIN, SECUNDARIO);
    private final JLabel posicion = texto("", 15, Font.BOLD, ACENTO);
    private final MusicaFondo musica = new MusicaFondo();
    private final ImagenCarta imagenActual;
    private final ImagenCarta imagenAnterior;
    private final ImagenCarta imagenSiguiente;
    private final JButton anterior;
    private final JButton siguiente;
    private Filtro filtro = Filtro.TODAS;
    private CatalogoCartas.Consulta consulta;

    VisorCartas(ListaDobleCircular<Carta> cartas, CatalogoCartas catalogo, Path carpetaImagenes) {
        super("Catálogo MP23 · Dueling Heroes");
        this.cartas = cartas;
        this.catalogo = catalogo;
        Map<String, BufferedImage> imagenes = new LinkedHashMap<>(32, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> entrada) {
                return size() > 32;
            }
        };
        imagenActual = new ImagenCarta(242, 352, carpetaImagenes, imagenes);
        imagenAnterior = new ImagenCarta(98, 142, carpetaImagenes, imagenes);
        imagenSiguiente = new ImagenCarta(98, 142, carpetaImagenes, imagenes);
        anterior = botonNavegacion("‹  Anterior", imagenAnterior, () -> mover(false));
        siguiente = botonNavegacion("Siguiente  ›", imagenSiguiente, () -> mover(true));

        JPanel contenido = new JPanel(new BorderLayout(0, 16));
        contenido.setBackground(FONDO);
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 24, 18, 24));
        contenido.add(encabezado(), BorderLayout.NORTH);
        contenido.add(escaparate(), BorderLayout.CENTER);
        contenido.add(ficha(), BorderLayout.SOUTH);
        setContentPane(contenido);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent evento) {
                musica.iniciar();
            }
        });
        setMinimumSize(new Dimension(730, 650));
        cambiarFiltro(Filtro.TODAS);
        pack();
        setLocationRelativeTo(null);
        tecla("LEFT", "anterior", () -> mover(false));
        tecla("RIGHT", "siguiente", () -> mover(true));
    }

    @Override
    public void dispose() {
        musica.cerrar();
        super.dispose();
    }

    private JPanel encabezado() {
        JPanel encabezado = new JPanel(new BorderLayout(0, 16));
        encabezado.setOpaque(false);

        JPanel titulo = new JPanel(new BorderLayout(16, 4));
        titulo.setOpaque(false);
        titulo.add(texto("Dueling Heroes", 32, Font.BOLD, TEXTO), BorderLayout.CENTER);
        titulo.add(texto("25.º aniversario · Mega Pack MP23", 14, Font.PLAIN, SECUNDARIO), BorderLayout.SOUTH);
        musica.setContentAreaFilled(false);
        musica.setOpaque(true);
        musica.setBackground(PANEL);
        musica.setForeground(ACENTO);
        musica.setPreferredSize(new Dimension(44, 38));
        musica.setBorder(BorderFactory.createLineBorder(BORDE));
        musica.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        titulo.add(musica, BorderLayout.EAST);
        encabezado.add(titulo, BorderLayout.NORTH);

        JPanel controles = new JPanel(new BorderLayout(8, 0));
        controles.setOpaque(false);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        botones.setOpaque(false);
        ButtonGroup grupo = new ButtonGroup();
        for (Filtro opcion : Filtro.values()) {
            JToggleButton boton = new JToggleButton(opcion.etiqueta);
            boton.setSelected(opcion == Filtro.TODAS);
            boton.setContentAreaFilled(false);
            boton.setOpaque(true);
            boton.setFont(boton.getFont().deriveFont(Font.BOLD, 13f));
            boton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            boton.addChangeListener(evento -> colorearFiltro(boton));
            boton.addActionListener(evento -> cambiarFiltro(opcion));
            colorearFiltro(boton);
            grupo.add(boton);
            botones.add(boton);
        }
        controles.add(botones, BorderLayout.WEST);

        selector.setOpaque(false);
        valores.setMaximumRowCount(10);
        valores.addActionListener(evento -> actualizarConsulta());
        selector.add(valores);
        controles.add(selector, BorderLayout.CENTER);
        encabezado.add(controles, BorderLayout.SOUTH);
        return encabezado;
    }

    private JPanel escaparate() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL);
        panel.setPreferredSize(new Dimension(770, 380));
        panel.setBorder(BorderFactory.createLineBorder(BORDE));
        imagenActual.setOpaque(true);
        imagenActual.setBackground(FONDO);
        imagenActual.setForeground(SECUNDARIO);
        GridBagConstraints celda = new GridBagConstraints();
        celda.weightx = 1;
        panel.add(anterior, celda);
        celda.gridx = 1;
        celda.weightx = 0;
        panel.add(imagenActual, celda);
        celda.gridx = 2;
        celda.weightx = 1;
        panel.add(siguiente, celda);
        return panel;
    }

    private JPanel ficha() {
        JPanel ficha = new JPanel();
        ficha.setOpaque(false);
        ficha.setLayout(new BoxLayout(ficha, BoxLayout.Y_AXIS));
        for (JLabel linea : List.of(nombre, detalle, posicion)) {
            linea.setAlignmentX(CENTER_ALIGNMENT);
            ficha.add(linea);
            ficha.add(Box.createVerticalStrut(5));
        }
        return ficha;
    }

    private void cambiarFiltro(Filtro opcion) {
        filtro = opcion;
        selector.setVisible(opcion != Filtro.TODAS);
        valores.getAccessibleContext().setAccessibleName(opcion.etiqueta);
        valores.setPreferredSize(new Dimension(opcion == Filtro.TIPO_MONSTRUO ? 150 : 116, 33));
        List<?> opciones = switch (opcion) {
            case TODAS -> List.of();
            case NIVEL -> catalogo.niveles();
            case ATRIBUTO -> catalogo.atributos();
            case ATAQUE -> catalogo.puntosAtaque();
            case DEFENSA -> catalogo.puntosDefensa();
            case TIPO_MONSTRUO -> catalogo.tiposMonstruo();
        };
        valores.setModel(new DefaultComboBoxModel<>(opciones.toArray()));
        valores.setEnabled(!opciones.isEmpty());
        actualizarConsulta();
    }

    private void actualizarConsulta() {
        Object valor = valores.getSelectedItem();
        valores.setToolTipText(valor == null ? null : valor.toString());
        if (filtro == Filtro.TODAS) {
            consulta = cartas.estaVacia() ? null : new CatalogoCartas.Consulta(cartas.cursor(), cartas.getTamanio());
        } else if (valor == null) {
            consulta = null;
        } else {
            consulta = switch (filtro) {
                case NIVEL -> catalogo.porNivel((Integer) valor);
                case ATRIBUTO -> catalogo.porAtributo((String) valor);
                case ATAQUE -> catalogo.porAtaque((Integer) valor);
                case DEFENSA -> catalogo.porDefensa((Integer) valor);
                case TIPO_MONSTRUO -> catalogo.porTipoMonstruo((String) valor);
                default -> throw new IllegalStateException("Filtro no disponible");
            };
        }
        mostrarCartas();
    }

    private void mover(boolean adelante) {
        if (consulta == null || consulta.total() < 2) {
            return;
        }
        if (adelante) {
            consulta.cursor().avanzar();
        } else {
            consulta.cursor().retroceder();
        }
        mostrarCartas();
    }

    private void mostrarCartas() {
        boolean hayVecinas = consulta != null && consulta.total() > 1;
        anterior.setEnabled(hayVecinas);
        siguiente.setEnabled(hayVecinas);
        if (consulta == null) {
            imagenActual.mostrar(null);
            imagenAnterior.mostrar(null);
            imagenSiguiente.mostrar(null);
            imagenActual.getAccessibleContext().setAccessibleName(null);
            anterior.setToolTipText(null);
            siguiente.setToolTipText(null);
            nombre.setText("No hay cartas para este filtro");
            nombre.setToolTipText(null);
            detalle.setText(" ");
            posicion.setText("0 / 0");
            return;
        }
        var cursor = consulta.cursor();
        Carta carta = cursor.actual();
        imagenActual.mostrar(carta);
        imagenActual.getAccessibleContext().setAccessibleName(carta.nombre());
        imagenAnterior.mostrar(hayVecinas ? cursor.anterior() : null);
        imagenSiguiente.mostrar(hayVecinas ? cursor.siguiente() : null);
        anterior.setToolTipText(hayVecinas ? cursor.anterior().nombre() : null);
        siguiente.setToolTipText(hayVecinas ? cursor.siguiente().nombre() : null);
        nombre.setText(carta.nombre());
        nombre.setToolTipText(carta.nombre());
        detalle.setText(switch (filtro) {
            case TODAS -> carta.tipo();
            case NIVEL -> "Nivel " + carta.nivel();
            case ATRIBUTO -> "Atributo · " + valores.getSelectedItem();
            case ATAQUE -> "Ataque · " + carta.ataque();
            case DEFENSA -> "Defensa · " + carta.defensa();
            case TIPO_MONSTRUO -> "Tipo · " + carta.tipoMonstruo();
        });
        posicion.setText(cursor.posicion() + " / " + consulta.total());
    }

    private void tecla(String tecla, String nombre, Runnable accion) {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(tecla), nombre);
        getRootPane().getActionMap().put(nombre, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent evento) {
                accion.run();
            }
        });
    }

    private static void colorearFiltro(JToggleButton boton) {
        boton.setBackground(boton.isSelected() ? ACENTO : PANEL);
        boton.setForeground(boton.isSelected() ? FONDO : TEXTO);
    }

    private static JButton botonNavegacion(String titulo, ImagenCarta imagen, Runnable accion) {
        JButton boton = new JButton();
        boton.setLayout(new BorderLayout(0, 8));
        boton.setBackground(FONDO);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setContentAreaFilled(false);
        boton.setOpaque(true);
        boton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        imagen.setForeground(SECUNDARIO);
        JLabel etiqueta = texto(titulo, 13, Font.BOLD, TEXTO);
        etiqueta.setHorizontalAlignment(SwingConstants.CENTER);
        boton.add(imagen, BorderLayout.CENTER);
        boton.add(etiqueta, BorderLayout.SOUTH);
        boton.getAccessibleContext().setAccessibleName(titulo);
        boton.addActionListener(evento -> accion.run());
        imagen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                if (boton.isEnabled() && SwingUtilities.isLeftMouseButton(evento)) {
                    boton.doClick(0);
                }
            }
        });
        boton.addPropertyChangeListener("enabled", evento -> etiqueta.setEnabled(boton.isEnabled()));
        return boton;
    }

    private static JLabel texto(String texto, int tamanio, int estilo, Color color) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(etiqueta.getFont().deriveFont(estilo, (float) tamanio));
        etiqueta.setForeground(color);
        return etiqueta;
    }
}
