package tcpclient;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TTPTCPClient {

    private static final String DEFAULT_SERVER_ADDRESS = "localhost";
    private static final int DEFAULT_SERVER_PORT = 9000;
    private static final int BUFFER_SIZE = 512;

    public static void main(String[] args) {
        String serverAddress = DEFAULT_SERVER_ADDRESS;
        int serverPort = DEFAULT_SERVER_PORT;

        try (Socket socket = new Socket(serverAddress, serverPort);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.print("Enter command (RRQ/WRQ/exit): ");
                String command = scanner.nextLine().trim();
                if ("exit".equalsIgnoreCase(command)) {
                    break;
                }

                System.out.print("Enter filename: ");
                String filename = scanner.nextLine().trim();

                if ("RRQ".equalsIgnoreCase(command)) {
                    sendRequest(out, "RRQ", filename);
                    receiveFile(in, filename);
                } else if ("WRQ".equalsIgnoreCase(command)) {
                    sendRequest(out, "WRQ", filename);
                    sendFile(out, filename);
                } else {
                    System.out.println("Invalid command");
                }
            }
        } catch (IOException e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void sendRequest(DataOutputStream out, String requestType, String filename) throws IOException {
        String request = requestType + "\0" + filename + "\0octet\0";
        out.write(request.getBytes());
    }

    private static void receiveFile(DataInputStream in, String filename) throws IOException {
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                if (bytesRead < BUFFER_SIZE) {
                    break;
                }
            }
        }
    }

    private static void sendFile(DataOutputStream out, String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("File not found");
            return;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}