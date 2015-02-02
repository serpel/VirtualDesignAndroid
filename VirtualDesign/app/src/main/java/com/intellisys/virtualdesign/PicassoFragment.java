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

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.intellisys.virtualdesign.Utils.Item;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

public class PicassoFragment extends BaseFragment {

    private ArrayList<Card> cards;
    private Activity activity;
    private CardArrayAdapter mCardArrayAdapter;

    @Override
    public int getTitleResourceId() {
        return R.string.title;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.demo_extras_fragment_picasso, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = getActivity();
        cards = new ArrayList<Card>();
        //threat
        //((MainActivity)getActivity()).getProgressDialog().
        new ListFetcher().execute();
    }

    public class ListFetcher extends AsyncTask<String, Void, ArrayList<Card>> {
        private static final String TAG = "ModelFetcher.java";
        private String SERVER_URL = "http://virtualdesign.azurewebsites.net/api/models/getmodelstemp";
        //private ArrayList<Card> mItemList;

        @Override
        protected void onPostExecute(ArrayList<Card> cards) {
            super.onPostExecute(cards);

            if(mCardArrayAdapter == null)
            {
                mCardArrayAdapter = new CardArrayAdapter(getActivity(), cards);
                CardListView listView = (CardListView) getActivity().findViewById(R.id.carddemo_extra_list_picasso);
                if (listView != null) {
                    listView.setAdapter(mCardArrayAdapter);

                    //listView.set
                    //listView.setOnClickListener(this);
                }
            }else
            {
                mCardArrayAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected ArrayList<Card> doInBackground(String... urls) {

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
                        List<Item> itemList = (List<Item>) gson.fromJson(reader,
                                new TypeToken<List<Item>>(){}.getType());
                        content.close();

                        if(cards == null)
                            cards = new ArrayList<Card>();
                        //mItemList = new ArrayList<Card>();
                        for(Item item : itemList){

                            if(item != null) {

                               item.PictureFile = TextUtils.isEmpty(item.PictureFile) ? "a.jpg" : item.PictureFile;
                               item.Description = TextUtils.isEmpty(item.Description) ? "b" : item.Description;
                               item.Name = TextUtils.isEmpty(item.Name) ? "a" : item.Name;
                               item.ModelFile = TextUtils.isEmpty(item.ModelFile) ? "a" : item.ModelFile;
                               //item.Description = item.Description != null ? item.Description : "";
                               //item.Name =  item.Name != null ? item.Name : "";
                               PicassoCard card = new PicassoCard(getActivity(), R.layout.carddemo_extra_picasso_inner_content, item.PictureFile, item.ModelFile);
                               card.setTitle(item.Name);
                               card.setSecondaryTitle(item.Description);
                               cards.add(card);
                            }
                        }

                    } catch (Exception ex) {
                        Log.e(TAG, "Failed to parse JSON due to: " + ex);
                    }
                } else {
                    Log.e(TAG, "Server responded with status code: " + statusLine.getStatusCode());
                }
            } catch(Exception ex) {
                Log.e(TAG, "Failed to send HTTP POST request due to: " + ex);
            }
            return cards;
        }

    }

}
