package com.kayo.srouter.api;

import android.content.Context;
import android.os.Bundle;

/**
 * 拦截器
 */
public interface Interceptor {
    String getTag();
    boolean intercept(Context context, Bundle bundle);
}
