package tcpclient;

import java.io.*;
import java.net.*;

public class TTPTCPClient {

    private static final String DEFAULT_SERVER_ADDRESS = "127.0.0.1";
    private static final int DEFAULT_SERVER_PORT = 9222;
    private static final int BUFFER_SIZE = 512;

    public static void main(String[] args) {
        String serverAddress = DEFAULT_SERVER_ADDRESS;
        int serverPort = DEFAULT_SERVER_PORT;

        try (Socket socket = new Socket(serverAddress, serverPort);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            // Read the user inputs to choose which operation to execute
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.print("\n1 to send a file \n2 to retrieve a file \n3 to exit\n:");
                String command = stdIn.readLine().trim();
                // The while loop will keep running until the user enters "3" to exit
                if ("3".equals(command)) {
                    break;
                }
                System.out.print("Enter the filename: ");
                String filename = stdIn.readLine().trim();

                if ("1".equals(command)) {
                    sendRequest(out, "WRQ", filename);
                    sendFile(out, filename);
                } else if ("2".equals(command)) {
                    sendRequest(out, "RRQ", filename);
                    receiveFile(in, filename);
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