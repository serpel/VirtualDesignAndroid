package com.intellisys.virtualdesign;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

public class ImageRecordsAdapter extends ArrayAdapter<ModelRecord> implements AdapterView.OnItemClickListener {
    private ImageLoader mImageLoader;

    public ImageRecordsAdapter(Context context) {
        super(context, R.layout.image_list_item);

        mImageLoader = new ImageLoader(VolleyApplication.getInstance().getRequestQueue(), new BitmapLruCache());
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.image_list_item, parent, false);
        }

        // NOTE: You would normally use the ViewHolder pattern here
        NetworkImageView imageView = (NetworkImageView) convertView.findViewById(R.id.image1);
        TextView textView = (TextView) convertView.findViewById(R.id.text1);

        ModelRecord modelRecord = getItem(position);

        imageView.setImageUrl(modelRecord.getImageUrl(), mImageLoader);
        textView.setText(modelRecord.getName());

        //convertView.on
        return convertView;
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ModelRecord modelRecord = getItem(i);
        String modelUrl =  modelRecord.getModelUrl();

        //download the file
        ((ImagesActivity) view.getContext()).downloadFileAsync(modelUrl);
    }

    public void swapImageRecords(List<ModelRecord> objects) {
        clear();

        for(ModelRecord object : objects) {
            add(object);
        }

        notifyDataSetChanged();
    }
}
