package top.hugongzi.foxconnect;

import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class getFileClient extends Thread {
    String savePath = "Download/file.txt";
    Socket socket;
String ip;
int port;
    public getFileClient(String ip, int port, String filenlocation) {
      savePath=filenlocation;
this.ip=ip;
this.port=port;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(ip, port);
            InputStream is = socket.getInputStream();
            FileOutputStream fos = new FileOutputStream(savePath);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            bos.close();
            fos.close();
            is.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
