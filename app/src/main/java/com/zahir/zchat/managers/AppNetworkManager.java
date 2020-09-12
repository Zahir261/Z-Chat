package com.zahir.zchat.managers;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.zahir.zchat.helpers.InputStreamVolleyRequest;

import java.util.HashMap;
import java.util.Map;

public class AppNetworkManager {

    private static volatile AppNetworkManager sSoleInstance;
    private RequestQueue mRequestQueue;
    //private Context context;

    //private constructor.
    private AppNetworkManager(Context context) {

        //Prevent form the reflection api.
        if (sSoleInstance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }

        //this.context = context;

        mRequestQueue = getRequestQueue(context);
    }

    private RequestQueue getRequestQueue(Context context) {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }

        return mRequestQueue;
    }

    static AppNetworkManager getInstance(Context context) {
        if (sSoleInstance == null) { //if there is no instance available... create new one
            synchronized (AppNetworkManager.class) {
                if (sSoleInstance == null) sSoleInstance = new AppNetworkManager(context);
            }
        }

        return sSoleInstance;
    }

    private <T> boolean addToRequestQueue(Request<T> req) {

        if (mRequestQueue == null) {
            return false;
        }

        mRequestQueue.add(req);

        return true;
    }

    private static final String DEFAULT_TAG = "REQUEST";

    private StringRequest createRequest(String url, final Response.Listener<String> success, final Response.ErrorListener error, final HashMap<String, String> map, String tag) {
        StringRequest request = new StringRequest( Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (success != null) {
                    success.onResponse(response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                if (error != null) {
                    error.onErrorResponse(e);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                return map;
            }
        };
        request.setTag(TextUtils.isEmpty(tag) ? DEFAULT_TAG : tag);
        return request;
    }

    public void makeRequest(final String url, Response.Listener<String> success, Response.ErrorListener error, HashMap<String, String> map) {
        addToRequestQueue(createRequest(url, success, error, map, ""));
    }

    public boolean makeRequest(final String url, Response.Listener<String> success, Response.ErrorListener error, HashMap<String, String> map, String tag) {
        return addToRequestQueue(createRequest(url, success, error, map, tag));
    }

    public void downloadFile(String url, final Response.Listener<byte[]> success, final Response.ErrorListener errorListener, String tag){
        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, url, new Response.Listener<byte[]>() {
            @Override
            public void onResponse(byte[] response) {
                success.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                errorListener.onErrorResponse(error);
            }
        }, null);

        request.setTag(tag);
        addToRequestQueue(request);
    }
}
