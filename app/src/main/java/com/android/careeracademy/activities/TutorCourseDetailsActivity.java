package com.android.careeracademy.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.careeracademy.R;
import com.android.careeracademy.common.AppConstants;
import com.android.careeracademy.models.CourseDo;
import com.android.careeracademy.models.CourseDocDo;
import com.android.careeracademy.models.EnrollCourseDo;
import com.android.careeracademy.models.RatingDo;
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

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

public class TutorCourseDetailsActivity extends BaseActivity {

    private FrameLayout llCDetails;
    private ImageView ivDocsFolder, ivCourseImg;
    private TextView tvTutorName,tvDuration, tvPrice, tvEnroll, tvSend;
    private EditText etDescription, etFeedback;
    private LinearLayout llFeedback, llComment;
    private RatingBar rbRating, rbFeedback;
    private String userType = "";
    private CourseDo courseDo;
    private static final String Storage_Path = "Career Academy/Docs/";

    @Override
    public void initialise() {
        llCDetails = (FrameLayout) inflater.inflate(R.layout.tutor_course_details_layout, null);
        addBodyView(llCDetails);
        lockMenu();
        if(getIntent().hasExtra("From")){
            userType = getIntent().getExtras().getString("From");
        }
        if(getIntent().hasExtra("CourseDo")){
            courseDo = (CourseDo) getIntent().getExtras().getSerializable("CourseDo");
        }
        tvTitle.setText("Course Details");
        initializeControls();
        ivBack.setVisibility(View.VISIBLE);
        llToolbar.setVisibility(View.VISIBLE);
        tvSignup.setVisibility(View.GONE);
        tvEnroll.setVisibility(View.VISIBLE);
        tvEnroll.setText("Update");
        llFeedback.setVisibility(View.GONE);
        llComment.setVisibility(View.GONE);
        ivMenu.setVisibility(View.VISIBLE);
        ivMenu.setImageResource(R.drawable.upload_icon);
        ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                if(!hasPermissions(TutorCourseDetailsActivity.this, PERMISSIONS)){
                    ActivityCompat.requestPermissions(TutorCourseDetailsActivity.this, PERMISSIONS, 201);
                }
                else {
                    String[] mimeTypes = {"image/*", "application/pdf", "text/plain"};
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                    startActivityForResult(intent, 1021);
                }
            }
        });
        bindData(courseDo);
        getData();
        getRatingForThisCourse();
        tvEnroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCourse();
            }
        });
        rbRating.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        rbRating.setFocusable(false);
        tvSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //send feedback
                float rating = rbFeedback.getRating();
                String comment = etFeedback.getText().toString().trim();
                if(rating < 0){
                    showAppCompatAlert("", "Please give rating", "OK", "", "", true);
                }
                else {
                    sendFeedback(rating, comment);
                }

            }
        });
        ivDocsFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TutorCourseDetailsActivity.this, CourseDocListActivity.class);
                intent.putExtra("courseDo", courseDo);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
    }

    private void initializeControls(){
        ivCourseImg                     = llCDetails.findViewById(R.id.ivCourseImg);
        ivDocsFolder                    = llCDetails.findViewById(R.id.ivDocsFolder);
        etDescription                   = llCDetails.findViewById(R.id.etDescription);
        tvTutorName                     = llCDetails.findViewById(R.id.tvTutorName);
        tvDuration                      = llCDetails.findViewById(R.id.tvDuration);
        tvPrice                         = llCDetails.findViewById(R.id.tvPrice);
        tvEnroll                        = llCDetails.findViewById(R.id.tvEnroll);
        tvSend                          = llCDetails.findViewById(R.id.tvSend);
        rbRating                        = llCDetails.findViewById(R.id.rbRating);
        rbFeedback                      = llCDetails.findViewById(R.id.rbFeedback);
        etFeedback                      = llCDetails.findViewById(R.id.etFeedback);
        llFeedback                      = llCDetails.findViewById(R.id.llFeedback);
        llComment                       = llCDetails.findViewById(R.id.llComment);
    }

    private void bindData(CourseDo courseDo){
        try {

            etDescription.setText(courseDo.courseDescription);
            tvTutorName.setText(courseDo.tutorName);
            tvDuration.setText(""+courseDo.courseDuration+" Hours");
            tvPrice.setText(""+courseDo.coursePrice+"$");
            rbRating.setRating(courseDo.courseRating);
            ivCourseImg.setBackgroundResource(getCourseImage(courseDo.courseName));
//            Picasso.get().load(courseDo.courseImgPath).fit().placeholder(R.drawable.course_placeholder).error(R.drawable.course_placeholder).into(ivCourseImg);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void updateCourse(){
        showLoader();
        courseDo.courseDescription = etDescription.getText().toString().trim();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference(AppConstants.Table_Course);
        databaseReference.child(courseDo.courseId).setValue(courseDo).
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        hideLoader();
                        showAppCompatAlert("", "Course updated successfully.", "OK", "", "Update", false);
                    }
                });

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
                    intent.setType("*/*");
                    startActivityForResult(intent, 1021);
                }
                else {
                    Log.e("Permission", "Denied");
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            final Uri imageUri = data.getData();
            showLoader();
            final StorageReference storageReference   = FirebaseStorage.getInstance().getReference();
            final String courseImgPath = Storage_Path +courseDo.courseName+"_"+ System.currentTimeMillis();
            storageReference.child(courseImgPath).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                    uri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String fileExt = "";
                            try {
                                fileExt = imageUri.toString().substring(imageUri.toString().lastIndexOf(".") + 1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            String courseDocPath = uri.toString();
                            String tutorId = preferenceUtils.getStringFromPreference(PreferenceUtils.EmailId, "");
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            CourseDocDo courseDocDo = new CourseDocDo(""+System.currentTimeMillis(),courseDo.courseId, courseDo.courseName, courseDocPath, fileExt, tutorId);
                            final DatabaseReference databaseReference = database.getReference(AppConstants.Table_Docs);
                            databaseReference.child(courseDocDo.docId).setValue(courseDocDo).
                                    addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            hideLoader();
                                            showAppCompatAlert("", "Document uploaded successfully!", "OK", "", "", true);
                                        }
                                    });
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
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference(AppConstants.Table_Enroll);
        showLoader();
        databaseReference.orderByChild("userId").equalTo(preferenceUtils.getStringFromPreference(PreferenceUtils.EmailId, "")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                hideLoader();
                Log.e("Courses Count " ,""+snapshot.getChildrenCount());
                ArrayList<EnrollCourseDo> enrollCourseDos = new ArrayList<>();
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    EnrollCourseDo enrollCourseDo = postSnapshot.getValue(EnrollCourseDo.class);
                    enrollCourseDos.add(enrollCourseDo);
                    Log.e("Get Data", enrollCourseDo.toString());
                    if(enrollCourseDo.courseId.equalsIgnoreCase(courseDo.courseId)){// already enrolled
                        tvEnroll.setEnabled(false);
                        tvEnroll.setBackgroundResource(R.drawable.btn_grey_bg);
                        tvEnroll.setText("Enrolled");
                        tvEnroll.setTextColor(getResources().getColor(R.color.black));
                        break;
                    }
                    else {
                        tvEnroll.setEnabled(true);
                        tvEnroll.setBackgroundResource(R.drawable.btn_bg);
                        tvEnroll.setText("Enroll");
                        tvEnroll.setTextColor(getResources().getColor(R.color.white));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hideLoader();
                Log.e("The read failed: " ,databaseError.getMessage());
            }
        });
    }

    private void sendFeedback(float rating, String comment){
        showLoader();
        String userId = preferenceUtils.getStringFromPreference(PreferenceUtils.EmailId, "");
        RatingDo ratingDo = new RatingDo(""+System.currentTimeMillis(), courseDo.courseId, courseDo.courseName, userId, comment, rating);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference(AppConstants.Table_Rating);
        databaseReference.child(ratingDo.ratingId).setValue(ratingDo).
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        hideLoader();
                        llComment.setVisibility(View.GONE);
                        llFeedback.setVisibility(View.GONE);
                        showAppCompatAlert("", "Thankyou for giving a valuable feedback.", "OK", "", "", false);
                    }
                });
    }

    private void getRatingForThisCourse(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference(AppConstants.Table_Rating);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                hideLoader();
                Log.e("Rating Count " ,""+snapshot.getChildrenCount());
                ArrayList<RatingDo> ratingDos = new ArrayList<>();
                float rating = 0, noOfRating = 0;
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    RatingDo ratingDo = postSnapshot.getValue(RatingDo.class);
                    ratingDos.add(ratingDo);
                    if(courseDo.courseId.equalsIgnoreCase(ratingDo.courseId)){
                        rating = rating + ratingDo.courseRating;
                        noOfRating = noOfRating + 1;
                        if(preferenceUtils.getStringFromPreference(PreferenceUtils.EmailId, "").equalsIgnoreCase(ratingDo.userId)){
                            llFeedback.setVisibility(View.GONE);
                            llComment.setVisibility(View.GONE);
                        }
                    }
                    Log.e("Get Data", ratingDo.toString());
                }
                rbRating.setRating(rating / noOfRating);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hideLoader();
                Log.e("The read failed: " ,databaseError.getMessage());
            }
        });
    }

    @Override
    public void onButtonYesClick(String from) {
        super.onButtonYesClick(from);
        if (from.equalsIgnoreCase("Update")){
            finish();
        }
    }
}
