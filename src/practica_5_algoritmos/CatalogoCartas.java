package practica_5_algoritmos;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/** Agrupa las cartas por cada valor disponible para la consulta. */
public final class CatalogoCartas {
    private final Map<Integer, ListaDobleCircular<Carta>> porNivel = new TreeMap<>();
    private final Map<String, ListaDobleCircular<Carta>> porAtributo = new TreeMap<>();
    private final Map<Integer, ListaDobleCircular<Carta>> porAtaque = new TreeMap<>();
    private final Map<Integer, ListaDobleCircular<Carta>> porDefensa = new TreeMap<>();

    public CatalogoCartas(ListaDobleCircular<Carta> cartas) {
        for (Carta carta : cartas) {
            agregar(porNivel, carta.nivel(), carta);
            agregar(porAtributo, normalizarAtributo(carta.atributo()), carta);
            agregar(porAtaque, carta.ataque(), carta);
            agregar(porDefensa, carta.defensa(), carta);
        }
    }

    public List<Integer> niveles() {
        return List.copyOf(porNivel.keySet());
    }

    public List<String> atributos() {
        return List.copyOf(porAtributo.keySet());
    }

    public List<Integer> puntosAtaque() {
        return List.copyOf(porAtaque.keySet());
    }

    public List<Integer> puntosDefensa() {
        return List.copyOf(porDefensa.keySet());
    }

    public Optional<Consulta> porNivel(int nivel) {
        return consulta(porNivel.get(nivel));
    }

    public Optional<Consulta> porAtributo(String atributo) {
        String clave = normalizarAtributo(atributo);
        return clave == null ? Optional.empty() : consulta(porAtributo.get(clave));
    }

    public Optional<Consulta> porAtaque(int ataque) {
        return consulta(porAtaque.get(ataque));
    }

    public Optional<Consulta> porDefensa(int defensa) {
        return consulta(porDefensa.get(defensa));
    }

    public record Consulta(ListaDobleCircular<Carta>.Cursor cursor, int total) { }

    private static <K> void agregar(Map<K, ListaDobleCircular<Carta>> grupos, K clave, Carta carta) {
        if (clave != null) {
            grupos.computeIfAbsent(clave, valor -> new ListaDobleCircular<>()).insertarFinal(carta);
        }
    }

    private static Optional<Consulta> consulta(ListaDobleCircular<Carta> grupo) {
        return grupo == null ? Optional.empty() : Optional.of(new Consulta(grupo.cursor(), grupo.getTamanio()));
    }

    private static String normalizarAtributo(String atributo) {
        return atributo == null || atributo.isBlank() ? null : atributo.strip().toUpperCase(Locale.ROOT);
    }
}
