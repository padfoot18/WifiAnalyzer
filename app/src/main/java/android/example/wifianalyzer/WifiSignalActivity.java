package android.example.wifianalyzer;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.mbms.StreamingService;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class WifiSignalActivity extends AppCompatActivity {
    private Handler handler = null;
    private WifiManager wifiManager;
    private TextView wifiSignalInfo;
    private TextView wifiConnDetails;
    private boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_signal);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(
                Context.WIFI_SERVICE);
        wifiSignalInfo = findViewById(R.id.wifi_info);
        wifiConnDetails = findViewById(R.id.wifiConnDetails);
        wifiSignalInfo.setMovementMethod(new ScrollingMovementMethod());
        checkConnected();

        if(isConnected) {
            getWifiInfo();
            handler = new Handler();
            handler.post(wifiSignalBgTask);
        }
        else
            Toast.makeText(getApplicationContext(), "Not connected to any wifi Network", Toast.LENGTH_LONG).show();
    }

    public void checkConnected() {
        ConnectivityManager connectionManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();

        if ( networkInfo != null && networkInfo.isConnectedOrConnecting()){
            isConnected = true;
        }else{
            isConnected = false;
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void getWifiInfo() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();
        String mac = wifiInfo.getBSSID();
        int freq = wifiInfo.getFrequency();
        String linkSpeedUnits = WifiInfo.LINK_SPEED_UNITS;
        int linkSpeed = wifiInfo.getLinkSpeed();
        int ip = wifiInfo.getIpAddress();
        String ipString = String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));

        String data = "\nSSID: "+ssid+"\nLink Speed: "+linkSpeed+" "+linkSpeedUnits
                +"\nFrequency: "+freq+"\nMAC Address: "+mac+"\nIP Address: "+ipString;
        wifiConnDetails.append(data);
    }

    Runnable wifiSignalBgTask = new Runnable() {
        @Override
        public void run() {
            int rssi = wifiManager.getConnectionInfo().getRssi();
            int level = WifiManager.calculateSignalLevel(rssi, 100);
            level++;
            wifiSignalInfo.append("\n"+rssi+","+level);
            handler.postDelayed(this, 2000);
        }
    };

    Runnable saveDataToFile = new Runnable() {
        @Override
        public void run() {
            Calendar now = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String date = df.format(now.getTime());
            String file_name = "wifi_signal_data"+date+".csv";
            Log.d("file_name", file_name);
            writeToFile(file_name);
        }
    };

    public void startWifiSignalTask() {
        wifiSignalBgTask.run();
    }

    public void stopWifiSignalTask() {
        handler.removeCallbacks(wifiSignalBgTask);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopWifiSignalTask();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.wifi_signal_info_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.saveToFile) {
            Thread file_write = new Thread(saveDataToFile);
            file_write.start();
            return true;
        } else if(id == R.id.refesh) {
            checkConnected();
            wifiSignalInfo.setText("RSSI,Level");
            wifiConnDetails.setText(R.string.wifiConnInfo);
            if(isConnected) {
                getWifiInfo();
                if(handler == null){
                    handler = new Handler();
                    handler.post(wifiSignalBgTask);
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "Not connected to any wifi Network", Toast.LENGTH_LONG).show();
                if (handler != null) {
                    stopWifiSignalTask();
                    handler = null;
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void writeToFile(String file_name) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "WiFiAnalyzer");
        String root_path = dir.getPath();
        if(!dir.mkdirs())
            Log.d("File", "Dir not created");

        try {
            File myFile = new File(dir, file_name);
            final String file_path = root_path + "/" + myFile.getName();
            PrintWriter pw = new PrintWriter(myFile);
            String data = wifiSignalInfo.getText().toString();
            pw.println(data);
            pw.close();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "File saved at "+file_path, Toast.LENGTH_LONG).show();
                }
            });
        } catch (FileNotFoundException e) {
            Log.d("Error-FileNotFound", e.toString());
        }
    }
}
