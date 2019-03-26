package com.kayo.srouter.api.wrapper;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.kayo.srouter.api.ActionSupport;
import com.kayo.srouter.api.Interceptor;

import java.util.Map;
import java.util.Set;

/**
 * 动作包装类
 */
public class ActionWrapper extends Wrapper{

    private Map<String, Class<? extends ActionSupport>> rules;

    public ActionWrapper() {
    }

    public ActionWrapper(Context context) {
        super(context);
    }

    public ActionWrapper(Fragment fragment) {
        super(fragment);
    }

    public static boolean isAction(String path, Map<String, Class<? extends ActionSupport>> actionRules){
        if (TextUtils.isEmpty(path)){
            return false;
        }
        if (actionRules == null
                || actionRules.isEmpty()){
            return false;
        }
        return actionRules.get(path) != null;
    }

    public ActionWrapper rules(Map<String, Class<? extends ActionSupport>> rules){
        this.rules = rules;
        return this;
    }

    @Override
    public void go(){
        if (paramsBundle == null) {
            paramsBundle = new Bundle();
        }
        Map<String, String> queries = routerPath.getQueries();
        if (queries != null && !queries.isEmpty()){
            Set<Map.Entry<String, String>> entries = queries.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)){
                    paramsBundle.putString(key,value);
                }
            }
        }
        //开启拦截器
        if (interceptors != null && !interceptors.isEmpty()) {
            for (Interceptor interceptor : interceptors) {
                if (interceptor != null){
                    if (interceptor.intercept(context,paramsBundle)) {
                        System.out.println("被拦截::"+interceptor.getTag()+"，path:"+routerPath.getPath());
                        return;
                    }
                }
            }
        }
        Class<? extends ActionSupport> aClass = rules.get(routerPath.getOriginUrl());
        try {
            ActionSupport actionSupport = aClass.newInstance();
            actionSupport.here(context,paramsBundle);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }
}
