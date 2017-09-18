package com.seniordesign.newsmartvoltmeter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by travr on 4/17/2017.
 */

public class Instructions_Activity extends Activity {
    Button home;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instructions_activity);

        Button home = (Button) findViewById(R.id.home);

        home.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view) {
                Intent pref = new Intent(Instructions_Activity.this, HomeActivity.class);
                startActivity(pref);
            }
        });

    }
}
