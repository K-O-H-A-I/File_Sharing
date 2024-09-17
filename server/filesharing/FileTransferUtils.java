import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FileTransferUtils {

    public static String getFileChecksum(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[1024];
            int bytesCount;

            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }
        byte[] bytes = digest.digest();
        
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static void sendFileWithProgress(byte[] fileContent, OutputStream outputStream) throws IOException {
        int totalBytes = fileContent.length;
        int bytesSent = 0;
        int chunkSize = 4096;

        while (bytesSent < totalBytes) {
            int remainingBytes = totalBytes - bytesSent;
            int bytesToSend = Math.min(chunkSize, remainingBytes);
            outputStream.write(fileContent, bytesSent, bytesToSend);
            bytesSent += bytesToSend;

            int progress = (int) ((bytesSent * 100L) / totalBytes);
            System.out.print("\rProgress: " + progress + "%");
        }
        outputStream.flush();
    }

    public static void receiveFileWithProgress(byte[] buffer, InputStream inputStream, long fileSize) throws IOException {
        int bytesRead;
        int totalBytesRead = 0;
        int chunkSize = 4096;

        while (totalBytesRead < fileSize) {
            int remainingBytes = (int) (fileSize - totalBytesRead);
            int bytesToRead = Math.min(chunkSize, remainingBytes);
            bytesRead = inputStream.read(buffer, totalBytesRead, bytesToRead);
            
            if (bytesRead == -1) {
                break;
            }
            
            totalBytesRead += bytesRead;
            int progress = (int) ((totalBytesRead * 100L) / fileSize);
            System.out.print("\rProgress: " + progress + "%");
        }
    }

    public static byte[] compressFile(byte[] data) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {
            gzipStream.write(data);
        }
        return byteStream.toByteArray();
    }

    public static byte[] decompressFile(byte[] compressedData) throws IOException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(compressedData);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (GZIPInputStream gzipStream = new GZIPInputStream(byteStream)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = gzipStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return outputStream.toByteArray();
    }

    public static void saveFile(byte[] content, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content);
        }
    }

    public static byte[] readFileToByteArray(File file) throws IOException {
        byte[] buffer = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(buffer);
        }
        return buffer;
    }
}