package server;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;



public class SimpleAuthService implements AuthService {

    private static final String DATABASE_NAME = "log.db";

    private Connection connection;

    @Override
    public void start() {
        try {
            this.connect(DATABASE_NAME);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void stop() {
        disconnect();
    }


    @Override
    public String addLoginPass(String login, String pass, String nick) {
        int count = 0;

        try {
            PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM login WHERE login = ? LIMIT 1");
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                count = rs.getInt(1);
            }

            if (count == 0) {
                ps = connection.prepareStatement("INSERT INTO login (login, pass, nick) VALUES (?, ?, ?)");
                ps.setString(1, login);
                ps.setString(2, this.stringToMd5(pass));
                ps.setString(3, nick);
                ps.execute();

                return login;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public ArrayList<String> getUsersList() {
        ArrayList<String> users = new ArrayList<>();

        try {
            ResultSet rs = connection.createStatement().executeQuery("SELECT login FROM login");

            while(rs.next()) {
                users.add(rs.getString("login"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }


    @Override
    public String getNickByLoginPass(String login, String pass) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT login FROM login WHERE login = ? AND pass = ? LIMIT 1");
            ps.setString(1, login);
            ps.setString(2, this.stringToMd5(pass));
            ResultSet rs = ps.executeQuery();

            while (rs.next()){
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    private void disconnect() {
        try {
            this.connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void connect(String filename) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        String url = "jdbc:sqlite:" + filename;

        this.connection = DriverManager.getConnection(url);


    }


    private String stringToMd5(String string) {
        String generatedPassword = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(string.getBytes());

            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();

            for (int i=0; i< bytes.length ;i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return generatedPassword;
    }
}