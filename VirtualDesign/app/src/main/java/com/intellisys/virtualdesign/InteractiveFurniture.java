// Copyright 2007-2014 metaio GmbH. All rights reserved.
package com.intellisys.virtualdesign;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.intellisys.virtualdesign.Utils.FileUtil;
import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.GestureHandlerAndroid;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.GestureHandler;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.ImageStruct;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.TrackingValues;
import com.metaio.sdk.jni.Vector2d;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import it.gmariotti.cardslib.library.internal.Card;

public class InteractiveFurniture extends ARViewActivity
{
	private MetaioSDKCallbackHandler mCallbackHandler;
	private IGeometry mTV;
    private IGeometry mObject;
	private GestureHandlerAndroid mGestureHandler;
	private TrackingValues mTrackingValues;
	private int mGestureMask;
	boolean mImageTaken;
	private Vector2d mMidPoint;
	private View mLayoutGeometries;
    private int count = 1;

    private BaseFragment mBaseFragment;
    private static String BUNDLE_SELECTEDFRAGMENT = "BDL_SELFRG";
    public int mCurrentTitle = R.string.app_name;
    private String serverURL =  "";
    private int mProgressStatus = 0;
    private ProgressDialog mProgressDialog ;
    private ProgressBarAsync mProgressbarAsync;
    private Card mCard;
    private final String TAG =  "InteractiveFurniture.java";

	/**
	 * File where camera image will be temporarily stored
	 */
	private File mImageFile;

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

