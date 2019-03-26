package com.kayo.srouter.api;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.kayo.srouter.annos.RouterConfig;
import com.kayo.srouter.api.wrapper.ActionWrapper;
import com.kayo.srouter.api.wrapper.ActivityWrapper;
import com.kayo.srouter.api.wrapper.Wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Router {
    public static String ORIGIN_URL = "origin_url";
    public static Map<RouterCallback,Integer> callbacks = new HashMap<>();
    private static final Router router = new Router();
    private Map<String, Class<? extends Activity>> activityRules;
    private Map<String, Class<? extends ActionSupport>> actionRules;
    private Map<String, Class<? extends Interceptor>> interceptors;

    private List<Interceptor> tempInterceptors;
    private Context appContext;

    private Router() {
        activityRules = new HashMap<>();
        actionRules = new HashMap<>();
        interceptors = new HashMap<>();
        tempInterceptors = new ArrayList<>();
    }

    public static synchronized void init(Application context) {
        router.bindApplication(context);
        //初始化路由映射表
        try {
            String[] packageNames;
            @SuppressWarnings("ReflectionForUnavailableAnnotation")
            RouterConfig annotation = context.getClass().getAnnotation(RouterConfig.class);
            if (annotation != null && annotation.pack().length > 0) {
                packageNames = annotation.pack();
            } else {
                packageNames = new String[]{context.getPackageName()};
            }
            for (String packageName : packageNames) {
                Class<?> clazz = Class.forName(packageName + ".RuleCreatorImpl");
                clazz.newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void init(Application context, List<String> configPaths) {
        router.bindApplication(context);
        //初始化路由映射表
        if (configPaths != null) {
            for (String configPath : configPaths)
                try {
                    Class<?> clazz = Class.forName(configPath + ".RuleCreatorImpl");
                    clazz.newInstance();
                } catch (IllegalAccessException | ClassNotFoundException | InstantiationException e) {
                    //do nothing is ok
                }
        }
    }

    public static void onActivityForResult(int requestCode, int resultCode, @Nullable Intent data) {
        Set<Map.Entry<RouterCallback, Integer>> entries = callbacks.entrySet();
        List<RouterCallback> temp = new ArrayList<>();
        for (Map.Entry<RouterCallback, Integer> entry : entries) {
            int value = entry.getValue();
            if (requestCode == value){
                RouterCallback key = entry.getKey();
                key.onBack(new Back(requestCode, resultCode, data));
                temp.add(key);
            }
        }
        if (!temp.isEmpty()) {
            for (RouterCallback routerCallback : temp) {
                callbacks.remove(routerCallback);
            }
        }
    }

    public static Router with(Object object) {
        if (router == null) {
            throw new IllegalArgumentException("Router need call method init() in Application ~");
        }
        router.matchObject(object);
        router.bundle = new Bundle();
        return router;
    }

    private List<Integer> flags;
    private int requestCode;//请求码
    private Bundle bundle;
    private String path;
    private Wrapper wrapper;
    private Activity activity;
    private Fragment fragment;

    private void matchObject(Object object) {
        if (object instanceof Fragment) {
            this.fragment = (Fragment) object;
        } else if (object instanceof Activity) {
            this.activity = (Activity) object;
        }
    }
    private void bindApplication(Application appContext){
        this.appContext = appContext.getApplicationContext();
    }

    /**
     * 相当于 startActivityForResult
     *
     * @param callback
     * @return
     */
    public Router callback(int requestCode,RouterCallback callback) {
        this.requestCode = requestCode;
        callbacks.put(callback,requestCode);
        return this;
    }

    public Router addFlag(int flag) {
        if (flags == null) {
            flags = new ArrayList<>();
        }
        flags.add(flag);
        return this;
    }

    public Router wrapper(Wrapper wrapper) {
        this.wrapper = wrapper;
        return this;
    }

    public Router addParam(String key, Object value) {
        BundleUtil.objectToBundle(bundle,key,value);
        return this;
    }

    public Router addParams(Map<String,Object> params) {
        if (params != null && !params.isEmpty()) {
            BundleUtil.mapToBundle(bundle,params);
        }
        return this;
    }

    public Router addParams(Bundle bundle) {
        BundleUtil.bundleToBundle(this.bundle,bundle);
        return this;
    }

    public Router addInterceptor(Interceptor interceptor) {
        if (interceptor != null) {
            tempInterceptors.add(interceptor);
        }
        return this;
    }

    /**
     * 添加路由创建器
     */
    public void addRuleCreator(RouterRuleCreator routerRuleCreator) {
        if (routerRuleCreator != null) {
            activityRules.putAll(routerRuleCreator.createActivityRules());
            actionRules.putAll(routerRuleCreator.createActionRules());
        }
    }

    public void addInterceptCreator(RouterInterceptCreator interceptCreator) {
        if (interceptCreator != null) {
            interceptors.putAll(interceptCreator.createInterceptors());
        }

    }

    public void go(String originPath) {
        if (fragment != null) {
            go(originPath, fragment);
            return;
        }
        if (activity != null) {
            go(originPath, activity);
        }
    }

    private void go(String originPath, Fragment fragment) {
        this.fragment = fragment;
        FragmentActivity activity = fragment == null ? null : fragment.getActivity();
        go(originPath, activity);
    }

    private void go(String originPath, Context context) {
        if (!TextUtils.isEmpty(originPath)) {
            matchParams(originPath);

            if (context == null) {
                context = appContext;
            }
            if (context instanceof Activity) {
                if (((Activity) context).isFinishing()) {
                    context = appContext;
                }
            }
            RouterPath routerPath = new RouterPath(originPath);
            path = routerPath.getPath();
            matchInterceptor(routerPath.getPath());
            if (wrapper != null){
                //自定义路由跳转规则
                wrapper.context(context)
                        .fragment(fragment)
                        .interceptors(tempInterceptors)
                        .addParams(bundle)
                        .flags(flags)
                        .requestCode(requestCode)
                        .routerPath(routerPath)
                        .go();
            }else if (ActivityWrapper.isActivity(path, activityRules)) {
                //activity支持
                new ActivityWrapper(context)
                        .rules(activityRules)
                        .fragment(fragment)
                        .interceptors(tempInterceptors)
                        .addParams(bundle)
                        .flags(flags)
                        .requestCode(requestCode)
                        .routerPath(routerPath)
                        .go();
            } else if (ActionWrapper.isAction(path, actionRules)) {
                //动作支持
                new ActionWrapper(context)
                        .rules(actionRules)
                        .interceptors(tempInterceptors)
                        .addParams(bundle)
                        .routerPath(routerPath)
                        .go();
            }
        }
        //重置路由
        resetRouter();
    }

    private void matchParams(String originPath) {
        bundle.putString(ORIGIN_URL, originPath);
    }

    private void matchInterceptor(String path) {
        Class<? extends Interceptor> aClass = interceptors.get(path);
        if (aClass != null) {
            try {
                Interceptor interceptor = aClass.newInstance();
                tempInterceptors.add(interceptor);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void resetRouter() {
        path = "";
        flags = null;
        bundle = null;
        wrapper = null;
        activity = null;
        fragment = null;
        requestCode = -1;
        tempInterceptors.clear();

    }

}
