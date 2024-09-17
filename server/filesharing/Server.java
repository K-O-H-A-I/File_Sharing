import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 1234;
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    private static final ConcurrentHashMap<String, Socket> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                String clientAddress = clientSocket.getInetAddress().getHostAddress();
                System.out.println("New client connected: " + clientAddress);

                clients.put(clientAddress, clientSocket);
                executor.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream)
        ) {
            String command = dataInputStream.readUTF();

            if (command.equals("SEND")) {
                handleSendRequest(dataInputStream, dataOutputStream);
            } else if (command.equals("RECEIVE")) {
                handleReceiveRequest(dataInputStream, dataOutputStream);
            }

        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                String clientAddress = clientSocket.getInetAddress().getHostAddress();
                clients.remove(clientAddress);
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private static void handleSendRequest(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        String fileName = dataInputStream.readUTF();
        String checksum = dataInputStream.readUTF();
        long fileSize = dataInputStream.readLong();

        System.out.println("Receiving file: " + fileName + " (Size: " + fileSize + " bytes)");

        byte[] fileContent = new byte[(int) fileSize];
        FileTransferUtils.receiveFileWithProgress(fileContent, dataInputStream, fileSize);

        System.out.println("\nFile received. Waiting for a receiver...");

        // Wait for a receiver to connect
        while (true) {
            for (Socket receiverSocket : clients.values()) {
                if (receiverSocket != dataInputStream.getChannel()) {
                    relayFile(receiverSocket, fileName, checksum, fileSize, fileContent);
                    return;
                }
            }
            try {
                Thread.sleep(1000);  // Wait before checking again
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void handleReceiveRequest(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        // This method is now empty as the server will automatically relay files to available receivers
    }

    private static void relayFile(Socket receiverSocket, String fileName, String checksum, long fileSize, byte[] fileContent) throws IOException {
        try (
            OutputStream outputStream = receiverSocket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream)
        ) {
            dataOutputStream.writeUTF("RECEIVE");
            dataOutputStream.writeUTF(fileName);
            dataOutputStream.writeUTF(checksum);
            dataOutputStream.writeLong(fileSize);

            FileTransferUtils.sendFileWithProgress(fileContent, outputStream);

            System.out.println("\nFile relayed to receiver: " + receiverSocket.getInetAddress().getHostAddress());
        }
    }
}