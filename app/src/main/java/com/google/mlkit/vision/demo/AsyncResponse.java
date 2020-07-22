package com.google.mlkit.vision.demo;

import org.json.JSONException;

public interface AsyncResponse {
    void processFinish(String output) throws JSONException;
}
