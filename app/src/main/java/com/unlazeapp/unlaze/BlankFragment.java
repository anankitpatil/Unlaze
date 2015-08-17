package com.unlazeapp.unlaze;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.unlazeapp.R;

/**
 * Created by AP on 15/07/15.
 */
public class BlankFragment extends Fragment {
    private static final String KEY_TITLE = "title";

    public BlankFragment() {
        // Required empty public constructor
    }

    public static BlankFragment newInstance(String title) {
        BlankFragment f = new BlankFragment();

        Bundle args = new Bundle();

        args.putString(KEY_TITLE, title);
        f.setArguments(args);

        return (f);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // don't look at this layout it's just a listView to show how to handle the keyboard
        return inflater.inflate(R.layout.fragment_blank, container, false);
    }
}
