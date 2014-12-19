/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xklusac.environment;

import java.util.Arrays;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Gabriela Podolnikova
 */
public class AleaConfigurationTest {
    
    static AleaConfiguration con;
    
    @BeforeClass
    public static void setUp() throws Exception {
        con = new AleaConfiguration();
    }
    
    @Test
    public void getString() {
        String s = con.getString("path");
        assertEquals("simon3/", s);
        System.out.println(s);
        
    }
    
    @Test
    public void getInt() {
        int i = con.getInt("runtime_multiplicator");
        assertEquals(1, i);
        System.out.println(i);
    }
    
    @Test
    public void getIntArray() {
        int[] ia = con.getIntArray("algorithms");
        assertArrayEquals(new int[]{8,8,12,8,8,8,9,8,3,2,4,7,4,9}, ia);
        for (int i = 0; i<ia.length; i++) {
            System.out.print(ia[i]);
            if (i<ia.length-1) {
                System.out.print(", ");
            } else {
                System.out.println();
            }
        }
    }
    
    @Test
    public void getBoolean() {
        Boolean bool = con.getBoolean("visualize");
        assertEquals(true,bool);
        System.out.println(bool);
    }
    
    @Test
    public void getStringArrray() {
        String[] sa = con.getStringArray("data_sets");
        System.out.println(Arrays.asList(sa));
        assertArrayEquals(new String[]{"wagap12-13.swf", "wagap12-13.swf", "wagap12-13.swf", "wagap12-13.swf", "wagap12-13.swf", "wagap12-13.swf", "meta4-2013.swf", "meta4-2013.swf", "meta-2013.swf", "meta1-2013.swf", "meta2-2013.swf", "meta3-2013.swf", "wagap-2013.swf", "zewura-pul-rok-2012.swf", "hpc2n.swf"}, sa); 
    }
    
    @Test
    public void getDouble() {
        double d = con.getDouble("baudRate");
        assertEquals(10000, d, 0.001);
        System.out.println(d);
    }
}
