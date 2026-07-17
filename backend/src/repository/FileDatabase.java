package repository ;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class FileDatabase {


    private static final ConcurrentHashMap<String, Object> fileLocks = new ConcurrentHashMap<>();

    private static Object lockFor(Path filePath) {
        String key = filePath.toAbsolutePath().normalize().toString();
        return fileLocks.computeIfAbsent(key, k -> new Object());
    }

    //Reading all lines of a text file
    public static List<String> readLines(Path filePath) {
        synchronized (lockFor(filePath)) {
            List<String> lines = new ArrayList<>() ;
            File file = filePath.toFile() ;

            if (!file.exists() || file.length() == 0) {
                return lines ;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file) , StandardCharsets.UTF_8))) {
                String line ;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        lines.add(line) ;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading file at " + filePath + ": " + e.getMessage());
            }
            return lines ;
        }
    }

    //Rewriting the whole file with a list of new lines
    public static void writeLines(Path filePath , List<String> lines) {
        synchronized (lockFor(filePath)) {
            File file = filePath.toFile() ;
            File parent = file.getParentFile() ;
            if (parent != null && !parent.exists()) {
                parent.mkdirs() ;
            }
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file , false) , StandardCharsets.UTF_8))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            } catch (IOException e) {
                System.err.println("Error writing to file at " + filePath + ": " + e.getMessage());
            }
        }
    }

    //A method for appending a line in the file
    public static void appendLine (Path filePath , String line) {
        synchronized (lockFor(filePath)) {
            File file = filePath.toFile() ;
            File parent = file.getParentFile() ;
            if (parent != null && !parent.exists()) {
                parent.mkdirs() ;
            }
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file , true) , StandardCharsets.UTF_8))) {
                writer.write(line);
                writer.newLine();
            } catch (IOException e) {
                System.err.println("Error appending to file at " + filePath + ": " + e.getMessage());
            }
        }
    }
}
