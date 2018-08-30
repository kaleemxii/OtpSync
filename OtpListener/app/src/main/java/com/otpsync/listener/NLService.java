package com.otpsync.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.otpsync.util.TinyDB;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class NLService extends android.service.notification
        .NotificationListenerService {

    private String TAG = this.getClass().getSimpleName();
    private NLServiceReceiver nlservicereciver;
    private static TinyDB tinydb;
    @Override
    public void onCreate() {
        super.onCreate();
        nlservicereciver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.NOTIFICATION_LISTENER_SERVICE);
        registerReceiver(nlservicereciver,filter);
        tinydb = new TinyDB(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nlservicereciver);
    }

    private Object getPrivateVariable(Object obj, String variableName){
        try {
            Field f = obj.getClass().getDeclaredField(variableName);
            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        if(!sbn.getPackageName().equals("com.microsoft.android.smsorganizer")) return;

        String sender="";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            sender = (String)sbn.getNotification().extras.get("android.title");
        }

        Log.i(TAG,"**********  onNotificationPosted");
        Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());


        String otp=null;
        ArrayList list = (ArrayList)getPrivateVariable(sbn.getNotification().contentView,"mActions");
        for (Object o : list) {
            Object value = getPrivateVariable(o, "value");
            if(value instanceof String && !sender.equals(value)){
                otp=(String)value;
                break;
            }
        }
        if(otp!=null) {

            try {
                for (LinkedDevice to : tinydb.getListObject(MainActivity.key_linkedDevices,LinkedDevice.class)) {
                    postOtp(to.getToken(), sender, otp);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Intent i = new  Intent(MainActivity.MAIN_ACTIVITY);
        i.putExtra("notification_event","onNotificationPosted :" + sbn.getPackageName() + "\n");
        sendBroadcast(i);

    }

    private void postOtp(String to, String sender, String otp) throws JSONException {
        final String URL = "https://fcm.googleapis.com/fcm/send";
        JSONObject fcmMessage = new JSONObject();
        fcmMessage.put("to", to);
        fcmMessage.put("ttl", 0);
        JSONObject data = new JSONObject();
        data.put("otp", otp);
        data.put("sender", sender);
        fcmMessage.put("data", data);

        JsonObjectRequest request_json = new JsonObjectRequest(URL,fcmMessage,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //Process os success response
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });

        // add the request object to the queue to be executed
        Volley.newRequestQueue(this).add(request_json);
    }


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG,"********** onNOtificationRemoved");
        Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText +"\t" + sbn.getPackageName());
        Intent i = new  Intent(MainActivity.MAIN_ACTIVITY);
        i.putExtra("notification_event","onNotificationRemoved :" + sbn.getPackageName() + "\n");

        sendBroadcast(i);
    }

    class NLServiceReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra("command").equals("clearall")){
                    NLService.this.cancelAllNotifications();
            }
            else if(intent.getStringExtra("command").equals("list")){
                Intent i1 = new  Intent(MainActivity.MAIN_ACTIVITY);
                i1.putExtra("notification_event","=====================");
                sendBroadcast(i1);
                int i=1;
                for (StatusBarNotification sbn : NLService.this.getActiveNotifications()) {
                    Intent i2 = new  Intent(MainActivity.MAIN_ACTIVITY);
                    i2.putExtra("notification_event",i +" " + sbn.getPackageName() + "\n");
                    sendBroadcast(i2);
                    i++;
                }
                Intent i3 = new  Intent(MainActivity.MAIN_ACTIVITY);
                i3.putExtra("notification_event","===== Notification List ====");
                sendBroadcast(i3);

            }

        }
    }

}
