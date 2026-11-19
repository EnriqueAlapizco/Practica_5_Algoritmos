/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package practica_5_algoritmos;

import java.io.IOException;
import java.nio.file.Path;

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
            ListaDobleCircular<Carta> cartas = args.length == 0
                    ? LectorCartasCsv.cargar()
                    : LectorCartasCsv.cargar(Path.of(args[0]));
            System.out.printf("Cartas cargadas: %d%n", cartas.getTamanio());
            System.out.printf("Primera carta: %s%n", cartas.cursor().actual().nombre());
        } catch (IOException ex) {
            System.err.println("No se pudo cargar el catálogo. " + ex.getMessage());
            System.exit(1);
        }
    }
    
}
