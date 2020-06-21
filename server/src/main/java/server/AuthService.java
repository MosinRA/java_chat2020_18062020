package server;

import java.util.ArrayList;

public interface AuthService {
    void start();
    void stop();
    ArrayList<String> getUsersList();
    String addLoginPass(String login, String pass, String nick);
    String getNickByLoginPass(String login, String pass);
}
