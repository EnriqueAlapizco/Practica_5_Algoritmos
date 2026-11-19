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
            CatalogoCartas catalogo = new CatalogoCartas(cartas);
            System.out.printf("Cartas cargadas: %d%n", cartas.getTamanio());
            System.out.printf("Primera carta: %s%n", cartas.cursor().actual().nombre());
            System.out.printf("Grupos disponibles: %d niveles, %d atributos, %d valores de ataque y %d de defensa.%n",
                    catalogo.niveles().size(), catalogo.atributos().size(),
                    catalogo.puntosAtaque().size(), catalogo.puntosDefensa().size());
        } catch (IOException ex) {
            System.err.println("No se pudo cargar el catálogo. " + ex.getMessage());
            System.exit(1);
        }
    }
    
}
