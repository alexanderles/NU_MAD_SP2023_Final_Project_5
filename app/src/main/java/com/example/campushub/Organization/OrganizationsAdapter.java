package com.example.campushub.Organization;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.campushub.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * Adapter for displaying an organization to a user
 */
public class OrganizationsAdapter extends RecyclerView.Adapter<OrganizationsAdapter.ViewHolder> {
    private ArrayList<Organization> organizations;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser mUser = mAuth.getCurrentUser();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private IOrganizationRowActions mListener;

    public OrganizationsAdapter() {
    }

    public OrganizationsAdapter(ArrayList<Organization> organizations, Context context) {
        this.organizations = organizations;
        if(context instanceof IOrganizationRowActions){
            this.mListener = (IOrganizationRowActions) context;
        }else{
            throw new RuntimeException(context.toString()+ "must implement IOrganizationRowActions");
        }
    }

    public ArrayList<Organization> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(ArrayList<Organization> organizations) {
        this.organizations = organizations;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView organizationName;
        private final TextView organizationEmail;
        private final ImageView organizerImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.organizationName = itemView.findViewById(R.id.org_row_name);
            this.organizationEmail = itemView.findViewById(R.id.org_row_email);
            this.organizerImage = itemView.findViewById(R.id.org_row_image);
        }

        public TextView getOrganizationName() {
            return organizationName;
        }

        public TextView getOrganizationEmail() {
            return organizationEmail;
        }

        public ImageView getOrganizerImage() {
            return organizerImage;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemRecyclerView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.organization_row_item,parent, false);

        return new ViewHolder(itemRecyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Organization currentOrg = this.getOrganizations().get(position);
        holder.getOrganizationName().setText(currentOrg.getOrg_Name());
        holder.getOrganizationEmail().setText(currentOrg.getEmail());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.loadOrganizationProfile(currentOrg);
            }
        });

        String imagePath = currentOrg.getProfileImage();
        if (imagePath != null) {
            StorageReference imageToLoad = storage.getReference().child(imagePath);
            imageToLoad.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Glide.with(holder.itemView)
                                .load(task.getResult())
                                .centerCrop()
                                .into(holder.getOrganizerImage());
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return this.getOrganizations().size();
    }

    public interface IOrganizationRowActions {
        void loadOrganizationProfile(Organization organization);
    }
}
