package com.example.campushub;

import java.io.Serializable;

public class Organization implements Serializable {
    private String organizationId;
    private String Org_Name;
    private String email;
    private String profileImage;

    public Organization() {
    }

    public Organization(String organizationId, String org_Name, String email, String profileImage) {
        this.organizationId = organizationId;
        this.Org_Name = org_Name;
        this.email = email;
        this.profileImage = profileImage;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrg_Name() {
        return Org_Name;
    }

    public void setOrg_Name(String org_Name) {
        Org_Name = org_Name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileiImage(String profileImage) {
        this.profileImage = profileImage;
    }

    @Override
    public String toString() {
        return "Organization{" +
                "organizationId='" + organizationId + '\'' +
                ", Org_Name='" + Org_Name + '\'' +
                ", email='" + email + '\'' +
                ", profileImage='" + profileImage + '\'' +
                '}';
    }
}
