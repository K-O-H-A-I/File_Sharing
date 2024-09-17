import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKey;

public class Client {
    private static final int MAX_FILE_SIZE = 1024 * 1024 * 100; // 100 MB

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter server IP address: ");
        String serverAddress = scanner.nextLine();
        int port = 1234;

        System.out.println("Do you want to send or receive a file? (send/receive)");
        String choice = scanner.nextLine();

        if (choice.equalsIgnoreCase("send")) {
            sendFile(scanner, serverAddress, port);
        } else if (choice.equalsIgnoreCase("receive")) {
            receiveFile(serverAddress, port);
        } else {
            System.out.println("Invalid choice. Please choose 'send' or 'receive'.");
        }

        scanner.close();
    }

    private static void sendFile(Scanner scanner, String serverAddress, int port) {
        System.out.print("Enter the path of the file to send: ");
        String filePath = scanner.nextLine();
        File fileToSend = new File(filePath);

        if (fileToSend.exists() && !fileToSend.isDirectory()) {
            if (fileToSend.length() > MAX_FILE_SIZE) {
                System.out.println("File is too large to send. Maximum size is " + (MAX_FILE_SIZE / (1024 * 1024)) + " MB.");
                return;
            }

            try (Socket socket = new Socket(serverAddress, port)) {
                System.out.println("Connected to the server");
                socket.setSoTimeout(30000); // 30-second timeout

                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

                // Send command
                dataOutputStream.writeUTF("SEND");

                // Send file name
                dataOutputStream.writeUTF(fileToSend.getName());

                // Generate and send checksum
                String checksum = FileTransferUtils.getFileChecksum(fileToSend);
                dataOutputStream.writeUTF(checksum);

                // Generate encryption key
                SecretKey key = EncryptionUtils.generateKey();

                // Read file content
                byte[] fileContent = FileTransferUtils.readFileToByteArray(fileToSend);

                // Encrypt file content
                byte[] encryptedContent = EncryptionUtils.encryptFile(fileContent, key);

                // Compress encrypted content
                byte[] compressedContent = FileTransferUtils.compressFile(encryptedContent);

                // Send file size
                dataOutputStream.writeLong(compressedContent.length);

                // Send file content
                FileTransferUtils.sendFileWithProgress(compressedContent, outputStream);

                System.out.println("\nFile sent to the server");

            } catch (IOException | NoSuchAlgorithmException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Encryption error: " + e.getMessage());
            }

        } else {
            System.out.println("Invalid file path.");
        }
    }

    private static void receiveFile(String serverAddress, int port) {
        System.out.println("Waiting to receive file from server...");
        try (Socket socket = new Socket(serverAddress, port)) {
            System.out.println("Connected to the server");
            socket.setSoTimeout(30000); // 30-second timeout

            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            OutputStream outputStream = socket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            // Send command
            dataOutputStream.writeUTF("RECEIVE");

            // Wait for incoming file
            String command = dataInputStream.readUTF();
            if (command.equals("RECEIVE")) {
                // Receive file name
                String fileName = dataInputStream.readUTF();

                // Receive checksum
                String receivedChecksum = dataInputStream.readUTF();

                // Receive file size
                long fileSize = dataInputStream.readLong();

                System.out.println("Receiving file: " + fileName + " (Size: " + fileSize + " bytes)");

                // Receive file content
                byte[] compressedContent = new byte[(int) fileSize];
                FileTransferUtils.receiveFileWithProgress(compressedContent, inputStream, fileSize);

                // Decompress content
                byte[] encryptedContent = FileTransferUtils.decompressFile(compressedContent);

                // TODO: Implement decryption here
                // For now, we'll save the encrypted content
                File receivedFile = new File("received_" + fileName);
                FileTransferUtils.saveFile(encryptedContent, receivedFile);

                // Verify checksum
                String calculatedChecksum = FileTransferUtils.getFileChecksum(receivedFile);
                if (calculatedChecksum.equals(receivedChecksum)) {
                    System.out.println("\nFile integrity verified. Checksum matches.");
                } else {
                    System.out.println("\nWarning: File integrity check failed. Checksums do not match.");
                }

                System.out.println("File received and saved as '" + receivedFile.getName() + "'");
            } else {
                System.out.println("Unexpected server response");
            }

        } catch (IOException | NoSuchAlgorithmException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}