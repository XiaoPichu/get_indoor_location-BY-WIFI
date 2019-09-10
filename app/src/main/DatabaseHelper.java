package com.example.dell.thankyou;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Path;
import android.util.Log;
//import android.icu.util.Calendar;  //this import need API at least to be 24

import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by DELL on 17.4.10.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "rssi.db";
    public static final String TABLE_NAME1 = "LOCATION";
    public static final String col1 = "LOCATION_ID";
    public static final String col2 = "ROOM";
    public static final String col3 = "X";
    public static final String col4 = "Y";

    public static final String TABLE_NAME2 = "RSSI";
    public static final String col_1 = "ID";
    public static final String col_2 = "BSSID";
    public static final String col_3 = "RSSI";
    public static final String col_4 = "TIME";
    public static final String col_5 = "LOCATION_ID";

    public static final String TABLE_NAME3 = "BSSID";
    public static final String TABLE_NAME4 = "BSSID_FINAL";
    public static final String TABLE_NAME5 = "OnlineRssi";
    public int resLocation, rescommonAP;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);//the number of this version is 1 ;  super:Call the fuction of SQlite constructor

    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME1 + "(LOCATION_ID INTEGER PRIMARY KEY AUTOINCREMENT,ROOM TEXT,X TEXT,Y TEXT)");
        db.execSQL("create table " + TABLE_NAME2 + "(ID INTEGER PRIMARY KEY AUTOINCREMENT,BSSID TEXT,RSSI INTEGER,TIME TEXT,LOCATION_ID INTEGER)");
        db.execSQL("create table " + TABLE_NAME3 + "(LOCATION_ID INTEGER,BSSID TEXT)");
        db.execSQL("create table " + TABLE_NAME4 + "(ID INTEGER PRIMARY KEY AUTOINCREMENT,BSSID TEXT)");
        db.execSQL("create table " + TABLE_NAME5 + "(ID INTEGER PRIMARY KEY AUTOINCREMENT,BSSID TEXT,RSSI INTEGER,TIME TEXT)");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME1);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME2);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME3);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME4);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME5);
        onCreate(db);
    }

    public void ClearAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME1);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME2);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME3);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME4);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME5);
        db.execSQL("DROP TABLE IF EXISTS Gaussian_0");
        db.execSQL("DROP TABLE IF EXISTS Gaussian_1");
        db.execSQL("DROP TABLE IF EXISTS TEMPTABLE");
        onCreate(db);
    }

    public long insertData(String ROOM, String X1, String Y1) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(col2, ROOM);
        contentValues.put(col3,X1);
        contentValues.put(col4, Y1);
        long result = db.insert(TABLE_NAME1, null, contentValues);
        return result;
    }
    public boolean insertData(String BSSID, int RSSI, String TIME, long LOCATION_ID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(col_2, BSSID);
        contentValues.put(col_3, RSSI);
        contentValues.put(col_4, TIME);
        contentValues.put(col_5, LOCATION_ID);
        long result = db.insert(TABLE_NAME2, null, contentValues);
        if (result == -1)
            return false;
        else return true;
    }

    public boolean updateTable(String TABLE) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("CREATE TABLE TEMP AS SELECT * FROM " + TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        db.execSQL("ALTER TABLE TEMP RENAME TO " + TABLE);
        return true;
    }

