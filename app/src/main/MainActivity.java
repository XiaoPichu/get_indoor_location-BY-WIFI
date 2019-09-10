package com.example.dell.thankyou;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by DELL on 17.5.2.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btnOffline,btnError,btnOnline;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnOffline = (Button) findViewById(R.id.button_Offline);
        btnError = (Button) findViewById(R.id.button_ERROR);
        btnOnline=(Button)findViewById(R.id.button_Online);
        btnOffline.setOnClickListener(this);
        btnOnline.setOnClickListener(this);
        btnError.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_Offline:
                Intent intent = new Intent(this,OfflineActivity.class);
                startActivity(intent);
                finish();
            break;
            case R.id.button_ERROR:
                Intent inten = new Intent();
                inten.setClass(this,ErrorActivity.class);
                startActivity(inten);
                finish();
            break;
            case R.id.button_Online:
                Intent inte = new Intent();
                inte.setClass(this,OnlineActivity.class);
                startActivity(inte);
                finish();
        }
    }
}
