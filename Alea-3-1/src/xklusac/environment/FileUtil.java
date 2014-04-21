package xklusac.environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Class FileUtil is used for working with files and paths.
 * 
 * @author Gabriela Podolnikova
 */
public class FileUtil {
    
    /**
     * Returns the path to the directory where the desired results are located.
     * 
     * @param path path to be changed
     * 
     * @return the new path
     */
    public static String getPath(String path) {
        path = path.replace("\\", "/");
        int i = path.lastIndexOf("/");
        String fileName = path.substring(i+1);
        String directory = path.substring(0,i);
        String newPath;
        if (fileName.startsWith("Results") || fileName.startsWith("RGraphs") || fileName.startsWith("SGraphs") || fileName.startsWith("WGraphs")) {
            newPath = directory + File.separator + ExperimentSetup.getDir(1) + File.separator + fileName;
        } else {
            newPath = directory + File.separator + ExperimentSetup.getDir(3) + File.separator + fileName;
        }
        //System.out.println(newPath);
        return newPath;
    }
    
    /**
     * Copies a file. 
     * 
     * @param sourceFile the source file
     * @param destFile the destination file
     * 
     * @throws IOException 
     * if copying fails
     */
    public static void copyFile(File sourceFile, File destFile) throws IOException {
	if (!sourceFile.exists()) {
		return;
	}
	if (!destFile.exists()) {
		destFile.createNewFile();
	}
	FileChannel source = null;
	FileChannel destination = null;
	source = new FileInputStream(sourceFile).getChannel();
	destination = new FileOutputStream(destFile).getChannel();
	if (destination != null && source != null) {
		destination.transferFrom(source, 0, source.size());
	}
	if (source != null) {
		source.close();
	}
	if (destination != null) {
		destination.close();
	}

}
}
