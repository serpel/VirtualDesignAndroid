package com.intellisys.virtualdesign;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.intellisys.virtualdesign.Utils.FileUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class ImagesActivity extends ActionBarActivity {

    private int mProgressStatus = 0;
    private ProgressDialog mProgressDialog;
    private ProgressBarAsync mProgressbarAsync;
    private final String TAG =  "ImagesActivity.java";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);
        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ImagesFragment())
                    .commit();
        }

        //progress bar when we download content
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMessage("Work in Progress ...");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.images, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Download a file
    public boolean downloadFileAsync(String url) {

        mProgressDialog.show();

        /** Creating an instance of ProgressBarAsync */
        mProgressbarAsync = new ProgressBarAsync();

        try {
            /** ProgressBar starts its execution */
            mProgressbarAsync.execute(url);
        }catch (Exception e){
            Log.e(TAG, "File not exist: " + e.getMessage());
        }

        return true;
    }

    private void sendDataToMainActivity(String filename){
        Intent resultIntent = new Intent();
        resultIntent.putExtra(InteractiveFurniture.FILENAME, filename);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private class ProgressBarAsync extends AsyncTask<String, Integer, String> {

        private File mFile;
        /** This callback method is invoked, before starting the background process */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressStatus = 0;
        }

        /** This callback method is invoked on calling execute() method
         * on an instance of this class */

        @Override
        protected String doInBackground(String...sUrl) {

            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;

            if(sUrl.length < 1)
                return null;

            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                int fileLength = connection.getContentLength();
                input = new BufferedInputStream(connection.getInputStream());

                //Aqui creo el archivo en el storage externo o en el cache
                FileUtil u = new FileUtil();
                File file = u.createFileInStorage(getApplicationContext(), url.toString());
                //file = null;
                if(file != null && !file.exists()) {
                    output = new FileOutputStream(file);

                    byte data[] = new byte[4096];
                    long total = 0;
                    int count;

                    while ((count = input.read(data)) != -1) {
                        if (isCancelled()) {
                            input.close();
                            return null;
                        }
                        total += count;
                        if (fileLength > 0) {
                            publishProgress((int) (total * 100 / fileLength));
                        }
                        output.write(data, 0, count);
                    }
                }
                mFile = file;

            } catch (Exception e){
                return e.toString();
            } finally {
                try{
                    if(output != null)
                        output.close();
                    if(input != null)
                        input.close();
                }catch (IOException ignored)
                { }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }


        /** This callback method is invoked when publishProgress()
         * method is called */
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgressDialog.setProgress(mProgressStatus);
        }


        /** This callback method is invoked when the background function
         * doInBackground() is executed completely */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mProgressDialog.dismiss();
            //TODO: agregar modelo
            //addCustomModel(mFile);
            sendDataToMainActivity(mFile.getAbsolutePath());
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ImagesFragment extends Fragment implements AdapterView.OnItemClickListener {

        private ImageRecordsAdapter mAdapter;

        public ImagesFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_images, container, false);
        }


        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Toast.makeText(getActivity(), "test click", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            mAdapter = new ImageRecordsAdapter(getActivity());

            ListView listView = (ListView) getView().findViewById(R.id.list1);
            listView.setOnItemClickListener(mAdapter);
            listView.setAdapter(mAdapter);
            //((ImagesActivity) view.getContext()).downloadFileAsync(ModelUrl);

            fetch();
        }

        private void fetch() {
            JsonObjectRequest request = new JsonObjectRequest(
                    "http://virtualdesign.azurewebsites.net/api/models/getall",
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            try {
                                List<ModelRecord> modelRecords = parse(jsonObject);

                                mAdapter.swapImageRecords(modelRecords);
                            }
                            catch(JSONException e) {
                                Toast.makeText(getActivity(), "Unable to parse data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Toast.makeText(getActivity(), "Unable to fetch data: " + volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            VolleyApplication.getInstance().getRequestQueue().add(request);
        }

        private List<ModelRecord> parse(JSONObject json) throws JSONException {
            ArrayList<ModelRecord> records = new ArrayList<ModelRecord>();

            JSONArray jsonImages = json.getJSONArray("models");

            for(int i =0; i < jsonImages.length(); i++) {
                JSONObject jsonImage = jsonImages.getJSONObject(i);
                String name = jsonImage.getString("Name");
                String imageUrl = jsonImage.getString("ImageUrl");
                String modelUrl = jsonImage.getString("ModelUrl");
                String category = jsonImage.getString("Category");

                ModelRecord record = new ModelRecord(name, imageUrl, modelUrl, category);
                records.add(record);
            }

            return records;
        }
    }
}
