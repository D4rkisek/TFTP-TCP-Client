package tcpclient;

import java.io.*;
import java.net.*;

public class TTPTCPClient {

    private static final String DEFAULT_SERVER_ADDRESS = "127.0.0.1"; // Local host
    private static final int DEFAULT_SERVER_PORT = 9222;
    private static final int BUFFER_SIZE = 512;

    public static void main(String[] args) {
        // Putting Default values
        String serverAddress = DEFAULT_SERVER_ADDRESS;
        int serverPort = DEFAULT_SERVER_PORT;

        // Initialisation of socket, and stream objects
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
                String filename = stdIn.readLine().trim(); // Getting rid of spaces and other additional keys

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
        // Concatenation of the request type, filename,
        // and "octet" (the mode). The null bytes ('\0') are required by the TFTP protocol
        String request = requestType + "\0" + filename + "\0octet\0";
        // Writes the request string to the output stream as a byte array.
        out.write(request.getBytes());
    }


    private static void receiveFile(DataInputStream in, String filename) throws IOException {
        // Creating a new file with the specified filename.
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            // Read data from the input stream into the buffer until there is no more data,
            // writing the contents of the buffer to the file as we go
            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                // If we read less than BUFFER_SIZE i.e., 512 bytes, we know we have reached the end of
                // the stream and can break out of the loop
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
        // Open a new FileInputStream to read the contents of the file
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            // Writes the content from the out para, reading in bytes
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}