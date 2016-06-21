package appdefinedbuttonexample.odg.com.appdefinedbuttonexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import appdefinedbuttonexample.odg.com.appdefinedbuttonexample.appbuttons.ButtonDefinedActivity;
import appdefinedbuttonexample.odg.com.appdefinedbuttonexample.videoplayer.VideoPlayer;

public class MainActivity extends AppCompatActivity {
    String[] mArray = {"App Defined Button Example",
                       "Video Palyer Defined Control UI Example"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listview, mArray);

        ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    Intent myIntent = new Intent(MainActivity.this, ButtonDefinedActivity.class);
                    startActivity(myIntent);
                } else {
                    Intent myIntent = new Intent(MainActivity.this, VideoPlayer.class);
                    startActivity(myIntent);
                }
            }
        });
    }
}
