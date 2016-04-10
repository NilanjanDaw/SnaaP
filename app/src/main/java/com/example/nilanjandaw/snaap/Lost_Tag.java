package com.example.nilanjandaw.snaap;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class Lost_Tag extends Activity {
    TextView tvIsConnected;
    EditText write_tagid;
    Button upload_to_server;
    TagDetails tag_details;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost__tag);
        tvIsConnected = (TextView) findViewById(R.id.connected);
        write_tagid = (EditText) findViewById(R.id.write_tagid);
        upload_to_server = (Button) findViewById(R.id.upload_to_server);
        if(isConnected()){                                                      //check if you are connected or not
            tvIsConnected.setBackgroundColor(0xFF00CC00);
            tvIsConnected.setText("You are connected");
        }
        else{
            tvIsConnected.setText("You are NOT connected");
        }
        upload_to_server.setOnClickListener(new View.OnClickListener() {        // add click listener to Button "POST"
            @Override
            public void onClick(View v) {
                if(!validate()) {
                    Toast.makeText(getBaseContext(), "Enter some data!", Toast.LENGTH_LONG).show();
                }else {
                    new HttpAsyncTask().execute("http://hmkcode.appspot.com/jsonservlet", write_tagid.getText().toString());    // call AsynTask to perform network operation on separate thread
                }
            }
        });
    }

    public static String POST(String url, TagDetails tag_details){
        InputStream inputStream = null;
        String result = "";
        try {

            HttpClient httpclient = new DefaultHttpClient();        // create HttpClient
            HttpPost httpPost = new HttpPost(url);                  // make POST request to the given URL
            String json = "";
            JSONObject jsonObject = new JSONObject();               // build jsonObject
            jsonObject.accumulate("name", tag_details.getTag_id());
            json = jsonObject.toString();                           // convert JSONObject to JSON to String
            StringEntity se = new StringEntity(json);               // set json to StringEntity
            httpPost.setEntity(se);                                 // set httpPost Entity
            httpPost.setHeader("Accept", "application/json");       // Set some headers to inform server about the type of the content
            httpPost.setHeader("Content-type", "application/json");
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




    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            tag_details = new TagDetails();
            tag_details.setTag_id(urls[1]);
            return POST(urls[0],tag_details);
        }

        @Override                    // onPostExecute displays the results of the AsyncTask.
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
        }
    }

    private boolean validate(){
        if(write_tagid.getText().toString().trim().equals(""))
            return false;
            else
            return true;
    }
    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }
}
