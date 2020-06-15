package common;

import server.models.User;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class consists exclusively of static methods that process files and folders.
 * @author Jakub Reszka
 */
public abstract class FileManager {

    /**
     * Lists directory given in path including excluding subdirectories
     * @param path the path to directory
     * @return list of files' names
     */
    public static List<String> listDir(String path) {

        File folder = new File(path);
        if(!folder.exists() || !folder.isDirectory())
            throw new InvalidPathException("Folder of given path: " + path + "doesn't exist.");

        return Stream.of(folder.listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toList());
    }


    /**
     * Reads file given with file handle into byte array
     * @param file the handle to file
     * @return the array of bytes containing file's data
     * @throws IOException if an I/O error occurs
     */
    public static byte[] readFileToByteArray(File file) throws IOException {
        byte[] fileBytesArray = new byte[(int) file.length()];
        FileInputStream fileStream = new FileInputStream(file);
        BufferedInputStream bufferedStream = new BufferedInputStream(fileStream);
        DataInputStream dataStream = new DataInputStream(bufferedStream);
        dataStream.readFully(fileBytesArray, 0, fileBytesArray.length);
        fileStream.close();
        return fileBytesArray;
    }

    /**
     * Saves file given with byte array in destination's path
     * @param fileBytes the array of bytes containing file's data
     * @param path the destination's path
     * @param filename the name of file to be saved
     * @return the success of operation
     */
    public static boolean saveFileFromByteArray(byte[] fileBytes, String path, String filename) {
        File file = new File(Paths.get(path, filename).toString());

        boolean success = false;

        if (file.exists()) return success;

        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(fileBytes);
            os.close();
            success = true;
        } catch (IOException e) {
            success = false;
        }

        return success;
    }

    /**
     * Compares two lists of files' names
     * @param oldList the old list of files' names
     * @param newList the new list of files' names
     * @return the dictionary of keys "Added" and "Deleted" and values as list of filenames
     */
    public static Hashtable<String, List<String>> compareLists(List<String> oldList, List<String> newList) {

        Hashtable<String, List<String>> differences = new Hashtable<>();

        List<String> addedModels = new ArrayList<>(newList);
        addedModels.removeAll(oldList);

        List<String> deletedModels = new ArrayList<>(oldList);
        deletedModels.removeAll(newList);

        if(!addedModels.isEmpty()) differences.put("Added", addedModels);
        if(!deletedModels.isEmpty()) differences.put("Deleted", deletedModels);

        return differences;
    }

    /**
     * Waits until file is ready to be processed
     * @param file the file handle
     * @throws InterruptedException if any thread has interrupted the current thread
     */

    public static void waitTillFileIsReady(File file) throws InterruptedException {
        boolean fileIsLocked = true;
        while(fileIsLocked) {
            fileIsLocked = !file.renameTo(file);
            Thread.sleep(100);
        }

    }

}
