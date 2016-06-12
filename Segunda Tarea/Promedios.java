import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
public class Promedios{
  public static void main(String[] args){
    try{
      File textFilesDirectory = new File("Resultados"); //Revisa la carpeta
      if(!textFilesDirectory.isDirectory()) {
        System.err.println("No directory called " + textFilesDirectory.getName() + "found..."); //Si no existe nada, reporta que no encontro el directorio
        System.err.println("Exit System");
        System.exit(1);
      }
      File[] documents = textFilesDirectory.listFiles(); //Crea un array de archivos
      int noDocuments = documents.length;
      for(int i = 0; i < noDocuments; i++)
      {
        Leer(documents[i]); //Lee el contenido
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    
  }
  public static void Leer(File file){
    try {
      BufferedReader input =  new BufferedReader(new FileReader(file));
      try {
        double promedio = 0;
        double suma = 0;
        String line = null;
        List<Double> tiempos = new ArrayList<Double>();
        while (( line = input.readLine()) != null) //lee linea por linea, si es nulo llego al final del archivo
        {
          if(line.length() > 0){
            if(line.charAt(0) >= '0' && line.charAt(0) <= '9'){
              tiempos.add(Double.parseDouble(line));
            }
          }
        }
        
        for(double val:tiempos){
          suma += val;
        }
        promedio = suma / tiempos.size();
        FileWriter arch = null;
        PrintWriter writer = null;
        try{
          arch = new FileWriter("Promedios.txt",true);
          writer = new PrintWriter(arch);
          writer.println("Promedio de "+file+": "+promedio);
        }catch(Exception e){
          e.printStackTrace();
        }
        finally {
          try {
            if (null != arch){
              arch.close();
            }
          } catch (Exception e2) {
            e2.printStackTrace();
          }
        }
      }
      finally
      {
        input.close();
      }
    }
    
    catch (IOException ex)
    {
      ex.printStackTrace();
    }      
    
  }
}