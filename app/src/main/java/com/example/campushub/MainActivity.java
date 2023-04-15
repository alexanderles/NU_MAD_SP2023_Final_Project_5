package com.example.campushub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.campushub.Calendar.CalendarFragment;
import com.example.campushub.Calendar.CalendarFragmentDay;
import com.example.campushub.Calendar.CalendarFragmentMonth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.LocalDate;

public class MainActivity extends AppCompatActivity implements
        OrganizationSignUpFragment.IregisterFragmentAction,
        MemberSignUpFragment.ImemberRegisterFragmentAction,
        LandingFragment.IloginFragmentAction,
        EventsAdapter.IEventRowActions,
        NavigationFragment.INavigationActions,
        CalendarFragmentMonth.ICalendarMonthActions{

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private boolean is_Orgprofile = false;
    private View navigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("CampusHub");
        mAuth = FirebaseAuth.getInstance();
        navigationBar = findViewById(R.id.fragmentContainerViewNav);
        navigationBar.setTransitionVisibility(View.GONE);
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
                navigationBar.setVisibility(View.VISIBLE);
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
            navigationBar.setVisibility(View.GONE);
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

    @Override
    public void homeClickedNav() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, HomeFragment.newInstance(),"homeFragment")
                .addToBackStack("Prev")
                .commit();
    }

    @Override
    public void searchClickedNav() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, SearchFragment.newInstance(),"homeFragment")
                .addToBackStack("Prev")
                .commit();
    }

    @Override
    public void calendarClickedNav() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, CalendarFragment.newInstance(),"homeFragment")
                .addToBackStack("Prev")
                .commit();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContainerViewCalendar, CalendarFragmentMonth.newInstance(),"dayFragment")
                .commit();
    }

    @Override
    public void profileClickedNav() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, UserProfileOwnerView.newInstance(),"homeFragment")
                .addToBackStack("Prev")
                .commit();
    }

    @Override
    public void dayClicked(LocalDate selectedDate) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerViewCalendar, CalendarFragmentDay.newInstance(selectedDate),"dayFragment")
                .addToBackStack("Month")
                .commit();
    }
}