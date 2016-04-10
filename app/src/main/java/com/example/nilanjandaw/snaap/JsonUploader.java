package com.example.nilanjandaw.snaap;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Aishwarya Sarkar on 4/10/2016.
 */
public class JsonUploader extends Fields_Details {
//    private static Fields_Details field_details;
    //    TagDetails tag_details;
    public String fields;
    static Fields_Details fields_details;


    public static String POST(String url, Fields_Details fields){
        InputStream inputStream = null;
        String result = "";


        try {
            //fields = new Fields_Details();
            HttpClient httpclient = new DefaultHttpClient();        // create HttpClient
            HttpPost httpPost = new HttpPost(url);                  // make POST request to the given URL
            String json = "";
            JSONObject jsonObject = new JSONObject();               // build jsonObject
            jsonObject.accumulate("longitude", fields.getLongitude());
            jsonObject.accumulate("latitude", fields.getLatitude());
            jsonObject.accumulate("timestamp", fields.getTimestamp());
            jsonObject.accumulate("macid", fields.getMacId());
            json = jsonObject.toString();                           // convert JSONObject to JSON to String
            StringEntity se = new StringEntity(json);               // set json to StringEntity
            httpPost.setEntity(se);                                 // set httpPost Entity
            httpPost.setHeader("Accept", "application/json");       // Set some headers to inform server about the type of the content
            httpPost.setHeader("Content-type", "application/json");
            Log.d("POSTING to Server",jsonObject.getString("latitude"));
            HttpResponse httpResponse = httpclient.execute(httpPost); // Execute POST request to the given URL
            inputStream = httpResponse.getEntity().getContent();      // receive response as inputStream
            if(inputStream != null)                                   // convert inputstream to string
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return result;                                                 // return result
    }




//    public boolean isConnected(){
//        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//        if (networkInfo != null && networkInfo.isConnected())
//            return true;
//        else
//            return false;
//    }

    public class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            fields_details = new Fields_Details();
//            Fields_Details = new Fields_Details();
            fields_details.setLongitude(urls[1]);
            fields_details.setLatitude(urls[2]);
            fields_details.setMacId(urls[3]);
            //Log.d("URL:", urls[3]+fields_details.setMacId(urls[3]));

            fields_details.setTimestamp(urls[4]);
            return POST(urls[0],fields_details);
        }

        @Override                    // onPostExecute displays the results of the AsyncTask.
        protected void onPostExecute(String result) {
//            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
        }
    }

    public boolean validate(){
//        fields_details = new Fields_Details();
//        if(fields_details.getLongitude().toString().trim().equals(""))
//            return false;
//        else if(fields_details.getLatitude().toString().trim().equals(""))
//            return false;
//        else if(fields_details.getMacId().toString().trim().equals(""))
//            return false;
//        else if(fields_details.getTimestamp().toString().trim().equals(""))
//            return false;
//        else
            return true;
    }
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }
}

