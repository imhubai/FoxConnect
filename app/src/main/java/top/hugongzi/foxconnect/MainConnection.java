package top.hugongzi.foxconnect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainConnection extends Thread {
    Socket socket;
    BufferedReader bf;
    PrintWriter out;
    BufferedReader in;
    boolean isconnected;
    String ip;
    int port;
    String cmd;

    public MainConnection(String ip, int port) {
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
            while(!(in==null)){
        cmd=in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendcmd(String s) {
        if (isconnected) {
            out.println(s);
        }
    }
    public String execcmd(){
return cmd;
    }

}