import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;


public class Main {


    public static void main(String[] args) {

            //Colocar aqui la logica.

    }


















    ///no borrar
    public static final String ARCHIVO_NO_EXISTE = "Archivo no existe";
    public static final String SIN_PERMISOS_PARA_CREAR_ARCHIVOS = "Sin permisos para crear archivos";
    public static final String ARCHIVO__NO_PUEDE_CREARSE_ = "Archivo No puede crearse. ";
    public static final String NO_SE_PUEDE_GRABAR_EN_UN_ARCHIVO_NO_CREAD = "No se puede grabar en un archivo no creado";
    public static final String ARCHIVO_YA_CERRADO = "Archivo ya cerrado";
    public static final String IMPOSIBLE_CERRAR_UN_ARCHIVO_QUE_NO_SE_ABR = "Imposible cerrar un archivo que no se abrio";

    private static HashMap<File, LectorArchivos> myArchs;
    private static HashMap<File, EscritorDeArchivos> myWrittenArchs;

    static {
        myArchs = new HashMap<File, LectorArchivos>();
        myWrittenArchs = new HashMap<File, EscritorDeArchivos>();
    }

    public static File abrir(String string) {
        File arch = new File(string);
        LectorArchivos l = new LectorArchivos(arch, string);
        myArchs.put(arch, l);
        return arch;
    }

