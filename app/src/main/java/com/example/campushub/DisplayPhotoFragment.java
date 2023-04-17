//Marko Krstulovic
//CS4250 In-Class Assignment 09
package com.example.campushub;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;


public class DisplayPhotoFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_URI = "imageUri";
    private static final String ARG_FRAGMENT = "fragment";

    private Uri imageUri;
    private String fromFragment;
    private ImageView imageViewPhoto;
    private Button buttonRetake;
    private Button buttonUpload;
    private RetakePhoto mListener;
    private ProgressBar progressBar;

    public DisplayPhotoFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static DisplayPhotoFragment newInstance(Uri imageUri, String fromFragment) {
        DisplayPhotoFragment fragment = new DisplayPhotoFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, imageUri);
        args.putString(ARG_FRAGMENT, fromFragment);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageUri = getArguments().getParcelable(ARG_URI);
            fromFragment = getArguments().getString(ARG_FRAGMENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_display_photo, container, false);
//        ProgressBar setup init.....
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        imageViewPhoto = view.findViewById(R.id.photo_to_display);
        buttonRetake = view.findViewById(R.id.button_retake_photo);
        buttonUpload = view.findViewById(R.id.button_confirm_image);
        Glide.with(view)
                .load(imageUri)
                .centerCrop()
                .into(imageViewPhoto);

        buttonRetake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onRetakePressed(fromFragment);
            }
        });

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onUploadButtonPressed(imageUri, progressBar, fromFragment);
            }
        });
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof CameraControlFragment.DisplayTakenPhoto){
            mListener = (RetakePhoto) context;
        }else{
            throw new RuntimeException(context+" must implement RetakePhoto");
        }
    }

    public interface RetakePhoto{
        void onRetakePressed(String fromFragment);

        void onUploadButtonPressed(Uri imageUri, ProgressBar progressBar, String fromFragment);
    }
}