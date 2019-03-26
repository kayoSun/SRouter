package com.kayo.srouter.api;

import android.app.Activity;

import java.util.Map;

public interface RouterRuleCreator {
    Map<String, Class<? extends Activity>> createActivityRules();
    Map<String, Class<? extends ActionSupport>> createActionRules();
}
