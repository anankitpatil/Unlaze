package com.unlazeapp.utils;

import org.json.JSONObject;

public interface ApiServiceListener {
    public void onSuccess(JSONObject result);
    public void onFailure();
}
