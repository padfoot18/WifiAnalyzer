package android.example.wifianalyzer;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    private Handler handler;
    private WifiManager wifiManager;
    private TextView wifiSignalInfo;
    private TextView wifiConnDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_signal);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(
                Context.WIFI_SERVICE);
        wifiSignalInfo = findViewById(R.id.wifi_info);
        wifiConnDetails = findViewById(R.id.wifiConnDetails);
        wifiSignalInfo.setMovementMethod(new ScrollingMovementMethod());
        getWifiInfo();

        handler = new Handler();
        handler.post(wifiSignalBgTask);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void getWifiInfo() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();
        String mac = wifiInfo.getBSSID();
        int freq = wifiInfo.getFrequency();
        String linkSpeedUnits = WifiInfo.LINK_SPEED_UNITS;
        int linkSpeed = wifiInfo.getLinkSpeed();
        String data = "\nSSID: "+ssid+"\nLink Speed: "+linkSpeed+" "+linkSpeedUnits;
        wifiConnDetails.append(data);
    }

    Runnable wifiSignalBgTask = new Runnable() {
        @Override
        public void run() {
            int rssi = wifiManager.getConnectionInfo().getRssi();
            int level = WifiManager.calculateSignalLevel(rssi, 100);
            level++;
            wifiSignalInfo.append("\n"+rssi+","+level);
            handler.postDelayed(this, 5000);
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
        String path = Environment.DIRECTORY_DOCUMENTS;
        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "WiFiAnalyzer");
        if(!dir.mkdirs())
            Log.d("File", "File not created");
        Log.d("File_path", path+"/"+file_name);

        try {
            File myFile = new File(dir, file_name);
            PrintWriter pw = new PrintWriter(myFile);
            String data = wifiSignalInfo.getText().toString();
            pw.println(data);
            pw.close();
        } catch (FileNotFoundException e) {
            Log.d("Error", e.toString());
        }
    }
}