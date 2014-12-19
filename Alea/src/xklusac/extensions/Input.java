package xklusac.extensions;
import java.io.*;
import java.util.*;
/**
 * Class Input<p>
 * This class reads data from specified file.
 *
 * @author Dalibor Klusacek
 */
public class Input {
    
    BufferedReader br;
    
    /**
     * Opens file "f".
     */
    public BufferedReader openFile(File f) {
        BufferedReader br = null;
        try{
            FileInputStream fr = new FileInputStream(f.getAbsoluteFile());
            InputStreamReader ifr = new InputStreamReader(fr, "Cp1250");
            br = new BufferedReader(ifr);
            return br;
        }catch(IOException ioe){
            //ioe.printStackTrace();
            System.out.println("Fail to open file!");
        }
        return br;
    }
    /**
     * Closes file mapped to the buffered reader "br".
     */
    
    public void closeFile(BufferedReader br) {
        try{
            br.close();
        }catch(IOException ioe){
            //ioe.printStackTrace();}
            System.out.println("Fail to close file!");
        }
    }
    
    /**
     * Reads all lines of file and stores them in a list (first line must contains number of klines of the file). Obsolete method.
     */
    
    public void  getLines(LinkedList list, BufferedReader br){
        String line = "";
        while(true){
            try {
                line = br.readLine();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if(line == null){
                break;
            }else{
                list.add(line);
            }
        }
    }
    public void  getLinesOrdered(LinkedList list, BufferedReader br){
        String end = "";
        String line_count = "0";
        int rounds = 0;
        
        
        try {
            line_count = br.readLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        rounds = Integer.parseInt(line_count);
        for(int i = 0; i < 1900; i++){
            list.add(" ");
        }
        for(int i = 0; i < rounds; i++){
            try {
                String line = br.readLine();
                int id = Integer.parseInt(line.split("\t")[0]);
                list.remove(id);                       
                list.add(id, line);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        for(int i = 0; i < 1000; i++){
            //System.out.println(i+" = "+list.get(i));
        }
    }
    
        public void  getCPUsLines(LinkedList list, BufferedReader br){
        String end = "";
        String line_count = "0";
        int rounds = 0;
        try {
            line_count = br.readLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        rounds = Integer.parseInt(line_count);
        for(int i = 0; i < rounds; i++){
            String line = "";
            try {
                line = br.readLine();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            
            int id = Integer.parseInt(line.split("\t")[0]);
            if(id > i){
                for(int j = i; j < id; j++){
                    list.add(j+" unused.");
                    i++;
                }
                list.add(line);
            }else{
                list.add(line);
            }
        }
    }
    /**
     * Reads specified number of lines of the file and stores them in a list.
     */
    
    public void  getLinesCount(LinkedList list, BufferedReader br, int count){
        String end = "";
        int rounds = count;
        for(int i = 0; i < rounds; i++){
            try {
                list.add(br.readLine());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    
    
}
