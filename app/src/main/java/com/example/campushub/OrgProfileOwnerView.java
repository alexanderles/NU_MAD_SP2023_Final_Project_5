package com.example.campushub;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class OrgProfileOwnerView extends Fragment {
    public OrgProfileOwnerView() {
        // Required empty public constructor
    }


    public static OrgProfileOwnerView newInstance() {
        OrgProfileOwnerView fragment = new OrgProfileOwnerView();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_org_profile_owner_view, container, false);
    }
}