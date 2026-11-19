/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package practica_5_algoritmos;

import java.io.IOException;
import java.nio.file.Path;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author enriq
 */
public class Practica_5_Algoritmos {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Path archivo = args.length == 0 ? null : Path.of(args[0]).toAbsolutePath();
            ListaDobleCircular<Carta> cartas = archivo == null
                    ? LectorCartasCsv.cargar()
                    : LectorCartasCsv.cargar(archivo);
            CatalogoCartas catalogo = new CatalogoCartas(cartas);
            Path carpetaImagenes = archivo == null ? Path.of("") : archivo.getParent();
            SwingUtilities.invokeLater(() -> new VisorCartas(cartas, catalogo, carpetaImagenes).setVisible(true));
        } catch (IOException | IllegalArgumentException ex) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, ex.getMessage(),
                    "No se pudo cargar el catálogo", JOptionPane.ERROR_MESSAGE));
        }
    }
    
}
