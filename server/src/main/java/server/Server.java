package server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


public class Server {
        private final int PORT = 7777;

        private Vector<ClientHandler> clients;
        private ServerSocket server;
        private AuthService authService;

        public Server() {
            Socket socket = null;
            clients = new Vector<>();

            try {
                server = new ServerSocket(PORT);
                authService = new SimpleAuthService();
                authService.start();

                System.out.println("Сервер запущен");

                while (true) {
                    System.out.println("Сервер ожидает подключение клиента");

                    socket = server.accept();
                    new ClientHandler(this, socket);

                    System.out.println("Клиент подключился");
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Ошибка при запуске сервера");
            } finally {
                try {
                    server.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                authService.stop();
            }
        }

        public synchronized void broadcast(String msg) {
            for (ClientHandler c: clients) {
                c.sendMsg(msg);
            }
        }

        public synchronized void broadcast(String msg, String... nicks) {
            int countCurrent = 0;
            int countAll = nicks.length;

            for (ClientHandler c: clients) {
                for (String nick : nicks) {
                    if (c.getName().equals(nick)) {
                        c.sendMsg(msg);

                        if (++countCurrent == countAll) {
                            return;
                        }
                    }
                }
            }
        }

        public synchronized boolean isNickBusy(String nick) {
            for (ClientHandler c: clients) {
                if (c.getName().equals(nick)) {
                    return true;
                }
            }

            return false;
        }

        public synchronized void subscribe(ClientHandler client) {
            clients.add(client);
        }

        public synchronized void unsubscribe(ClientHandler client) {
            clients.remove(client);
        }

        public AuthService getAuthService() {
            return this.authService;
        }

        public void broadcastUserList() {
            StringBuffer sb = new StringBuffer("/user_list");

            ArrayList<String> logins = this.getAuthService().getUsersList();


            for (ClientHandler client: clients) {
                sb.append(" " + client.getName() + ":онлайн");
                // ... по ходу дела исключая их общего списка их ...
                logins.remove(client.getName());
            }


            for (String login: logins) {
                sb.append(" " + login + ":офлайн");
            }

            for (ClientHandler client: clients) {
                client.sendMsg(sb.toString());
            }
        }
    }
