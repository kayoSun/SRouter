package com.kayo.srouter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.kayo.srouter.annos.RouterRule;
import com.kayo.srouter.api.Router;

@RouterRule({"/main2activity"})
public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        findViewById(R.id.v_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Router.getRouter().go("/maintactivity",Main2Activity.this);
            }
        });
    }
}
