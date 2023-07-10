package top.hugongzi.foxconnect;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

import static top.hugongzi.foxconnect.R.*;

public class MenuActivity extends AppCompatActivity {
    String hostname;
    MainConnection niceConnection;
    String ip;
    int port;
    String filelocation;
    String fileoperation = "upload";
    private static final int REQUEST_FILE_CODE = 200;
    String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_menu);
        hostname = this.getIntent().getStringExtra("hostname");
        ip = this.getIntent().getStringExtra("ip");
        port = this.getIntent().getIntExtra("port", 37699);
        TextView tv_connectinfo = findViewById(id.tv_connectinfo);
        TextView tv_connectip = findViewById(id.tv_connectip);
        ImageButton ibt_disconnect = findViewById(id.ibt_disconnect);
        CardView card_sendfile = findViewById(id.card_sendfile);
        tv_connectinfo.setText("连接成功 - " + hostname);
        tv_connectip.setText(ip + ":" + port);

        niceConnection = new MainConnection(ip, port);
        niceConnection.start();
        new Thread(() -> {
            while (true) {
                if (niceConnection.cmd != null) {
                    execcmd(niceConnection.cmd);
                    niceConnection.cmd = null;
                }
                if (!niceConnection.isconnected && !niceConnection.isAlive()) {
                    break;
                } else {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
        ibt_disconnect.setOnClickListener(view -> {
            niceConnection.interrupt();
            this.finish();
        });
        card_sendfile.setOnClickListener(view -> {
            OpenFile(view);
        });
    }

    public void execcmd(String cmd) {
        String msg = cmd.substring(0, cmd.indexOf("<"));
        switch (msg) {
            case "pcpaste":
                String pastestr = cmd.substring(cmd.indexOf("<pcpaste>") + 9, cmd.indexOf("</pcpaste>"));
                break;
            case "pcsendfile":
                try {
                    System.out.println(cmd + InetAddress.getLocalHost());
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
                int port = Integer.parseInt(cmd.substring(cmd.indexOf("<port>") + 6, cmd.indexOf("</port>")));
                String filename = cmd.substring(cmd.indexOf("<filename>") + 10, cmd.indexOf("</filename>"));
                System.out.println(ip+" "+port+" "+filelocation);
                getFileClient getFileClient = new getFileClient(ip, port, Environment.getExternalStorageDirectory().getAbsolutePath()+ "/Download/" + filename);
                getFileClient.start();
                 break;
        }
    }

    //
    public String getPath(Context context) {
        File dir = null;
        boolean state = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (state) {
            if (Build.VERSION.SDK_INT >= 29) {
                dir = context.getExternalFilesDir(null);
            } else {
                dir = Environment.getExternalStorageDirectory();
            }
        } else {
            dir = Environment.getRootDirectory();
        }
        return dir.toString();
    }

    private void OpenFile(View view) {
        String[] mimeTypes = {"*/*"};
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        StringBuilder mimeTypesStr = new StringBuilder();
        for (String mimeType : mimeTypes) {
            mimeTypesStr.append(mimeType).append("|");
        }
        intent.setType(mimeTypesStr.substring(0, mimeTypesStr.length() - 1));
        startActivityForResult(Intent.createChooser(intent, "选择文件"), REQUEST_FILE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_FILE_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            url = FileUtil.getPath(this, uri);
            if (!TextUtils.isEmpty(url)) {
                filelocation = url;
                if (filelocation != null && Objects.equals(fileoperation, "upload")) {
                    niceConnection.sendcmd("File" + "<port>" + 37698 + "</port>");
                    FileClient fileClient = new FileClient(ip, 37698, filelocation);
                    fileClient.start();
                } else {

                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}