    private void openFragment(BaseFragment baseFragment) {
       //if (savedInstanceState == null) {
            mBaseFragment = new PicassoFragment();

            if(mBaseFragment != null) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.main_relative, mBaseFragment);
                fragmentTransaction.commit();
            }
        //}
    }

    public BaseFragment getBaseFragment() {
        return mBaseFragment;
    }

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mGestureMask = GestureHandler.GESTURE_ALL;
		mImageTaken = false;

		mCallbackHandler = new MetaioSDKCallbackHandler();
		mGestureHandler = new GestureHandlerAndroid(metaioSDK, mGestureMask);
		mMidPoint = new Vector2d();

		mImageFile = new File(Environment.getExternalStorageDirectory(), "target.jpg");

        //new ModelFetcher().execute(serverURL);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMessage("Work in Progress ...");

        /*
        if (savedInstanceState == null) {
            mBaseFragment = new PicassoFragment();

            if(mBaseFragment != null) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.container, mBaseFragment);
                fragmentTransaction.commit();
            }
        }*/
        count = 0;
	}


    public void onOpenList(View v)
    {
        try {

            /*
            mTV.setVisible(true);
            Vector3d translation = metaioSDK.get3DPositionFromViewportCoordinates(1, mMidPoint);
            mTV.setTranslation(translation);
            mTV.setScale(50f);*/

            openFragment(new PicassoFragment());
           // File file = AssetsManager.getAssetPathAsFile(getApplicationContext(), "tv.obj");
            //addCustomModel(file);

           // Vector3d translation = metaioSDK.get3DPositionFromViewportCoordinates(1, mMidPoint);
           // mObject.setTranslation(translation);
            //mObject.setScale(50f);
            //mObject.setVisible(true);
            //Log.i(TAG, " x: " + translation.getX() + " y: " + translation.getY());


        }catch (Exception e){
            Log.e(TAG, "No se puede abrir lista");
        }
    }

	@Override
	protected void onPause()
	{
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		mLayoutGeometries = mGUIView.findViewById(R.id.layoutGeometries);

		// if a tracking target image exists, then the app is still running in the background
		if (mImageFile.exists() && mTrackingValues != null)
		{
			// the tracking target has to be reset and so are the tracking values
			metaioSDK.setImage(mImageFile);
			metaioSDK.setCosOffset(1, mTrackingValues);

		}

	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		mCallbackHandler.delete();
		mCallbackHandler = null;

		// delete the tracking target image before exit if it has been generated
		if (mImageFile.exists())
		{
			boolean result = mImageFile.delete();
			MetaioDebug.log("The file has been deleted: " + result);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		super.onTouch(v, event);

		mGestureHandler.onTouch(v, event);

		return true;
	}

	@Override
	protected int getGUILayout()
	{
		return R.layout.interactive_furniture;
	}

	@Override
	protected IMetaioSDKCallback getMetaioSDKCallbackHandler()
	{
		return mCallbackHandler;
	}

	@Override
	public void onSurfaceChanged(int width, int height)
	{
		super.onSurfaceChanged(width, height);

		// Update mid point of the view
		mMidPoint.setX(width / 2f);
		mMidPoint.setY(height / 2f);
	}

	@Override
	public void onDrawFrame()
	{
		super.onDrawFrame();

		// reset the location and scale of the geometries
		if (mImageTaken == true)
		{
			// load the dummy tracking config file
			boolean result = metaioSDK.setTrackingConfiguration("DUMMY");
			MetaioDebug.log("Tracking data Dummy loaded: " + result);

			metaioSDK.setCosOffset(1, mTrackingValues);

			mImageTaken = false;
		}


	}

	public void onButtonClick(View v)
	{
		finish();
	}

	// called when the save screenshot button has been pressed
	public void onSaveScreen(View v)
	{
		// request screen shot
		metaioSDK.requestScreenshot();
	}

    Boolean isPictureTaken = false;
    // called when the save screenshot button has been pressed
    public void onSaveOrReset(View v)
    {
        // request screen shot
        if(!isPictureTaken) {
            metaioSDK.requestCameraImage(mImageFile);
            isPictureTaken = true;
        }else{
            startCamera();
            isPictureTaken = false;
            // delete the tracking target if generated
            String imagepath = Environment.getExternalStorageDirectory().getPath() + "/target.jpg";
            File file = new File(imagepath);
            if (file.exists())
            {
                boolean result = file.delete();
                MetaioDebug.log("The file has been deleted: " + result);
            }

            // load the ORIENTATION tracking config file again
            boolean result = metaioSDK.setTrackingConfiguration("ORIENTATION_FLOOR");
            MetaioDebug.log("Tracking data loaded: " + result);

            mLayoutGeometries.setVisibility(View.GONE);
            mGUIView.bringToFront();
        }
    }


	// called when the take picture button has been pressed
	public void onTakePicture(View v)
	{
		// take a picture using the SDK and save it to external storage
		metaioSDK.requestCameraImage(mImageFile);

	}


    public void addCustomModel(final File filepath){

            mSurfaceView.queueEvent( new Runnable() {
                                         @Override
                                         public void run() {
                                            loadModel(filepath);
                                         }
                                     }
            );
    }

    public void loadModel(File file)
    {
        if(file == null)
            return;

        boolean result = metaioSDK.setTrackingConfiguration("ORIENTATION_FLOOR");
        Log.i(TAG, "load: " + result );
        MetaioDebug.log("Tracking data loaded: " + result);

        mObject = metaioSDK.createGeometry(file);
        if (mObject != null)
        {
            mObject.setScale(40f);
            mObject.setRotation(new Rotation((float)Math.PI / 2f, 0f, -(float)Math.PI / 4f));
            mObject.setTranslation(new Vector3d(0f, 10f, 0f));
            mObject.setVisible(true);
            mGestureHandler.addObject(mObject, count);
            count++;

            /*
            Vector3d translation = metaioSDK.get3DPositionFromViewportCoordinates(1, mMidPoint);
            mObject.setTranslation(translation);
            mObject.setScale(50f);
            mObject.setVisible(true);
            metaioSDK.reloadOpenGLResources();
            */
        }
        else
        {
            MetaioDebug.log(Log.ERROR, "Error loading geometry: " + file);
        }
    }

	@Override
	protected void loadContents()
	{
		try
		{
			// TODO: Load desired tracking data for planar marker tracking
			boolean result = metaioSDK.setTrackingConfiguration("ORIENTATION_FLOOR");
			MetaioDebug.log("Tracking data loaded: " + result);

			// Load all the geometries
			// Load TV

			File filepath =
					AssetsManager.getAssetPathAsFile(getApplicationContext(),
							"tv.obj");
			if (filepath != null)
			{
				mTV = metaioSDK.createGeometry(filepath);

				if (mTV != null)
				{
					mTV.setScale(50f);
					mTV.setRotation(new Rotation((float)Math.PI / 2f, 0f, -(float)Math.PI / 4f));
					mTV.setTranslation(new Vector3d(0f, 10f, 0f));
					mGestureHandler.addObject(mTV, 1);
				}
				else
				{
					MetaioDebug.log(Log.ERROR, "Error loading geometry: " + filepath);
				}
			}
		}
		catch (Exception e)
		{
			MetaioDebug.log(Log.ERROR, "loadContents failed: " + e);
		}
	}


	@Override
	protected void onGeometryTouched(final IGeometry geometry)
	{
		MetaioDebug.log("MetaioSDKCallbackHandler.onGeometryTouched: " + geometry);
	}

	final class MetaioSDKCallbackHandler extends IMetaioSDKCallback
	{
		/**
		 * Get path to Pictures directory if it exists
		 * 
		 * @return Path to Pictures directory on the device if found, else <code>null</code>
		 */
		private File getPicturesDirectory()
		{
			File picPath = null;

			try
			{
				picPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
				File path = new File(picPath, "Metaio Example");
				boolean success = path.mkdirs() || path.isDirectory();
				if (!success)
				{
					path = new File(Environment.getExternalStorageDirectory(), "Pictures");
				}
				success = path.mkdirs() || path.isDirectory();
				if (!success)
				{
					path = Environment.getDataDirectory();
				}

				return path.getAbsoluteFile();
			}
			catch (Exception e)
			{
				return null;
			}
		}

		@Override
		public void onSDKReady()
		{
			// show GUI
			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					mGUIView.setVisibility(View.VISIBLE);
				}
			});
		}

		// callback function for taking images using SDK
		@Override
		public void onCameraImageSaved(final File filePath)
		{
			// save the tracking values in case the application exits improperly
			mTrackingValues = metaioSDK.getTrackingValues(1);
			mImageTaken = true;

			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					if (filePath.getPath().length() > 0)
					{
						metaioSDK.setImage(filePath);
						mLayoutGeometries.setVisibility(View.VISIBLE);
					}
				}
			});

		}

		@Override
		public void onScreenshotImage(ImageStruct image)
		{
			final File directory = getPicturesDirectory();
			if (directory == null)
			{
				image.release();
				image.delete();

				MetaioDebug.log(Log.ERROR, "Could not find pictures directory, not saving screenshot");
				return;
			}

			// Creating directory
			directory.mkdirs();

			try
			{
				// Creating file
				final File screenshotFile = new File(directory, "screenshot_" + System.currentTimeMillis() + ".jpg");
				screenshotFile.createNewFile();

				FileOutputStream stream = new FileOutputStream(screenshotFile);

				boolean result = false;
				Bitmap bitmap = image.getBitmap();
				try
				{
					result = bitmap.compress(CompressFormat.JPEG, 100, stream);
				}
				finally
				{
					// release screenshot ImageStruct
					image.release();
					image.delete();

					stream.close();
				}

				if (!result)
				{
					MetaioDebug.log(Log.ERROR, "Failed to save screenshot to " + screenshotFile);
					return;
				}

				final String url =
						MediaStore.Images.Media.insertImage(getContentResolver(), bitmap,
								"screenshot_" + System.currentTimeMillis(), "screenshot");


				// Recycle the bitmap
				bitmap.recycle();
				bitmap = null;

				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						String message = "The screenshot has been added to the gallery.";
						if (url == null)
						{
							message = "Unable to add the screen shot to the gallery";
						}
						else
						{
							MediaScannerConnection.scanFile(getApplicationContext(),
									new String[] {screenshotFile.getAbsolutePath()}, new String[] {"image/jpg"},
									new OnScanCompletedListener()
									{
										@Override
										public void onScanCompleted(String path, Uri uri)
										{
											MetaioDebug.log("Screen saved at path " + path);
										}
									});
						}

						Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
				});
			}
			catch (IOException e)
			{
				MetaioDebug.printStackTrace(Log.ERROR, e);
			}
		}
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
                   mFile = file;
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
            addCustomModel(mFile);
        }

    }
}
