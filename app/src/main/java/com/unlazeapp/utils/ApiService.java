package com.unlazeapp.utils;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by AP on 15/07/15.
 */
public class ApiService {
    private static final String BASE_URL = "http://104.238.103.98:4711/api/";
    private static ApiService instance;
    private AsyncHttpClient client;

    private ApiService() {
        client = new AsyncHttpClient();
        client.setMaxRetriesAndTimeout(10, 5000);
    }

    public static ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }

    public void geoCall(String place, final ApiServiceListener listener) {
        client.removeAllHeaders();
        client.get("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + place + "&ka&sensor=false", new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                listener.onSuccess(response);
                Log.d("GEO SUCCESS:", response.toString());
            }
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                listener.onFailure();
                // Log.d("GEO FAILED:", errorResponse.toString());
            }
        });
    }

    public void createUser(String id, final ApiServiceListener listener) {
        client.removeAllHeaders();
        client.get(getAbsoluteUrl("id/") + id, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if(response.toString().equals("{}")) {
                    client.addHeader("Accept", "text/json");
                    client.addHeader("content-type", "application/json");
                    StringEntity se = null;
                    try {
                        final JSONObject currentUser = new JSONObject();
                        currentUser.put("detail", GlobalVars.getInstance().userDetail);
                        se = new StringEntity(currentUser.toString());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    client.post(null, getAbsoluteUrl("users"), se, "application/json", new JsonHttpResponseHandler() {
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                GlobalVars.getInstance().userDetail = response.getJSONObject("detail");
                                listener.onSuccess(response);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.d("POST new user SUCCESS:", response.toString());
                        }
                        public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                            listener.onFailure();
                            // Log.d("POST new user FAILED:", errorResponse.toString());
                        }
                    });
                } else {
                    try {
                        GlobalVars.getInstance().userDetail = response.getJSONObject("detail");
                        listener.onSuccess(response);
                        Log.d("GET new user SUCCESS:", response.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.d("GET user SUCCESS:", response.toString());
            }
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONArray errorResponse) {
                listener.onFailure();
                // Log.d("GET user FAILED:", errorResponse.toString());
            }
        });
    }

    public void getDetail(String id, final ApiServiceListener listener) {
        client.removeAllHeaders();
        client.get(getAbsoluteUrl("detail/") + id, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                listener.onSuccess(response);
                Log.d("GET detail SUCCESS:", response.toString());
            }

            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                listener.onFailure();
                // Log.d("GET detail FAILED:", errorResponse.toString());
            }
        });
    }

    public void updateDetail(String id, JSONObject detail, final ApiServiceListener listener) {
        client.addHeader("Accept", "text/json");
        client.addHeader("content-type", "application/json");
        StringEntity se;
        HttpEntity entity = null;
        try {
            se = new StringEntity(detail.toString());
            entity = se;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        client.put(null, getAbsoluteUrl("update/detail/") + id, entity, "application/json", new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                listener.onSuccess(response);
                Log.d("PUT detail SUCCESS:", response.toString());
            }

            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                listener.onFailure();
                // Log.d("PUT detail FAILED:", errorResponse.toString());
            }
        });
    }

    public void getActivity(String id, final ApiServiceListenerP listener) {
        client.removeAllHeaders();
        client.get(getAbsoluteUrl("activity/") + id, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                listener.onSuccess(response);
                Log.d("GET activity SUCCESS:", response.toString());
            }

            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONArray errorResponse) {
                listener.onFailure();
                // Log.d("GET activity FAILED:", errorResponse.toString());
            }
        });
    }

    public void updateActivity(String id, JSONArray activity, final ApiServiceListenerP listener) {
        client.addHeader("Accept", "text/json");
        client.addHeader("content-type", "application/json");
        StringEntity se;
        HttpEntity entity = null;
        try {
            se = new StringEntity(activity.toString());
            entity = se;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        client.put(null, getAbsoluteUrl("update/activity/") + id, entity, "application/json", new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                listener.onSuccess(response);
                Log.d("PUT activity SUCCESS:", response.toString());
            }

            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONArray errorResponse) {
                listener.onFailure();
                // Log.d("PUT activity FAILED:", errorResponse.toString());
            }
        });
    }

    public void getConnect(String id, final ApiServiceListenerP listener) {
        client.removeAllHeaders();
        client.get(getAbsoluteUrl("connect/") + id, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                listener.onSuccess(response);
                Log.d("GET connect SUCCESS:", response.toString());
            }

            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONArray errorResponse) {
                listener.onFailure();
                // Log.d("GET connect FAILED:", errorResponse.toString());
            }
        });
    }

    public void updateConnect(String id, JSONArray connect, final ApiServiceListenerP listener) {
        client.addHeader("Accept", "text/json");
        client.addHeader("content-type", "application/json");
        StringEntity se;
        HttpEntity entity = null;
        try {
            se = new StringEntity(connect.toString());
            entity = se;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        client.put(null, getAbsoluteUrl("update/connect/") + id, entity, "application/json", new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                listener.onSuccess(response);
                Log.d("PUT connect SUCCESS:", response.toString());
            }

            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONArray errorResponse) {
                listener.onFailure();
                // Log.d("PUT connect FAILED:", errorResponse.toString());
            }
        });
    }

    public void getRequest(String id, final ApiServiceListenerP listener) {
        client.removeAllHeaders();
        client.get(getAbsoluteUrl("request/") + id, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                listener.onSuccess(response);
                Log.d("GET request SUCCESS:", response.toString());
            }

            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONArray errorResponse) {
                listener.onFailure();
                // Log.d("GET request FAILED:", errorResponse.toString());
            }
        });
    }

    public void updateRequest(String id, JSONArray request, final ApiServiceListenerP listener) {
        client.addHeader("Accept", "text/json");
        client.addHeader("content-type", "application/json");
        StringEntity se;
        HttpEntity entity = null;
        try {
            se = new StringEntity(request.toString());
            entity = se;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        client.put(null, getAbsoluteUrl("update/request/") + id, entity, "application/json", new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                listener.onSuccess(response);
                Log.d("PUT request SUCCESS:", response.toString());
            }

            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONArray errorResponse) {
                listener.onFailure();
                // Log.d("PUT request FAILED:", errorResponse.toString());
            }
        });
    }

    public void getPeople(int gender, String activity, int radius, final ApiServiceListenerP listener) {
        String url = null;
        try {
            url = getAbsoluteUrl("search/") + gender + "/" + activity + "/" + GlobalVars.getInstance().userDetail.getJSONObject("coords").getJSONArray("coordinates").getDouble(0) + "/" + GlobalVars.getInstance().userDetail.getJSONObject("coords").getJSONArray("coordinates").getDouble(1) + "/" + radius;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("GET people URL:", url);
        client.removeAllHeaders();
        client.get(url, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                listener.onSuccess(response);
                Log.d("GET people SUCCESS:", response.toString());
            }

            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONArray errorResponse) {
                listener.onFailure();
                // Log.d("GET people FAILED:", errorResponse.toString());
            }
        });
    }

    public void sendNotification(JSONArray request, final ApiServiceListenerP listener) {
        client.addHeader("Accept", "text/json");
        client.addHeader("content-type", "application/json");
        StringEntity se;
        HttpEntity entity = null;
        String to = null;
        String from = null;
        try {
            se = new StringEntity(request.toString());
            entity = se;
            to = GlobalVars.getInstance().personDetail.getString("id");
            from = GlobalVars.getInstance().userDetail.getString("id");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        client.put(null, getAbsoluteUrl("notif/") + to + "/" + from + "/" + GlobalVars.getInstance().selectedActivity.replace(" ", "%20"), entity, "application/json", new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                listener.onSuccess(response);
                Log.d("PUT notif SUCCESS:", response.toString());
            }

            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONArray errorResponse) {
                listener.onFailure();
                // Log.d("PUT notif FAILED:", errorResponse.toString());
            }
        });
    }

    public void deleteUser(String id, final ApiServiceListener listener) {
        client.delete(null, getAbsoluteUrl("users/") + id, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                listener.onSuccess(null);
                Log.v("DELETE SUCCESS:", responseBody.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                listener.onFailure();
                // Log.d("DELETE FAILED:", responseBody.toString());
            }
        });
    }

    public void getChatWith(String id, String with, final ApiServiceListenerP listener) {
        client.removeAllHeaders();
        client.get(getAbsoluteUrl("chat/") + id + "/" + with, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                listener.onSuccess(response);
                Log.d("GET chat SUCCESS:", response.toString());
            }

            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONArray errorResponse) {
                listener.onFailure();
                // Log.d("GET chat FAILED:", errorResponse.toString());
            }
        });
    }

    public void sendChatMessage(JSONObject content, final ApiServiceListener listener) {
        client.addHeader("Accept", "text/json");
        client.addHeader("content-type", "application/json");
        StringEntity se;
        HttpEntity entity = null;
        String to = null;
        String from = null;
        try {
            se = new StringEntity(content.toString());
            entity = se;
            to = GlobalVars.getInstance().personDetail.getString("id");
            from = GlobalVars.getInstance().userDetail.getString("id");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        client.put(null, getAbsoluteUrl("chat/") + to + "/" + from, entity, "application/json", new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                listener.onSuccess(response);
                Log.d("PUT chat SUCCESS:", response.toString());
            }

            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                listener.onFailure();
                // Log.d("PUT chat FAILED:", errorResponse.toString());
            }
        });
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}