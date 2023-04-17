package com.example.campushub;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity implements
        OrganizationSignUpFragment.IregisterFragmentAction,
        MemberSignUpFragment.ImemberRegisterFragmentAction,
        LandingFragment.IloginFragmentAction,
        EventsAdapter.IEventRowActions, CameraControlFragment.DisplayTakenPhoto,
        DisplayPhotoFragment.RetakePhoto, HomeFragment.IhomeButtonActions,
        EditProfileFragment.IeditProfileActions, OrgProfileOwnerView.IorgViewButtonActions {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private boolean is_Orgprofile = false;
    private String galleryFragmentInfo = null;

    private static final int PERMISSIONS_CODE = 0x100;
    private FirebaseStorage storage;

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
    }

    private void loadCorrectFragment(boolean isOrgUser) {
        if (isOrgUser) {
            // Load the org profile screen
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, OrgProfileOwnerView.newInstance(), "orgViewFragment")
                    .addToBackStack("landingFragment")
                    .commit();
        } else {
            // Load the home screen for member
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerMain, HomeFragment.newInstance(), "homeFragment")
                    .addToBackStack("landingFragment")
                    .commit();
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
                                loadCorrectFragment(false);
                            } else {
                                // User is an organization
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




   /* private void populateScreen() {
        //      Check for Authenticated users ....
        if(currentUser != null){
            if(is_Orgprofile) {
                // load the org profile screen
                // else load the home screen
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.containerMain, OrgProfileOwnerView.newInstance(),"orgViewFragment")
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
    } */


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
    public void orgLoadTakePhotoFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, CameraControlFragment.newInstance("orgRegisterFragment"), "cameraFragment")
                .addToBackStack("take_photo")
                .commit();
    }

    @Override
    public void memberRegisterDone(FirebaseUser mUser) {
        this.currentUser = mUser;
        is_Orgprofile = false;
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

    }

    @Override
    public void onTakePhoto(Uri imageUri, String fromFragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, DisplayPhotoFragment.newInstance(imageUri, fromFragment),"displayFragment")
                .addToBackStack("display_photo")
                .commit();
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
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.containerMain, returnToControl)
                                .commit();
                    }
                }

            }
    );

    private void openGallery(String fromFragment) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        galleryLauncher.launch(intent);
    }

    @Override
    public void onOpenGalleryPressed(String fromFragment) {
        galleryFragmentInfo = fromFragment;
        openGallery(fromFragment);
    }

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
//        Upload an image from local file....
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

                        if (fromFragment.equals("memRegisterFragment")) {
                            MemberSignUpFragment returnFragment =
                                    (MemberSignUpFragment) getSupportFragmentManager()
                                            .findFragmentByTag(fromFragment);
                            getSupportFragmentManager().popBackStack();
                            if (galleryFragmentInfo == null) {
                                getSupportFragmentManager().popBackStack();
                            }
                            galleryFragmentInfo = null;
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.containerMain, returnFragment)
                                    .commit();
                            returnFragment.updateImage(childPath);
                        }
                        else if (fromFragment.equals("orgRegisterFragment")) {
                            OrganizationSignUpFragment returnFragment =
                                    (OrganizationSignUpFragment) getSupportFragmentManager()
                                            .findFragmentByTag(fromFragment);
                            getSupportFragmentManager().popBackStack();
                            if (galleryFragmentInfo == null) {
                                getSupportFragmentManager().popBackStack();
                            }
                            galleryFragmentInfo = null;
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.containerMain, returnFragment)
                                    .commit();
                            returnFragment.updateImage(childPath);
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
    public void logoutPressed() {
        mAuth.signOut();
        currentUser = null;
        populateScreen();
    }

    @Override
    public void loadEditProfile(String fname, String lname, String profileImagePath) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, EditProfileFragment.newInstance(fname, lname, profileImagePath), "edit_profile")
                .addToBackStack("edit_profile")
                .commit();
    }

    @Override
    public void loadTakeNewProfilePhoto() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, CameraControlFragment.newInstance("edit_profile"), "cameraFragment")
                .addToBackStack("take_photo")
                .commit();
    }

    @Override
    public void loadHome() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerMain, HomeFragment.newInstance(),"homeFragment")
                .commit();
    }

    @Override
    public void orgLogoutPressed() {
        mAuth.signOut();
        currentUser = null;
        populateScreen();
    }
}