package practica_5_algoritmos;

import java.util.Objects;

/** Datos de una carta. Los campos de juego pueden faltar según su tipo. */
public record Carta(String nombre, String tipo, Integer nivel, String atributo,
        Integer ataque, Integer defensa, String imagen) {

    public Carta {
        Objects.requireNonNull(nombre, "El nombre es obligatorio");
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
    }

    public String tipoMonstruo() {
        if (tipo == null) {
            return null;
        }
        // El CSV guarda el tipo seguido de subtipos, por ejemplo Dragon/Fusion/Effect.
        String familia = tipo.split("/", 2)[0].strip();
        if (familia.isEmpty() || familia.equalsIgnoreCase("Spell") || familia.equalsIgnoreCase("Trap")) {
            return null;
        }
        return familia;
    }
}
