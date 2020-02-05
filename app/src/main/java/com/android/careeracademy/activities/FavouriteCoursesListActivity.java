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
import com.android.careeracademy.models.FavouriteDo;
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

public class FavouriteCoursesListActivity extends BaseActivity {

    private LinearLayout llFavourite;
    private TextView tvNoCoursesFound;
    private RecyclerView rvEnrollCoursesList;
    private CoursesListAdapter coursesListAdapter;

    @Override
    public void initialise() {
        llFavourite = (LinearLayout) inflater.inflate(R.layout.favourite_courses_list_layout, null);
        addBodyView(llFavourite);
        llToolbar.setVisibility(View.VISIBLE);
        tvSignup.setVisibility(View.GONE);
        tvTitle.setVisibility(View.VISIBLE);
        ivBack.setVisibility(View.VISIBLE);
        ivMenu.setVisibility(View.GONE);
        lockMenu();
        tvTitle.setText("Favourite Courses");
        initializeControls();
        coursesListAdapter = new CoursesListAdapter(FavouriteCoursesListActivity.this, new ArrayList<CourseDo>());
        rvEnrollCoursesList.setAdapter(coursesListAdapter);
        getData();
    }

    private void initializeControls(){
        tvNoCoursesFound                    = llFavourite.findViewById(R.id.tvNoCoursesFound);
        rvEnrollCoursesList                 = llFavourite .findViewById(R.id.rvEnrollCoursesList);
        rvEnrollCoursesList.setLayoutManager(new LinearLayoutManager(FavouriteCoursesListActivity.this, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void getData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference(AppConstants.Table_Favourite);
        showLoader();
        databaseReference.orderByChild("userId").equalTo(preferenceUtils.getStringFromPreference(PreferenceUtils.EmailId, ""))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        hideLoader();
                        Log.e("Fav Courses Count " ,""+snapshot.getChildrenCount());
                        ArrayList<FavouriteDo> favouriteDos = new ArrayList<>();
                        for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                            FavouriteDo favouriteDo = postSnapshot.getValue(FavouriteDo.class);
                            favouriteDos.add(favouriteDo);
                            Log.e("Get Data", favouriteDo.toString());
                        }
                        if(favouriteDos!=null && favouriteDos.size() > 0){
                            rvEnrollCoursesList.setVisibility(View.VISIBLE);
                            tvNoCoursesFound.setVisibility(View.GONE);
                            getCourseList(favouriteDos);
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

    private void getCourseList(final ArrayList<FavouriteDo> favouriteDos){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference(AppConstants.Table_Course);
        showLoader();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                hideLoader();
                Log.e("Courses Count " ,""+snapshot.getChildrenCount());
                ArrayList<CourseDo> courseDos = new ArrayList<>();
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    CourseDo courseDo = postSnapshot.getValue(CourseDo.class);
                    Log.e("Get Data", courseDo.toString());
                    for (int i=0; i<favouriteDos.size();i++){
                        if (favouriteDos.get(i).courseId.equalsIgnoreCase(courseDo.courseId)){
                            courseDos.add(courseDo);
                            break;
                        }
                    }
                }

                if(courseDos.size() > 0){
                    rvEnrollCoursesList.setVisibility(View.VISIBLE);
                    tvNoCoursesFound.setVisibility(View.GONE);
                    coursesListAdapter.refreshAdapter(courseDos);
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
                holder.ivFav.setImageResource(R.drawable.favourite_icon);
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
//                if(!courseDo.courseImgPath.equalsIgnoreCase("")){
//                    Picasso.get().load(courseDo.courseImgPath).fit().placeholder(R.drawable.course_placeholder).error(R.drawable.course_placeholder).into(holder.ivCourse);
//                }
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
