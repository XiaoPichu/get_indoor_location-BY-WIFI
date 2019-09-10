package com.example.dell.thankyou;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.ListIterator;

public class OfflineActivity extends AppCompatActivity {
    //private IntentFilter mWifiIntentFilter;// 过滤器 ：wifi信号状态
    //private BroadcastReceiver mWifiIntentReceiver;//系统在wifi信号强度改变的时候会发送消息：执行wifi过滤
    //private ImageButton mIconWifi;
    //private TextView mLabelWifi;

    //private final String TAG="InformationActivity";
    private EditText editRoom1,editx1,edity1,editCount,editId;
    private Button btnSuoyin,btnStart,btnDatabase,btnDataprocess,btnOnline,btnNew,btnDelete,btnUpdate;
    private ImageButton btnViewAll;

    private String ROOM,X1,Y1,COUNT,ID;
    private boolean scanning = false;
    private int count = 0,shaomiaocishu;
    public long LOCATION_ID;
    public final String string="Do u want to delete all data?";
    private BroadcastReceiver rcvWifiScan;
    private Handler mHandler;// mHandler: time delay
    private WifiManager wifi;

    public DatabaseHelper myDb;//private or public

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myDb=new DatabaseHelper(this);
        setContentView(R.layout.activity_offline);
        editRoom1=(EditText)findViewById(R.id.editText_Room1);
        editx1=(EditText)findViewById(R.id.editText_x1);
        edity1=(EditText)findViewById(R.id.editText_y1);
        editCount=(EditText)findViewById(R.id.editText_count);
        editId= (EditText) findViewById(R.id.editText_id);

        btnSuoyin=(Button)findViewById(R.id.button_Suoyin);
        btnStart=(Button)findViewById(R.id.button_Start);
        btnDatabase=(Button)findViewById(R.id.button_Database);
        btnDataprocess=(Button)findViewById(R.id.button_dataprocess);
        btnOnline=(Button)findViewById(R.id.button_ERROR);
        btnNew=(Button)findViewById(R.id.button_New);
        btnDelete=(Button)findViewById(R.id.button_Delete);
        btnUpdate=(Button)findViewById(R.id.button_update);

