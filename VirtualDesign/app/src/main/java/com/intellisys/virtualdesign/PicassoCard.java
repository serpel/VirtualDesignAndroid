/*
 * ******************************************************************************
 *   Copyright (c) 2013-2014 Gabriele Mariotti.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  *****************************************************************************
 */

package com.intellisys.virtualdesign;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardThumbnail;

//import it.gmariotti.cardslib.demo.extras.R;

/**
 * This class provides a simple card with Thumbnail loaded with built-in method and Picasso library
 * Please refer to https://github.com/square/picasso for full doc
 *
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class PicassoCard extends Card {

    protected String mTitle;
    protected String mSecondaryTitle;
    protected String ImageUrl;
    protected String FileUrl;
    protected int count;

    public PicassoCard(Context context) {
        this(context, R.layout.carddemo_extra_picasso_inner_content, "", "");
    }

    public PicassoCard(Context context, int innerLayout, String imageUrl, String fileUrl ) {
        super(context, innerLayout);
        this.ImageUrl = imageUrl;
        this.FileUrl = fileUrl;
        init();
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    private void init() {

        //Add thumbnail
        PicassoCardThumbnail cardThumbnail = new PicassoCardThumbnail(mContext);
        cardThumbnail.setUrl(this.ImageUrl);

        //It must be set to use a external library!
        cardThumbnail.setExternalUsage(true);
        addCardThumbnail(cardThumbnail);

        //Add ClickListener
        setOnClickListener(new OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                try {
                    ((InteractiveFurniture) view.getContext()).executeProgressbarAsync(FileUrl);
                    //String cardTitle = mId != null ? mId : (mTitle + FileUrl);
                    //Toast.makeText(getContext(), "Click Listener card=" + cardTitle, Toast.LENGTH_SHORT).show();

                    BaseFragment baseFragment = ((InteractiveFurniture) view.getContext()).getBaseFragment();

                    if(baseFragment != null) {
                        FragmentManager fragmentManager = ((InteractiveFurniture) view.getContext()).getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.remove(baseFragment);
                        fragmentTransaction.commit();
                    }


                }catch (Exception e){
                    Log.e(TAG, "Error al descargar archivo detalle: "+ e.getMessage());
                }
            }
        });
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        //Retrieve elements
        TextView title = (TextView) parent.findViewById(R.id.carddemo_extra_picasso_main_inner_title);
        TextView secondaryTitle = (TextView) parent.findViewById(R.id.carddemo_extra_picasso_main_inner_secondaryTitle);

        if (title != null)
            title.setText(mTitle);

        if (secondaryTitle != null)
            secondaryTitle.setText(mSecondaryTitle);

    }


    class PicassoCardThumbnail extends CardThumbnail {

        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public PicassoCardThumbnail(Context context) {
            super(context);
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View viewImage) {

            /*
             * If your cardthumbnail uses external library you have to provide how to load the image.
             * If your cardthumbnail doesn't use an external library it will use a built-in method
             */

            //Here you have to set your image with an external library
            //Only for test, use a Resource Id and a Url

            if (!TextUtils.isEmpty(url)) {
                Picasso.with(getContext()).setIndicatorsEnabled(true);  //only for debug tests
                Picasso.with(getContext())
                        .load(url)
                        .resize(96,96)
                        .centerCrop()
                        .error(R.drawable.ic_error_loadingsmall)
                        .into((ImageView) viewImage);
            } else {
                Picasso.with(getContext()).setIndicatorsEnabled(true);  //only for debug tests
                Picasso.with(getContext())
                        .load(R.drawable.ic_tris)
                        .resize(96, 96)
                        .centerCrop()
                        .into((ImageView) viewImage);
            }
            /*
            viewImage.getLayoutParams().width = 96;
            viewImage.getLayoutParams().height = 96;
            */
        }
    }


    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getSecondaryTitle() {
        return mSecondaryTitle;
    }

    public void setSecondaryTitle(String secondaryTitle) {
        mSecondaryTitle = secondaryTitle;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
