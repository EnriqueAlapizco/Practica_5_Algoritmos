package practica_5_algoritmos;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Botón de bocina y reproducción de un archivo WAV local en bucle. */
final class MusicaFondo extends JButton {
    private static final Path TEMA_PREDETERMINADO = Path.of("duel-links.wav");

    private Clip clip;
    private boolean reproduciendo;
    private boolean cerrado;

    MusicaFondo() {
        actualizarBoton();
        addActionListener(evento -> pulsar());
    }

    void iniciar() {
        if (Files.isRegularFile(TEMA_PREDETERMINADO)) {
            cargar(TEMA_PREDETERMINADO, false);
        }
    }

    void cerrar() {
        cerrado = true;
        if (clip != null) {
            clip.close();
            clip = null;
        }
    }

    private void pulsar() {
        if (clip == null) {
            JFileChooser selector = new JFileChooser();
            selector.setDialogTitle("Elige el tema de Duel Links");
            selector.setFileFilter(new FileNameExtensionFilter("Audio WAV", "wav"));
            if (selector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                cargar(selector.getSelectedFile().toPath(), true);
            }
            return;
        }
        if (reproduciendo) {
            clip.stop();
            reproduciendo = false;
            actualizarBoton();
        } else {
            reproducir();
        }
    }

    private void cargar(Path archivo, boolean elegido) {
        setEnabled(false);
        setToolTipText("Cargando música…");
        new SwingWorker<Clip, Void>() {
            @Override
            protected Clip doInBackground() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
                Clip nuevo = AudioSystem.getClip();
                try (var entrada = AudioSystem.getAudioInputStream(archivo.toFile())) {
                    nuevo.open(entrada);
                    return nuevo;
                } catch (IOException | UnsupportedAudioFileException | LineUnavailableException | RuntimeException ex) {
                    nuevo.close();
                    throw ex;
                }
            }

            @Override
            protected void done() {
                try {
                    Clip nuevo = get();
                    if (cerrado) {
                        nuevo.close();
                        return;
                    }
                    clip = nuevo;
                    reproducir();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    informarError(archivo, elegido);
                } catch (ExecutionException ex) {
                    informarError(archivo, elegido);
                } finally {
                    if (!cerrado) {
                        setEnabled(true);
                    }
                }
            }
        }.execute();
    }

    private void reproducir() {
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        reproduciendo = true;
        actualizarBoton();
    }

    private void informarError(Path archivo, boolean elegido) {
        if (cerrado) {
            return;
        }
        String mensaje = "No se pudo reproducir " + archivo.getFileName();
        setToolTipText(mensaje + ". Pulsa para elegir un WAV.");
        if (elegido) {
            JOptionPane.showMessageDialog(this, mensaje + ". Verifica que sea un WAV PCM y que haya una salida de audio.",
                    "Música de fondo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizarBoton() {
        String accion = clip == null ? "Elegir música de Duel Links (WAV)"
                : reproduciendo ? "Silenciar música" : "Reanudar música";
        setIcon(new Bocina(reproduciendo));
        setToolTipText(accion);
        getAccessibleContext().setAccessibleName(accion);
    }

    private record Bocina(boolean activa) implements Icon {
        @Override
        public int getIconWidth() { return 24; }

        @Override
        public int getIconHeight() { return 24; }

        @Override
        public void paintIcon(Component componente, Graphics graficos, int x, int y) {
            Graphics2D dibujo = (Graphics2D) graficos.create();
            dibujo.translate(x, y);
            dibujo.setColor(componente.getForeground());
            dibujo.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            dibujo.fillPolygon(new int[]{2, 6, 12, 12, 6, 2}, new int[]{9, 9, 4, 20, 15, 15}, 6);
            dibujo.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (activa) {
                dibujo.drawArc(9, 7, 9, 10, -60, 120);
                dibujo.drawArc(7, 3, 16, 18, -60, 120);
            } else {
                dibujo.drawLine(16, 9, 22, 15);
                dibujo.drawLine(22, 9, 16, 15);
            }
            dibujo.dispose();
        }
    }
}
