package com.kayo.srouter;

import android.app.Application;

import com.kayo.srouter.annos.RouterConfig;
import com.kayo.srouter.api.Router;

import java.util.ArrayList;
import java.util.List;

/**
 * Kayo
 * 2018/12/15
 * 03:02
 */
@RouterConfig()
public class TestApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Router.init(this);
    }
}
