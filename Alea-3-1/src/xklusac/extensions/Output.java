package xklusac.extensions;
import java.io.*;
import xklusac.environment.FileUtil;
/**
 * Class Output<p>
 * This class is used to store results into text file.
 * @author Dalibor Klusacek
 */
public class Output{
    
    /** This methods stores results "value" into file "s"
     *
     */
    
    public void writeResults(String s, double value)
    throws IOException {
        
        String add = FileUtil.getPath(s);
        PrintWriter pw = new PrintWriter(new FileWriter(add,true));
        pw.println(value);
        pw.close();
    }
    
    /** This methods writes out string "value" into file "s"
     *
     */
    
    public void writeString(String s, String value)
    throws IOException {
        
        String add = FileUtil.getPath(s);
        PrintWriter pw = new PrintWriter(new FileWriter(add,true));
        pw.println(value);
        pw.close();
    }
    
    /** This methods deletes file specified "s" parameter.
     *
     */
    public void deleteResults(String s)throws IOException {
        
        String add = FileUtil.getPath(s);
        PrintWriter pw = new PrintWriter(new FileWriter(add));
        //PrintWriter pw = new PrintWriter(s);
        pw.close();
    }
    
}


