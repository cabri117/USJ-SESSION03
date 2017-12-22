package com.example.hawk.imhungry.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.example.hawk.imhungry.views.adapters.ListAdapter;
import com.example.hawk.imhungry.R;
import com.example.hawk.imhungry.models.Restaurant;

import java.util.List;

/**
 * Created by mcvasquez on 11/24/17.
 */

public class RestaurantListFragment extends ListFragment {

    public static RestaurantListFragment createInstance(double latitude, double longitude, List<Restaurant> items) {
        RestaurantListFragment fragment = new RestaurantListFragment();
        fragment.mLat = latitude;
        fragment.mLong = longitude;
        fragment.mItems = items;
        return fragment;
    }

    OnRestaurantSelectedListener mCallback;

    ListAdapter mAdapter;
    double mLat, mLong;
    List<Restaurant> mItems;

    public ListAdapter getAdapter() {
        return mAdapter;
    }

    // The container Activity must implement this interface so the frag can deliver messages
    public interface OnRestaurantSelectedListener {
        /** Called by HeadlinesFragment when a list item is selected */
        public void onRestaurantSelected(int position);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mItems == null)
            return;

        mAdapter = new ListAdapter(getContext(), R.layout.adapter_list, mItems, mLat, mLong);
        setListAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        // When in two-pane layout, set the listview to highlight the selected list item
        // (We do this during onStart because at the point the listview is available.)
        if (getFragmentManager().findFragmentById(R.id.details_fragment) != null) {
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (OnRestaurantSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnRestaurantSelectedListener");
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // Notify the parent activity of selected item
        mCallback.onRestaurantSelected(position);

        // Set the item as checked to be highlighted when in two-pane layout
        getListView().setItemChecked(position, true);
    }

    public void setupListAdapter(double latitude, double longitude, List<Restaurant> items) {
        mLat = latitude;
        mLong = longitude;
        mItems = items;

        if (mItems == null)
            return;

        mAdapter = new ListAdapter(getContext(), R.layout.adapter_list, mItems, mLat, mLong);
        setListAdapter(mAdapter);
    }
}
