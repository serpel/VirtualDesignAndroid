package com.intellisys.virtualdesign;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.intellisys.virtualdesign.Utils.FileUtil;
import com.intellisys.virtualdesign.Utils.ModelFetcher;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import it.gmariotti.cardslib.library.internal.Card;


public class MainActivity extends ActionBarActivity {

    private BaseFragment mBaseFragment;
    private static String BUNDLE_SELECTEDFRAGMENT = "BDL_SELFRG";
    public int mCurrentTitle = R.string.app_name;
    private String serverURL =  "";
    private int mProgressStatus = 0;
    private ProgressDialog mProgressDialog ;
    private ProgressBarAsync mProgressbarAsync;
    private Card mCard;
    private final String TAG =  "MainActivity.java";

    public ProgressDialog getProgressDialog() {
        return mProgressDialog;
    }

    public boolean executeProgressbarAsync(String url) {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new ModelFetcher().execute(serverURL);

        if (savedInstanceState == null) {
            mBaseFragment = new PicassoFragment();

            if(mBaseFragment != null) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.container, mBaseFragment);
                fragmentTransaction.commit();
            }
        }


        //TODO: fix listener
        //mCard =  findViewById(R.id.card)
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMessage("Work in Progress ...");

        /*View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Show the progress dialog window
                mProgressDialog.show();

                /// Creating an instance of ProgressBarAsync
                mProgressbarAsync = new ProgressBarAsync();

                // ProgressBar starts its execution
                mProgressbarAsync.execute("https://virtualdesign.blob.core.windows.net/models/b0e7f84b-306c-409f-8fce-00f698dbfc10-screen.obj");
            }
        };*/
        //mBtnStart.setOnClickListener(listener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ProgressBarAsync extends AsyncTask<String, Integer, String> {

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

                FileUtil u = new FileUtil();
                File file = u.createFileInStorage(getApplicationContext(), url.toString());

                if(file != null) {
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
        }

    }
}