        btnViewAll=(ImageButton)findViewById(R.id.icon_ViewAll);

        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Sure(string);

            }
        });

        btnSuoyin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                ROOM = editRoom1.getText().toString();
                X1 = editx1.getText().toString();
                Y1 = edity1.getText().toString();
                if(ROOM.equals("")||X1.equals("")||Y1.equals("")){
                    Toast.makeText(OfflineActivity.this,"Please input the location",Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    LOCATION_ID=myDb.insertData(ROOM,X1,Y1);
                    if(LOCATION_ID==-1){
                        Toast.makeText(OfflineActivity.this,"Fail to insert",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(OfflineActivity.this,"Insert successfully",Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ROOM = editRoom1.getText().toString();
                X1 = editx1.getText().toString();
                Y1 = edity1.getText().toString();
                ID = editId.getText().toString();
                myDb.updateData(ID,ROOM,X1,Y1);
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(LOCATION_ID==-1)
                {
                    Toast.makeText(OfflineActivity.this,"Fail to insert",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(ROOM.equals("")||X1.equals("")||Y1.equals("")){
                    Toast.makeText(OfflineActivity.this,"Please input the location",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!scanning){
                    count=0;//scanning times
                    scanning = true;
                    btnStart.setText("Stop");
                    btnEnable(false);
                }else{
                    count=0;
                    scanning = false;
                    btnStart.setText("Start");
                    btnEnable(true);
                }
            }
        });

        //扫描次数最好存在另外一个数据里 这样子的话当程序退出之后 也会正常工作 否则程序退出了之后就get不到东西了
        //scanning times better store into another para..  use function then after this. end can still get things wanted
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ID = editId.getText().toString();
                if(ID.equals("")){
                    Toast.makeText(OfflineActivity.this,"Please input id to delete~",Toast.LENGTH_SHORT).show();
                    return;
                }
                shaomiaocishu=COUNT();
                int delRows = myDb.deleteLocation(ID,shaomiaocishu);
                if (delRows == 0){
                    Toast.makeText(OfflineActivity.this,"No Such Row",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(OfflineActivity.this,"Delete Successfully",Toast.LENGTH_SHORT).show();
                }
            }
        });

        wifi=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        rcvWifiScan=new mrcvWifiScan();

        registerReceiver(rcvWifiScan,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        //  register receiver then after receice the scanning result -> onreceive
        //wifi.startScan();
        mHandler=new Handler();
        mHandler.post(new TimerProcess());

        btnDataprocess.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                shaomiaocishu=COUNT();
                if(myDb.DataProcessing(shaomiaocishu)==true)
                    Toast.makeText(OfflineActivity.this, "Data processing", Toast.LENGTH_SHORT).show();
                else{
                    Toast.makeText(OfflineActivity.this, "New Location did not have rssi data", Toast.LENGTH_SHORT).show();
                }
            }
        }
        );

        btnOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OfflineActivity.this,ErrorActivity.class);
                startActivity(intent);
                //finish();
            }

        });

        ViewAll();
        File();

    }

    private void btnEnable(boolean b) {
        btnSuoyin.setEnabled(b);
        btnDataprocess.setEnabled(b);
        btnOnline.setEnabled(b);
        btnDelete.setEnabled(b);
        btnDatabase.setEnabled(b);
        btnNew.setEnabled(b);
        btnUpdate.setEnabled(b);
    }
    //oncreate 结束

    public int COUNT(){
        COUNT = editCount.getText().toString();
        int shao;
        if(COUNT.equals("")){
            shao=1200;
        }
        else
            shao = Integer.parseInt(COUNT);
        return shao;
    }
    private class TimerProcess implements  Runnable{
        public void run(){
            if(scanning){
                wifi.startScan();
            }
            mHandler.postDelayed(this,3000);//5000ms=5s   scanning interval
        }
    }

    public void File(){
        btnDatabase.setOnClickListener(
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

    private class mrcvWifiScan extends BroadcastReceiver{
        @Override
        public void onReceive(Context context,Intent intent) {
            if(!scanning)return;
            count++;

            List<ScanResult> resultList= wifi.getScanResults();
            int foundCount=resultList.size();
            Toast.makeText(OfflineActivity.this,"Scan done,"+foundCount+"found",
                    Toast.LENGTH_SHORT).show();
            ListIterator<ScanResult> results=resultList.listIterator();
//            String fullInfo="Scan Results:\n";
            while (results.hasNext())
            {
                ScanResult info=results.next();
                myDb.insertData(info.BSSID,info.level,myDb.Time(),LOCATION_ID);
                /*String wifiinfo2 = info.BSSID + " " + info.level + " " + info.SSID + "\n";
                //write2File(content,wifiinfo2);
                String wifiinfo ="Name:"+info.SSID+";\ncapabilities:"+info.capabilities+";\nBSSID:"+info.BSSID+";\nRSSI:"+info.level+"dBm";
                Log.v("Wifi",wifiinfo);
                fullInfo += wifiinfo +"\n\n";*/
                shaomiaocishu=COUNT();
                if (count == shaomiaocishu)//  count :startpoint is zeor so the true scanning times is shaomiaoccishu+1
                {
                    scanning = false;
                    btnStart.setText("Start");
                    btnEnable(true);
                    count = 0;
                }


            }

            //mTextView.setText(fullInfo);


        }
    }

    public void ViewAll(){
        btnViewAll.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Cursor res =myDb.getAllLocaton();
                        if(myDb.resLocation==0){
                            showMessage("Error","Nothing Found");
                            return;
                        }

                        StringBuffer buffer=new StringBuffer();
                        while (res.moveToNext()){
                            buffer.append("ID :"+ res.getString(0)+"\n");
                            buffer.append("ROOM :"+res.getString(1)+"\n");
                            buffer.append("X :"+res.getString(2)+"\n");
                            buffer.append("Y :"+res.getString(3)+"\n\n");
                        }
                        showMessage(DatabaseHelper.TABLE_NAME1,buffer.toString());
                    }
                }
        );
    }
   public  void showMessage(String title,String Message){
      AlertDialog.Builder builder=new AlertDialog.Builder(this);
      builder.setCancelable(true);
      builder.setTitle(title);
      builder.setMessage(Message);
      builder.show();
   }

    public void Sure(String string) {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        //Uncomment the below code to Set the message and title from the strings.xml file
        //builder.setMessage(R.string.dialog_message) .setTitle(R.string.dialog_title);

        //Setting message manually and performing action on button click
        builder.setMessage(string)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        myDb.ClearAll();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //  Action for 'NO' Button
                        dialog.cancel();
                    }
                });

        //Creating dialog box
        android.support.v7.app.AlertDialog alert = builder.create();
        //Setting the title manually
        alert.setTitle("Delete Location");
        alert.show();
    }
}