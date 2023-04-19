package com.example.campushub;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.campushub.Events.AddEditEventFragment;
import com.example.campushub.Events.Event;
import com.example.campushub.Events.EventsAdapter;
import com.example.campushub.Events.OwnerEventView;
import com.example.campushub.Events.UserEventView;
import com.example.campushub.Organization.OrgProfileOwnerView;
import com.example.campushub.Organization.OrgProfileUserView;
import com.example.campushub.Organization.Organization;
import com.example.campushub.Organization.OrganizationSignUpFragment;
import com.example.campushub.Organization.OrganizationsAdapter;
import com.example.campushub.Profile.AppInfoFragment;
import com.example.campushub.Profile.EditProfileFragment;
import com.example.campushub.Profile.UserProfileFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.example.campushub.Calendar.CalendarFragment;
import com.example.campushub.Calendar.CalendarFragmentDay;
import com.example.campushub.Calendar.CalendarFragmentMonth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * Main Activity for CampusHub Application
 */
public class MainActivity extends AppCompatActivity implements
        OrganizationSignUpFragment.IregisterFragmentAction,
        MemberSignUpFragment.ImemberRegisterFragmentAction,
        LandingFragment.IloginFragmentAction,
        CameraControlFragment.DisplayTakenPhoto,
        DisplayPhotoFragment.RetakePhoto,
        EditProfileFragment.IeditProfileActions,
        EventsAdapter.IEventRowActions,
        OrgProfileOwnerView.IOrgProfileOwnerActions,
        AddEditEventFragment.IAddEditEventActions,
        OwnerEventView.IOwnerEventDetailsActions,
        NavigationFragment.INavigationActions,
        CalendarFragmentMonth.ICalendarMonthActions,
        OrganizationsAdapter.IOrganizationRowActions,
        UserProfileFragment.IUserProfileAction {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private boolean isOrgProfile = false;

    private String galleryFragmentInfo = null;

    private static final int PERMISSIONS_CODE = 0x100;
    private FirebaseStorage storage;

    private View navigationBar;
    private boolean returningFromGallery = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("CampusHub");
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        Boolean cameraAllowed = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        Boolean readAllowed = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        Boolean writeAllowed = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if(cameraAllowed && readAllowed && writeAllowed){
            Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show();

        }else{
            requestPermissions(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PERMISSIONS_CODE);
        }

        if (!isInternetAvailable()) {
            Toast.makeText(MainActivity.this,
                    "Internet not available. Please connect to use this app.",
                    Toast.LENGTH_LONG).show();
        }
        navigationBar = findViewById(R.id.fragmentContainerViewNav);
        navigationBar.setTransitionVisibility(View.GONE);
    }

    private void loadCorrectFragment(boolean isOrgUser) {
        if (isOrgUser) {
            // Load the org profile screen
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, OrgProfileOwnerView.newInstance(), "orgViewFragment")
                    .commit();
        } else {
            // Load the home screen for member
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, HomeFragment.newInstance(), "homeFragment")
                    .commit();
            navigationBar.setVisibility(View.VISIBLE);
        }
    }


    private void populateScreen() {
        // Check for Authenticated users...
        if (currentUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Member_Users")
                    .document(currentUser.getEmail())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // User is a member
                                isOrgProfile = false;
                                loadCorrectFragment(false);
                            } else {
                                // User is an organization
                                isOrgProfile = true;
                                loadCorrectFragment(true);
                            }
                        } else {
                            Toast.makeText(this, "Unable to login.", Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            // The user is not logged in, load the login Fragment...
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, LandingFragment.newInstance(), "landingFragment")
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if (returningFromGallery) {
            returningFromGallery = false;
        } else {
            populateScreen();
        }
    }

    @Override
    public void onBackPressed() {
        Fragment dayFound = getSupportFragmentManager().findFragmentByTag("dayFragment");
        if (dayFound != null && !dayFound.isVisible()) {
            CalendarFragmentDay newDay = (CalendarFragmentDay) dayFound;
            getSupportFragmentManager().popBackStack();
            getSupportFragmentManager().popBackStack();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerViewCalendar, CalendarFragmentDay.newInstance(newDay.getSelectedDate()),"dayFragment")
                    .addToBackStack("day_fragment")
                    .commit();
        }
        else {
            super.onBackPressed();
        }
    }


    @Override
    public void registerDone(FirebaseUser mUser) {
        this.currentUser = mUser;
        isOrgProfile = true;
        populateScreen();
    }

    @Override
    public void orgLoadTakePhotoFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, CameraControlFragment.newInstance("orgRegisterFragment"), "cameraFragment")
                .addToBackStack("take_photo")
                .commit();
    }

    @Override
    public void memberRegisterDone(FirebaseUser mUser) {
        this.currentUser = mUser;
        isOrgProfile = false;
        populateScreen();
    }

    @Override
    public void memLoadTakePhotoFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, CameraControlFragment.newInstance("memRegisterFragment"), "cameraFragment")
                .addToBackStack("take_photo")
                .commit();

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
        if (isOrgProfile) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, OwnerEventView.newInstance(event), "ownerEventViewFragment")
                    .addToBackStack("owner_event_view")
                    .commit();
        }
        else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, UserEventView.newInstance(event), "userEventViewFragment")
                    .addToBackStack("user_event_view")
                    .commit();
        }
    }

    @Override
    public void orgProfileLogout() {
        mAuth.signOut();
        currentUser = null;
        navigationBar.setVisibility(View.GONE);
        populateScreen();
    }

    @Override
    public void addEvent() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, new AddEditEventFragment(), "eventAddFragment")
                .addToBackStack("add_event")
                .commit();
    }

    @Override
    public void loadOrgProfile() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void editEvent(Event event) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, AddEditEventFragment.newInstance(event), "eventEditFragment")
                .addToBackStack("edit_event")
                .commit();
    }

    @Override
    public void deleteEventRedirect() {
        getSupportFragmentManager().popBackStack();
    }

    private void clearBackStack() {
        int backstackCount = getSupportFragmentManager().getBackStackEntryCount();
        for (int i = 0; i < backstackCount; i++) {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void homeClickedNav() {
        clearBackStack();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, HomeFragment.newInstance(),"homeFragment")
                .commit();
    }

    @Override
    public void searchClickedNav() {
        clearBackStack();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, SearchFragment.newInstance(),"homeFragment")
                .addToBackStack("search")
                .commit();
    }

    @Override
    public void calendarClickedNav() {
        clearBackStack();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, CalendarFragment.newInstance(),"calendarFragment")
                .replace(R.id.fragmentContainerViewCalendar, CalendarFragmentMonth.newInstance(),"monthFragment")
                .addToBackStack("calendar")
                .commit();
    }

    @Override
    public void profileClickedNav() {
        clearBackStack();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, UserProfileFragment.newInstance(),"homeFragment")
                .addToBackStack("profile")
                .commit();
    }

    @Override
    public void dayClicked(LocalDate selectedDate) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerViewCalendar, CalendarFragmentDay.newInstance(selectedDate),"dayFragment")
                .addToBackStack("day_fragment")
                .commit();
    }

    @Override
    public void onTakePhoto(Uri imageUri, String fromFragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, DisplayPhotoFragment.newInstance(imageUri, fromFragment),"displayFragment")
                .addToBackStack("display_photo")
                .commit();
    }

    @Override
    public void onOpenGalleryPressed(String fromFragment) {
        galleryFragmentInfo = fromFragment;
        returningFromGallery = true;
        openGallery(fromFragment);
    }

    private void openGallery(String fromFragment) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        galleryLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode()==RESULT_OK){
                        Intent data = result.getData();
                        Uri selectedImageUri = data.getData();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.containerMain, DisplayPhotoFragment.newInstance(selectedImageUri, galleryFragmentInfo),"displayFragment")
                                .addToBackStack("display_photo")
                                .commit();
                    }
                    else if (result.getResultCode() == RESULT_CANCELED) {
                        CameraControlFragment returnToControl = (CameraControlFragment) getSupportFragmentManager().findFragmentByTag("cameraFragment");
                    }
                }

            }
    );

    @Override
    public void onRetakePressed(String fromFragment) {
        getSupportFragmentManager().popBackStack();
        if (galleryFragmentInfo != null) {
            CameraControlFragment returnToControl = (CameraControlFragment) getSupportFragmentManager().findFragmentByTag("cameraFragment");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, returnToControl)
                    .commit();
        }

    }

    @Override
    public void onUploadButtonPressed(Uri imageUri, ProgressBar progressBar, String fromFragment) {
        progressBar.setVisibility(View.VISIBLE);
        // Upload an image from local file....
        String childPath = "images/"+imageUri.getLastPathSegment();
        StorageReference storageReference = storage.getReference().child(childPath);
        UploadTask uploadImage = storageReference.putFile(imageUri);

        uploadImage.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Upload Failed! Try again!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(MainActivity.this, "Upload successful! Check Firestore", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);

                        switch (fromFragment) {
                            case "memRegisterFragment": {
                                MemberSignUpFragment returnFragment =
                                        (MemberSignUpFragment) getSupportFragmentManager()
                                                .findFragmentByTag(fromFragment);
                                getSupportFragmentManager().popBackStack();
                                getSupportFragmentManager().popBackStack();
                                galleryFragmentInfo = null;
                                returnFragment.updateImage(childPath);
                                break;
                            }
                            case "orgRegisterFragment": {
                                OrganizationSignUpFragment returnFragment =
                                        (OrganizationSignUpFragment) getSupportFragmentManager()
                                                .findFragmentByTag(fromFragment);
                                getSupportFragmentManager().popBackStack();
                                getSupportFragmentManager().popBackStack();
                                galleryFragmentInfo = null;
                                returnFragment.updateImage(childPath);
                                break;
                            }
                            case "edit_profile": {
                                EditProfileFragment returnFragment =
                                        (EditProfileFragment) getSupportFragmentManager()
                                                .findFragmentByTag(fromFragment);
                                getSupportFragmentManager().popBackStack();
                                getSupportFragmentManager().popBackStack();
                                galleryFragmentInfo = null;

                                returnFragment.updateImage(childPath);
                                break;
                            }
                        }
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        progressBar.setProgress((int) progress);
                    }
                });

    }

    @Override
    public void editUserProfileClicked(String firstName, String lastName, String profileImagePath) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, EditProfileFragment.newInstance(firstName, lastName, profileImagePath), "edit_profile")
                .addToBackStack("edit_profile")
                .commit();
    }

    @Override
    public void accountInfoClicked() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, AppInfoFragment.newInstance(), "edit_profile")
                .addToBackStack("edit_profile")
                .commit();
    }

    @Override
    public void signoutClicked() {
        mAuth.signOut();
        currentUser = null;
        navigationBar.setVisibility(View.GONE);
        populateScreen();
    }

    @Override
    public void loadTakeNewProfilePhoto() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, CameraControlFragment.newInstance("edit_profile"), "cameraFragment")
                .addToBackStack("take_photo")
                .commit();
    }

    @Override
    public void saveProfileChangesClicked() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void loadOrganizationProfile(Organization organization) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain,
                        OrgProfileUserView.newInstance(organization.getEmail()),
                        "orgProfileUserViewFragment")
                .addToBackStack("org_profile_member_view")
                .commit();
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
}