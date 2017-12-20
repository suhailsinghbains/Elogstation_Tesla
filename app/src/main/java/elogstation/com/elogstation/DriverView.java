package elogstation.com.elogstation;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.maps.SupportMapFragment;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import elogstation.com.elogstation.dummy.MapsActivity;

public class DriverView extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    final String[] verticalLabels = new String[] {"", "Off Duty", "Sleeping", "Driving", "Not Driving On Duty"};
    String date;
    String EVENTSURL = "http://54.208.49.250/api/events";
    String STATUSURL = "http://54.208.49.250/api/status";
    String CERTIFYURL = "http://54.208.49.250/api/certify";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        TextView editDateTextView = (TextView) findViewById(R.id.editDateText);

        editDateTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(isDateValid(s.toString())){
                    Log.d("somechange", s.toString());

                    getValuesForGraph(s.toString());

                }
            }
        });

        Button offdutybutton = (Button)findViewById(R.id.offdutybutton);
        Button sleeperbutton = (Button)findViewById(R.id.sleeperbutton);
        Button drivingbutton = (Button)findViewById(R.id.drivingbutton);
        Button notdrivingbutton = (Button)findViewById(R.id.notdrivingbutton);
        Button certifybutton = (Button)findViewById(R.id.certifybutton);


        View.OnClickListener statusButtonListener = new View.OnClickListener() {
            public void onClick(View v) {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
//                nameValuePairs.add(new BasicNameValuePair("user_id", "2"));
                nameValuePairs.add(new BasicNameValuePair("user_id", user_id));
                TextView currentStatusText = (TextView) findViewById(R.id.currentStatus);

                switch (v.getId() /*to get clicked view id**/) {
                    case R.id.offdutybutton:
                        currentStatusText.setText(verticalLabels[1]);
                        nameValuePairs.add(new BasicNameValuePair("status", "1"));
                        Log.d("status", "1");
                        new MyAsyncTaskDriverViewToPost(STATUSURL, nameValuePairs).execute();
                        break;
                    case R.id.sleeperbutton:
                        currentStatusText.setText(verticalLabels[2]);
                        nameValuePairs.add(new BasicNameValuePair("status", "2"));
                        Log.d("status", "2");
                        new MyAsyncTaskDriverViewToPost(STATUSURL, nameValuePairs).execute();
                        break;
                    case R.id.drivingbutton:
                        currentStatusText.setText(verticalLabels[3]);
                        nameValuePairs.add(new BasicNameValuePair("status", "3"));
                        Log.d("status", "3");
                        new MyAsyncTaskDriverViewToPost(STATUSURL, nameValuePairs).execute();
                        break;
                    case R.id.notdrivingbutton:
                        currentStatusText.setText(verticalLabels[4]);
                        nameValuePairs.add(new BasicNameValuePair("status", "4"));
                        Log.d("status", "4");
                        new MyAsyncTaskDriverViewToPost(STATUSURL, nameValuePairs).execute();
                        break;
                    case R.id.certifybutton:
                        Log.d("certify", "");
                        new MyAsyncTaskDriverViewToPost(CERTIFYURL, nameValuePairs).execute();
                        break;
                    default:
                        break;
                }
            }
        };

        offdutybutton.setOnClickListener(statusButtonListener);
        sleeperbutton.setOnClickListener(statusButtonListener);
        drivingbutton.setOnClickListener(statusButtonListener);
        notdrivingbutton.setOnClickListener(statusButtonListener);
        certifybutton.setOnClickListener(statusButtonListener);


    }


    public void getValuesForGraph(String rawDate){
        date = rawDate.replace("/", "");
        new MyAsyncTaskDriverView().execute();
    }

    public void Wifi_toggle(View view) {
        WifiManager wifiManager = (WifiManager)this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        ToggleButton btn = findViewById(R.id.wifi_button);
        /*if(wifiManager.isWifiEnabled())
        {
            wifiManager.setWifiEnabled(true);
        }*/
        wifiManager.setWifiEnabled(true);
    }
    public void Bluetooth_toggle(View view){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        ToggleButton btn = findViewById(R.id.bluetooth_button);
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            btn.setChecked(false);
        }
        else{
            mBluetoothAdapter.enable();
            btn.setChecked(true);
        }
    }

    private class MyAsyncTaskDriverView extends AsyncTask<JSONObject, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            // TODO Auto-generated method stub
            return postData();
        }


        public JSONObject postData() {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(EVENTSURL);

            JSONObject obj = null;

            TextView signinText = (TextView) findViewById(R.id.signintext);

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
//                nameValuePairs.add(new BasicNameValuePair("user_id", "2"));
                nameValuePairs.add(new BasicNameValuePair("user_id", user_id));
                nameValuePairs.add(new BasicNameValuePair("todayslog", date));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));


                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

                try {
                    String json = EntityUtils.toString(response.getEntity());
                    obj = new JSONObject(json);

                }catch (Exception e){
                }
                return obj;

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                return obj;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                return obj;
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        @Override
        protected void onPostExecute(JSONObject json) {

            TextView graphTextView = (TextView) findViewById(R.id.graphTextView);

            try {
                Log.d("events", json.toString());
                JSONObject jsonObject = new JSONObject(json.toString());
                JSONArray jsonArray = jsonObject.getJSONArray("result");

                DataPoint[] dataPoint = new DataPoint[jsonArray.length()];
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSS");

                for(int i=0;i<jsonArray.length();i++)
                {
                    JSONObject curr = jsonArray.getJSONObject(i);
                    String Event_Time = curr.getString("Event_Time");
                    String Event_Code = curr.getString("Event_Code");
//                    Log.d("Event_Time", Event_Time);
//                    Log.d("Event_Code", Event_Code);

                    Date dateTimeFormatted = new Date();

                    dateTimeFormatted = format.parse(Event_Time);

//                    dataPoint[i] = new DataPoint(dateTimeFormatted, i%5);
                    dataPoint[i] = new DataPoint(dateTimeFormatted, Integer.parseInt(Event_Code));

                }

                GraphView graph = (GraphView) findViewById(R.id.graph);
                LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoint);
                series.setDrawDataPoints(true);
                graph.removeAllSeries();
                graph.addSeries(series);

                graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                    @Override
                    public String formatLabel(double value, boolean isValueX) {
                        if (isValueX) {
                            return sdf.format(new Date((long) value));
                        }else{
                            //value of Y Axis
                            return verticalLabels[((int) value)%5];
                        }
                    }
                });
