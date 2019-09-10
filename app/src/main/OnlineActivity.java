package com.example.dell.thankyou;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Champ on 12/05/2017.
 */

public class OnlineActivity extends AppCompatActivity {




    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new DrawView(this));
    }
}
