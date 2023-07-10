package top.hugongzi.foxconnect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class connection extends Thread {
    Socket socket;
    BufferedReader bf;
    PrintWriter out;
    BufferedReader in;
    boolean isconnected;
    String ip;
    int port;

    public connection(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(ip, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            isconnected = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sendcmd(String s) throws IOException {
        if (isconnected) {
            out.println(s);
            return in.readLine();
        } else {
            return "not connected";
        }
    }

}