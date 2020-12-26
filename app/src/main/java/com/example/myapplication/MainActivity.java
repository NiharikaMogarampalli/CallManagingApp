package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.telephony.SmsManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import androidx.loader.content.CursorLoader;

public class MainActivity extends AppCompatActivity {
    int paused=0;
    EditText editText;
    final private int REQUEST_SEND_SMS = 123;
    private static final int PERMISSIONS_REQUEST_CODE = 999;
    String[] appPermissions = {
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.READ_PHONE_STATE
    };
    private int flag = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Call Log");
        editText=findViewById(R.id.et);
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},REQUEST_SEND_SMS);
    }


    @Override
    protected void onStart() {
        super.onStart();
        paused=0;
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused=1;

    }
    public void onResume() {
        super.onResume();

    }



    public boolean CheckAndRequestPermission() {
        //checking which permissions are granted
        List<String> listPermissionNeeded = new ArrayList<>();
        for (String item: appPermissions){
            if(ContextCompat.checkSelfPermission(this, item)!= PackageManager.PERMISSION_GRANTED)
                listPermissionNeeded.add(item);
        }
        //Ask for non-granted permissions
        if (!listPermissionNeeded.isEmpty()){
            ActivityCompat.requestPermissions(this, listPermissionNeeded.toArray(new String[listPermissionNeeded.size()]),
                    PERMISSIONS_REQUEST_CODE);
            return false;
        }
        //App has all permissions. Proceed ahead
        return true;
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case PERMISSIONS_REQUEST_CODE:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                if (requestCode == PERMISSIONS_REQUEST_CODE) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 0)
                        fetchcalllog();
                }
                    break;
            case REQUEST_SEND_SMS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this,
                            "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode,
                        permissions, grantResults);
        }
    }



    private void fetchcalllog() {
         Uri allContacts = Uri.parse("content://call_log/calls");
         String sortOrder= CallLog.Calls.DATE + " DESC";
         StringBuffer sb = new StringBuffer();
         Cursor managedCursor = managedQuery(allContacts, null, null, null, sortOrder);
         sb.append("Call Details :");
         int count=0;
         String name,callDuration="0",result;
         while (managedCursor.moveToNext() && count!=1) {
             name = managedCursor.getString(managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
             name = name==null || name.equals("") ? "Unknown" : name;
             callDuration = managedCursor.getString(managedCursor.getColumnIndex(CallLog.Calls.DURATION));
             if(Integer.parseInt(callDuration) < 60)
                 result=callDuration+" sec";
             else{
                 int min = Integer.parseInt(callDuration)/60;
                 int sec = Integer.parseInt(callDuration)%60;
                 if(sec==0)
                     result = min + " min" ;
                 else
                    result = min + " min " + sec + " sec";
             }

            sb.append("\nContact Name:--- " + name
                    + " \nCall duration :--- " + result);
            count=1;
        }
        managedCursor.close();
         Toast.makeText(this, sb.toString(), Toast.LENGTH_SHORT).show();
        if(Integer.parseInt(callDuration)==0) {
           SmsManager sms = SmsManager.getDefault();
           sms.sendTextMessage("+919010796130", null, "The Number you have called is currently busy", null, null);
       }

    }


    public void call(View view) {
        String num=editText.getText().toString();
        Uri uri=Uri.parse("tel:"+num);
        Intent intent=new Intent(Intent.ACTION_VIEW,uri);
        startActivity(intent);
        if(CheckAndRequestPermission() && paused==1){
            fetchcalllog();
        }
    }
}
