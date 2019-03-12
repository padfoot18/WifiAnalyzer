package android.example.wifianalyzer;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class NearbyWifiActivity extends AppCompatActivity {
    private WifiManager wifiManager;
    private TableLayout wifiInfoTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_wifi);

        Context context = getApplicationContext();
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiInfoTable = findViewById(R.id.wifiInfoTable);

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @TargetApi(Build.VERSION_CODES.M)
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                } else {
                    // scan failure handling
                    scanFailure();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);

        boolean success = wifiManager.startScan();
        if (!success) {
            // scan failure handling
            scanFailure();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void scanSuccess() {
        List<ScanResult> results = wifiManager.getScanResults();
        LayoutInflater inflater = getLayoutInflater();
        int i;

        wifiInfoTable.removeViews(1, wifiInfoTable.getChildCount()-1);

        for (ScanResult result: results) {
            i = 0;
            Log.d("Scan_Result", result.toString());
            String ssid = result.SSID;
            int level = result.level;
            int freq = result.frequency;
            String macAddr = result.BSSID;
            TableRow tableRow = (TableRow) inflater.inflate(R.layout.wifi_info_row_layout, null);

            TextView textView = (TextView) inflater.inflate(R.layout.wifi_info_text_view, null);
            textView.setId(View.generateViewId());
            textView.setText(ssid);
            textView.setLayoutParams(new TableRow.LayoutParams(i++));
            textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.3f));
            tableRow.addView(textView);

            textView = (TextView) inflater.inflate(R.layout.wifi_info_text_view, null);
            textView.setId(View.generateViewId());
            textView.setText(""+level);
            textView.setLayoutParams(new TableRow.LayoutParams(i++));
            textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.15f));
            tableRow.addView(textView);

            textView = (TextView) inflater.inflate(R.layout.wifi_info_text_view, null);
            textView.setId(View.generateViewId());
            textView.setLayoutParams(new TableRow.LayoutParams(i++));
            textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.15f));
            textView.setText(""+freq);
            tableRow.addView(textView);

            textView = (TextView) inflater.inflate(R.layout.wifi_info_text_view, null);
            textView.setId(View.generateViewId());
            textView.setText(macAddr);
            textView.setLayoutParams(new TableRow.LayoutParams(i));
            textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.4f));
            tableRow.addView(textView);
            wifiInfoTable.addView(tableRow);
        }
    }

    public void scanFailure() {
        Toast.makeText(
                getApplicationContext(),
                "No Wifi Networks Found!",
                Toast.LENGTH_LONG)
                .show();
    }


}
