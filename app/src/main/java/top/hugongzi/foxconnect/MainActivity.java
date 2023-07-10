package top.hugongzi.foxconnect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    String hostname;
    connection pcConnection;
    String ip;
    int port;
    String lastip;
    int lastport;
    String lasthostname;
    String devicename;
    String deviceid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        requsetPermission();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        ImageButton bt_connect = findViewById(R.id.bt_connect);
        ImageButton bt_scan = findViewById(R.id.bt_scan);
        CardView lastcardview = findViewById(R.id.LastConnectCard);
        TextView lastconnectname = findViewById(R.id.tv_LastConnectName);
        TextView lastconnectip = findViewById(R.id.tv_LastConnectIP);
        devicename = Settings.Secure.getString(getContentResolver(), "bluetooth_name");
        deviceid = PreferencesUtil.getPre(this, "deviceid", "null");
        if (Objects.equals(deviceid, "null")) {
            UUID uuid = UUID.randomUUID();
            PreferencesUtil.setPre(this, "deviceid", uuid.toString());
            deviceid = uuid.toString();
        }
        lastip = PreferencesUtil.getPre(this, "lastip", null);
        lastport = PreferencesUtil.getPre(this, "lastport", 37699);
        lasthostname = PreferencesUtil.getPre(this, "lasthostname", null);
        if (lastip == null || lasthostname == null) {
            lastcardview.setVisibility(View.INVISIBLE);
        } else {
            ip = lastip;
            port = lastport;
            hostname = lasthostname;
            lastconnectname.setText("PC" + lasthostname);
            lastconnectip.setText(lastip + ":" + lastport);
            bt_connect.setOnClickListener(view -> {
                String lastconnection = "<ip>" + lastip + "</ip>" + "<port>" + lastport + "</port>" + "<hostname>" + lasthostname + "</hostname>";
                makeConnection(lastconnection);
            });
        }
        bt_scan.setOnClickListener(view -> {
            IntentIntegrator intentIntegrator = new IntentIntegrator(MainActivity.this);
            intentIntegrator.setBeepEnabled(true);
            intentIntegrator.setCaptureActivity(ScanActivity.class);
            intentIntegrator.initiateScan();
        });

    }

    public void makeConnection(String codestr) {
        if (!codestr.contains("<ip>")) {
            Toast.makeText(this, "二维码失效", Toast.LENGTH_LONG).show();
        } else {
            ip = codestr.substring(codestr.indexOf("<ip>") + 4, codestr.indexOf("</ip>"));
            port = Integer.parseInt(codestr.substring(codestr.indexOf("<port>") + 6, codestr.indexOf("</port>")));
            hostname = codestr.substring(codestr.indexOf("<hostname>") + 10, codestr.indexOf("</hostname>"));
            PreferencesUtil.setPre(this, "lastip", ip);
            PreferencesUtil.setPre(this, "lastport", port);
            PreferencesUtil.setPre(this, "lasthostname", hostname);
            pcConnection = new connection(ip, port);
            pcConnection.start();
            new Thread(() -> {
                try {
                    while (!pcConnection.isconnected) {
                        Thread.sleep(200);
                    }
                    Looper.prepare();
                    sendAlive();
                    Looper.loop();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }

    public void showdialog() {
        View view = getLayoutInflater().inflate(R.layout.content_codedialog, null);
        codeDialog dialog1 = new codeDialog(this, 0, 0, view, R.style.Dialog);
        dialog1.setCancelable(true);
        Button bt_ok = dialog1.findViewById(R.id.bt_dialog_ok);
        EditText et_code = dialog1.findViewById(R.id.et_dialog_code);
        bt_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = String.valueOf(et_code.getText());
                sendCode(code);
                dialog1.cancel();
            }
        });
        dialog1.show();
    }

    public void sendAlive() {
        try {
            String recall = pcConnection.sendcmd("Alive<deviceid>" + PreferencesUtil.getPre(this, "deviceid", "null") + "</deviceid><devicename>" + devicename + "</devicename>");
            String verifycode = null;
            System.out.println(recall);
            if (recall.contains("msg:")) {
                verifycode = recall.substring(recall.indexOf("<requirecode>") + 13, recall.indexOf("</requirecode>"));
                showdialog();
                Toast.makeText(this, "配对码为" + verifycode, Toast.LENGTH_LONG).show();
            } else if (recall.contains("true")) {
                Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("ip", ip);
                bundle.putInt("port", port);
                bundle.putString("hostname", hostname);
                intent.putExtras(bundle);
                startActivity(intent);
                this.pcConnection.interrupt();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendCode(String code) {
        try {
            String recall = pcConnection.sendcmd("RequireCode<deviceid>" + PreferencesUtil.getPre(this, "deviceid", "null") + "</deviceid><verifycode>" + code + "</verifycode>");
            String verifycode = null;
            if (recall.contains("<requirecode>")) {
                verifycode = recall.substring(recall.indexOf("<requirecode>") + 13, recall.indexOf("</requirecode>"));
                Toast.makeText(this, "配对码为" + verifycode, Toast.LENGTH_LONG).show();
            }
            Toast.makeText(this, "配对成功", Toast.LENGTH_LONG).show();
            sendAlive();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};

    private void requsetPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 1);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                autoObtainLocationPermission();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1);
            }
        } else {
            autoObtainLocationPermission();
        }
    }

    private void autoObtainLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO},
                    2);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; i++) {
            //Log.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "取消扫描二维码", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "正在连接.." + result.getContents(), Toast.LENGTH_LONG).show();
                String resulttext = result.getContents();
                makeConnection(resulttext);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}