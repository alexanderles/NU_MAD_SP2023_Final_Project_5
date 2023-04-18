package com.example.campushub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class SearchFragment extends Fragment {

    private static final String ARG_ORGS = "organizations";

    private ArrayList<Organization> mOrgs;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private RecyclerView recyclerView;
    private OrganizationsAdapter organizationsAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    private EditText searchInput;
    private ImageView searchIcon;
    private String searchExp = "";

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ORGS, new ArrayList<Organization>());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(ARG_ORGS)) {
                mOrgs = (ArrayList<Organization>) args.getSerializable(ARG_ORGS);
            }
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        loadData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = rootView.findViewById(R.id.organization_recyclerview);
        recyclerViewLayoutManager = new LinearLayoutManager(getContext());
        organizationsAdapter = new OrganizationsAdapter(mOrgs, getContext());
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setAdapter(organizationsAdapter);

        searchInput = rootView.findViewById(R.id.edit_search_input);
        searchIcon = rootView.findViewById(R.id.search_icon);

        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchExp = searchInput.getText().toString();
                loadData();
            }
        });


        db.collection("Org_Users")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        ArrayList<Organization> newOrgs = new ArrayList<>();
                        db.collection("Org_Users")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot snap : task.getResult()) {
                                                String orgName = snap.get("Org_Name").toString();
                                                if (orgName.matches("(?i).*" + searchExp + ".*")) {
                                                    Object potentialOrgImage = snap.get("profileImage");
                                                    String orgProfileImage = (potentialOrgImage == null) ?
                                                            null : potentialOrgImage.toString();
                                                    Organization newOrg = new Organization(
                                                            snap.getId(),
                                                            orgName,
                                                            snap.get("email").toString(),
                                                            orgProfileImage
                                                    );
                                                    newOrgs.add(newOrg);
                                                }
                                            }
                                            organizationsAdapter.setOrganizations(newOrgs);
                                            organizationsAdapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                    }
                });

        return rootView;
    }

    private void loadData() {
        ArrayList<Organization> orgs = new ArrayList<>();
        db.collection("Org_Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot snap : task.getResult()) {
                                String orgName = snap.get("Org_Name").toString();
                                if (orgName.matches("(?i).*" + searchExp + ".*")) {
                                    Object potentialOrgImage = snap.get("profileImage");
                                    String orgProfileImage = (potentialOrgImage == null) ?
                                            null : potentialOrgImage.toString();
                                    Organization newOrg = new Organization(
                                            snap.getId(),
                                            orgName,
                                            snap.get("email").toString(),
                                            orgProfileImage
                                    );
                                    orgs.add(newOrg);
                                }
                            }
                            updateRecyclerView(orgs);
                        }
                    }
                });
    }

    private void updateRecyclerView(ArrayList<Organization> orgs) {
        organizationsAdapter.setOrganizations(orgs);
        organizationsAdapter.notifyDataSetChanged();
    }
}