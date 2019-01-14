package com.garrocho.dewserving;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Servidor servidor;
    TextView infoip, msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        infoip = (TextView) findViewById(R.id.infoip);
        msg = (TextView) findViewById(R.id.msg);
        servidor = new Servidor(this);
        infoip.setText("Servidor Executando em: "  + servidor.getIpAddress() + ":" + servidor.getPort());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        servidor.onDestroy();
    }
}
