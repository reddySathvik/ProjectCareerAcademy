package com.android.careeracademy.activities;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.careeracademy.R;
import com.android.careeracademy.common.AppConstants;
import com.android.careeracademy.models.CourseDo;
import com.android.careeracademy.models.EnrollCourseDo;
import com.android.careeracademy.utils.PreferenceUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class EnrolledCoursesListActivity extends BaseActivity {

    private LinearLayout llEnroll;
    private TextView tvNoCoursesFound;
    private RecyclerView rvEnrollCoursesList;
    private CoursesListAdapter coursesListAdapter;

    @Override
    public void initialise() {
        llEnroll = (LinearLayout) inflater.inflate(R.layout.favourite_courses_list_layout, null);
        addBodyView(llEnroll);
        llToolbar.setVisibility(View.VISIBLE);
        tvSignup.setVisibility(View.GONE);
        tvTitle.setVisibility(View.VISIBLE);
        ivBack.setVisibility(View.VISIBLE);
        ivMenu.setVisibility(View.GONE);
        lockMenu();
        tvTitle.setText("Enrolled Courses");
        initializeControls();
        coursesListAdapter = new CoursesListAdapter(EnrolledCoursesListActivity.this, new ArrayList<CourseDo>());
        rvEnrollCoursesList.setAdapter(coursesListAdapter);
        getData();

    }

    private void initializeControls(){
        tvNoCoursesFound                    = llEnroll.findViewById(R.id.tvNoCoursesFound);
        rvEnrollCoursesList                 = llEnroll.findViewById(R.id.rvEnrollCoursesList);
        rvEnrollCoursesList.setLayoutManager(new LinearLayoutManager(EnrolledCoursesListActivity.this, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void getData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference(AppConstants.Table_Enroll);
        showLoader();
        databaseReference.orderByChild("userId").equalTo(preferenceUtils.getStringFromPreference(PreferenceUtils.EmailId, ""))
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                hideLoader();
                Log.e("Courses Count " ,""+snapshot.getChildrenCount());
                ArrayList<EnrollCourseDo> enrollCourseDos = new ArrayList<>();
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    EnrollCourseDo enrollCourseDo = postSnapshot.getValue(EnrollCourseDo.class);
                    enrollCourseDos.add(enrollCourseDo);
                    Log.e("Get Data", enrollCourseDo.toString());
                }
                if(enrollCourseDos!=null && enrollCourseDos.size() > 0){
                    rvEnrollCoursesList.setVisibility(View.VISIBLE);
                    tvNoCoursesFound.setVisibility(View.GONE);
                    getCourseList(enrollCourseDos);
                }
                else {
                    rvEnrollCoursesList.setVisibility(View.GONE);
                    tvNoCoursesFound.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hideLoader();
                Log.e("The read failed: " ,databaseError.getMessage());
            }
        });
    }

    private void getCourseList(final ArrayList<EnrollCourseDo> enrollDos){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference(AppConstants.Table_Course);
        showLoader();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                hideLoader();
                Log.e("Courses Count " ,""+snapshot.getChildrenCount());
                ArrayList<CourseDo> enrolledCourseDos = new ArrayList<>();
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    CourseDo courseDo = postSnapshot.getValue(CourseDo.class);
                    Log.e("Get Data", courseDo.toString());
                    for (int i=0; i<enrollDos.size();i++){
                        if (enrollDos.get(i).courseId.equalsIgnoreCase(courseDo.courseId)){
                            enrolledCourseDos.add(courseDo);
                            break;
                        }
                    }
                }

                if(enrolledCourseDos!=null && enrolledCourseDos.size() > 0){
                    rvEnrollCoursesList.setVisibility(View.VISIBLE);
                    tvNoCoursesFound.setVisibility(View.GONE);
                    coursesListAdapter.refreshAdapter(enrolledCourseDos);
                }
                else {
                    rvEnrollCoursesList.setVisibility(View.GONE);
                    tvNoCoursesFound.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hideLoader();
                Log.e("The read failed: " ,databaseError.getMessage());
            }
        });

    }

    private class CoursesListAdapter extends RecyclerView.Adapter<CourseHolder>{

        private Context context;
        private ArrayList<CourseDo> courseDos;
        public CoursesListAdapter(Context context, ArrayList<CourseDo> courseDos){
            this.context = context;
            this.courseDos = courseDos;
        }

        private void refreshAdapter(ArrayList<CourseDo> courseDos){
            this.courseDos = courseDos;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CourseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View convertVIew = LayoutInflater.from(context).inflate(R.layout.course_list_item_cell, parent, false);
            return new CourseHolder(convertVIew);
        }

        @Override
        public void onBindViewHolder(@NonNull CourseHolder holder, int position) {
            try {
                final CourseDo courseDo = courseDos.get(position);
                holder.tvCourseName.setText(courseDo.courseName);
                holder.ivFav.setVisibility(View.GONE);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, CourseDetailsActivity.class);
                        intent.putExtra("From", AppConstants.User);
                        intent.putExtra("CourseDo", courseDo);
                        startActivity(intent);
                        overridePendingTransition(R.anim.enter, R.anim.exit);
                    }
                });
                holder.ivCourse.setBackgroundResource(getCourseImage(courseDo.courseName));
//                Picasso.get().load(courseDo.courseImgPath).placeholder(R.drawable.course_placeholder).error(R.drawable.course_placeholder).into(holder.ivCourse);
            }
            catch (Exception e) {
                e.printStackTrace();

            }
        }

        @Override
        public int getItemCount() {
            return courseDos!=null ? courseDos.size():0;
        }
    }

    private static class CourseHolder extends RecyclerView.ViewHolder{

        private ImageView ivCourse, ivFav;
        private TextView tvCourseName;
        public CourseHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseName         = itemView.findViewById(R.id.tvCourseName);
            ivCourse             = itemView.findViewById(R.id.ivCourse);
            ivFav                = itemView.findViewById(R.id.ivFav);
        }
    }

}