// update id
    public boolean updateData(String ID,String ROOM,String X,String Y){
        SQLiteDatabase db =this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(col1,ID);
        contentValues.put(col2,ROOM);
        contentValues.put(col3,X);
        contentValues.put(col4,Y);
        db.update(TABLE_NAME1,contentValues,"LOCATION_ID =?",new String[] { ID });
        return true;
    }

    public boolean DataProcessing(int shaomiaocishu) {
        SQLiteDatabase db = this.getWritableDatabase();

        //for test
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME3);
        db.execSQL("create table " + TABLE_NAME3 + "(LOCATION_ID INTEGER,BSSID TEXT)");

        Cursor maxLocation,maxRssi;
        maxLocation=db.rawQuery("select max("+col1+") from "+TABLE_NAME1,null);
        maxRssi=db.rawQuery("select max(LOCATION_ID) from "+TABLE_NAME2,null);
        maxLocation.moveToFirst();
        maxRssi.moveToFirst();
        int maxlocation,maxrssi;
        maxlocation=maxLocation.getInt(0);
        maxrssi=maxRssi.getInt(0);
        if (maxlocation!=maxrssi) {
            maxLocation.close();
            maxRssi.close();
            return false;
        }
        else {
            maxLocation.close();
            maxRssi.close();

            int detrow = db.delete(TABLE_NAME2, "RSSI <= ?", new String[]{String.valueOf(-90)});
            updateTable(TABLE_NAME2);
            Log.v("shaomiaocishu", String.valueOf(shaomiaocishu));
            if (detrow != -1) {
                //此时的数据却是按照最原始的数据库的扫描出现频率算的 去掉出现次数在
                //the fruquency of data is the original. so weird .
                int res = getAllLocaton().getCount();
                int count1 = 1;
                while (res >= count1) {
                    db.execSQL(
                            "DELETE FROM " + TABLE_NAME2 + " WHERE BSSID IN (SELECT BSSID FROM " + TABLE_NAME2 + " WHERE " + col1 + "=" + count1 + " GROUP BY BSSID HAVING COUNT(BSSID)<(0.9*" + shaomiaocishu + "))");
                    count1++;
                }

                updateTable(TABLE_NAME2);
                if (count1 > res) {
                    //filter the common BSSID then store into table BSSID+FIANL
                    int count2 = 1;
                    while (res >= count2) {
                        db.execSQL(
                                "INSERT INTO " + TABLE_NAME3 +
                                        " SELECT " + col1 + ",BSSID FROM " + TABLE_NAME2 + " WHERE " + col1 + "=" + count2 + " GROUP BY BSSID"
                        );
                        count2++;
                    }//table3 BSSID AND ID
                    if (count2 > res) {
                        db.execSQL("DELETE FROM " + TABLE_NAME3 + " WHERE BSSID IN (SELECT BSSID FROM " + TABLE_NAME3 + " GROUP BY BSSID HAVING COUNT(" + col1 + ")<" + res + ") ");
                        //how to update database?

                        db.execSQL("insert into  " + TABLE_NAME4 + "(BSSID) select DISTINCT BSSID FROM " + TABLE_NAME3);
                        //db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME3);
                        //db.execSQL("ALTER TABLE BSSID_FINAL RENAME TO "+TABLE_NAME3);

                        Result();

                        Gaussian(shaomiaocishu);

                        int d = db.delete("BSSID_FINAL", "BSSID in (SELECT BSSID FROM Gaussian_1 WHERE SIGEMA >70)", null);
                        if (d > 0) {
                            db.execSQL("Create Table temp(ID INTEGER PRIMARY KEY AUTOINCREMENT,BSSID)");
                            db.execSQL("insert into temp(BSSID) select BSSID from BSSID_FINAL");
                            db.execSQL("drop table if exists BSSID_FINAL");
                            db.execSQL("alter table temp rename to BSSID_FINAL");
                            db.execSQL("Drop table if exists Gaussian_1");
                            Gaussian(shaomiaocishu);
                        }

                    }
                }

            }
        }

        db.execSQL("Drop table if exists " + TABLE_NAME3);
        db.execSQL("Drop table if exists Gaussian_0");
        return true;
    }

    public int deleteLocation(String LocationID,int shaomiaocishu){
        SQLiteDatabase db = this.getWritableDatabase();
        int delrows = db.delete(TABLE_NAME1,"LOCATION_ID=?",new String[]{LocationID});
        if (delrows > 0){
            db.execSQL("CREATE TABLE TEMP (LOCATION_ID INTEGER PRIMARY KEY AUTOINCREMENT,ROOM TEXT,X TEXT,Y TEXT)");
            db.execSQL("INSERT INTO TEMP(ROOM,X,Y) SELECT ROOM,X,Y FROM "+TABLE_NAME1);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME1);
            db.execSQL("ALTER TABLE TEMP RENAME TO " + TABLE_NAME1);
//            db.delete(TABLE_NAME2,"LOCATION_ID IN (SELECT LOCATION_ID FROM ? WHERE ROOM = ? AND X = ? AND Y = ?)",new String[]{TABLE_NAME1,ROOM,X1,Y1});
//            db.delete(TABLE_NAME3,"LOCATION_ID IN (SELECT LOCATION_ID FROM ? WHERE ROOM = ? AND X = ? AND Y = ?)",new String[]{TABLE_NAME1,ROOM,X1,Y1});
            db.delete(TABLE_NAME2,"LOCATION_ID=?",new String[]{LocationID});
            updateTable(TABLE_NAME2);
        }
    return delrows;
    }

    public void Gaussian(int shaomiaocishu) {
        double[] bestFit = new double[3];// the first is the Mold value(no use so 0 can be indentify to another ocassion)
        SQLiteDatabase db = this.getWritableDatabase();
        Result();

        //生成 Gaussian_1
        db.execSQL("Drop table if exists Gaussian_1");
        db.execSQL("Create table Gaussian_1(ID INTEGER PRIMARY KEY AUTOINCREMENT,"+col1+" INTEGER,BSSID TEXT,A REAL,MEAN REAL,SIGEMA REAL)");
        //Log.d("AAA", String.valueOf(resLocation));
        for (int locationIndex = 1; locationIndex <= resLocation; locationIndex++) {
            db.execSQL("insert into Gaussian_1(BSSID) select BSSID from " + TABLE_NAME4);
            //Log.d("AAA",String.valueOf(locationIndex));
            db.execSQL("update Gaussian_1 set "+col1+"=" + locationIndex + " where ID<=" + rescommonAP + "*" + locationIndex + " and ID>" + rescommonAP + "*(" + locationIndex + "-1)");
        }
        for (int i = 90; i >= 0; i--) {
            db.execSQL("alter table Gaussian_1 add column RSSI" + i);
        }
        //        Cursor cu = db.rawQuery("select SIGEMA from Gaussian_1 where ID=" + rescommonAP + "*" + resLocation, null);
//        cu.moveToFirst();
//        double shai = cu.getDouble(cu.getColumnIndex("SIGEMA"));

        db.execSQL("Drop table if exists Gaussian_0");
        for (int locationIndex = 1; locationIndex <= resLocation; locationIndex++) {
            db.execSQL("Drop table if exists Gaussian_0");
            db.execSQL("CREATE TABLE Gaussian_0(ID INTEGER PRIMARY KEY AUTOINCREMENT,BSSID TEXT,RSSI TEXT)");
            db.execSQL("insert into Gaussian_0(BSSID,RSSI) select BSSID,RSSI from " + TABLE_NAME2 + " where "+col1+"=" + locationIndex + " and BSSID IN (SELECT BSSID FROM " + TABLE_NAME4 + " GROUP BY BSSID)");
            //get table  Gaussian_1  which has BSSID and RSSI

            for (int i = 90; i >= 0; i--) {
                Log.v("12", String.valueOf(i));
                int j = 1;
                while (j <= rescommonAP) {
                    db.execSQL(
                            "update Gaussian_1 set RSSI" + i + "=(select count(RSSI) from Gaussian_0 where BSSID=(SELECT BSSID FROM BSSID_FINAL WHERE ID=" + j + " )and RSSI=\"-" + i + "\") where (BSSID=(SELECT BSSID FROM BSSID_FINAL WHERE ID=" + j + " ) AND LOCATION_ID="+locationIndex+")"
                    );
                    j++;
                }
            }

            GaussianCurveFitter fitter = GaussianCurveFitter.create();
            WeightedObservedPoints obs = new WeightedObservedPoints();
            double[] x = new double[91];
            double[] y = new double[91];

            for (int fanwei = -90, i = 0; fanwei <= 0; fanwei++) {
                x[i] = fanwei;
                i++;
            }
            for (int j = 1; j <= rescommonAP; j++) {
                // ylable
                int a = 0;
                for (int fanwei = 90; fanwei >= 0; fanwei--) {
                    Cursor cur = db.rawQuery("select RSSI" + fanwei + " from Gaussian_1 where ( BSSID=("+
                            "SELECT BSSID FROM BSSID_FINAL WHERE ID=" + j + ") and "+col1+"="+locationIndex+")", null);
                    cur.moveToFirst();
                    y[90 - fanwei] = cur.getInt(cur.getColumnIndex("RSSI" + fanwei));
                    if (y[90 - fanwei] > 0) a++;
                    cur.close();
                }
                if (a > 3) {
                    for (int t = 0; t < 91; t++)
                        obs.add(x[t], y[t]);
                    bestFit = fitter.fit(obs.toList());
                } else {
                    double sum = 0, c = 0, mean, sigama;
                    for (int d = 0; d < 91; d++) {
                        c = c + y[d];
                        sum = sum + x[d] * y[d];

                    }
                    mean = sum / c;
                    sum = 0;
                    for (int d = 0; d < 91; d++) {
                        sum = sum + Math.pow(x[d] - mean,2) * y[d];
                    }
                    sigama = Math.sqrt(sum/c);
                    Log.v("Q", String.valueOf(sum));
                    bestFit[0] = 0;
                    bestFit[1] = mean;
                    bestFit[2] = sigama;
                }

                db.execSQL(
                        "update  Gaussian_1 set A=" + bestFit[0] + ",MEAN=" + bestFit[1] +
                                ",SIGEMA=" + bestFit[2] + " where ID=" + j + "+" + rescommonAP + "*(" + locationIndex + "-1)");
            }
        }

        // delete the sigema >150 because it's possible that the returned value is wrong
        //when the data have two peak . can't get the fitted curve but the mean and sigema still will be returned
        int dd = db.delete("BSSID_FINAL", "BSSID in (SELECT BSSID FROM Gaussian_1 WHERE SIGEMA >100)", null);
        if (dd > 0) {
            db.execSQL("Create Table temp(ID INTEGER PRIMARY KEY AUTOINCREMENT,BSSID)");
            db.execSQL("insert into temp(BSSID) select BSSID from BSSID_FINAL");
            db.execSQL("drop table if exists BSSID_FINAL");
            db.execSQL("alter table temp rename to BSSID_FINAL");
            db.execSQL("Drop table if exists Gaussian_1");
            Gaussian(shaomiaocishu);
        }

        Result();
        Cursor curmean,cursigema;
        double mean,sigema;
        for (int locationIndex = 1; locationIndex <= resLocation; locationIndex++) {
            for (int i = 90; i >= 0; i--) {
                int j = 1;
                while (j <= rescommonAP) {
                    curmean=db.rawQuery("select MEAN from Gaussian_1 where BSSID=(SELECT BSSID FROM BSSID_FINAL WHERE ID="+j+" ) and "+col1+"="+locationIndex,null);
                    cursigema=db.rawQuery("select SIGEMA from Gaussian_1 where BSSID=(SELECT BSSID FROM BSSID_FINAL WHERE ID="+j+" ) and Location_ID="+locationIndex,null);
                    curmean.moveToFirst();
                    cursigema.moveToFirst();
                    mean=curmean.getDouble(0);
                    sigema=cursigema.getDouble(0);
                    curmean.close();
                    cursigema.close();
                    db.execSQL(
                        " update Gaussian_1 set RSSI"+i+"=0 where ((-"+i+"<"+mean+"-0.9*"+sigema+") || ( -"+i+">"+mean+" +0.9*"+sigema+") )and BSSID=(SELECT BSSID FROM BSSID_FINAL WHERE ID="+j+" ) and location_id ="+locationIndex
                    );
                    j++;
                }
            }

            GaussianCurveFitter fit = GaussianCurveFitter.create();
            WeightedObservedPoints ob = new WeightedObservedPoints();

            //x label
            double[] o = new double[91];
            double[] p = new double[91];

            for (int fanwei = -90, i = 0; fanwei <= 0; fanwei++) {
                o[i] = fanwei;
                i++;
            }

            for (int j = 1; j <= rescommonAP; j++) {
                // y label
                int a = 0;
                for (int fanwei = 90; fanwei >= 0; fanwei--) {
                    Cursor cur = db.rawQuery("select RSSI" + fanwei + " from Gaussian_1 where BSSID=(SELECT BSSID FROM BSSID_FINAL WHERE ID=" + j + ") and "+col1+"="+locationIndex, null);
                    cur.moveToFirst();
                    p[90 - fanwei] = cur.getInt(cur.getColumnIndex("RSSI" + fanwei));
                    if (p[90 - fanwei] > 0) a++;
                    cur.close();
                }
                if (a > 3) {
                    for (int t = 0; t < 91; t++)
                        ob.add(o[t], p[t]);
                    bestFit = fit.fit(ob.toList());
                } else {
                            double sum = 0, c = 0;
                            for (int d = 0; d < 91; d++) {
                                c = c + p[d];
                                sum = sum + o[d] * p[d];

                            }
                            mean = sum / c;
                            sum = 0;
                            for (int d = 0; d < 91; d++) {
                                sum = sum + Math.pow(o[d] - mean,2) * p[d];
                            }
                            if (sum > 0)
                                sigema = Math.sqrt(sum/c);
                            else
                                sigema = 0;
                            Log.v("Q", String.valueOf(sum));
                            bestFit[0] = 0;
                            bestFit[1] = mean;
                            bestFit[2] = sigema;
                }
                if(Double.toString(bestFit[1])!="NaN" && Double.toString(bestFit[2])!="NaN")
                db.execSQL(
                        "update Gaussian_1 set A=" + bestFit[0] + ",MEAN=" + bestFit[1] + ",SIGEMA=" + bestFit[2] + " where ID=" + j + "+" + rescommonAP + "*(" + locationIndex + "-1)");
                 }
        }
        db.execSQL("Drop table if exists Gaussian_0");
    }

    public void Online()
    {   SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME5);
        db.execSQL("DROP TABLE IF EXISTS TEMPTABLE");
        db.execSQL("DROP TABLE IF EXISTS ONLINE");
        db.execSQL("create table "+TABLE_NAME5 +"(ID INTEGER PRIMARY KEY AUTOINCREMENT,BSSID TEXT,RSSI INTEGER,TIME TEXT)");
    }
    public boolean insertOnline(String BSSID, int RSSI, String TIME) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(col_2, BSSID);
        contentValues.put(col_3, RSSI);
        contentValues.put(col_4, TIME);
        long result = db.insert(TABLE_NAME5, null, contentValues);
        if (result == -1)
            return false;
        else return true;
    }

    public double[]Location(){
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("delete from "+TABLE_NAME5+" where rssi <=-90");
        db.execSQL("delete from "+TABLE_NAME5+" where BSSID not in (select bssid from bssid_final)");
        updateTable(TABLE_NAME5);

        db.execSQL("DROP TABLE IF EXISTS TEMPTABLE");
        db.execSQL("DROP TABLE IF EXISTS ONLINE");
        db.execSQL("create table TEMPTABLE AS SELECT * FROM BSSID_FINAL");
        db.execSQL("ALTER TABLE TEMPTABLE ADD COLUMN MEAN");
        double[] wrong={-10000 -10000};

        Result();

        Cursor cu,cuso;
        double[] mean= new double[rescommonAP],meanAP=new double[rescommonAP],meancha=new double[rescommonAP],distance=new double[resLocation];

        for(int j=1;j<=rescommonAP;j++) {
            int sum = 0, i = 0;
            int[] s = {0, 0};
            cu = db.rawQuery("select rssi from " + TABLE_NAME5 + " where BSSID=(SELECT BSSID FROM BSSID_FINAL WHERE ID=" + j + ")", null);
            if (cu.getCount() == 0) {
                return wrong;
            }
            else {
                while (cu.moveToNext()) {
                    s[i] = cu.getInt(cu.getColumnIndex("RSSI"));
                    sum = sum + s[i];
                    i++;
                }
                if (s[1] == 0)
                    i = 1;
                else
                    i = 2;
                db.execSQL("update TEMPTABLE set MEAN=" + sum / i + " where ID=" + j);
                cu.close();
            }
        }
        double sumdistance=0;
        //distance ： the distance to each location
        for(int s=1;s<=resLocation;s++){
            double sum =0;
            for(int i=1;i<=rescommonAP;i++) {
                cu = db.rawQuery("select mean from TEMPTABLE where BSSID=(SELECT BSSID FROM BSSID_FINAL WHERE ID=" + i + ")", null);
                cuso = db.rawQuery("select mean from Gaussian_1 where (BSSID=(SELECT BSSID FROM BSSID_FINAL WHERE ID=" + i + ") and "+col1+"=" + s + ")", null);
                Log.v("AAA",String.valueOf(cu.getColumnName(0)));
                cu.moveToFirst();
                cuso.moveToFirst();
                mean[i-1] = cu.getDouble(cu.getColumnIndex("MEAN"));
                meanAP[i-1] = cuso.getDouble(cuso.getColumnIndex("MEAN"));
                meancha[i-1] =Math.pow(meanAP[i-1]-mean[i-1],2);
                sum=sum+meancha[i-1];
            }
            distance[s-1]=Math.sqrt(sum);
            sumdistance=sumdistance+Math.pow(distance[s-1],-1);
        }

        double[] x=new double[resLocation],y=new double[resLocation];
        Cursor cs;
        for (int i=0;i<resLocation;i++){
            cs=db.rawQuery("select X,Y from " + TABLE_NAME1 + " where "+col1+"=" + (i+1), null);
            cs.moveToFirst();
            x[i]=cs.getDouble(0);
            y[i]=cs.getDouble(1);
        }

        double X,Y,DXD=0,DYD=0;
        for(int s=0;s<resLocation;s++){
            DXD=DXD+x[s]/distance[s];
            DYD=DYD+y[s]/distance[s];
        }
        X=DXD/sumdistance;
        Y=DYD/sumdistance;
        double[] Location={X,Y};
        db.execSQL("alter table temptable rename to Online");
        db.execSQL("DROP table if exists "+TABLE_NAME5);
        return Location;
    }

    public void Correct(){
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL(" DELETE FROM BSSID_FINAL WHERE BSSID not IN (SELECT BSSID FROM OnlineRssi)");

    }

    public double Error(String ROOM,String X,String Y,String Unit,double[] xy){
        double error=0;
        String room=ROOM;
        double x=Double.valueOf(X);
        double y=Double.valueOf(Y);
        double unit=Double.valueOf(Unit);

        error=unit*Math.sqrt(Math.pow(x-xy[0],2)+Math.pow(y-xy[1],2));
        // Log.v("aaa",x+""+y+""+unit+""+(x-xy[0])+""+Math.pow(x-xy[0],2)+""+(y-xy[1])+""+Math.pow(y-xy[1],2));
        return error;
    }

    public String Time() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        int second = Calendar.getInstance().get(Calendar.SECOND);
        String out = String.format("%02d:%02d:%02d", hour, minute, second); //11:27:13
        return out;
    }

    public Cursor getAllLocaton() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME1, null);//*所有
        resLocation = res.getCount();
        return res;
    }// get the number of Location
    public Cursor getAllBSSID() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME4, null);//Cursor res=db.rawQuery("select * from "+TABLE_NAME2+" WHERE LOCATION_ID < "+9+" AND LOCATION_ID >"+0+";",null);//*所有
        return res;
    }// get the number of selected AP
    public void Result(){
        rescommonAP = getAllBSSID().getCount();
        resLocation = getAllLocaton().getCount();
    }

}