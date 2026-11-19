package practica_5_algoritmos;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

/** Carga la imagen en segundo plano y la ajusta al espacio sin deformarla. */
final class ImagenCarta extends JLabel {
    private final Dimension espacio;
    private final Path carpeta;
    private final Map<String, BufferedImage> imagenesRecientes;
    private String origenActual;
    private SwingWorker<BufferedImage, Void> carga;

    ImagenCarta(int ancho, int alto, Path carpeta, Map<String, BufferedImage> imagenesRecientes) {
        super("", CENTER);
        espacio = new Dimension(ancho, alto);
        this.carpeta = carpeta;
        this.imagenesRecientes = imagenesRecientes;
        setPreferredSize(espacio);
        setMinimumSize(espacio);
    }

    void mostrar(Carta carta) {
        String origen = carta == null ? null : carta.imagen();
        if (origenActual != null && Objects.equals(origenActual, origen)) {
            return;
        }
        if (carga != null) {
            carga.cancel(true);
            carga = null;
        }
        origenActual = origen;
        setIcon(null);
        if (origen == null || origen.isBlank()) {
            setText(carta == null ? "" : "Sin imagen");
            return;
        }
        BufferedImage guardada = imagenesRecientes.get(origen);
        if (guardada != null) {
            ponerImagen(guardada);
            return;
        }

        setText("Cargando…");
        carga = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() throws IOException {
                if (origen.startsWith("https://") || origen.startsWith("http://")) {
                    var conexion = URI.create(origen).toURL().openConnection();
                    conexion.setConnectTimeout(4000);
                    conexion.setReadTimeout(4000);
                    try (var entrada = conexion.getInputStream()) {
                        return ImageIO.read(entrada);
                    }
                }
                try (var entrada = Files.newInputStream(carpeta.resolve(origen))) {
                    return ImageIO.read(entrada);
                }
            }

            @Override
            protected void done() {
                if (isCancelled() || carga != this) {
                    return;
                }
                carga = null;
                try {
                    BufferedImage imagen = get();
                    if (imagen == null) {
                        setText("Sin imagen");
                        return;
                    }
                    imagenesRecientes.put(origen, imagen);
                    ponerImagen(imagen);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    setText("Sin imagen");
                } catch (ExecutionException ex) {
                    setText("Sin imagen");
                }
            }
        };
        carga.execute();
    }

    private void ponerImagen(BufferedImage imagen) {
        double escala = Math.min((double) espacio.width / imagen.getWidth(),
                (double) espacio.height / imagen.getHeight());
        int ancho = Math.max(1, (int) Math.round(imagen.getWidth() * escala));
        int alto = Math.max(1, (int) Math.round(imagen.getHeight() * escala));
        var ajustada = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D dibujo = ajustada.createGraphics();
        dibujo.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        dibujo.drawImage(imagen, 0, 0, ancho, alto, null);
        dibujo.dispose();
        setText("");
        setIcon(new ImageIcon(ajustada));
    }
}
