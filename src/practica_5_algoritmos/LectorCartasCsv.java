package practica_5_algoritmos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LectorCartasCsv {
    private static final String RECURSO = "cartas-mp23.csv";
    private static final List<String> COLUMNAS = List.of(
            "nombre", "tipo", "nivel", "atributo", "ataque", "defensa", "imagen");
    // Una carta por línea. Los campos entre comillas admiten comas y comillas dobles escapadas.
    private static final Pattern CAMPO = Pattern.compile("\\G(?:\"((?:[^\"]|\"\")*)\"|([^,\"]*))(,|$)");

    private LectorCartasCsv() { }

    public static ListaDobleCircular<Carta> cargar() throws IOException {
        var entrada = LectorCartasCsv.class.getResourceAsStream(RECURSO);
        if (entrada == null) {
            throw new IOException("Falta el catálogo " + RECURSO);
        }
        try (var lector = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8))) {
            return leer(lector);
        }
    }

    public static ListaDobleCircular<Carta> cargar(Path archivo) throws IOException {
        try (var lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            return leer(lector);
        }
    }

    private static ListaDobleCircular<Carta> leer(BufferedReader lector) throws IOException {
        String encabezado = lector.readLine();
        if (encabezado == null || !COLUMNAS.equals(campos(encabezado.replaceFirst("^\uFEFF", ""), 1))) {
            throw new IOException("Encabezado esperado: " + String.join(",", COLUMNAS));
        }

        var cartas = new ListaDobleCircular<Carta>();
        String fila;
        int linea = 1;
        while ((fila = lector.readLine()) != null) {
            linea++;
            if (fila.isBlank()) {
                continue;
            }
            List<String> datos = campos(fila, linea);
            if (datos.size() != COLUMNAS.size() || datos.get(0).isEmpty() || datos.get(1).isEmpty()) {
                throw new IOException("Línea " + linea + ": se esperan siete campos, con nombre y tipo");
            }
            cartas.insertarFinal(new Carta(datos.get(0), datos.get(1),
                    numero(datos.get(2), "nivel", linea), opcional(datos.get(3)),
                    numero(datos.get(4), "ataque", linea), numero(datos.get(5), "defensa", linea),
                    opcional(datos.get(6))));
        }
        if (cartas.estaVacia()) {
            throw new IOException("El catálogo CSV está vacío");
        }
        return cartas;
    }

    private static List<String> campos(String fila, int linea) throws IOException {
        var valores = new ArrayList<String>(COLUMNAS.size());
        Matcher coincidencia = CAMPO.matcher(fila);
        while (coincidencia.find()) {
            String citado = coincidencia.group(1);
            String valor = citado == null ? coincidencia.group(2) : citado.replace("\"\"", "\"");
            valores.add(valor.strip());
            if (coincidencia.group(3).isEmpty()) {
                return valores;
            }
        }
        throw new IOException("Línea " + linea + ": formato CSV incorrecto");
    }

    private static Integer numero(String valor, String campo, int linea) throws IOException {
        if (valor.isEmpty() || valor.equals("?") || valor.equals("-")) {
            return null;
        }
        try {
            return Integer.valueOf(valor);
        } catch (NumberFormatException ex) {
            throw new IOException("Línea " + linea + ": valor inválido para " + campo + " (" + valor + ")", ex);
        }
    }

    private static String opcional(String valor) {
        return valor.isEmpty() ? null : valor;
    }
}
