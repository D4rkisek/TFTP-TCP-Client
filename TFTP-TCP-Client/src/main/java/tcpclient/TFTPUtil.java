package tcpclient;

import java.io.*;
import java.net.Socket;

public class TFTPUtil {
    public static final short OP_RRQ = 1;
    public static final short OP_WRQ = 2;
    public static final short OP_DATA = 3;
    public static final short OP_ACK = 4;

    public static void sendFile(Socket socket, String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + fileName);
        }

        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            short blockNumber = 0;
            byte[] buffer = new byte[512];
            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                dataOutputStream.writeShort(OP_DATA);
                dataOutputStream.writeShort(blockNumber);
                dataOutputStream.writeInt(bytesRead);
                dataOutputStream.write(buffer, 0, bytesRead);
                System.out.println("Sent data packet with block number: " + blockNumber);

                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                short opcode = dataInputStream.readShort();
                short ackBlockNumber = dataInputStream.readShort();

                if (opcode != OP_ACK || ackBlockNumber != blockNumber) {
                    throw new IOException("Invalid ACK received");
                }

                System.out.println("Received ACK packet with block number: " + ackBlockNumber);
                blockNumber++;
            }
        }
    }

    public static void receiveFile(Socket socket, String fileName) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            short expectedBlockNumber = 0;

            while (true) {
                short opcode = dataInputStream.readShort();
                short blockNumber = dataInputStream.readShort();
                int dataLength = dataInputStream.readInt();

                if (opcode != OP_DATA || blockNumber != expectedBlockNumber) {
                    throw new IOException("Invalid data packet received");
                }

                byte[] buffer = new byte[dataLength];
                dataInputStream.readFully(buffer, 0, dataLength);
                fileOutputStream.write(buffer, 0, dataLength);
                System.out.println("Received data packet with block number: " + expectedBlockNumber);

                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeShort(OP_ACK);
                dataOutputStream.writeShort(blockNumber);
                System.out.println("Sent ACK packet with block number: " + blockNumber);

                expectedBlockNumber++;

                if (dataLength < 512) {
                    break;
                }
            }
        }
    }
}
