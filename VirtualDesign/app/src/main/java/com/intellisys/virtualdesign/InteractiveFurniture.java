// Copyright 2007-2014 metaio GmbH. All rights reserved.
package com.intellisys.virtualdesign;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.Toast;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class InteractiveFurniture extends ARViewActivity
{
	private MetaioSDKCallbackHandler mCallbackHandler;
    private ArrayList<IGeometry> objectList;
	private GestureHandlerAndroid mGestureHandler;
	private TrackingValues mTrackingValues;
	private int mGestureMask;
	boolean mImageTaken;
	private Vector2d mMidPoint;
	private View mLayoutGeometries;
    private int count = 1;

    private static String BUNDLE_SELECTEDFRAGMENT = "BDL_SELFRG";
    public int mCurrentTitle = R.string.app_name;
    private final String TAG =  "InteractiveFurniture.java";
    public static String FILENAME = "FILENAME";
    public final int MODEL_LIST_ACTIVITY = 0;
    private String filepath = "";

	/**
	 * File where camera image will be temporarily stored
	 */
	private File mImageFile;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mGestureMask = GestureHandler.GESTURE_ALL;
		mImageTaken = false;

		mCallbackHandler = new MetaioSDKCallbackHandler();
		mGestureHandler = new GestureHandlerAndroid(metaioSDK, mGestureMask);

        //default configuration
        mMidPoint = new Vector2d();
        objectList = new ArrayList<>();
		mImageFile = new File(Environment.getExternalStorageDirectory(), "target.jpg");
	}

	@Override
	public void onSurfaceCreated() {
		super.onSurfaceCreated();
		addCustomModel(this.filepath);
	}

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case MODEL_LIST_ACTIVITY:
                if(resultCode == RESULT_OK && data != null){
                    final String filename = data.getStringExtra(FILENAME);
                    String ext = MimeTypeMap.getFileExtensionFromUrl(filename).toLowerCase();
                    // validate the extension
                    //TODO: dinamic validation for support more formats
                    if(ext.equals("fbx") || ext.equals("obj") || ext.equals("zip"))
                    {
                        // if you call addcustommodel it gives a big error
                        this.filepath = filename;
                    }else{
                        Toast.makeText(this, "3D Format not supported: " + ext, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                break;
        }
    }

    public void onOpenList(View v)
    {
        try {
            Intent intent = new Intent(this, ImagesActivity.class);
            startActivityForResult(intent, MODEL_LIST_ACTIVITY);
        }catch (Exception e){
            Log.e(TAG, "onOpenList: error on start Activity 'ImageActivity' "+e.getMessage());
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
            ImageButton b = (ImageButton)findViewById(R.id.button_add);
            b.setVisibility(v.VISIBLE);

            ImageButton b1 = (ImageButton)findViewById(R.id.button_screenshot);
            b1.setVisibility(v.VISIBLE);

            //ImageButton b2 = (ImageButton)findViewById(R.id.button_refresh);
            //b2.setVisibility(v.VISIBLE);

            //mGestureHandler.
            //IGeometry mObject

        }else{

            ImageButton b = (ImageButton)findViewById(R.id.button_add);
            b.setVisibility(v.INVISIBLE);

            ImageButton b1 = (ImageButton)findViewById(R.id.button_screenshot);
            b1.setVisibility(v.INVISIBLE);

            //ImageButton b2 = (ImageButton)findViewById(R.id.button_refresh);
            //b2.setVisibility(v.INVISIBLE);

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

            //mGestureHandler.removeObjects();

            for(IGeometry i:objectList){
                i.setVisible(false);
                //Vector3d translation = metaioSDK.get3DPositionFromViewportCoordinates(1, mMidPoint);
                //o.setTranslation(translation);
            }
            //mGestureHandler.notifyAll();
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

    /*public void onRefresh(View v){

        if(!this.filepath.isEmpty()){
            if(mSurfaceView == null){
                return;
            }
            addCustomModel(this.filepath);
            this.filepath = "";
        }
    }*/

    public void addCustomModel(final String filepath){
            //this run in the Main Thread
            if(mSurfaceView == null) {
                return;
            }

                mSurfaceView.queueEvent(new Runnable() {
                                            @Override
                                            public void run() {
                                                loadModel(filepath);
                                            }
                                        }
                );
    }

    public void loadModel(String filepath)
    {
        File file = new File(filepath);

        if(file == null)
            return;

        IGeometry mObject = metaioSDK.createGeometry(file);

        if (mObject != null)
        {
            mObject.setScale(40f);
            mObject.setRotation(new Rotation((float)Math.PI / 2f, 0f, -(float)Math.PI / 4f));
            mObject.setTranslation(new Vector3d(0f, 10f, 0f));
            mGestureHandler.addObject(mObject, count++);

            Vector3d translation = metaioSDK.get3DPositionFromViewportCoordinates(1, mMidPoint);
            mObject.setTranslation(translation);
            mObject.setScale(70f);
            mObject.setVisible(true);

            objectList.add(mObject);
        }
        else
        {
            //Toast toast = Toast.makeText(this, "Error loading geometry", Toast.LENGTH_SHORT);
            MetaioDebug.log(Log.ERROR, "Error loading geometry: " + file);
            //toast.show();
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

            //aqui se tiene que cargar esto siempre sea como sea.
            if(!this.filepath.isEmpty()) {
                //loadModel(this.filepath);
                addCustomModel(this.filepath);
                this.filepath = "";
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

    private void shareImage(String imagePath) {

        if(mImageFile != null) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/*");
            File imageFileToShare = new File(imagePath);
            Uri uri = Uri.fromFile(imageFileToShare);
            share.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(share, "Share Image!"));
        }
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
                            //share image to another apps
                            shareImage(screenshotFile.getAbsolutePath());
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

}
