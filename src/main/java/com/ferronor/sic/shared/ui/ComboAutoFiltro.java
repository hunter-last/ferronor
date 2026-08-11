package com.ferronor.sic.shared.ui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Function;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;

public class ComboAutoFiltro {
    /**
     * Configura cualquier JComboBox para ser editable y filtrarse dinámicamente.
     * 
     * @param <T> Clase del Modelo (Medico, Paciente, Especialidad, etc.)
     * @param comboBox El JComboBox a configurar
     * @param searchFunction Función lambda o método DAO a ejecutar según el texto
     */
    
    /*
    En java [<T>] representa un Tipo de Dato Gneérico. La T viene por convención de la palabra Type.
    
    Lo que resuelve <T> es que le estamos diciendo a Java:
    - Este método es una plantilla. Cuando alguien lo ejecute, la [ T ] adoptará automaticamente la
    clase concreta del combo que te pase (sea Medico, Paciente o Especialidad).
    Además me garantizas que el resultado devuelto será una lista exactamente de esa misma clase
    [ List<T> ].
    -
    ¿Qué es Function<String, List<T>?
    ---------------------------------
    Una Function <I,O> es una interfaz que representa una instrucción de procesamiento.
        - I (Input): El tipo de dato que recibe la función.
        - O (Output): El tipo de dato que devuelve la función.
    En nuestro caso: Function <String, List<T>>
        - Entrada (String): El texto que el usuario va escribiendo en el combo box.
        - Salida (List<T>): La listade resultados que la capa service devuelve tras la consulta filtrada.
    Cuando en la vista escribamos:
        texto -> medicoService.buscarFiltroTotal (texto, especialidad)
    Estaremos enviandole al método mejorarCombo, la receta exacta de a quién debe llamar para consultar
    los datos, sin que el propio método, sepa si está buscando médicos, pacientes o especialidades.
    */
    public static <T> void mejorarCombo(JComboBox<T> comboBox, Function<String, List<T>> searchFunction) {
        comboBox.setEditable(true);
        
        /*
        Un JComboBox en Swing está compuesto internamente por un botón y un cuadro de texto (JTextComponent)
        Al activar [ setEditable(true) ], habilitamos dicho cuadro de texto.
        Con la instancia editor, capturamos ese cuadro de texto para poder escuchar las teclas del usuario.
        */
        JTextComponent editor = (JTextComponent) comboBox.getEditor().getEditorComponent();

        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Ignorar teclas de navegación para permitir seleccionar elementos con las flechas
                if (e.getKeyCode() == KeyEvent.VK_UP || 
                    e.getKeyCode() == KeyEvent.VK_DOWN || 
                    e.getKeyCode() == KeyEvent.VK_ENTER || 
                    e.getKeyCode() == KeyEvent.VK_ESCAPE ||
                    e.getKeyCode() == KeyEvent.VK_TAB) {
                    return;
                }
                
                /*
                Guardamos el estado actual del texto y del cursor (la barra parpadenate "|")
                para evitar que cuando el combo cargue nuevas opciones, el cursor regrese al inicio
                del texto.
                Ej.
                Escribimos p, el combo se verá así: pe|
                y apenas soltemos el teclado, el combo cambia, o carga la lista de medicos
                y ahora el combo se ve así: |pe, provocando que si queremos escribir pedrales, escribamos:
                drales|pe
                Por eso siempre guardamos el texto que está tecleando el usuario, y la posición del cursor
                en el último caracter de este.
                */
                String textoFiltro = editor.getText();
                int posicionCursor = editor.getCaretPosition();

                // Consultar y actualizar lista
                List<T> resultados = searchFunction.apply(textoFiltro);

                DefaultComboBoxModel<T> model = (DefaultComboBoxModel<T>) comboBox.getModel();
                model.removeAllElements();

                for (T item : resultados) {
                    model.addElement(item);
                }

                // Restaurar el texto ingresado y el cursor (evita que el combo autoseleccione y borre lo escrito)
                editor.setText(textoFiltro);
                try {
                    editor.setCaretPosition(Math.min(posicionCursor, textoFiltro.length()));
                } catch (Exception ignored) {}

                // Desplegar menú de resultados si hay elementos
                if (!resultados.isEmpty() && editor.isFocusOwner()) {
                    comboBox.showPopup();
                } else {
                    comboBox.hidePopup();
                }
            }
        });
    }
}
