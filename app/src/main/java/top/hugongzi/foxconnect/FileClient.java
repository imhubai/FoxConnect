package top.hugongzi.foxconnect;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;


public class FileClient  extends  Thread{
    String location;
    int port;
    String ip;
    Socket client;
    @Override
    public void run() {
        try {
            client = new Socket(ip, port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedReader reader = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            File f = new File(location);
            OutputStream out = client.getOutputStream();
            FileInputStream fis = new FileInputStream(f);
            bis = new BufferedInputStream(fis);
            bos = new BufferedOutputStream(out);
            byte[] nbt = new byte[0];
                nbt = f.getName().getBytes();
            bos.write(nbt.length);
            bos.write(nbt);
            int data = 0;
            while ((data = bis.read()) != -1) {
                bos.write(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public FileClient(String ip,int port,String location) {
        this.ip =ip;
        this.port=port;
        this.location=location;
    }

    public FileClient() {
    }
}