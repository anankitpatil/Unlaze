package com.unlazeapp.utils;

import org.json.JSONArray;

public interface ApiServiceListenerP {
    public void onSuccess(JSONArray result);
    public void onFailure();
}
