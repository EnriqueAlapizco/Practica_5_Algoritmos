package practica_5_algoritmos;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.ConcurrentModificationException;

/** El anterior del inicio es el fin y el siguiente del fin es el inicio. */
public class ListaDobleCircular<T> implements Iterable<T> {
    private NodoDoble<T> inicio;
    private int tamanio;
    private int modificaciones;

    public boolean estaVacia() { return tamanio == 0; }
    public int getTamanio() { return tamanio; }

    public void insertarFinal(T dato) {
        NodoDoble<T> nuevo = new NodoDoble<>(dato);
        if (estaVacia()) {
            nuevo.setAnterior(nuevo);
            nuevo.setSiguiente(nuevo);
            inicio = nuevo;
        } else {
            NodoDoble<T> fin = inicio.getAnterior();
            nuevo.setAnterior(fin);
            nuevo.setSiguiente(inicio);
            fin.setSiguiente(nuevo);
            inicio.setAnterior(nuevo);
        }
        tamanio++;
        modificaciones++;
    }

    public void insertarInicio(T dato) {
        insertarFinal(dato);
        inicio = inicio.getAnterior();
    }

    public T eliminarInicio() { return eliminar(0); }
    public T eliminarFinal() { return eliminar(tamanio - 1); }

    public T eliminar(int indice) {
        NodoDoble<T> actual = nodoEn(indice);
        if (tamanio == 1) { inicio = null; }
        else {
            actual.getAnterior().setSiguiente(actual.getSiguiente());
            actual.getSiguiente().setAnterior(actual.getAnterior());
            if (actual == inicio) { inicio = actual.getSiguiente(); }
        }
        tamanio--;
        modificaciones++;
        return actual.getDato();
    }

    public T obtener(int indice) { return nodoEn(indice).getDato(); }
    public void reemplazar(int indice, T dato) { nodoEn(indice).setDato(dato); }

    public Cursor cursor() {
        if (estaVacia()) {
            throw new NoSuchElementException("La lista está vacía");
        }
        return new Cursor();
    }

    /** Recorre la lista en ambos sentidos a partir de su primer nodo. */
    public final class Cursor {
        private final int modificacionesIniciales = modificaciones;
        private NodoDoble<T> actual = inicio;
        private int indice;

        private Cursor() { }

        public T actual() {
            verificarLista();
            return actual.getDato();
        }

        public T anterior() {
            verificarLista();
            return actual.getAnterior().getDato();
        }

        public T siguiente() {
            verificarLista();
            return actual.getSiguiente().getDato();
        }

        public int posicion() {
            verificarLista();
            return indice + 1;
        }

        public void avanzar() {
            verificarLista();
            actual = actual.getSiguiente();
            indice = (indice + 1) % tamanio;
        }

        public void retroceder() {
            verificarLista();
            actual = actual.getAnterior();
            indice = (indice + tamanio - 1) % tamanio;
        }

        private void verificarLista() {
            if (modificacionesIniciales != modificaciones) {
                throw new ConcurrentModificationException("La lista cambió durante el recorrido");
            }
        }
    }

    private NodoDoble<T> nodoEn(int indice) {
        if (indice < 0 || indice >= tamanio) {
            throw new IndexOutOfBoundsException("Indice fuera de la lista: " + indice);
        }
        NodoDoble<T> actual;
        if (indice <= tamanio / 2) {
            actual = inicio;
            for (int i = 0; i < indice; i++) { actual = actual.getSiguiente(); }
        } else {
            actual = inicio.getAnterior();
            for (int i = tamanio - 1; i > indice; i--) { actual = actual.getAnterior(); }
        }
        return actual;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private NodoDoble<T> actual = inicio;
            private int restantes = tamanio;
            public boolean hasNext() { return restantes > 0; }
            public T next() {
                if (!hasNext()) { throw new NoSuchElementException(); }
                T dato = actual.getDato();
                actual = actual.getSiguiente();
                restantes--;
                return dato;
            }
        };
    }
}
