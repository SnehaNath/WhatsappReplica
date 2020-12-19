package com.infinitebitcode.whatsappreplica.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.infinitebitcode.whatsappreplica.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private AppCompatButton UpdateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;

    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    //requestCode for sending user to his/her gallery
    private static final int GalleryPick = 1;

    //creating another storageReference to create a folder inside the FBStorage
    private StorageReference UserProfileImagesRef;                  //store all the users profile image


    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFields();

        userName.setVisibility(View.INVISIBLE);

        RetrieveUserInfo();

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Select image from gallery
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");                           //type of content to be selected
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });
    }

    private void InitializeFields() {
        UpdateAccountSettings = (AppCompatButton) findViewById(R.id.update_setting_button);
        userName = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_profile_status);
        userProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {               //get the result of the selected image
        super.onActivityResult(requestCode, resultCode, data);

        //allow cropping of image
        if(requestCode == GalleryPick && resultCode == RESULT_OK /* = -1*/ && data != null) {
            Uri ImageUri = data.getData();

            userProfileImage.setImageURI(ImageUri);              //add this line

            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()                                                                    //open the crop image activity where user crops
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)                                //1:1, full screen etc
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            //to get the result/crop image
            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait, your profile image is updating");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();                    //contain the crop image

                //storing the image in FB
                final StorageReference filePath = UserProfileImagesRef.child(currentUserId + ".jpg");         //random key can be used for storing the profile image
                //but in whatsapp new profile image replaces the old one



                Task<Uri> uriTask = filePath.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        //Continue with the task to get the download URL
                        return filePath.getDownloadUrl();
                    }
                })
                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
//                                    Uri downloadUri = task.getResult();
//                                    Getting image upload ID
                                    final String downloadURL = task.getResult().toString();
                                    String ImageUploadId = RootRef.push().getKey();

                                    //Adding image upload id's child element into databaseReference
                                    RootRef.child("Users").child(currentUserId).child("image")
                                            .setValue(downloadURL);
                                    Toast.makeText(SettingsActivity.this, "Data Successfully Uploaded", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                } else {
                                    //Handle error
                                    loadingBar.dismiss();
                                    Toast.makeText(SettingsActivity.this, "Error While Uploading", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                /*filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {       //actual storage
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "Profile image uploaded successfully", Toast.LENGTH_SHORT).show();

                            //to get link of the profile image
//                            final String downloadUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString();
                            final String downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();

                            //store this link in FB database
                            RootRef.child("Users").child(currentUserId).child("image")
                                    .setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                Toast.makeText(SettingsActivity.this, "Image saved in database successfully", Toast.LENGTH_SHORT).show();

                                            } else {
                                                String message = task.getException().toString();
                                                Toast.makeText(SettingsActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();

                                            }
                                            loadingBar.dismiss();
                                        }
                                    });
                        } else {
                            String message = task.getException().toString();            //if image is not stored in FB storage
                            Toast.makeText(SettingsActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();

                            loadingBar.dismiss();
                        }
                    }
                });*/

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void UpdateSettings() {
        String setUserName = userName.getText().toString();
        String setUserStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName) || TextUtils.isEmpty(setUserStatus)) {
            if(TextUtils.isEmpty(setUserName)) {
                Toast.makeText(this, "Username is must", Toast.LENGTH_SHORT).show();
            }

            if(TextUtils.isEmpty(setUserStatus)) {
                Toast.makeText(this, "You can update your status", Toast.LENGTH_SHORT).show();
            }
        } else {
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserId);                       //TODO : why it's needed as userId already stored in FB, key-value for others uid2...?
            profileMap.put("name", setUserName);
            profileMap.put("status", setUserStatus);

            RootRef.child("Users"/*Parent child*/).child(currentUserId).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                SendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.hasChild("name") && snapshot.hasChild("image")) {
                    String retrieveUserName = snapshot.child("name").getValue().toString();
                    String retrieveStatus = snapshot.child("status").getValue().toString();
                    String retrieveProfileImage = snapshot.child("image").getValue().toString();

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveStatus);
                    Picasso.get().load(retrieveProfileImage).into(userProfileImage);

                } else if (snapshot.exists() && snapshot.hasChild("name")){
                    String retrieveUserName = snapshot.child("name").getValue().toString();
                    String retrieveStatus = snapshot.child("status").getValue().toString();

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveStatus);
                } else {
                    userName.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this, "Please set and update your profile information", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}