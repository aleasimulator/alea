package xklusac.extensions;
import java.io.*;
import xklusac.environment.FileUtil;
/**
 * Class Output<p>
 * This class is used to store results into text file.
 * @author Dalibor Klusacek
 */
public class Output{
    
    /** This method stores results "value" into file "s"
     *  
     */
    
    public void writeResults(String s, double value)
    throws IOException {
        
        String add = FileUtil.getPath(s);
        PrintWriter pw = new PrintWriter(new FileWriter(add,true));
        pw.println(value);
        pw.close();
    }
        
    /** This method writes out string "value" into file "s"
     *
     */
    
    public void writeString(String s, String value)
    throws IOException {
        
        String add = FileUtil.getPath(s);
        PrintWriter pw = new PrintWriter(new FileWriter(add,true));
        
        //PrintWriter pw = new PrintWriter(new FileWriter(s,true));
        pw.println(value);
        pw.close();
    }
    
    /** This method writes out string "value" into file "s"
     *
     */
    
    public void writeStringWriter(PrintWriter pw, String value)
    throws IOException {
        //System.out.println("ok "+value);
        pw.println(value);
        //pw.close();
    }
    
    public void writeStringWriterErr(PrintWriter pw, String value)
    throws IOException {
        //System.out.println("ok "+value);
        pw.println(value);
        //pw.close();
    }
        
    /** This method deletes file specified "s" parameter.
     *
     */
    public void deleteResults(String s)throws IOException {
        String add = FileUtil.getPath(s);
        PrintWriter pw = new PrintWriter(new FileWriter(add));
        
        //System.out.println("???? DELETED "+s);
        //PrintWriter pw = new PrintWriter(new FileWriter(s));
        //PrintWriter pw = new PrintWriter(s);
        pw.close();
    }
    
    public void closeWriter(PrintWriter pw)
    throws IOException {
        //System.out.println("Closing writer... ");
        pw.close();
    }
}


