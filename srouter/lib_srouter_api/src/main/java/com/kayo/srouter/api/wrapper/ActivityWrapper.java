package com.kayo.srouter.api.wrapper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.kayo.srouter.api.Interceptor;
import com.kayo.srouter.api.RouterPath;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 页面包装类
 */
public class ActivityWrapper extends Wrapper {

    private Map<String, Class<? extends Activity>> rules;

    public ActivityWrapper() {
    }

    public ActivityWrapper(Context context) {
        super(context);
    }

    public ActivityWrapper(Fragment fragment) {
        super(fragment);
    }

    public static boolean isActivity(String path, Map<String, Class<? extends Activity>> activityRules){
        if (TextUtils.isEmpty(path)){
            return false;
        }
        if (activityRules == null
                || activityRules.isEmpty()){
            return false;
        }
        return activityRules.get(path) != null;
    }

    public ActivityWrapper rules(Map<String, Class<? extends Activity>> rules){
        this.rules = rules;
        return this;
    }


    @Override
    public void go() {
        //组装规则
        String originUrl = routerPath.getOriginUrl();
        Class<? extends Activity> aClass = rules.get(originUrl);
        if (aClass != null){
            Intent intent = new Intent(context,aClass);
            if (flags != null && !flags.isEmpty()) {
                if (flags.size() == 1){
                    intent.setFlags(flags.get(0));
                }else {
                    for (int flag : flags) {
                        intent.addFlags(flag);
                    }
                }
            }
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
            intent.putExtras(paramsBundle);
            if (context != null) {
                if (context instanceof Activity){
                    if (requestCode != -1){
                        if (fragment != null){
                            fragment.startActivityForResult(intent,requestCode);
                        }else {
                            ((Activity)context).startActivityForResult(intent,requestCode);
                        }
                    }else {
                        context.startActivity(intent);
                    }
                }else {
                    context.startActivity(intent);
                }
            }
        }
    }
}
