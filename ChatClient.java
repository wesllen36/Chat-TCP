import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    public static void main(String[] args) {
        String serverIp = "127.0.0.1";
        int serverPort = 6666;

        try (Socket socket = new Socket(serverIp, serverPort)) {
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            String mensagem = input.readUTF();
            System.out.println(mensagem);

            Scanner scanner = new Scanner(System.in);
            String clientName = scanner.nextLine();
            output.writeUTF(clientName);

            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        String receivedMessage = input.readUTF();
                        System.out.println(receivedMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Erro ao receber mensagem: " + e.getMessage());
                }
            });
            readThread.start();

            while (true) {
                String sendMessage = scanner.nextLine();
                output.writeUTF(sendMessage);
                if (sendMessage.equalsIgnoreCase("/quit")) {
                    break;
                }
            }

            readThread.join();
            scanner.close();

        } catch (IOException | InterruptedException e) {
            System.out.println("Erro no cliente: " + e.getMessage());
        }
    }
}
