
/**
 * Clase que maneja lo relacionado a la interacción con el usuario.
 */

import java.lang.*;
import javax.swing.JOptionPane;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.lang.Object;
import java.awt.Dimension;

public class Interfaz extends JFrame
{
    private Object[] options = {"Indexar","Salir"};

    /**
     * Constructor for objects of class Interfaz
     */
    public Interfaz()
    {}

    public String consultar()
    {
        String texto = JOptionPane.showInputDialog(null,"Escriba su Consulta:");
        if (texto == null)
        {
            System.exit(1);   //si se selecciona salir o se cierra la ventana
        }
        texto.toLowerCase();
        return texto;
    }

    /**
     * @brief: Ventana que da la opcion de iniciar la construcción del índice o salir del programa
     * @params: N/A
     * @return: opcion seleccionada
     */
    public int comenzar()
    {
        int opcion = JOptionPane.showOptionDialog(null, 
                "Que desea hacer?", 
                "Seleccione una opcion", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE, 
                null, 
                options,
                "Indexar");
        if (opcion == JOptionPane.CLOSED_OPTION || opcion == 1)
        {
            System.exit(1);   //si se selecciona salir o se cierra la ventana
        }

        return opcion;
    }

    /**
     * @brief: Ventana para seleccionar opciones de normalización de términos y de construcción del índice o salir del programa
     * @params: N/A
     * @return: opciones seleccionadas en forma de booleanos
     */
    public boolean[] configuracion(){
        JCheckBox stemmingCheck = new JCheckBox("Stemming");
        JCheckBox simbolosCheck = new JCheckBox("Signos de puntuacion");
        JCheckBox stopCheck = new JCheckBox("Elimina stop words");
        Object[] params = {stemmingCheck, simbolosCheck,stopCheck, "Aceptar", "Cancelar"};
        int n = JOptionPane.showOptionDialog(null,null, "Seleccione las opciones de indexado",JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE, 
                null,  params, "Opciones");
        if (n == JOptionPane.CLOSED_OPTION || n == 4)
        {
            System.exit(1);  //Si se selecciona salir o se cierra la ventana se finaliza el programa
        }
        boolean []vals = {stemmingCheck.isSelected(), simbolosCheck.isSelected(), stopCheck.isSelected()}; //stemm = [0], simbolos = [1], sin stopWords = [2]
        return vals;
    }

    public void mostrarResultados(String results){
        JTextArea textArea = new JTextArea(results);
        JScrollPane scrollPane = new JScrollPane(textArea);  
        textArea.setLineWrap(true);  
        textArea.setWrapStyleWord(true); 
        scrollPane.setPreferredSize( new Dimension( 500, 500 ) );
        JOptionPane.showMessageDialog(null, scrollPane, "Resultados de la búsqueda",  
                JOptionPane.OK_CANCEL_OPTION);
    }
}
