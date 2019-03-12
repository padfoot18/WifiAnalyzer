package android.example.wifianalyzer;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {
    private Button wifiSignalBtn;
    private Button nearbyWifiBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context = getApplicationContext();
        wifiSignalBtn = findViewById(R.id.wifiSignalBtn);
        nearbyWifiBtn = findViewById(R.id.nearByWifiBtn);

        wifiSignalBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), WifiSignalActivity.class));
            }
        });

        nearbyWifiBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), NearbyWifiActivity.class));
            }
        });
    }
}
