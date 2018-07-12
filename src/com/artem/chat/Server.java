package com.artem.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();


    public static void sendBroadcastMessage(Message message) {

        for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {

            String key = entry.getKey();
            Connection value = entry.getValue();
            try {
                value.send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Don't send message to " + key);
            }

        }
    }

    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Enter the port..");
        int port = ConsoleHelper.readInt();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            ConsoleHelper.writeMessage("com.artem.chat.Server starting...");
            ConsoleHelper.writeMessage(serverSocket.getInetAddress() + " " + serverSocket);
            while (true) {
                Handler handler = new Handler(serverSocket.accept());
                handler.start();
            }


        } catch (IOException e) {
            ConsoleHelper.writeMessage("ServerSocket Exception");
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                ConsoleHelper.writeMessage("ServerSocket Exception");
            }
        }
    }

    private static class Handler extends Thread {

        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }


        @Override
        public void run() {
            ConsoleHelper.writeMessage("" + socket.getRemoteSocketAddress());

            String name = "";

            try (Connection connection = new Connection(socket)) {

                name = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, name));
                sendListOfUsers(connection, name);
                serverMainLoop(connection, name);

            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Error with change data to remote server");
            }
            if (!name.isEmpty()) {
                connectionMap.remove(name);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, name));
            }
            ConsoleHelper.writeMessage("com.artem.chat.Connection with server is closed");
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {

            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message message = connection.receive();
                if (message.getType() == MessageType.USER_NAME) {
                    if (message.getData() != null && !message.getData().isEmpty()) {
                        if (!connectionMap.containsKey(message.getData())) {
                            connectionMap.put(message.getData(), connection);
                            connection.send(new Message(MessageType.NAME_ACCEPTED));
                            return message.getData();
                        }
                    }
                }
            }
        }

        private void sendListOfUsers(Connection connection, String userName) throws IOException {

            for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {

                String name = entry.getKey();

                if (!userName.equals(name)) {
                    connection.send(new Message(MessageType.USER_ADDED, name));


                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {

            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    String mess = userName + ": " + message.getData();
                    sendBroadcastMessage(new Message(MessageType.TEXT, mess));
                } else {
                    ConsoleHelper.writeMessage("This message don't TEXT");
                }


            }
        }
    }

}

