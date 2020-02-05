package com.android.careeracademy.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.careeracademy.R;
import com.android.careeracademy.common.AppConstants;
import com.android.careeracademy.models.CourseDo;
import com.android.careeracademy.utils.PreferenceUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

public class AddCourseActivity extends BaseActivity {

    private static final String TAG = "AddCourseActivity";
    private LinearLayout llAddCourse;
    private Spinner spCourseName;
    private EditText etDuration, etCourseFee, etDescription;
    private TextView tvAddCourse, tvAttachment;
    private ImageView ivAttachment;
    private Uri imageUri;
    private String userType = "", selectedCourseName = "";
    private ArrayList<CourseDo> courseDos = new ArrayList<>();
    private static final String Storage_Path = "Career Academy/Logos/";

    @Override
    public void initialise() {
        llAddCourse = (LinearLayout) inflater.inflate(R.layout.add_course_layout, null);
        addBodyView(llAddCourse);
        llToolbar.setVisibility(View.VISIBLE);
        ivBack.setVisibility(View.VISIBLE);
        ivMenu.setVisibility(View.GONE);
        tvSignup.setVisibility(View.GONE);
        tvTitle.setText("Add Course");
        lockMenu();
        if(getIntent().hasExtra("From")){
            userType = getIntent().getExtras().getString("From");
        }
        if(getIntent().hasExtra("CoursesList")){
            courseDos = (ArrayList<CourseDo>) getIntent().getExtras().getSerializable("CoursesList");
        }

        initializeControls();
        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(AddCourseActivity.this, android.R.layout.simple_spinner_item, AppConstants.courseNamesList);
        stringArrayAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spCourseName.setAdapter(stringArrayAdapter);
        spCourseName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i != 0){
                    selectedCourseName = AppConstants.courseNamesList[i];
                    if(isCourseExist(selectedCourseName)){
                        spCourseName.setSelection(0);
                        showAppCompatAlert("", "This course is already exist, please check in the list.", "OK", "", "", true);
                    }
//                    imageUri = Uri.fromFile(new File("//android_asset/"+AppConstants.courseNamesList[i].toLowerCase()+".png"));
                }
                else {
                    selectedCourseName = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        tvAddCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCourse();
            }
        });

        ivAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                if(!hasPermissions(AddCourseActivity.this, PERMISSIONS)){
                    ActivityCompat.requestPermissions(AddCourseActivity.this, PERMISSIONS, 201);
                }
                else {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, 1021);
                }
            }
        });
    }

    private boolean isCourseExist(String courseName){
        if(courseDos !=null && courseDos.size() > 0){
            for (int i=0; i<courseDos.size();i++){
                if(courseName.equalsIgnoreCase(courseDos.get(i).courseName)){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch (requestCode) {
            case 201: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("Permission", "Granted");
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, 1021);
                }
                else {
                    Log.e("Permission", "Denied");
                }
                return;
            }
        }
    }

    private void initializeControls(){
        spCourseName                 = llAddCourse.findViewById(R.id.spCourseName);
        etDuration                   = llAddCourse.findViewById(R.id.etDuration);
        etCourseFee                  = llAddCourse.findViewById(R.id.etCourseFee);
        etDescription                = llAddCourse.findViewById(R.id.etDescription);
        tvAttachment                 = llAddCourse.findViewById(R.id.tvAttachment);
        ivAttachment                 = llAddCourse.findViewById(R.id.ivAttachment);
        tvAddCourse                  = llAddCourse.findViewById(R.id.tvAddCourse);
    }

    private void addCourse(){
        String duration = etDuration.getText().toString();
        String fee = etCourseFee.getText().toString();
        String description = etDescription.getText().toString();
        String userId = preferenceUtils.getStringFromPreference(PreferenceUtils.EmailId, "");
        String attachment = tvAttachment.getText().toString();
        if (selectedCourseName.equalsIgnoreCase("")){
            showAppCompatAlert("", "Please enter course name", "OK", "", "", true);
        }
        else if (duration.equalsIgnoreCase("")){
            showAppCompatAlert("", "Please enter course duration", "OK", "", "", true);
        }
        else if (Integer.parseInt(duration) < 1){
            showAppCompatAlert("", "Please enter valid course duration", "OK", "", "", true);
        }
        else if (fee.equalsIgnoreCase("")){
            showAppCompatAlert("", "Please enter course fee", "OK", "", "", true);
        }
        else if (Integer.parseInt(fee) < 1){
            showAppCompatAlert("", "Please enter valid course fee", "OK", "", "", true);
        }
        else if (description.equalsIgnoreCase("")){
            showAppCompatAlert("", "Please enter course description", "OK", "", "", true);
        }
        else {
            CourseDo courseDo = new CourseDo(""+System.currentTimeMillis(), selectedCourseName, "", description, 0.0f, userId,
                    Double.parseDouble(fee), Integer.parseInt(duration));
            showLoader();
            if(imageUri!=null){
                uploadCourseLogo(imageUri, courseDo);
            }
            else {
                uploadCourse(courseDo);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageUri = data.getData();
        try {
//            File file = from(context, imageUri);
            tvAttachment.setText(""+imageUri.getPath());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadCourseLogo(Uri imgUri, final CourseDo courseDo){
        final StorageReference storageReference            = FirebaseStorage.getInstance().getReference();
        final String courseImgPath = Storage_Path +courseDo.courseName+"_"+ System.currentTimeMillis() + "." + "jpeg";
        storageReference.child(courseImgPath).putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                uri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        courseDo.courseImgPath = uri.toString();
                        uploadCourse(courseDo);
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

//
//        StorageReference riversRef = null;
//        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
//        //to create a separate folder with all the pictures uploaded
//        riversRef = mStorageRef.child("pictures/" + "unique_value");
//        UploadTask uploadTask = riversRef.putFile(imgUri);
//        uploadTask.addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                hideLoader();
//                // Handle unsuccessful uploads
//            }
//        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
//                downloadUrl = taskSnapshot.getDownloadUrl();
//                Log.d("downloadUrl", "" + downloadUrl);
//
//            }
//        });

    }

    private void uploadCourse(CourseDo courseDo){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference(AppConstants.Table_Course);
        databaseReference.child(courseDo.courseId).setValue(courseDo).
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        hideLoader();
                        showAppCompatAlert("", "Congratulations! You have successfully added a course.", "OK", "", "Added", false);
                    }
                });
    }


//    @Override
//    public void onBackPressed() {
//        showAppCompatAlert("", "Do you want ot exit from app?", "Exit", "Cancel", "Exit", true);
//    }

    @Override
    public void onButtonYesClick(String from) {
        super.onButtonYesClick(from);
        if (from.equalsIgnoreCase("Added")){
            Intent intent = new Intent(AddCourseActivity.this, CoursesListActivity.class);
            intent.putExtra("From", userType);
            startActivity(intent);
        }
        else if (from.equalsIgnoreCase("Exit")){
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void getData() {

    }
}
