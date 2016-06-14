/**
 * Controlador que maneja las instrucciones a ejecutar del programa
 *
 * @Daniel Muñoz, b24645
 * @Jeffry Venegas, b37495
 * @version 2, 15/5/16
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.io.*;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Comparator;
import java.util.Collections;
import java.lang.*;
import java.util.*;

public class Controlador
{
    private Arbol arbol = new Arbol(); //Indice
    private Interfaz interfaz = new Interfaz();
    private Stemm_es st; //clase que implementa el algoritmo de Porter para stemming
    private String resultados = "";
    private String cantidad = "";
    private String[] terminosConsulta;
    private List terminos = new ArrayList();
    private List consultas = new ArrayList();
    private TreeMap<String,Integer> termDocs = new TreeMap<String, Integer>();
    private TreeMap<String, Double> pesoConsultas = new TreeMap<String, Double>();
    private TreeMap<String, Double> ranking = new TreeMap<String, Double>();
    private TreeMap<String,String[]> docs = new TreeMap<String, String[]>();
    private int cuenta = 0;
    private int noDocuments;

    /**
     * Constructor
     */
    public Controlador()
    {}

    /**
     * Revisa los documentos a leer
     *
     * @PARAM = boolean stem, simb y stop: configuración de la construcción del indice.
     * @return = N/A
     */
    public String run(boolean stemm, boolean simb, boolean stop, boolean jaccard)
    {
        try
        {
            File textFilesDirectory = new File("documents"); //Revisa la carpeta
            if(!textFilesDirectory.isDirectory()) {
                System.err.println("No directory called " + textFilesDirectory.getName() + "found..."); //Si no existe nada, reporta que no encontro el directorio
                System.err.println("Exit System");
                System.exit(1);
            }
            File[] documents = textFilesDirectory.listFiles(); //Crea un array de archivos
            noDocuments = documents.length;
            for(int i = 0; i < noDocuments; i++)
            {
                Leer(documents[i], stemm, simb, stop, jaccard, terminosConsulta); //Lee el contenido, le envía las opciones ingresadas por el usuario
            }

            File temp;
            File[] documentos = documents;
            //             for(Map.Entry<String,Double> entry : ranking.entrySet()) {
            //                 String key = entry.getKey();
            //                 Double value = entry.getValue();
            // 
            //                 System.out.println(key + " => " + value);
            //             }

            //Se ordenan los resultados usando bubblesort
            //             for (int i = 0; i < ranking.size(); i++) 
            //             {
            //                 for (int j = 1; j < ranking.size() - i; j++) 
            //                 { 
            //                     if(jaccard == true){ //Jaccard ordena de mayor a menor puntaje
            //                         if (ranking.get(documentos[j - 1].getName()) < ranking.get(documentos[j].getName())) 
            //                         {                        
            //                             temp = documentos[j - 1];
            //                             documentos[j - 1] = documentos[j];
            //                             documentos[j] = temp;
            //                         }
            //                     }
            //                     else{//ordena de menor a mayor angulo de coseno
            //                         if (ranking.get(documentos[j - 1].getName()) > ranking.get(documentos[j].getName())) 
            //                         {                        
            //                             temp = documentos[j - 1];
            //                             documentos[j - 1] = documentos[j];
            //                             documentos[j] = temp;
            //                         }
            //                     }
            //                 }
            //             }

            documentos = quickSort(documentos,0,ranking.size()-1, jaccard);
            int resultsRelevantes = 0;
            int cuantos = documentos.length;
            int i = 0;
            try{
                if(Integer.parseInt(cantidad) < cuantos){
                    cuantos = Integer.parseInt(cantidad);
                }
            }
            catch(NumberFormatException n){}

            while(i < documentos.length && cuantos > 0)
            {
                if(ranking.get(documentos[i].getName()) > 0){
                    resultados += documentos[i].getName()+" -- "+ranking.get(documentos[i].getName())+"\n";
                    resultsRelevantes++;
                    cuantos--;
                }
                i++;
            }
            resultados += resultsRelevantes+" resultados\n";

            //int contar = arbol.contar(); 
            //System.out.println("Tamaño del índice "+contar);  //Tamaño del indice            
            PrintWriter writer = new PrintWriter("index" + File.separator + "invertedindex.txt", "UTF-8"); // Escribe el contenido de la tabla hash formada
            Set setOfKeys1 = arbol.getRaiz().keySet();
            Iterator iterator1 = setOfKeys1.iterator();

            while (iterator1.hasNext() == true) { //se guarda el indice en un archivo
                String key = (String) iterator1.next();
                writer.println(key+"="+ranking.get(key)); //se guarda el termino y su respectiva lista de postings
            }
            writer.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return resultados;
    }

    /**
     * Lee los contenidos de los archivos
     *
     * @PARAM = File: archivo a leer; stem, simb y stop: configuración de la construcción del indice; consultas: terminos de la consulta
     * @return = N/A
     */
    public void Leer(File aFile, boolean stemm, boolean simb, boolean stop, boolean jaccard,String[] consultas)
    {
        ArrayList<String> documentText = new ArrayList<String>();
        try 
        {
            BufferedReader input =  new BufferedReader(new FileReader(aFile));  //archivo a leer
            try
            {
                termDocs.clear();
                terminos.clear();
                pesoConsultas.clear();
                cuenta = 0;
                arbol.limpiar();
                List pesosDocumentos = new ArrayList();
                List documentosNormal = new ArrayList();
                String line = null;

                for(int x = 0; x < consultas.length; x++)
                {
                    termDocs.put(consultas[x],0);
                    terminos.add(consultas[x]);
                    if(pesoConsultas.containsKey(consultas[x]) == false)
                    {
                        pesoConsultas.put(consultas[x],1.0);
                    }
                    else
                    {
                        double peso = pesoConsultas.get(consultas[x]);
                        peso++;
                        pesoConsultas.put(consultas[x],peso);
                    }
                }

                while (( line = input.readLine()) != null) //lee linea por linea, si es nulo llego al final del archivo
                {
                    String term = line.toLowerCase(); //forma de parsear, vuelve todo a minuscula para no caer en duplicacion de datos                   
                    term = term.replaceAll("á","a"); //Se reemplazan las tildes
                    term = term.replaceAll("é","e");
                    term = term.replaceAll("í","i");
                    term = term.replaceAll("ó","o");
                    term = term.replaceAll("ú","u");

                    if(simb == true || stemm == true){ //si el usuario elige eliminar signos de puntuacion o stemming
                        term = term.replaceAll("[^\\w:/%$]","");
                    }

                    if(stop == true){ // si el usuario elige eliminar stopwords
                        String stops = "a aca ahi al algo algun alguna algunas alguno algunos ante aquel aquella aquellas"+
                            "aquello aquellos aqui arriba asi atras aun aunque bajo"+
                            "bien cabe cada casi cierta ciertas cierto ciertos como con cual cuales cualquier"+
                            "cualquiera cualquieras cuan cuando cuanta cuantas cuanto cuantos de del demas dentro"+
                            "desde donde dos el ella ellas ello ellos en encima entonces entre era eramos eran"+
                            "eras eres es esa esas ese eso esos esta estaba estado estais estamos estan estar"+
                            "estas este esto estos estoy etc fin fue fueron fui fuimos ha hace hacemos hacen hacer haces hacia hago hasta incluso ir"+
                            "jamas junto juntos la las lo los mas me menos mi mia mias mientras mio mios mis"+
                            "misma mismas mismo mismos modo mucha muchas mucho muchos muy nada niningun ninguna"+
                            "ningunas ninguno ningunos no nos nosotras nosotros nuestra nuestras nuestro nuestros otra otras otro otros"+
                            "para parecer pero poca pocas poco pocos por porque puede pueden puedo"+
                            "pues que querer quien quien quienes quienesquiera quienquiera se segun ser si siempre siendo sin sino so sobre"+
                            "sois solamente solo somos soy sr sra sres sta su consultarsus suya suyas suyo suyos tal"+
                            "tales tan tanta tantas tanto tantos te teneis tenemos tener tengo ti tiene tienen toda"+
                            "todas todo todos tras tu tu tus tuya tuyo tuyos un una unas uno unos usa usamos usan usar usas"+
                            "uso usted ustedes va vamos van varias varios vaya vosotras vosotros voy vuestra vuestras vuestro vuestros y ya yo";
                        String[] stopwords = stops.split(" "); //Se genera un arreglo con los stopwords para iterar
                        for(int j = 0; j < stopwords.length; j++){ //se revisa si el termino es un stop word
                            if(term.equals(stopwords[j]) == true){
                                term = "";
                            }
                        }                    
                    }

                    if (stemm == true){ //si el usuario elige hacer stemming se usa el algoritmo de la clase Stemm_es
                        term = st.stemm(term);
                    }

                    if(jaccard == true){
                        documentText.add(term);
                    }

                    if(term.equals("") == false && term.equals (" ") == false){ //Una vez aplicados los cambios al término se revisa el indice
                        if(arbol.buscar(term) == false)
                        {
                            arbol.agregar(term, aFile.getName()); //si no existe el termino, lo agrega con su respectivo archivo
                            if(terminos.contains(term) == false)
                            {
                                terminos.add(term); // agrega los terminos de todo el documento pero revisa antes si ya estan incluidos por la consulta
                            }
                            if(pesoConsultas.containsKey(term) == false)
                            {
                                double a = 0;
                                pesoConsultas.put(term,a); // agrega los terminos de todo el documento pero revisa antes si ya estan incluidos por la consulta
                            }
                            if(termDocs.containsKey(term) == true)
                            {
                                int dfActual = termDocs.get(term);
                                dfActual++;
                                termDocs.put(term,dfActual);
                            }
                            else
                            {
                                termDocs.put(term,1); 
                            }
                        }
                        else
                        {
                            arbol.sumar(term,aFile.getName()); // si existe busca el termino, luego el archivo y le suma 1 a la frecuencia
                        }
                    }
                }

                for(int o = 0; o < terminos.size(); o++) 
                {
                    pesosDocumentos.add(arbol.buscarTF((String)terminos.get(o), aFile.getName())); //busca la frecuencia con logaritmos de una vez por cada termino en el documento
                }

                documentosNormal = arbol.normalizar(pesosDocumentos); //normaliza los vectores usando coseno
                cuenta++;
                obtenerDocs(aFile.getName());

                for(int g = 0; g < terminos.size(); g++)
                {
                    if(pesoConsultas.containsKey(terminos.get(g)) == true)
                    {
                        double pesoC = pesoConsultas.get(terminos.get(g));
                        if(pesoC > 0.0)
                        {
                            pesoC = 1 + (Math.log10(pesoC));
                            pesoConsultas.put((String)terminos.get(g), pesoC);
                        }
                    }
                } 

                if(jaccard == true){
                    String [] doc = documentText.toArray(new String[0]);
                    double peso = pesoJaccard(terminosConsulta, doc);
                    ranking.put(aFile.getName(),peso);
                }
                else{
                    sacarPeso();
                    double pesoFinal = 0;

                    for(int a = 0; a < terminos.size(); a++){
                        double producto = (double)documentosNormal.get(a)*pesoConsultas.get(terminos.get(a));
                        pesoFinal += producto;                     
                    }
                    ranking.put(aFile.getName(),pesoFinal);
                }
            }
            finally
            {
                input.close(); //se cierra el archivo de lectura
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * @brief Calcula el tf-idf y lo guarda en un mapa que contiene el termino y su peso.
     */
    public void sacarPeso()
    {
        for(int i = 0; i < terminos.size(); i++)
        {
            int df = termDocs.get(terminos.get(i));
            if(df == 0)
            {
                df = 1;
            }
            double idf = 1 + (Math.log10(noDocuments/df));
            double tf = pesoConsultas.get(terminos.get(i));
            double peso = tf*idf;        
            pesoConsultas.put((String)terminos.get(i), peso);
        }
    }

    /**
     * @brief Calcula la similitud entre consulta y documento usando el coeficiente de Jaccard. Recibe terminos de consulta y documento en un arreglo de Strings.
     * @param String[], String[]
     * @return similutud de la consulta y el documento, un valor entre 0 y 1.
     */
    public double pesoJaccard(String[] sSplit, String[] tSplit)  
    {  
        //calcula intersection  
        List<String> intersection=new ArrayList<String>();  
        for(int i=0;i<sSplit.length;i++)  
        {  
            for(int j=0;j<tSplit.length;j++)  
            {  
                if(!intersection.contains(sSplit[i])){      
                    if(sSplit[i].equals(tSplit[j]))   
                    {  
                        intersection.add(sSplit[i]);  
                        break;  
                    }  
                }
            }  
        }  

        //calcula union  
        List<String> union=new ArrayList<String>();  
        if(sSplit.length>tSplit.length)                      
        {  
            for(int i=0;i<sSplit.length;i++){ 
                if(!union.contains(sSplit[i])){  
                    union.add(sSplit[i]);  
                }
            }
            for(int i=0;i<tSplit.length;i++){
                if(!union.contains(tSplit[i])){
                    union.add(tSplit[i]);  
                }
            }
        }  
        else  
        {  
            for(int i=0;i<tSplit.length;i++){  
                if(!union.contains(tSplit[i])){  
                    union.add(tSplit[i]);  
                }
            }
            for(int i=0;i<sSplit.length;i++){
                if(!union.contains(sSplit[i])){
                    union.add(sSplit[i]);  
                }
            }
        }    
        return ((double)intersection.size())/((double)union.size());  
    }    

    /**
     * Obtiene todos los busca el termino, recibido por parametro, en todos los documentos.
     * @param String
     */
    public void obtenerDocs(String nombre)
    {
        File textFilesDirectory = new File("documents"); //Revisa la carpeta
        if(!textFilesDirectory.isDirectory()) {
            System.err.println("No directory called " + textFilesDirectory.getName() + "found..."); //Si no existe nada, reporta que no encontro el directorio
            System.err.println("Exit System");
            System.exit(1);
        }
        File[] documents = textFilesDirectory.listFiles(); //Crea un array de archivos
        int nuDocuments = documents.length;
        for(int i = 0; i < nuDocuments; i++)//busca en todos los documentos
        {
            cuenta++;
            LeerDF(documents[i],nombre); //Lee el contenido, le envía las opciones ingresadas por el usuario          
        }
    }

    /**
     * @brief Calcula la frecuencia (DF) de un termino en un documento.Recibe el archivo y el termino a buscar.
     * @param, File, String
     * @return void
     */
    public void LeerDF(File aFile, String nombre)
    {
        try 
        {
            BufferedReader input =  new BufferedReader(new FileReader(aFile));  //archivo a leer
            try
            {
                if(nombre.equals(aFile.getName()) == false)
                {
                    String line = null;
                    List margen = new ArrayList(); 
                    while (( line = input.readLine()) != null) //lee linea por linea, si es nulo llego al final del archivo
                    {
                        String term = line.toLowerCase();

                        if(term.equals("") == false && term.equals (" ") == false){ //Una vez aplicados los cambios al término se revisa el indice
                            if(termDocs.containsKey(term) == true)
                            {
                                int dfActual = termDocs.get(term);
                                if(margen.contains(term) == false)
                                {
                                    dfActual++;
                                    termDocs.put(term,dfActual);
                                    margen.add(term);
                                }
                            }
                        }
                    }
                }
            }
            finally
            {
                input.close(); //se cierra el archivo de lectura
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        } 
    }

    /**
     * Transforma la consulta recibida en la interfaz a un arreglo de Strings
     */
    public void dividirConsulta(String consulta)
    {
        terminosConsulta = consulta.split(" ");
        for(int k = 0; k < terminosConsulta.length;k++)
        {
            //normalizacion de la consulta
            terminosConsulta[k].toLowerCase();
            terminosConsulta[k] = terminosConsulta[k].replaceAll("á","a"); //Se reemplazan las tildes
            terminosConsulta[k] = terminosConsulta[k].replaceAll("é","e");
            terminosConsulta[k] = terminosConsulta[k].replaceAll("í","i");
            terminosConsulta[k] = terminosConsulta[k].replaceAll("ó","o");
            terminosConsulta[k] = terminosConsulta[k].replaceAll("ú","u");
            terminosConsulta[k] = terminosConsulta[k].replaceAll("[^\\w:/%$]","");

            //             st = new Stemm_es();
            //             terminosConsulta[k] = st.stemm(terminosConsulta[k]);
            if(terminosConsulta[k] != null && terminosConsulta[k] != " "){
                terminos.add(terminosConsulta[k]);    
                consultas.add(terminosConsulta[k]);
            }
        }
    }

    /**
     * @brief: metodo de ordenamiento que utiliza el algoritmo quicksort. Dependiendo de si se usa jaccard o cosenos para puntaje ordena
     * de forma ascendente o descendente. Recibe un arreglo de archivos a ordenar, los limites del arreglo y un booleano indicando si se usa jaccard.
     * @param: File[]documentos, int left, int right, boolean jaccard
     */
    public File[] quickSort(File[] documentos, int left, int right, boolean jaccard) {
        int i = left, j = right;
        File tmp;
        double pivot = ranking.get(documentos[(left + right) / 2].getName());

        /* partition */

        while (i <= j) {
            if(jaccard == true){//De mayor a menor coeficiente
                while (ranking.get(documentos[i].getName()) > pivot)
                    i++;

                while (ranking.get(documentos[j].getName()) < pivot)
                    j--;
            }
            else{//de menor a mayor angulo de coseno
                while (ranking.get(documentos[i].getName()) < pivot)
                    i++;

                while (ranking.get(documentos[j].getName()) > pivot)
                    j--;
            }
            if (i <= j) {
                tmp = documentos[i];
                documentos[i] = documentos[j];
                documentos[j] = tmp;
                i++;
                j--;
            }
        }

        /* recursion */

        if (left < j)
            quickSort(documentos, left, j, jaccard);

        if (i < right)
            quickSort(documentos, i, right, jaccard);
        return documentos;
    }

    /**
     * Programa principal para elegir las opciones de configuración y normalizacion
     */
    public static void main(String args[])
    {
        boolean simb = false;
        boolean stemm = false; 
        boolean stop = false;
        boolean jaccard = false;
        Interfaz interfaz = new Interfaz();
        Controlador controlador = new Controlador();
        int opcion = 0;
        while(true){
            String consulta = interfaz.consultar();
            controlador.resultados = "Los resultados de la consulta: \""+consulta+" \"\n"+
            "Documento -- similitud consulta-documento\n"+
            "----------------------------------------------------------------\n";
            controlador.dividirConsulta(consulta);
            Object [] conf = interfaz.configuracion(); //se piden las opciones de normalización
            //             for(int i = 0 ; i < 100; i++){
            if((boolean)conf[0] == true)
            {
                controlador.st = new Stemm_es();
            }
            stemm = (boolean)conf[0];
            simb = (boolean)conf[1];
            stop = (boolean)conf[2];
            jaccard = (boolean)conf[3];
            controlador.cantidad = (String)conf[4];
            double inicioIndex = System.currentTimeMillis();
            String resultados = controlador.run(stemm, simb, stop, jaccard);  //se construye el  indice, se calculan pesos y se devuelven los documentos en orden de relevancia 
            interfaz.mostrarResultados(resultados);
            double finIndex = System.currentTimeMillis();
            double tiempo = finIndex - inicioIndex;  //se calcula la duracipn de construccion del indice
            FileWriter arch = null;
            PrintWriter writer = null;
            try{
                arch = new FileWriter("results.txt",true); // se guarda el tiempo de de ejecución de construccion del indice en un archivo
                writer = new PrintWriter(arch);
                writer.println(tiempo); 
                //                 writer.println(resultados);
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                try {
                    if (null != arch){
                        arch.close(); // se cierra archivo de escritura
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            //             }
        }
    }
}