//                graph.getViewport().setScalable(true);
//                graph.getViewport().setScalableY(true);

                graph.getViewport().setMinY(0);
                graph.getViewport().setMaxY(4.0);
//                graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling

//                graph.getGridLabelRenderer().setNumHorizontalLabels(3);
                graph.getGridLabelRenderer().setNumVerticalLabels(5);

            }catch (Exception e){
                Log.d("some error parsing data", e.getMessage());
            }

        }
    }


    final String DATE_FORMAT = "dd/MM/yyyy";

    public boolean isDateValid(String date)
    {
        try {
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    String user_id = "";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.driver_view, menu);

        final String loginName = getIntent().getStringExtra("loginName");
        final String accountName = getIntent().getStringExtra("accountName");
//        user_id = "2";
        user_id = getIntent().getStringExtra("user_id");

        TextView accountNameTextView = (TextView) findViewById(R.id.accountName);
        TextView loginNameTextView = (TextView) findViewById(R.id.loginName);
        TextView driverNameTextView = (TextView) findViewById(R.id.driverName);


        accountNameTextView.setText(accountName);
        loginNameTextView.setText(loginName);
        driverNameTextView.setText(accountName);

        TextView editDateTextView = (TextView) findViewById(R.id.editDateText);

        getValuesForGraph(editDateTextView.getText().toString());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        android.app.FragmentManager fragmentManager = getFragmentManager();

        if (id == R.id.nav_home) {
            setTitle("Driver View");
            findViewById(R.id.driver_details).setVisibility(View.VISIBLE);
            fragmentManager.beginTransaction().replace(R.id.content_frame, new Fragment()).commit();
        } else if (id == R.id.nav_logs) {
            setTitle("Logs");
            findViewById(R.id.driver_details).setVisibility(View.GONE);
            fragmentManager.beginTransaction().replace(R.id.content_frame, new logs()).commit();
        } else if (id == R.id.nav_senddata) {
            setTitle("Send Data");
            findViewById(R.id.driver_details).setVisibility(View.GONE);
            fragmentManager.beginTransaction().replace(R.id.content_frame, new senddata()).commit();
        } else if (id == R.id.nav_map) {
            setTitle("Map");
            findViewById(R.id.driver_details).setVisibility(View.GONE);
            fragmentManager.beginTransaction().replace(R.id.content_frame, new map()).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private class MyAsyncTaskDriverViewToPost extends AsyncTask<JSONObject, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            // TODO Auto-generated method stub
            return postData();
        }

        private String url;
        private List<NameValuePair> nameValuePairs;

        MyAsyncTaskDriverViewToPost(String url, List<NameValuePair> nameValuePairs){
            this.url = url;
            this.nameValuePairs = nameValuePairs;
        }

        public JSONObject postData() {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);

            JSONObject obj = null;

            try {
                // Add your data
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));


                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

                try {
                    String json = EntityUtils.toString(response.getEntity());
                    obj = new JSONObject(json);

                }catch (Exception e){
                }
                return obj;

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                return obj;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                return obj;
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        @Override
        protected void onPostExecute(JSONObject json) {

            TextView graphTextView = (TextView) findViewById(R.id.graphTextView);

            try {
                Log.d("result", json.toString());
                graphTextView.setText(json.toString());

            }catch (Exception e){
                Log.d("some error parsing data", e.getMessage());
            }

        }
    }
}
