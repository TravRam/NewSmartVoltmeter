package com.seniordesign.newsmartvoltmeter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by travr on 4/17/2017.
 */

public class HomeActivity extends Activity {
    Button pairedDevs;
    Button instructions;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button pairedDevs = (Button) findViewById(R.id.paired);
        Button instructions = (Button) findViewById(R.id.instructions);

        pairedDevs.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view) {
                Intent pref = new Intent(HomeActivity.this, DeviceListActivity.class);
                startActivity(pref);
            }
        });

        instructions.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view) {
                Intent pref = new Intent(HomeActivity.this, Instructions_Activity.class);
                startActivity(pref);
            }
        });
    }
}
