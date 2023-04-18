package com.example.campushub;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserProfileFragment extends Fragment {

    private TextView textViewName;
    private TextView textViewEmail;
    private ImageView imageViewProfileImage;

    private Button buttonEditProfile;
    private Button buttonChangePassword;
    private Button buttonAccountInfo;
    private Button buttonSignout;


    private UserProfileFragment.IUserProfileAction mListener;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseStorage storage;
    private String firstName, lastName, profileImagePath;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment UserProfileOwnerView.
     */
    public static UserProfileFragment newInstance() {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);

        textViewName = rootView.findViewById(R.id.textView_user_name);
        textViewEmail = rootView.findViewById(R.id.textView_user_email);
        imageViewProfileImage = rootView.findViewById(R.id.user_profile_image);
        buttonEditProfile = rootView.findViewById(R.id.button_edit_profile);
        buttonChangePassword = rootView.findViewById(R.id.button_change_password);
        buttonAccountInfo = rootView.findViewById(R.id.button_account_info);
        buttonSignout = rootView.findViewById(R.id.button_sign_out_user);

        loadUserInfo();

        buttonEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.editUserProfileClicked(firstName, lastName, profileImagePath);
            }
        });

        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.changeUserPasswordClicked();
            }
        });

        buttonAccountInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.changeUserPasswordClicked();
            }
        });

        buttonSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.signoutClicked();
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof UserProfileFragment.IUserProfileAction){
            this.mListener = (UserProfileFragment.IUserProfileAction) context;
        }else{
            throw new RuntimeException(context.toString()+ "must implement IUserProfileAction");
        }
    }

    private void loadUserInfo() {
        db.collection("Member_Users")
                .document(mUser.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot snap = task.getResult();
                            firstName = snap.get("firstname").toString();
                            lastName = snap.get("lastname").toString();

                            String userName = firstName + " " + lastName;
                            textViewName.setText(userName);
                            textViewEmail.setText(mUser.getEmail());

                            Object imageStorage = snap.get("profileImage");
                            if (imageStorage != null) {
                                profileImagePath = imageStorage.toString();
                                StorageReference imageToLoad = storage.getReference().child(profileImagePath);
                                imageToLoad.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful() && isAdded()) {
                                            Glide.with(getActivity())
                                                    .load(task.getResult())
                                                    .centerCrop()
                                                    .into(imageViewProfileImage);
                                        }
                                        else if (isAdded()) {
                                            Toast.makeText(getActivity(),
                                                    "Unable to download image.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
    }

    interface IUserProfileAction {
        public void editUserProfileClicked(String firstName, String lastName, String profileImagePath);
        public void changeUserPasswordClicked();
        public void accountInfoClicked();
        public void signoutClicked();
    }
}