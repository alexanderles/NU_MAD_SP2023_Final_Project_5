package com.example.campushub;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class OrgProfileOwnerView extends Fragment {

    private Button button_sign_out_user;
    private IorgViewButtonActions  mListener;
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
        View rootView = inflater.inflate(R.layout.fragment_org_profile_owner_view, container, false);
        button_sign_out_user = rootView.findViewById(R.id.button_sign_out_user);
        button_sign_out_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.orgLogoutPressed();
            }
        });
        return rootView;
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IorgViewButtonActions){
            this.mListener = (IorgViewButtonActions) context;
        }else{
            throw new RuntimeException(context.toString()
                    + "must implement RegisterRequest");
        }
    }

    public interface IorgViewButtonActions {
        void orgLogoutPressed();

    }
}