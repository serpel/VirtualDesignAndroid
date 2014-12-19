package com.intellisys.virtualdesign.Utils;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

/**
 * Created by serpe_000 on 15/12/2014.
 */
public class ModelFetcher extends AsyncTask<String, Void, List<Item>> {
    private static final String TAG = "ModelFetcher.java";
    private static String SERVER_URL = "http://virtualdesign.azurewebsites.net/api/models";
    private List<Item> mItemList;

    @Override
    protected void onPostExecute(List<Item> items) {
        super.onPostExecute(items);
        //items = this.mItemList;
    }

    @Override
    protected List<Item> doInBackground(String... urls) {

        if(urls.length > 0)
            SERVER_URL = urls[0];

        try {
            //Create an HTTP client
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(SERVER_URL);

            //Perform the request and check the status code
            HttpResponse response = client.execute(get);
            StatusLine statusLine = response.getStatusLine();

            if(statusLine.getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();

                try {
                    //Read the server response and attempt to parse it as JSON
                    Reader reader = new InputStreamReader(content);

                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();
                    this.mItemList = (List<Item>) gson.fromJson(reader,
                            new TypeToken<List<Item>>(){}.getType());
                    content.close();

                } catch (Exception ex) {
                    Log.e(TAG, "Failed to parse JSON due to: " + ex);
                }
            } else {
                Log.e(TAG, "Server responded with status code: " + statusLine.getStatusCode());
            }
        } catch(Exception ex) {
            Log.e(TAG, "Failed to send HTTP POST request due to: " + ex);
        }
        return mItemList;
    }

    public List<Item> getItems(){
        return mItemList;
    }

    private void printList(){
        for(Item item: mItemList){
            Log.i(TAG, item.Name + ":" + item.Description + ":" + item.PictureFile +  ":" + item.ModelFile);
        }
    }
}
