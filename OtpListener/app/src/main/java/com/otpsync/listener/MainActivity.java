package com.otpsync.listener;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.otpsync.barcodereader.BarcodeCaptureActivity;
import com.otpsync.util.TinyDB;

import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private TextView txtView;
    private ListView listView;
    private ArrayList<LinkedDevice> linkedDevices;
    private MyCustomAdapter listViewAdapter;
    private NotificationReceiver nReceiver;
    private static final int RC_BARCODE_CAPTURE = 9001;
    public static final String key_linkedDevices = "linkedDevices";
    public static final String NOTIFICATION_LISTENER_SERVICE = "com.otpsync.listener.NOTIFICATION_LISTENER_SERVICE";
    public static final String MAIN_ACTIVITY = "com.otpsync.listener.NOTIFICATION_LISTENER_EXAMPLE";
    private static TinyDB tinydb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtView = (TextView) findViewById(R.id.textView);
        listView = (ListView) findViewById(R.id.listView);
        initChannels(this);
        nReceiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.MAIN_ACTIVITY);
        registerReceiver(nReceiver,filter);
        tinydb = new TinyDB(this);
        linkedDevices = tinydb.getListObject(key_linkedDevices,LinkedDevice.class);
        listView.setAdapter(listViewAdapter = new MyCustomAdapter(linkedDevices,this));
        listViewAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(
                        "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("default",
                "Channel name",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Channel description");
        notificationManager.createNotificationChannel(channel);
    }

    public void buttonClicked(View v){

        if(v.getId() == R.id.btnCreateNotify){
            NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder ncomp = new NotificationCompat.Builder(this, "default");
            ncomp.setContentTitle("My Notification");
            ncomp.setContentText("Notification Listener Service Example");
            ncomp.setTicker("Notification Listener Service Example");
            ncomp.setSmallIcon(R.drawable.ic_launcher);
            ncomp.setAutoCancel(true);
            nManager.notify((int)System.currentTimeMillis(),ncomp.build());
        }
        else if(v.getId() == R.id.btnClearNotify){
            Intent i = new Intent(MainActivity.NOTIFICATION_LISTENER_SERVICE);
            i.putExtra("command","clearall");
            sendBroadcast(i);
        }
        else if(v.getId() == R.id.btnListNotify){
            Intent i = new Intent(MainActivity.NOTIFICATION_LISTENER_SERVICE);
            i.putExtra("command","list");
            sendBroadcast(i);
        }
        else if(v.getId() == R.id.btnLinkDevice){
            // launch barcode activity.
            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
            intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        }


    }

    class NotificationReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String temp = intent.getStringExtra("notification_event") + "\n" + txtView.getText();
            txtView.setText(temp);
        }
    }


    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p/>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     * <p/>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    addLinkedDevice(barcode);
                    txtView.setText(R.string.barcode_success+", code :"+barcode.displayValue + "\n" + txtView.getText());
                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                } else {
                    txtView.setText(R.string.barcode_failure+ "\n" + txtView.getText());
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                txtView.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode))+ "\n" + txtView.getText());
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void addLinkedDevice(Barcode barcode) {
        HashSet<LinkedDevice> list = new HashSet(linkedDevices);
        list.add(new LinkedDevice(barcode.displayValue));
        tinydb.putListObject(key_linkedDevices,list);
        listViewAdapter.notifyDataSetChanged();
    }

    class MyCustomAdapter extends BaseAdapter implements ListAdapter {
        private ArrayList<LinkedDevice> list;
        private Context context;

        public MyCustomAdapter(ArrayList<LinkedDevice> list, Context context) {
            this.list = list;
            this.context = context;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int pos) {
            return list.get(pos);
        }

        @Override
        public long getItemId(int pos) {
            return pos;
            //just return 0 if your list items do not have an Id variable.
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.list_item, null);
            }

            //Handle TextView and display string from your list
            TextView tvContact= (TextView)view.findViewById(R.id.deviceName);
            tvContact.setText(list.get(position).getName());

            //Handle buttons and add onClickListeners
            Button unlinkBtn= (Button)view.findViewById(R.id.unlink);

            unlinkBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    //do something
                    list.remove(v.getId());
                    notifyDataSetChanged();

                }
            });

            return view;
        }
    }
}
