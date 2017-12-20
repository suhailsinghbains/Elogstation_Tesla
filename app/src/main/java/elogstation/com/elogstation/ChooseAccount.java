package elogstation.com.elogstation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ChooseAccount extends Activity {

    String[] mobileArray = {"Android","IPhone","WindowsMobile","Blackberry",
            "WebOS","Ubuntu","Windows7","Max OS X"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_account);

        final String name = getIntent().getStringExtra("name");
        final String user_id = getIntent().getStringExtra("user_id");

        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        mActionBarToolbar.setTitle(name);

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, mobileArray);

        final ListView listView = (ListView) findViewById(R.id.account_list);
        listView.setAdapter(adapter);

        listView.setClickable(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                Object accountName = listView.getItemAtPosition(position);
                Log.d("accountName", accountName.toString());

                Intent intent = new Intent(ChooseAccount.this, DriverView.class);
                intent.putExtra("loginName", name);
                intent.putExtra("accountName", accountName.toString());
                intent.putExtra("user_id", user_id);
                startActivity(intent);

    /* write you handling code like...
    String st = "sdcard/";
    File f = new File(st+o.toString());
    // do whatever u want to do with 'f' File object
    */
            }
        });

    }

}
