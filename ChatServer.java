import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ChatServer {
    public static void main(String args[]) {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        int serverPort = 6666;

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("Servidor de CHAT-UFPB iniciado no endereço: " + InetAddress.getLocalHost() + ":" + serverPort);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Conexão estabelecida com: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

                ClientHandler handler = new ClientHandler(clientSocket, threadPool);
                threadPool.submit(handler);
            }
        } catch (IOException e) {
            System.out.println("Erro no socket: " + e.getMessage());
        }
    }
}

class ClientHandler extends Thread {
    private DataInputStream in;
    private DataOutputStream out;
    private Socket clientSocket;
    private static final ConcurrentHashMap<Socket, String> clientMap = new ConcurrentHashMap<>();
    private ExecutorService threadPool;

    public ClientHandler(Socket socket, ExecutorService threadPool) {
        clientSocket = socket;
        this.threadPool = threadPool;
        try {
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Erro ao criar canais de comunicação: " + e.getMessage());
        }
    }

    public void run() {
        try {
            out.writeUTF("Bem-vindo ao CHAT-UFPB! Digite seu nome:");
            String clientName = in.readUTF();
            clientMap.put(clientSocket, clientName);

            broadcast(clientName + " entrou no chat.");

            while (true) {
                String message = in.readUTF();
                if (message.equalsIgnoreCase("/quit")) {
                    break;
                }
                broadcast(clientName + ": " + message);
            }

            clientSocket.close();
            clientMap.remove(clientSocket);
            broadcast(clientName + " saiu do chat.");

        } catch (IOException e) {
            System.out.println("Erro na comunicação com o cliente: " + e.getMessage());
        }
    }

    private void broadcast(String message) {
        for (Socket socket : clientMap.keySet()) {
            threadPool.submit(() -> {
                try {
                    DataOutputStream clientOut = new DataOutputStream(socket.getOutputStream());
                    clientOut.writeUTF(message);
                } catch (IOException e) {
                    System.out.println("Erro ao enviar mensagem para cliente: " + e.getMessage());
                }
            });
        }
    }
}