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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Landing fragment for all users who are not logged in
 */
public class LandingFragment extends Fragment implements View.OnClickListener {

    private EditText editTextLoginEmail, editTextLoginPassword;
    private Button registerclub, button_registerMemeber, button_login;
    private String email, password;
    private FirebaseAuth mAuth;
    private IloginFragmentAction mListener;

    public LandingFragment() {
        // Required empty public constructor
    }
    public static LandingFragment newInstance() {
        LandingFragment fragment = new LandingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_landing, container, false);

        editTextLoginEmail = rootView.findViewById(R.id.editTextLoginEmail);
        editTextLoginPassword = rootView.findViewById(R.id.editTextLoginPassword);
        registerclub = rootView.findViewById(R.id.registerclub);
        button_registerMemeber = rootView.findViewById(R.id.button_registerMemeber);
        button_login = rootView.findViewById(R.id.button_login);

        registerclub.setOnClickListener(this);
        button_registerMemeber.setOnClickListener(this);
        button_login.setOnClickListener(this);


        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IloginFragmentAction){
            this.mListener = (IloginFragmentAction) context;
        }else{
            throw new RuntimeException(context.toString()+ "must implement PopulateMainFragment");
        }
    }

    @Override
    public void onClick(View view) {
        if (!isInternetAvailable()) {
            Toast.makeText(getActivity(),
                    "Internet not available. Please connect to use this app.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if(view.getId() == R.id.button_login){
            email = editTextLoginEmail.getText().toString().trim();
            password = editTextLoginPassword.getText().toString().trim();
            if(email.equals("")){
                editTextLoginEmail.setError("Must input email!");
            }
            if(password.equals("")){
                editTextLoginPassword.setError("Password must not be empty!");
            }
            if(!email.equals("") && !password.equals("")){
//                    Sign in to the account....
                mAuth.signInWithEmailAndPassword(email,password)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Toast.makeText(getContext(), "Login Successful! ", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Login Failed! "+e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    mListener.populateHomeFragment(mAuth.getCurrentUser());
                                }
                            }
                        })
                ;
            }

        }else if(view.getId()== R.id.button_registerMemeber){
            mListener.populateMemberRegisterFragment();
        }else if(view.getId()== R.id.registerclub) {
            mListener.populateClubRegisterFragment();
        }

    }

    /**
     * Checks for internet connection
     */
    private boolean isInternetAvailable() {
        InetAddress inetAddress = null;
        try {
            Future<InetAddress> future = Executors.newSingleThreadExecutor()
                    .submit(new Callable<InetAddress>() {
                        @Override
                        public InetAddress call() throws Exception {
                            try {
                                return InetAddress.getByName("www.google.com");
                            } catch (UnknownHostException e) {
                                return null;
                            }
                        }
                    });
            inetAddress = future.get(1000, TimeUnit.MILLISECONDS);
            future.cancel(true);
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
        }
        return inetAddress != null;
    }

    public interface IloginFragmentAction {
        void populateHomeFragment(FirebaseUser mUser);
        void populateClubRegisterFragment();
        void populateMemberRegisterFragment();
    }
}