package service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

public class Filestorageservice {
    private static final String STORAGE_DIR = "media" ;

    //limites sending huge files

    private static final long MAX_FILE_SIZE_BYTES = 20L * 1024 * 1024 ;

    //Constructor

    public Filestorageservice () {
        try {
            Files.createDirectories(Paths.get(STORAGE_DIR)) ;
        } catch (IOException e) {
            throw new RuntimeException("Could not create media storage directory: " + e.getMessage());
        }
    }

    //Saving the given file bytes under a new unique name and returns the relative path
    public String saveFile(byte[] fileBytes , String originalFileName) {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new RuntimeException("File is empty");
        }
        if (fileBytes.length > MAX_FILE_SIZE_BYTES) {
            throw new RuntimeException("File is too large (max " + (MAX_FILE_SIZE_BYTES / (1024 * 1024)) + " MB)") ;
        }

        String extensions = extractExtension(originalFileName) ;
        String uniqueName = UUID.randomUUID().toString() + extensions ;
        Path targetPath = Paths.get(STORAGE_DIR , uniqueName) ;

        try {
            Files.write(targetPath , fileBytes) ;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + e.getMessage()) ;
        }
        return STORAGE_DIR + "/" + uniqueName ;
    }

    // Convenience overload: accepts a Base64-encoded string, which is what the
    // frontend sends over JSON
    public String saveFileFromBase64(String base64Data, String originalFileName) {
        if (base64Data == null || base64Data.trim().isEmpty()) {
            throw new RuntimeException("File data is required") ;
        }
        String cleanBase64 = base64Data ;
        int commaIndex = base64Data.indexOf(",") ;
        if (base64Data.startsWith("data:") && commaIndex != -1) {
            cleanBase64 = base64Data.substring(commaIndex + 1) ;
        }
        byte [] fileBytes ;
        try {
            fileBytes = Base64.getDecoder().decode(cleanBase64) ;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("File data is not valid Base64");
        }
        return saveFile(fileBytes , originalFileName) ;
    }

    //Reades back a previously saved file
    public byte [] getFile (String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            throw new RuntimeException("File path is required");
        }
        if (relativePath.contains("..")) {
            throw new RuntimeException("Invalid file path") ;
        }
        Path fullPath = Paths.get(relativePath).normalize() ;
        if (!fullPath.startsWith(Paths.get(STORAGE_DIR))) {
            throw new RuntimeException("Invalid file path") ;
        }
        if (!Files.exists(fullPath)) {
            throw new RuntimeException("File not found") ;
        }
        try {
            return Files.readAllBytes(fullPath) ;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + e.getMessage());
        }
    }

    //Guessing the content type from the file extensions

    public String guessContentType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".txt")) return "text/plain";
        if (lower.endsWith(".mp4")) return "video/mp4";
        if (lower.endsWith(".mp3")) return "audio/mpeg";
        return "application/octet-stream";
    }

    //Extracting files simple and safe extensions

    private String extractExtension(String fileName) {
        if (fileName == null) return "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) return "";
        String ext = fileName.substring(dotIndex);
        if (ext.matches("\\.[a-zA-Z0-9]{1,10}")) {
            return ext.toLowerCase();
        }
        return "";
    }
}
