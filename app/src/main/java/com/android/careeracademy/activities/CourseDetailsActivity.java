package com.android.careeracademy.activities;

import android.Manifest;
import android.content.Intent;
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
import com.android.careeracademy.models.EnrollCourseDo;
import com.android.careeracademy.models.RatingDo;
import com.android.careeracademy.utils.PreferenceUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

public class CourseDetailsActivity extends BaseActivity {

    private FrameLayout llCDetails;
    private ImageView ivCourseImg, ivDocsFolder;
    private TextView tvDescription, tvTutorName,tvDuration, tvPrice, tvEnroll, tvSend;
    private EditText etFeedback;
    private LinearLayout llFeedback, llComment;
    private RatingBar rbRating, rbFeedback;
    private String userType = "";
    private CourseDo courseDo;
    @Override
    public void initialise() {
        llCDetails = (FrameLayout) inflater.inflate(R.layout.course_details_layout, null);
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
        ivMenu.setVisibility(View.GONE);
        llToolbar.setVisibility(View.VISIBLE);
        if(userType.equalsIgnoreCase(AppConstants.Admin)) {
            tvSignup.setVisibility(View.VISIBLE);
            tvSignup.setText("Logout");
            tvEnroll.setVisibility(View.GONE);
            llFeedback.setVisibility(View.GONE);
            llComment.setVisibility(View.GONE);
            tvSignup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                }
            });
        }
        else if(userType.equalsIgnoreCase(AppConstants.Guest)) {
            tvSignup.setVisibility(View.VISIBLE);
            tvEnroll.setVisibility(View.GONE);
            llFeedback.setVisibility(View.GONE);
            llComment.setVisibility(View.GONE);
            tvSignup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, RegistrationActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                }
            });
        }
        else if(userType.equalsIgnoreCase(AppConstants.Tutor)) {
            tvSignup.setVisibility(View.GONE);
            tvEnroll.setVisibility(View.GONE);
        }
        else if(userType.equalsIgnoreCase(AppConstants.User)) {
            tvSignup.setVisibility(View.GONE);
            tvEnroll.setVisibility(View.VISIBLE);
            ivMenu.setVisibility(View.VISIBLE);
            ivMenu.setImageResource(R.drawable.docs_folder_icon);
            ivMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(tvEnroll.getText().toString().equalsIgnoreCase("Enrolled")){
                        Intent intent = new Intent(CourseDetailsActivity.this, CourseDocListActivity.class);
                        intent.putExtra("courseDo", courseDo);
                        intent.putExtra("Enroll", tvEnroll.getText().toString());
                        startActivity(intent);
                        overridePendingTransition(R.anim.enter, R.anim.exit);
                    }
                    else {
                        showToast("Please enroll the course before downloading");
                    }
                }
            });
        }

        bindData(courseDo);
        getData();
//        getRatingForThisCourse();
        tvEnroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enrollCourse();
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
        ivDocsFolder.setVisibility(View.GONE);
        ivDocsFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CourseDetailsActivity.this, CourseDocListActivity.class);
                intent.putExtra("courseDo", courseDo);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
    }

    private void initializeControls(){
        ivCourseImg                     = llCDetails.findViewById(R.id.ivCourseImg);
        ivDocsFolder                    = llCDetails.findViewById(R.id.ivDocsFolder);
        tvDescription                   = llCDetails.findViewById(R.id.tvDescription);
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

            tvDescription.setText(courseDo.courseDescription);
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

    private void enrollCourse(){
        Intent intent = new Intent(CourseDetailsActivity.this, EnrollCourseActivity.class);
        intent.putExtra("CourseDo", courseDo);
        startActivityForResult(intent, 101);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101 && resultCode == 101){

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
                        llFeedback.setVisibility(View.VISIBLE);
                        llComment.setVisibility(View.VISIBLE);
                        break;
                    }
                    else {
                        tvEnroll.setEnabled(true);
                        tvEnroll.setBackgroundResource(R.drawable.btn_bg);
                        tvEnroll.setText("Enroll");
                        tvEnroll.setTextColor(getResources().getColor(R.color.white));
                        llFeedback.setVisibility(View.GONE);
                        llComment.setVisibility(View.GONE);
                    }
                }
                getRatingForThisCourse();
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
                        showAppCompatAlert("", "Thankyou for giving a valuable feedback.", "OK", "", "Added", false);
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
}
