package com.ferronor.sic.shared.ui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.text.JTextComponent;

public final class ComboAutoFiltro {

    private static final int RETARDO_BUSQUEDA_MS = 250;

    private ComboAutoFiltro() {
        // Clase utilitaria
    }

    /**
     * Configura cualquier JComboBox para ser editable y filtrarse
     * dinámicamente.
     *
     * La búsqueda se ejecuta fuera del EDT para no bloquear la interfaz. Si
     * mientras se ejecuta una búsqueda el usuario escribe nuevamente, el
     * resultado anterior se considera obsoleto y no se aplica al combo.
     *
     * @param <T> tipo de elementos que contiene el combo
     * @param comboBox combo que será configurado
     * @param searchFunction función que recibe el texto y devuelve los
     * resultados
     */
    public static <T> void mejorarCombo(
            JComboBox<T> comboBox,
            Function<String, List<T>> searchFunction) {

        if (comboBox == null) {
            throw new IllegalArgumentException("comboBox no puede ser null");
        }

        if (searchFunction == null) {
            throw new IllegalArgumentException("searchFunction no puede ser null");
        }

        comboBox.setEditable(true);

        JTextComponent editor
                = (JTextComponent) comboBox.getEditor().getEditorComponent();

        /*
         * Cada búsqueda recibe un número de generación.
         *
         * Ejemplo:
         *
         * "p"   -> generación 1
         * "pr"  -> generación 2
         * "pro" -> generación 3
         *
         * Si la consulta "p" termina después de "pro", su resultado
         * ya no puede modificar el combo porque pertenece a una
         * generación anterior.
         */
        AtomicLong generacionActual = new AtomicLong();

        /*
         * Mantiene referencia a la búsqueda actualmente ejecutándose
         * para solicitar su cancelación cuando aparece una nueva.
         *
         * cancel(true) no garantiza que JDBC interrumpa inmediatamente
         * la consulta, por eso también usamos generacionActual como
         * mecanismo definitivo contra resultados obsoletos.
         */
        AtomicReference<SwingWorker<List<T>, Void>> trabajadorActual
                = new AtomicReference<>();
        /*
         * Swing Timer:
         *
         * El usuario escribe:
         *
         * P
         * Pr
         * Pro
         * Prod
         *
         * No hacemos cuatro consultas inmediatamente.
         *
         * Esperamos RETARDO_BUSQUEDA_MS después de la última tecla.
         */
        Timer temporizador = new Timer(
                RETARDO_BUSQUEDA_MS,
                e -> {

                    String textoFiltro = editor.getText();
                    int posicionCursor = editor.getCaretPosition();

                    long generacion = generacionActual.get();
                    /*
                     * Cancelamos el trabajador anterior si todavía existe.
                     */
                    SwingWorker<?, ?> anterior = trabajadorActual.get();

                    if (anterior != null && !anterior.isDone()) {
                        anterior.cancel(true);
                    }

                    SwingWorker<List<T>, Void> trabajador
                    = new SwingWorker<List<T>, Void>() {

                @Override
                protected List<T> doInBackground() {

                    /*
                             * ESTA llamada ocurre fuera del EDT.
                             *
                             * Aquí puede ejecutarse:
                             *
                             * Service
                             *    ↓
                             * DAO
                             *    ↓
                             * PostgreSQL
                             *
                             * sin congelar la interfaz.
                     */
                    return searchFunction.apply(textoFiltro);
                }

                @Override
                protected void done() {

                    /*
                             * done() vuelve a ejecutarse en el EDT.
                     */

 /*
                             * Si ya existe una búsqueda más reciente,
                             * este resultado quedó obsoleto.
                     */
                    if (generacion != generacionActual.get()) {
                        return;
                    }

                    /*
                             * Si el usuario ya cambió el texto mientras
                             * esta consulta terminaba, tampoco aplicamos
                             * el resultado.
                     */
                    if (!editor.getText().equals(textoFiltro)) {
                        return;
                    }

                    List<T> resultados;

                    try {
                        resultados = get();
                    } catch (Exception ex) {

                        /*
                                 * Si fue cancelado, simplemente ignoramos
                                 * el resultado.
                         */
                        if (isCancelled()) {
                            return;
                        }

                        /*
                                 * Una excepción de búsqueda no debe destruir
                                 * el estado actual del combo.
                         */
                        return;
                    }

                    if (resultados == null) {
                        return;
                    }

                    /*
                             * Todavía verificamos la generación justo antes
                             * de modificar el combo.
                             *
                             * Esto protege contra una nueva pulsación que
                             * haya ocurrido mientras llegábamos hasta aquí.
                     */
                    if (generacion != generacionActual.get()) {
                        return;
                    }

                    actualizarModelo(
                            comboBox,
                            resultados,
                            textoFiltro,
                            posicionCursor,
                            editor);
                }
            };

                    trabajadorActual.set(trabajador);
                    trabajador.execute();
                });

        /*
         * Reinicia el temporizador cada vez que el usuario escribe.
         */
        temporizador.setRepeats(false);

        editor.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {

                /*
                 * Las teclas de navegación no deben iniciar búsquedas.
                 */
                if (esTeclaNavegacion(e)) {
                    return;
                }

                /*
                 * Cada nueva modificación invalida conceptualmente
                 * cualquier búsqueda anterior.
                 */
                generacionActual.incrementAndGet();
                /*
                 * Cancelación temprana del trabajador anterior.
                 */
                SwingWorker<?, ?> anterior = trabajadorActual.get();

                if (anterior != null && !anterior.isDone()) {
                    anterior.cancel(true);
                }

                /*
                 * Esperamos a que el usuario deje de escribir.
                 */
                temporizador.restart();
            }
        });
    }

    private static boolean esTeclaNavegacion(KeyEvent e) {

        return e.getKeyCode() == KeyEvent.VK_UP
                || e.getKeyCode() == KeyEvent.VK_DOWN
                || e.getKeyCode() == KeyEvent.VK_ENTER
                || e.getKeyCode() == KeyEvent.VK_ESCAPE
                || e.getKeyCode() == KeyEvent.VK_TAB;
    }

    private static <T> void actualizarModelo(
            JComboBox<T> comboBox,
            List<T> resultados,
            String textoFiltro,
            int posicionCursor,
            JTextComponent editor) {

        /*
         * Usamos un modelo nuevo para evitar depender de que el modelo
         * configurado originalmente sea realmente un
         * DefaultComboBoxModel.
         */
        DefaultComboBoxModel<T> modelo
                = new DefaultComboBoxModel<>();

        for (T item : resultados) {
            modelo.addElement(item);
        }

        comboBox.setModel(modelo);

        /*
         * setModel puede alterar el texto del editor.
         * Lo restauramos exactamente como lo escribió el usuario.
         */
        editor.setText(textoFiltro);

        try {
            editor.setCaretPosition(
                    Math.min(posicionCursor, textoFiltro.length()));
        } catch (IllegalArgumentException ignored) {
            // El texto cambió durante la actualización.
        }

        /*
         * Solo mostramos resultados si el editor continúa teniendo
         * el foco y existen coincidencias.
         */
        if (!resultados.isEmpty() && editor.isFocusOwner()) {
            comboBox.showPopup();
        } else {
            comboBox.hidePopup();
        }
    }
}
