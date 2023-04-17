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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class MemberSignUpFragment extends Fragment implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore firestore;
    private EditText editText_firstname, editText_lastname2, editTextTextEmailAddress2, editTextTextPassword2;
    private Button button_memRegister;
    private String firstname, lastname, email, password;
    private ImemberRegisterFragmentAction mListener;
    private FirebaseStorage storage;
    private String profileImageURL = null;
    private ImageView memProfilePhoto;
    public MemberSignUpFragment() {
        // Required empty public constructor
    }

    public static MemberSignUpFragment newInstance() {
        MemberSignUpFragment fragment = new MemberSignUpFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ImemberRegisterFragmentAction){
            this.mListener = (ImemberRegisterFragmentAction) context;
        }else{
            throw new RuntimeException(context.toString()
                    + "must implement RegisterRequest");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_member_sign_up, container, false);
        editText_firstname = rootView.findViewById(R.id.editText_firstname);
        editText_lastname2 = rootView.findViewById(R.id.editText_lastname2);
        editTextTextEmailAddress2 = rootView.findViewById(R.id.editTextTextEmailAddress2);
        editTextTextPassword2 = rootView.findViewById(R.id.editTextTextPassword2);
        memProfilePhoto = rootView.findViewById(R.id.memProfilePhoto);
        button_memRegister = rootView.findViewById(R.id.button_memRegister);
        button_memRegister.setOnClickListener(this);

        memProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.memLoadTakePhotoFragment();
            }
        });
        return rootView;
    }

    @Override
    public void onClick(View view) {
        this.firstname = String.valueOf(editText_firstname.getText()).trim();
        this.lastname = String.valueOf(editText_lastname2.getText()).trim();
        this.email = String.valueOf(editTextTextEmailAddress2.getText()).trim();
        this.password = String.valueOf(editTextTextPassword2.getText()).trim();

        if (view.getId() == R.id.button_memRegister) {
            if (firstname.equals("")) {
                editText_firstname.setError("Must input first name!");
            }

            if (lastname.equals("")) {
                editText_lastname2.setError("Must input first name!");
            }

            if (email.equals("")) {
                editTextTextEmailAddress2.setError("Must input email!");
            }
            if (password.equals("")) {
                editTextTextPassword2.setError("Password must not be empty!");
            }


            if (!firstname.equals("") && !lastname.equals("") && !email.equals("")
                    && !password.equals("")) {

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    mUser = task.getResult().getUser();

//                                    Adding name to the FirebaseUser...
                                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(firstname)
                                            .build();

                                    mUser.updateProfile(profileChangeRequest)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        // Prepare user data for Firestore
                                                        Map<String, Object> userData = new HashMap<>();
                                                        userData.put("firstname", firstname);
                                                        userData.put("lastname", lastname);
                                                        userData.put("email", email);
                                                        //userData.put("password", password);
                                                        if (profileImageURL != null) {
                                                            userData.put("profileimage", profileImageURL);
                                                        }
                                                        firestore.collection("Member_Users")
                                                                .document(email)
                                                                .set(userData)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        // User data successfully saved to Firestore
                                                                        Toast.makeText(getActivity(), "User data saved successfully.", Toast.LENGTH_SHORT).show();

                                                                        // Call registerDone() to navigate to the MainFragment
                                                                        mListener.memberRegisterDone(mUser);
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        // Handle failure to save user data to Firestore
                                                                        Toast.makeText(getActivity(), "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    }
                                                }
                                            });
                                }
                            }
                        });

            }
        }
    }

    public void updateImage(String imagePath) {
        StorageReference imageToLoad = storage.getReference().child(imagePath);
        imageToLoad.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    profileImageURL = imagePath;
                    Glide.with(getActivity())
                            .load(task.getResult())
                            .centerCrop()
                            .into(memProfilePhoto);
                }
                else {
                    Toast.makeText(getActivity(),
                            "Unable to download image.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public interface ImemberRegisterFragmentAction {
        void memberRegisterDone(FirebaseUser mUser);
        void memLoadTakePhotoFragment();
    }
}