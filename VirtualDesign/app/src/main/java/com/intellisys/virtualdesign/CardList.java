package com.intellisys.virtualdesign;

import android.app.Activity;
import android.os.Bundle;

import com.intellisys.virtualdesign.Utils.ModelFetcher;

public class CardList extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        new ModelFetcher().execute();

        /*
        int listImages[] = new int[]{R.drawable.angry_1, R.drawable.angry_2,
                R.drawable.angry_3, R.drawable.angry_4, R.drawable.angry_5};

        ArrayList<Card> cards = new ArrayList<Card>();

        for (int i = 0; i<5; i++) {
            // Create a Card
            Card card = new Card(this);
            // Create a CardHeader
            CardHeader header = new CardHeader(this);
            // Add Header to card
            header.setTitle("Angry bird: " + i);
            card.setTitle("sample title");
            card.addCardHeader(header);

            CardThumbnail thumb = new CardThumbnail(this);
            thumb.setDrawableResource(listImages[i]);
            card.addCardThumbnail(thumb);

            cards.add(card);
        }


        CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(this, cards);

        CardListView listView = (CardListView) this.findViewById(R.id.myList);
        if (listView != null) {
            listView.setAdapter(mCardArrayAdapter);
        }
        */
    }
}