    public static File abrir(String string, boolean createIfNotExists) {
        if (createIfNotExists) {
            File arch = new File(string);

            if (!arch.exists()) {
                try {
                    if (arch.createNewFile()) {
                        arch.setReadable(true);
                        arch.setWritable(true);
                    } else {
                        throw new IOException(SIN_PERMISOS_PARA_CREAR_ARCHIVOS);
                    }
                } catch (IOException ex) {
                    System.out.println(ARCHIVO__NO_PUEDE_CREARSE_ + ex.getClass().getName() + ex.getMessage());
                }
            }

            if (arch.exists()) {
                FileWriter fw = null;
                try {
                    fw = new FileWriter(arch.getAbsoluteFile());
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write("");
                    bw.close();
                    fw.close();
                    EscritorDeArchivos esc = new EscritorDeArchivos(arch);
                    myWrittenArchs.put(arch, esc);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            return arch;
        } else {
            return abrir(string);
        }
    }

    public static <T> void grabar(File archS, T reg) {
        EscritorDeArchivos l = myWrittenArchs.get(archS);
        if (l != null) {
            l.setRegistro(reg);
            l.recordToFile(reg);
        } else {
            System.out.println(NO_SE_PUEDE_GRABAR_EN_UN_ARCHIVO_NO_CREAD);
        }
    }

    public static <T> T leer(File archivo, T registro) {
        LectorArchivos l = myArchs.get(archivo);
        if (l != null) {
            registro = (T) l.getNextRecord(registro);
            return registro;
        }
        return null;
    }

    public static <T> T leerIndexado(File archivo, T registro) {
        LectorArchivos l = myArchs.get(archivo);
        if (l != null) {
            registro = (T) l.getNextRecordIndexed(registro);
            return registro;
        }
        return null;
    }

    public static boolean FDA(File archivo) {
        LectorArchivos l = myArchs.get(archivo);
        if (l == null) {
            System.out.println(ARCHIVO_NO_EXISTE);
        }
        return l.FDA();
    }

    public static void cerrar(File archivo) {
        if (archivo != null) {
            LectorArchivos l = myArchs.get(archivo);
            if (l != null) {
                //l.dispose();
                myArchs.remove(archivo);
            } else {
                EscritorDeArchivos tle = myWrittenArchs.get(archivo);
                if (tle != null) {
                    tle.writeIntoFileContents(tle.getRegistro());
                    myWrittenArchs.remove(archivo);
                } else {
                    System.out.println(ARCHIVO_YA_CERRADO);
                }
            }
        } else {
            System.out.println(IMPOSIBLE_CERRAR_UN_ARCHIVO_QUE_NO_SE_ABR);
        }
    }
}



class Constants {

    public static final String DATE_SEPARATOR = "/";
    public static final String SPACE = " ";
    public static final String SLASH = "/";
    public static final String SET = "set";
    public static final String GET = "get";
    public static final String EXCEPCION_OCURRIDA_ = "Excepcion ocurrida ";
    public static final String FIELD_SEPARATOR = ":";
    public static final String NEW_LINE = "\n";
    public static final String GM_T3 = "GMT-3";
    public static final String COMMENTED_LINE="#";
    public static final String NOT_SUPPORTED="Not supported yet.";
    public static final String COMMA = ",";
    public static final String DOT=".";
}

class EscritorDeArchivos {


    private File myFile;
    private ArrayList<String> lines;
    private Object registro;

    public Object getRegistro() {
        return registro;
    }

    public void setRegistro(Object registro) {
        this.registro = registro;
    }

    public EscritorDeArchivos(File myFile) {
        this.myFile = myFile;
        lines = new ArrayList<String>();
    }

    public <T> void recordToFile(T reg) {
        StringBuilder builder = new StringBuilder();
        for (Field f : reg.getClass().getDeclaredFields()) {
            try {
                Method method = reg.getClass().getDeclaredMethod(Utils.getGetMethod(f.getName()));
                Object results = method.invoke(reg);
                if (results.getClass().isAssignableFrom(Date.class)) {
                    results = formatDate(results);
                }
                builder.append(results);
                builder.append(Constants.FIELD_SEPARATOR);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        String lineToSave = builder.toString().substring(0, builder.length() - 1);
        //    System.out.println(lineToSave);
        lines.add(lineToSave);
    }

    private void writeIntoFile(String substring) {
        StringBuilder myString = new StringBuilder();
        for (String lineas : lines) {
            myString.append(lineas);
            myString.append(Constants.NEW_LINE);
        }
    }

    private Object formatDate(Object results) {
        StringBuilder builderInternal = new StringBuilder();
        Calendar cal = Calendar.getInstance();
        cal.setTime((Date) results);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        builderInternal.append(Constants.SPACE);
        builderInternal.append(day);
        builderInternal.append(Constants.DATE_SEPARATOR);
        builderInternal.append(month);
        builderInternal.append(Constants.DATE_SEPARATOR);
        builderInternal.append(year);
        builderInternal.append(Constants.SPACE);
        return builderInternal;
    }

    protected void writeIntoFileContents(Object registro) {
        try {


            int cantClaves = 0;

            ArrayList<Object> theContents = new ArrayList<Object>();
            for (String currentLine : lines) {
                String[] fields = currentLine.split(Constants.FIELD_SEPARATOR);
                Object localReg = (Object) registro.getClass().newInstance();
                int currentFieldNumber = 0;
                for (Field f : registro.getClass().getDeclaredFields()) {
                    Method method = localReg.getClass().getDeclaredMethod(Utils.getSetMethod(f.getName()), f.getType());
                    method.invoke(localReg, Utils.tranformAccordingType(f.getType(), fields[currentFieldNumber]));
                    currentFieldNumber++;
                    if (f.getAnnotation(Clave.class)!=null){
                        cantClaves+=1;
                    }
                }
                theContents.add(localReg);
            }

            if (cantClaves > 0){
                final int claves = cantClaves;
                Collections.sort(theContents, new Comparator<Object>() {
                    @Override
                    public int compare(Object obj1, Object obj2) {
                        try {
                            Field[] campos = obj1.getClass().getDeclaredFields();
                            for (int i = 0; i < claves; i++) {
                                Field f = campos[i];
                                Method method = obj1.getClass().getDeclaredMethod(Utils.getGetMethod(f.getName()));
                                Method method2 = obj2.getClass().getDeclaredMethod(Utils.getGetMethod(f.getName()));
                                Comparable comparable = (Comparable) method.invoke(obj1);
                                Comparable comparable2 = (Comparable) method2.invoke(obj2);
                                int comparation = comparable.compareTo(comparable2);
                                if (comparation != 0) {
                                    return comparation;
                                }
                            }
                            return 0;
                        } catch (Exception e) {

                        }
                        return 0;
                    }
                });
            }




            StringBuilder finalBuilder = new StringBuilder();
            for (Object lineas : theContents) {

                StringBuilder builder = new StringBuilder();
                for (Field f : lineas.getClass().getDeclaredFields()) {
                    try {
                        Method method = lineas.getClass().getDeclaredMethod(Utils.getGetMethod(f.getName()));
                        Object results = method.invoke(lineas);
                        if (results.getClass().isAssignableFrom(Date.class)) {
                            results = formatDate(results);
                        }
                        builder.append(results);
                        builder.append(Constants.FIELD_SEPARATOR);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                String lineToSave = builder.toString().substring(0, builder.length() - 1);
                finalBuilder.append(lineToSave);
                finalBuilder.append(Constants.NEW_LINE);
            }

            String contents = finalBuilder.toString();
            FileWriter fw = null;

            fw = new FileWriter(myFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(contents);
            bw.close();
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}


class LectorArchivos<T> {


    private File myFile;
    private int currentCounter;
    private ArrayList<String> lines;
    private ArrayList<Object> theContents;
    private Object registro;

    public LectorArchivos(File myFile, String name) {
        this.myFile = myFile;
        currentCounter = 0;
    }

    public <T> void getContents(T registro) {

        this.registro = registro;

        theContents = (ArrayList<Object>) new ArrayList<T>();
        try {

                 lines = new ArrayList<String>();
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); //Here you declare your BufferedReader object and instance it.
                for (int i = 0; i < 2000; i++) {
                    lines.add(br.readLine());
                }

                for (String currentLine : lines) {
                    String[] fields = currentLine.split(Constants.FIELD_SEPARATOR);
                    T localReg = (T) registro.getClass().newInstance();
                    int currentFieldNumber = 0;
                    for (Field f : registro.getClass().getDeclaredFields()) {
                        Method method = localReg.getClass().getDeclaredMethod(Utils.getSetMethod(f.getName()), f.getType());
                        method.invoke(localReg, Utils.tranformAccordingType(f.getType(), fields[currentFieldNumber]));
                        currentFieldNumber++;
                    }
                    theContents.add(localReg);
                }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public Object getRegistro() {
        return registro;
    }

    public <T> T getNextRecord(T registro) {

        if (currentCounter == 0) {
            getContents(registro);
        }
        if (currentCounter < theContents.size()) {
            T localVar = (T) theContents.get(currentCounter);
            currentCounter++;
            return localVar;
        }
        currentCounter++;
        return null;

    }

    public boolean FDA() {
        int value = (theContents == null) ? 0 : theContents.size();
        return currentCounter > (value);
    }

    void dispose() {
        throw new UnsupportedOperationException(Constants.NOT_SUPPORTED); //To change body of generated methods, choose Tools | Templates.
    }

    public <T> T getNextRecordIndexed(T registro) {
        getContents(registro);
        try {
            for (Object object : theContents){
                for (Field f : object.getClass().getDeclaredFields()) {
                    Method method = object.getClass().getDeclaredMethod(Utils.getGetMethod(f.getName()));
                    Object value = method.invoke(object);

                    //en el host
                    Method method2 = registro.getClass().getDeclaredMethod(Utils.getGetMethod(f.getName()));
                    Object value2 = method.invoke(registro);
                    if (f.getAnnotation(Clave.class)!=null){
                        if (value.equals(value2)){
                            return (T) object;
                        }
                    }
                }
            }
        } catch (Exception e){

        }
        return null;
    }
}

class Utils {


    public static <T> T tranformAccordingType(Class<T> type, String string) {

        if (type.isAssignableFrom(String.class)) {
            return (T) string;
        } else if (type.isAssignableFrom(Date.class)) {
            String[] date = string.split(Constants.DATE_SEPARATOR);
            int year = Integer.parseInt(date[2].trim());
            int month = Integer.parseInt(date[1].trim());
            int day = Integer.parseInt(date[0].trim());
            Date d;
            Calendar cal = GregorianCalendar.getInstance();
            cal.set( year, month, day);
            d = cal.getTime();
            return (T) d;
        }  else if (type.isAssignableFrom(double.class) || (type.isAssignableFrom(Double.class))) {
            return (T) new Double(string.trim().replaceAll(Constants.COMMA, Constants.DOT));
        } else if (type.isAssignableFrom(float.class) || type.isAssignableFrom(Float.class)) {
            return (T) new Float(string.trim().replaceAll(Constants.COMMA, Constants.DOT));
        } else if (type.isAssignableFrom(int.class) || type.isAssignableFrom(Integer.class)) {
            //takeout all spaces
            return (T) new Integer(string.trim());
        }
        return null;
        //To change body of generated methods, choose Tools | Templates.
    }

    public static <T> T[] copyArray(T[] vector){
        T[] another = (T[]) Array.newInstance(vector.getClass().getComponentType(), vector.length);
        System.arraycopy(vector, 0, another, 0, vector.length);
        return another;
    }

    public static String getSetMethod(String fieldName) {
        // TODO Auto-generated method stub
        String firstWithCapitalLetter = fieldName.toUpperCase().substring(0, 1);
        String restOfMethodName = fieldName.substring(1, fieldName.length());
        return Constants.SET + firstWithCapitalLetter + restOfMethodName;
    }

    public static String getGetMethod(String fieldName) {
        // TODO Auto-generated method stub
        String firstWithCapitalLetter = fieldName.toUpperCase().substring(0, 1);
        String restOfMethodName = fieldName.substring(1, fieldName.length());
        String methodName = Constants.GET + firstWithCapitalLetter + restOfMethodName;
        return methodName;
    }


    public static boolean esMenor(Object obj1, Object obj2) {
        return  compareTo(obj1, obj2) < 0;
    }

    private static int compareTo(Object obj1, Object obj2) {
        try {
            Object localReg = (Object) obj1.getClass().newInstance();
            int currentFieldNumber = 0;
            int claves = 0;
            for (Field f : obj1.getClass().getDeclaredFields()) {
                if (f.getAnnotation(Clave.class)!=null){
                    claves+=1;
                }
            }

            Field[] campos = obj1.getClass().getDeclaredFields();
            for (int i = 0; i < claves; i++) {
                Field f = campos[i];
                Method method = obj1.getClass().getDeclaredMethod(Utils.getGetMethod(f.getName()));
                Method method2 = obj2.getClass().getDeclaredMethod(Utils.getGetMethod(f.getName()));
                Comparable comparable = (Comparable) method.invoke(obj1);
                Comparable comparable2 = (Comparable) method2.invoke(obj2);
                int comparation = comparable.compareTo(comparable2);
                if (comparation != 0) {
                    return comparation;
                }
            }
            return 0;
        } catch (Exception e) {

        }
        return 0;
    }

    public static boolean esMayor(Object someString, Object provincia1) {
        return compareTo(someString, provincia1) > 0;
    }

    public static boolean esIgual(String someString, String provincia1) {
        return compareTo(someString, provincia1) == 0;
    }

    public static String getType(String type) {
        if (type.equalsIgnoreCase("Alfanumerico") || type.equalsIgnoreCase("cadena") || type.equalsIgnoreCase("char")|| type.equalsIgnoreCase("caracter") ){
            return "String";
        } else if (type.equalsIgnoreCase("entero")){
            return "int";
        } else if (type.equalsIgnoreCase("real")){
            return "double";
        } else {
            return "boolean";
        }
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD) //can use in method only.
@interface Clave {
}
