package com.example.campushub;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class EditProfileFragment extends Fragment {

    private static final String ARG_FNAME = "fname";
    private static final String ARG_LNAME = "lname";
    private static final String ARG_PROFILEIMAGE = "profileImage";

    private String fname;
    private String lname;
    private String profileImagePath;
    private String newProfileImagePath = null;

    private ImageView imageView_camera;
    private EditText edit_Firstname, edit_lastName;
    private Button button_save_profile;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private IeditProfileActions mListener;


    public EditProfileFragment() {
        // Required empty public constructor
    }


    public static EditProfileFragment newInstance(String fname, String lname, String profileImagePath) {
        EditProfileFragment fragment = new EditProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FNAME, fname);
        args.putString(ARG_LNAME, lname);
        args.putString(ARG_PROFILEIMAGE, profileImagePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fname = getArguments().getString(ARG_FNAME);
            lname = getArguments().getString(ARG_LNAME);
            profileImagePath = getArguments().getString(ARG_PROFILEIMAGE);
        }

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        imageView_camera = rootView.findViewById(R.id.imageView_camera);
        edit_Firstname = rootView.findViewById(R.id.edit_Firstname);
        edit_Firstname.setText(fname);
        edit_lastName = rootView.findViewById(R.id.edit_lastName);
        edit_lastName.setText(lname);
        button_save_profile = rootView.findViewById(R.id.button_save_profile);
        if (profileImagePath != null) {
            loadImage();
        }

        imageView_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.loadTakeNewProfilePhoto();
            }
        });

        button_save_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DocumentReference userInfo = db.collection("Member_Users").document(mUser.getEmail());

                db.runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        transaction.update(userInfo, "firstname", edit_Firstname.getText().toString());
                        transaction.update(userInfo, "lastname", edit_lastName.getText().toString());
                        if (newProfileImagePath != null) {
                            transaction.update(userInfo, "profileImage", newProfileImagePath);
                        }
                        return null;
                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mListener.saveProfileChangesClicked();
                        }
                        else {
                            Toast.makeText(getActivity(),
                                    "Unable to update profile.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof IeditProfileActions) {
            mListener = (IeditProfileActions) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement IeditProfileActions");
        }
    }

    public void loadImage() {
        StorageReference imageToLoad = storage.getReference().child(profileImagePath);
        imageToLoad.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Glide.with(getActivity())
                            .load(task.getResult())
                            .centerCrop()
                            .into(imageView_camera);
                }
                else {
                    Toast.makeText(getActivity(),
                            "Unable to download image.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void updateImage(String imagePath) {
        StorageReference imageToLoad = storage.getReference().child(imagePath);
        imageToLoad.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    newProfileImagePath = imagePath;
                    Glide.with(getActivity())
                            .load(task.getResult())
                            .centerCrop()
                            .into(imageView_camera);
                }
                else {
                    Toast.makeText(getActivity(),
                            "Unable to download image.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public interface IeditProfileActions {
        void loadTakeNewProfilePhoto();
        void saveProfileChangesClicked();
    }
}