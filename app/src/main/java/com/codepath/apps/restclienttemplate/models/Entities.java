package com.codepath.apps.restclienttemplate.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;

public class Entities {
    public static ArrayList<String> media;

    // empty constructor so we can parcel
    public Entities() {}

    public static ArrayList<String> fromJson(JSONObject jsonObject) throws JSONException {
        media = new ArrayList();

        if (jsonObject.has("media")){
            JSONArray jsonMedias = jsonObject.getJSONArray("media");
            Log.i("Entities", String.valueOf(jsonMedias));
            for (int i = 0; i<jsonMedias.length(); i++){
                JSONObject currentMedia = jsonMedias.getJSONObject(i);
                String mediaType = currentMedia.getString("type");
                if (mediaType.equals("photo")){
                    media.add(currentMedia.getString("media_url_https"));
                }
            }
            Log.i("Entities", String.valueOf(media));
        }

        return media;
    }
}
