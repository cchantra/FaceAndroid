package com.google.mlkit.vision.demo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;


import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;



public class FaceAPIUtil  extends AsyncTask <String, Integer, String> {

    private URL url;

    public AsyncResponse delegate = null;//Call back interface

    public FaceAPIUtil(URL url, AsyncResponse asyncResponse) {
        this.url = url;
        delegate = asyncResponse;
    }




    @Override
    protected String doInBackground(String... params) {

        String res=sendPost( params);

        return res;
    }

    @Override
    protected void onPostExecute (String result)  {
        //progressBar.setVisibility(View.GONE);
        //progess_msz.setVisibility(View.GONE);
        //Toast.makeText(getApplicationContext(), result, 3000).show();
        try {
            delegate.processFinish(result);
        }
        catch (JSONException e)
        {
            Log.e("error"," JSON exception");

        }
    }



    public  String sendPost( String[] message) {

        String res = null;
        try {
            System.out.println(url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            JSONObject jsonParam = new JSONObject();

            jsonParam.put("username", message[0]);
            jsonParam.put("password", message[1]); /***/


            Log.i("JSON", jsonParam.toString());
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());


            //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
            os.writeBytes(jsonParam.toString());

            os.flush();
            os.close();


            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
            Log.i("MSG", conn.getResponseMessage());

           res = readResponse(conn);


            conn.disconnect();
            return res;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public String readResponse(HttpURLConnection con)
    {
        String res = null;
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            Log.e("response",response.toString());
            res = response.toString();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  res;
    }




}
