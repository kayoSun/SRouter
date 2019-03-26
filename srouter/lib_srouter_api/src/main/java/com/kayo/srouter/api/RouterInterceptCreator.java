package com.kayo.srouter.api;

import java.util.Map;

/**
 * 路由生成
 */
public interface RouterInterceptCreator {
    Map<String,Class<? extends Interceptor>> createInterceptors();

}
