package com.artem.chat.client;


import com.artem.chat.Connection;
import com.artem.chat.ConsoleHelper;
import com.artem.chat.Message;
import com.artem.chat.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public class SocketThread extends Thread {
        protected void processIncomingMessage(String message) {
            if (message == null) return;
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            if (userName == null) return;
            ConsoleHelper.writeMessage("user : " + userName + " was added");
        }

        protected void informAboutDeletingNewUser(String userName) {
            if (userName == null) return;
            ConsoleHelper.writeMessage("user : " + userName + " left the chat room");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            Message message;
            while (true) {
                try {
                    message = connection.receive();
                } catch (Exception e) {
                    break;
                }
                if (message != null) {
                    if (message.getType() == MessageType.NAME_REQUEST) {
                        connection.send(new Message(MessageType.USER_NAME, getUserName()));
                    } else {
                        if (message.getType() == MessageType.NAME_ACCEPTED) {
                            notifyConnectionStatusChanged(true);
                            return;
                        } else {
                            throw new IOException("Unexpected com.artem.chat.MessageType");
                        }
                    }
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            Message message;
            while (true) {
                try {
                    message = connection.receive();
                } catch (Exception e) {
                    break;
                }
                if (message != null) {
                    if (message.getType() == MessageType.TEXT) {
                        processIncomingMessage(message.getData());
                    } else {
                        if (message.getType() == MessageType.USER_ADDED) {
                            informAboutAddingNewUser(message.getData());
                        } else {
                            if (message.getType() == MessageType.USER_REMOVED) {
                                informAboutDeletingNewUser(message.getData());
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
            throw new IOException("Unexpected com.artem.chat.MessageType");


        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket(getServerAddress(), getServerPort());
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);


            }

        }

    }


    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Enter server address");
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage("Enter server port");
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage("Enter user name");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {

        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Exception send Text com.artem.chat.Message");
            clientConnected = false;
        }
    }

    public void run() {

        try {
            SocketThread socketThread = getSocketThread();
            socketThread.setDaemon(true);
            socketThread.start();
            synchronized (this) {
                this.wait();
            }


        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("Exception in Socket Thread");
            return;
        }
        if (clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
        } else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }

        String mess = "";
        while (clientConnected) {

            mess = ConsoleHelper.readString();
            if (mess.equalsIgnoreCase("exit")) {
                break;
            }
            if (shouldSendTextFromConsole()) {
                sendTextMessage(mess);
            }


        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
