package server;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String name;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.server = server;
            this.name = "";
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            try {

                while (true) {
                    while (true) {
                        String str = in.readUTF();

                        System.out.println("<-Клиент: " + str);

                        if (str.startsWith("/auth ")) {
                            String[] elements = str.split(" ");

                            if (elements.length == 3) {
                                String nick = server.getAuthService().getNickByLoginPass(elements[1], elements[2]);
                                if (nick != null) {
                                    if (!server.isNickBusy(nick)) {
                                        sendMsg("/authok " + nick);
                                        this.name = nick;
//                                        File file = new File("client/src/main/java/client/log/history_" + getName() + ".txt");
//                                        file.createNewFile();
                                        try (Scanner sc = new Scanner(new FileInputStream("client/src/main/java/client/log/history_" + getName() + ".txt"))) {
                                            while (sc.hasNext()) {
                                                sendMsg(sc.nextLine());
                                            }

                                        }

                                        setAuthorized(true);

                                        break;
                                    } else {
                                        sendMsg("Учётная запись уже используется");
                                    }
                                } else {
                                    sendMsg("Неверный логин / пароль");
                                }
                            } else {
                                sendMsg("Неверное кол-во параметров");
                            }
                        } else if (str.startsWith("/register ")) {
                            String[] elements = str.split(" ");

                            if (elements.length == 4) {
                                String nick = server.getAuthService().addLoginPass(elements[1], elements[2], elements[3]);
                                if (nick != null) {
                                    sendMsg("/register_ok " + nick);
                                    sendMsg("/authok " + nick);

                                    this.name = nick;
                                    File file = new File("client/src/main/java/client/log/history_" + getName() + ".txt");
                                    file.createNewFile();
                                    setAuthorized(true);

                                    break;
                                } else {
                                    sendMsg("Этот логин уже занят");
                                }
                            } else {
                                sendMsg("Неверное кол-во параметров");
                            }
                        } else {
                            sendMsg("Для начала нужна авторизация");
                        }
                    }

                    while (true) {
                        String str = in.readUTF();

                        System.out.println("Клиент " + name + ": " + str);

                        if (str.equalsIgnoreCase("/end")) {
                            server.broadcast(str, name);

                            break;
                        } else if (str.startsWith("/w ")) {
                            String[] elements = str.split(" ");

                            server.broadcast(name + " -> " + elements[1] + " (DM): " + elements[2], name, elements[1]);


                            break;
                        } else {
                            server.broadcast(name + " : " + str);
                        }
                    }

                    setAuthorized(false);
                }
            } catch (IOException e) {
                // e.printStackTrace();
            } finally {
                setAuthorized(false);

                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendMsg(String msg) {
        try {
            System.out.println("Клиент" + (this.name != null ? " " + this.name : "") + ": " + msg);
//            try (FileOutputStream writeHistory = new FileOutputStream("client/src/main/java/client/log/history_" + getName() + ".txt", true);) {
//                String msgHist = msg + "\n";
//                writeHistory.write(msgHist.getBytes());
//            }
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    private void setAuthorized(boolean isAuthorized) {
        if (isAuthorized) {
            server.subscribe(this);

            if (!name.isEmpty()) {
                server.broadcast("Пользователь " + name + " зашёл в чат");
                server.broadcastUserList();
            }
        } else {
            server.unsubscribe(this);

            if (!name.isEmpty()) {
                server.broadcast("Пользователь " + name + " вышел из чата");
                server.broadcastUserList();
            }
        }
    }
}