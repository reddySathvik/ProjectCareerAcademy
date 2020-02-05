package com.android.careeracademy.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.careeracademy.R;
import com.android.careeracademy.common.AppConstants;
import com.android.careeracademy.models.CourseDo;
import com.android.careeracademy.models.LoginDo;
import com.android.careeracademy.utils.PreferenceUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

public class MyProfileActivity extends BaseActivity {

    private static final String TAG = "MyProfileActivity";
    private static final String Storage_Path = "Career Academy/Profiles/";
    private FrameLayout llProfile;
    private LinearLayout llChangePassword;
    private EditText etFirstName, etLastName, etEmail, etOldPassword, etNewPassword;
    private ImageView civProfile, ivEditFirstName, ivEditLastName, ivEditEmail;
    private TextView tvChangePassword, tvSave, tvSubmit;
    private LoginDo loginDo;

    @Override
    public void initialise() {
        llProfile = (FrameLayout) inflater.inflate(R.layout.profile_layout, null);
        addBodyView(llProfile);
        llToolbar.setVisibility(View.VISIBLE);
        ivBack.setVisibility(View.VISIBLE);
        ivMenu.setVisibility(View.GONE);
        tvTitle.setText("My Profile");
        initializeControls();
        etFirstName.setEnabled(false);
        etLastName.setEnabled(false);
        etEmail.setEnabled(false);
        ivEditEmail.setVisibility(View.GONE);
        llChangePassword.setVisibility(View.GONE);
        etEmail.setText(preferenceUtils.getStringFromPreference(PreferenceUtils.EmailId, ""));
        ivEditFirstName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etFirstName.setEnabled(true);
                etFirstName.requestFocus();
                etFirstName.setSelection(etFirstName.getText().toString().trim().length());
            }
        });
        ivEditLastName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etLastName.setEnabled(true);
                etLastName.requestFocus();
                etLastName.setSelection(etLastName.getText().toString().trim().length());
            }
        });
        tvChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(llChangePassword.getVisibility() == View.VISIBLE){
                    llChangePassword.setVisibility(View.GONE);
                }
                else {
                    llChangePassword.setVisibility(View.VISIBLE);
                }
            }
        });
        tvSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!etOldPassword.getText().toString().equalsIgnoreCase(preferenceUtils.getStringFromPreference(PreferenceUtils.Password, ""))){
                    showAppCompatAlert("", "Please enter correct old password", "OK", "", "", true);
                }
                else if(etNewPassword.getText().toString().trim().length()<6){
                    showAppCompatAlert("", "Please enter minimum 6 characters new password", "OK", "", "", true);
                }
                else if(etOldPassword.getText().toString().trim().equalsIgnoreCase(etNewPassword.getText().toString().trim())){
                    showAppCompatAlert("", "Old password and new password should not be same.", "OK", "", "", true);
                }
                else {
                    //update password
                    changePassword(preferenceUtils.getStringFromPreference(PreferenceUtils.EmailId, ""), etNewPassword.getText().toString().trim());
                }
            }
        });
        tvSave.setVisibility(View.GONE);
        tvSignup.setVisibility(View.VISIBLE);
        tvSignup.setText("Save");
        tvSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etFirstName.getText().toString().trim().equalsIgnoreCase("")){
                    showAppCompatAlert("", "Please enter your first name", "OK", "", "", true);
                }
                else {
                    // update first name/last name
                    final String email = preferenceUtils.getStringFromPreference(PreferenceUtils.EmailId, "");
                    showLoader();
                    if(imageUri != null){
                        final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                        final String courseImgPath = Storage_Path +email+"_"+ System.currentTimeMillis() + "." + "jpeg";
                        storageReference.child(courseImgPath).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                                uri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String profileImgPath = uri.toString();
                                        updateProfile(email, profileImgPath);
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                hideLoader();
                                Log.e("Image Upload", "Exception : "+e.getMessage());
                                showToast("Error while uploaidng : "+e.getMessage());
                            }
                        });
                    }
                    else {
                        updateProfile(email,"");
                    }
                }
            }
        });
        civProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                if(!hasPermissions(MyProfileActivity.this, PERMISSIONS)){
                    ActivityCompat.requestPermissions(MyProfileActivity.this, PERMISSIONS, 201);
                }
                else {
                    selectProfilePic();
                }
            }
        });
        getData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (requestCode == 201) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.e("Permissions", "Granted");
                selectProfilePic();
            }
            else {
                Log.e("Permission", "Denied");
                String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                if(!hasPermissions(MyProfileActivity.this, PERMISSIONS)){
                    ActivityCompat.requestPermissions(MyProfileActivity.this, PERMISSIONS, 201);
                }
            }
        }
    }

    private Uri imageUri;

    private void selectProfilePic(){
        String userId = preferenceUtils.getStringFromPreference(PreferenceUtils.EmailId, "");
        File file = new File(Environment.getExternalStorageDirectory(),userId+"_profile_" + System.currentTimeMillis() + ".png");
        imageUri = Uri.fromFile(file);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        Intent chooser = Intent.createChooser(galleryIntent, "Select an option");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { cameraIntent });
        startActivityForResult(chooser, 1210);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1210 && resultCode == RESULT_OK){
            if(data == null){// from camer
                civProfile.setImageURI(imageUri);
            }
            else {// from file storage
                imageUri = data.getData();
                civProfile.setImageURI(imageUri);
            }
        }
    }

    private void initializeControls(){
        etFirstName                             = llProfile.findViewById(R.id.etFirstName);
        etLastName                              = llProfile.findViewById(R.id.etLastName);
        etEmail                                 = llProfile.findViewById(R.id.etEmail);
        etOldPassword                           = llProfile.findViewById(R.id.etOldPassword);
        etNewPassword                           = llProfile.findViewById(R.id.etNewPassword);
        civProfile                              = llProfile.findViewById(R.id.civProfile);
        ivEditFirstName                         = llProfile.findViewById(R.id.ivEditFirstName);
        ivEditLastName                          = llProfile.findViewById(R.id.ivEditLastName);
        ivEditEmail                             = llProfile.findViewById(R.id.ivEditEmail);
        llChangePassword                        = llProfile.findViewById(R.id.llChangePassword);
        tvChangePassword                        = llProfile.findViewById(R.id.tvChangePassword);
        tvSave                                  = llProfile.findViewById(R.id.tvSave);
        tvSubmit                                = llProfile.findViewById(R.id.tvSubmit);
    }

    private void changePassword(final String email, final String newPassword){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference(AppConstants.Table_Users);
        showLoader();
        databaseReference.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        hideLoader();
                        databaseReference.orderByChild("email") .equalTo(email).removeEventListener(this);
                        insertIntoDB(email, newPassword, databaseReference);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        hideLoader();
                        Log.e(TAG, "Failed to reading email.", databaseError.toException());
                    }
                });
    }

    private void insertIntoDB(String email, String newPassword, DatabaseReference databaseReference){
        LoginDo loginDo1 = new LoginDo(loginDo.firstName, loginDo.lastName, email, newPassword, loginDo.profileImgPath, loginDo.userType);
        databaseReference.child(email.split("@")[0]).setValue(loginDo1).
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        hideLoader();
                        showAppCompatAlert("", "Your password has been changed successfully.", "OK", "", "ChangePassword", false);
                    }
                });
    }

    private void updateProfile(final String email, final String profileImgPath){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference(AppConstants.Table_Users);
        showLoader();
        databaseReference.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        hideLoader();
                        LoginDo loginDo1 = new LoginDo(etFirstName.getText().toString().trim(), etLastName.getText().toString().trim(), email, loginDo.password, profileImgPath, loginDo.userType);
                        databaseReference.child(email.split("@")[0]).setValue(loginDo1).
                                addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        hideLoader();
                                        showAppCompatAlert("", "Your profile has been changed successfully.", "OK", "", "UpdateProfile", false);
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        hideLoader();
                        Log.e(TAG, "Failed to fetch details.", databaseError.toException());
                    }
                });
    }

    @Override
    public void onButtonYesClick(String from) {
        super.onButtonYesClick(from);
        if(from.equalsIgnoreCase("ChangePassword")){
            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.enter, R.anim.exit);
        }
        else if(from.equalsIgnoreCase("UpdateProfile")){
            finish();
            overridePendingTransition(R.anim.enter, R.anim.exit);
        }
    }

    @Override
    public void getData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference(AppConstants.Table_Users);
        showLoader();
        String email = preferenceUtils.getStringFromPreference(PreferenceUtils.EmailId, "");
        databaseReference.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null && dataSnapshot.exists()) {
                            hideLoader();
                            for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                loginDo = postSnapshot.getValue(LoginDo.class);
                                Log.e("Get Data", loginDo.toString());
                                bindData(loginDo);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        hideLoader();
                        Log.e(TAG, "Failed to reading email.", databaseError.toException());
                    }
                });
    }

    private void bindData(LoginDo loginDo){
        etFirstName.setText(""+loginDo.firstName);
        etLastName.setText(""+loginDo.lastName);
        if(!loginDo.profileImgPath.equalsIgnoreCase("")){
            Picasso.get().load(loginDo.profileImgPath).placeholder(R.drawable.user_default_icon).error(R.drawable.user_default_icon).into(civProfile);
        }
    }
}
