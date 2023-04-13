package com.example.campushub;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements
        OrganizationSignUpFragment.IregisterFragmentAction,
        MemberSignUpFragment.ImemberRegisterFragmentAction,
        LandingFragment.IloginFragmentAction,
        EventsAdapter.IEventRowActions{

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private boolean is_Orgprofile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("CampusHub");
        mAuth = FirebaseAuth.getInstance();
    }

    private void populateScreen() {
        //      Check for Authenticated users ....
        if(currentUser != null){
            if(is_Orgprofile) {
                // load the org profile screen
                // else load the home screen
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.containerMain, OrgProfileOwnerView.newInstance(),"homeFragment")
                        .addToBackStack("landingFragment")
                        .commit();
            }else if(!is_Orgprofile) {
                //The user is authenticated, Populating The Home Fragment....
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.containerMain, HomeFragment.newInstance(),"homeFragment")
                        .addToBackStack("landingFragment")
                        .commit();
            }


        }else{
//            The user is not logged in, load the login Fragment....
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, LandingFragment.newInstance(),"landingFragment")
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        populateScreen();
    }
    @Override
    public void onBackPressed() {
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("homeFragment");

        if (homeFragment != null && homeFragment.isVisible()) {
            // If the HomeFragment is currently visible, replace it with the LandingFragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, LandingFragment.newInstance(), "landingFragment")
                    .commit();
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void registerDone(FirebaseUser mUser) {
        this.currentUser = mUser;
        is_Orgprofile = true;
        populateScreen();
    }

    @Override
    public void memberRegisterDone(FirebaseUser mUser) {
        this.currentUser = mUser;
        populateScreen();
    }

    @Override
    public void populateHomeFragment(FirebaseUser mUser) {
        this.currentUser = mUser;
        populateScreen();
    }

    @Override
    public void populateClubRegisterFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, OrganizationSignUpFragment.newInstance(), "orgRegisterFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void populateMemberRegisterFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, MemberSignUpFragment.newInstance(), "memRegisterFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void loadEventDetailsFragment(Event event) {

    }
}