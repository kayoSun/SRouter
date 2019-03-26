package com.kayo.srouter.api.wrapper;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;


import com.kayo.srouter.api.Interceptor;
import com.kayo.srouter.api.RouterPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Wrapper {
    protected Context context;
    protected Fragment fragment;
    protected RouterPath routerPath;
    protected List<Integer> flags;
    protected int requestCode = -1;//请求码
    protected Bundle paramsBundle;
    protected List<Interceptor> interceptors;

    public Wrapper() {
    }

    public Wrapper(Context context) {
        this.context = context;
    }

    public Wrapper(Fragment fragment){
        this.fragment = fragment;
    }

    public Wrapper context(Context context){
        this.context = context;
        return this;
    }
    public Wrapper fragment(Fragment fragment){
        this.fragment = fragment;
        return this;
    }

    public Wrapper addFlag(int flag) {
        if (flags == null) {
            flags = new ArrayList<>();
        }
        flags.add(flag);
        return this;
    }

    public Wrapper routerPath(RouterPath routerPath) {
        this.routerPath = routerPath;
        return this;
    }

    public Wrapper requestCode(int requestCode) {
        this.requestCode = requestCode;
        return this;
    }

    public Wrapper addParams(Bundle paramsBundle) {
        this.paramsBundle = paramsBundle;
        return this;
    }

    public Wrapper flags(List<Integer> flags) {
        this.flags = flags;
        return this;
    }

    public Wrapper interceptors(List<Interceptor> interceptors){
        this.interceptors = new ArrayList<>();
        this.interceptors.addAll(interceptors);
        return this;
    }

    public abstract void go();
}
