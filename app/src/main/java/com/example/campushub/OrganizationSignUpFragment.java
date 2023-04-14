package com.example.campushub;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class OrganizationSignUpFragment extends Fragment implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore firestore;
    private EditText editText_orgname, editTextTextEmailAddress4, editTextTextPassword4;
    private Button button_register;
    private String orgname, email, password;
    private IregisterFragmentAction mListener;




    public OrganizationSignUpFragment() {
        // Required empty public constructor
    }

    public static OrganizationSignUpFragment newInstance() {
        OrganizationSignUpFragment fragment = new OrganizationSignUpFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IregisterFragmentAction){
            this.mListener = (IregisterFragmentAction) context;
        }else{
            throw new RuntimeException(context.toString()
                    + "must implement RegisterRequest");
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_organization_sign_up, container, false);
        editText_orgname = rootView.findViewById(R.id.editText_orgname);
        editTextTextEmailAddress4 = rootView.findViewById(R.id.editTextTextEmailAddress4);
        editTextTextPassword4 = rootView.findViewById(R.id.editTextTextPassword4);
        button_register = rootView.findViewById(R.id.button_register);
        button_register.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        this.orgname = String.valueOf(editText_orgname.getText()).trim();
        this.email = String.valueOf(editTextTextEmailAddress4.getText()).trim();
        this.password = String.valueOf(editTextTextPassword4.getText()).trim();

        if (view.getId() == R.id.button_register) {
            if (orgname.equals("")) {
                editText_orgname.setError("Must input first name!");
            }


            if (email.equals("")) {
                editTextTextEmailAddress4.setError("Must input email!");
            }
            if (password.equals("")) {
                editTextTextPassword4.setError("Password must not be empty!");
            }


            if (!orgname.equals("") && !email.equals("")
                    && !password.equals("")) {

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    mUser = task.getResult().getUser();

//                                    Adding name to the FirebaseUser...
                                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(orgname)
                                            .build();

                                    mUser.updateProfile(profileChangeRequest)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        // Prepare user data for Firestore
                                                        Map<String, Object> userData = new HashMap<>();
                                                        userData.put("Org_Name", orgname);
                                                        userData.put("email", email);
                                                        //userData.put("password", password);
                                                        firestore.collection("Org_Users")
                                                                .document(email)
                                                                .set(userData)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        // User data successfully saved to Firestore
                                                                        Toast.makeText(getActivity(), "User data saved successfully.", Toast.LENGTH_SHORT).show();

                                                                        // Call registerDone() to navigate to the MainFragment
                                                                        mListener.registerDone(mUser);
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

    public interface IregisterFragmentAction {
        void registerDone(FirebaseUser mUser);
    }
}