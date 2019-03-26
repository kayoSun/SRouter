package com.kayo.srouter;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.kayo.srouter.annos.RouterConfig;
import com.kayo.srouter.annos.RouterRule;
import com.kayo.srouter.api.Router;

@RouterConfig()
@RouterRule({"/maintactivity"})
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.v_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Router.getRouter().go("/main2activity",MainActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
