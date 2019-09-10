package com.example.dell.thankyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by DELL on 17.5.2.
 */

public class ErrorActivity extends AppCompatActivity {
    private Button btnOnline,btnError,btnData;
    private EditText editRoom,editx,edity,editUnit;
    private TextView textX,textY,textError;

    public DatabaseHelper myDb;
    private BroadcastReceiver rcvWifiScan;
    private Handler mHandler;// mHandler: time delay
    private WifiManager wifi;

    private boolean scanning = false;
    private int count = 0,wrong=0 ;

    private String ROOM,X2,Y2,Unit;
    private double[]xy;
    private double error;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myDb=new DatabaseHelper(this);
        setContentView(R.layout.activity_error);

    btnOnline=(Button)findViewById(R.id.button_ERROR);
    btnError=(Button)findViewById(R.id.button_Wucha);
    btnData=(Button)findViewById(R.id.button_create);
    //btnLocation=(Button)findViewById(R.id.button);
    textX=(TextView)findViewById(R.id.textView_X);
    textY=(TextView)findViewById(R.id.textView_Y);
    textError=(TextView)findViewById(R.id.textView_Error);
    editRoom=(EditText)findViewById(R.id.editText_Room2);
    editx=(EditText)findViewById(R.id.editText_x2);
    edity=(EditText)findViewById(R.id.editText_y2);
    editUnit=(EditText)findViewById(R.id.editText_fendu);

//    btnLocation.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                double[] xy=myDb.Location();
//                textX.setText(Double.toString(xy[0]));
//                textY.setText(Double.toString(xy[1]));
//            }
//    });


    btnOnline.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
           Processing1();
        }

    });

    Processing2();
    File();



    Error();

    }

    private class TimerProcess implements  Runnable{
        public void run(){
            if(scanning){
                wifi.startScan();
            }
            mHandler.postDelayed(this,1000);//1s
        }
    }
    private class mrcvWifiScan extends BroadcastReceiver{
        @Override
        public void onReceive(Context context,Intent intent) {

            if (!scanning) return;
            count++;
            List<ScanResult> resultList = wifi.getScanResults();
            ListIterator<ScanResult> results = resultList.listIterator();

            while (results.hasNext()) {
                ScanResult info = results.next();
                myDb.insertOnline(info.BSSID, info.level, myDb.Time());
            }
            if (count >= 2) {
                scanning = false;
                count = 0;
                Toast.makeText(ErrorActivity.this, "get all data", Toast.LENGTH_SHORT).show();
                xy = myDb.Location();

                if (xy[0] < 0 && xy[1] < 0) {
                    myDb.Online();
                    wrong++;
                    count = 0;
                    scanning = true;
                    if (wrong >= 2) {
                        myDb.Correct();
                        myDb.Online();
                        wrong = 0;
                        scanning = true;
                    }
                } else {
                    wrong = 0;
                    textX.setText("X = " + Double.toString(xy[0]));
                    textY.setText("Y = " + Double.toString(xy[1]));
                    //Toast.makeText(OnlineActivity.this, "database is available", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void File(){
        btnData.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        File f = new File("/data/data/com.example.dell.thankyou/databases/rssi.db");
                        FileInputStream fis = null;
                        FileOutputStream fos = null;

                        try {
                            fis = new FileInputStream(f);
                            fos = new FileOutputStream("/sdcard/db_dump.sqlite");
                            while (true) {
                                int i = fis.read();
                                if (i != -1) {
                                    fos.write(i);
                                } else {
                                    break;
                                }
                            }
                            fos.flush();
                            Toast.makeText(getBaseContext(), "Database dump successfully", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getBaseContext(), "Fail to dump database", Toast.LENGTH_SHORT).show();
                        } finally {
                            try {
                                fos.close();
                                fis.close();
                            } catch (Exception ioe) {
                            }
                        }
                    }
                }
        );
    }

    public void Error(){
        btnError.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                ROOM = editRoom.getText().toString();
                X2 = editx.getText().toString();
                Y2 = edity.getText().toString();
                Unit=editUnit.getText().toString();
                if(ROOM.equals("")||X2.equals("")||Y2.equals("")||Unit.equals("")){
                    Toast.makeText(ErrorActivity.this,"please input the location",Toast.LENGTH_SHORT).show();
                    return;
                }
                else
                    Toast.makeText(ErrorActivity.this,"Calculate error",Toast.LENGTH_SHORT).show();
           error= myDb.Error(ROOM,X2,Y2,Unit,xy);
           textError.setText(Double.toString(error));
            }
        });

    }

    public void Processing1(){
        myDb.Result();
        if(myDb.resLocation==0)
        {
            Toast.makeText(ErrorActivity.this, "please create offline database first", Toast.LENGTH_SHORT).show();
            Intent offline = new Intent( ErrorActivity.this,OfflineActivity.class);
            startActivity(offline);
            finish();
        }
        else {
            if (!scanning) {
                myDb.Online();
                count = 0;//scanning times
                scanning = true;
                Toast.makeText(ErrorActivity.this, "calculating", Toast.LENGTH_SHORT).show();
            }

        }
    }
    public void Processing2(){
        wifi=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        rcvWifiScan=new ErrorActivity.mrcvWifiScan();
        registerReceiver(rcvWifiScan,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mHandler=new Handler();
        mHandler.post(new ErrorActivity.TimerProcess());

    }

}