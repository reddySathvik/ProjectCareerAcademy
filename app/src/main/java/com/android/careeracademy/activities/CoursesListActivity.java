package com.android.careeracademy.activities;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.careeracademy.R;
import com.android.careeracademy.common.AppConstants;
import com.android.careeracademy.models.CourseDo;
import com.android.careeracademy.models.FavouriteDo;
import com.android.careeracademy.utils.PreferenceUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CoursesListActivity extends BaseActivity {

    private View llCourses;
    private FrameLayout flSearch;
    private TextView tvNoCoursesFound;
    private EditText etSearchCourse;
    private ImageView ivAddCourse, ivSearchCourse;
    private RecyclerView rvCourseList;
    private CoursesListAdapter coursesListAdapter;
    private String userType = "";

    @Override
    public void initialise() {
        llCourses =  inflater.inflate(R.layout.courses_list_layout, null);
        addBodyView(llCourses);
        lockMenu();
        if(getIntent().hasExtra("From")){
            userType = getIntent().getExtras().getString("From");
        }
        tvTitle.setText(R.string.app_name);
        initializeControls();
        llToolbar.setVisibility(View.VISIBLE);
        ivSearchCourse.setVisibility(View.GONE);
        if(userType.equalsIgnoreCase(AppConstants.Admin)) {
            ivMenu.setVisibility(View.GONE);
            tvSignup.setVisibility(View.VISIBLE);
            tvSignup.setText("Logout");
            ivBack.setVisibility(View.GONE);
            ivAddCourse.setVisibility(View.VISIBLE);
            flSearch.setVisibility(View.VISIBLE);
        }
        else if(userType.equalsIgnoreCase(AppConstants.Guest)) {
            ivMenu.setVisibility(View.GONE);
            tvSignup.setVisibility(View.VISIBLE);
            ivBack.setVisibility(View.VISIBLE);
            ivAddCourse.setVisibility(View.GONE);
            flSearch.setVisibility(View.VISIBLE);
        }
        else if(userType.equalsIgnoreCase(AppConstants.Tutor)) {
            ivMenu.setVisibility(View.VISIBLE);
            tvSignup.setVisibility(View.GONE);
            ivBack.setVisibility(View.GONE);
            ivAddCourse.setVisibility(View.GONE);
            flSearch.setVisibility(View.VISIBLE);
        }
        else if(userType.equalsIgnoreCase(AppConstants.User)) {
            ivMenu.setVisibility(View.VISIBLE);
            tvSignup.setVisibility(View.GONE);
            ivBack.setVisibility(View.GONE);
            ivAddCourse.setVisibility(View.GONE);
            flSearch.setVisibility(View.VISIBLE);
        }
        coursesListAdapter = new CoursesListAdapter(CoursesListActivity.this, new ArrayList<CourseDo>(), new ArrayList<FavouriteDo>());
        rvCourseList.setAdapter(coursesListAdapter);
        ivAddCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CoursesListActivity.this, AddCourseActivity.class);
                intent.putExtra("From", userType);
                intent.putExtra("CoursesList", courseDos);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
        etSearchCourse.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchCourse(editable.toString());
            }
        });
        getData();
    }

    private void searchCourse(String courseName){
        if(!courseName.equalsIgnoreCase("")){
            ArrayList<CourseDo> filteredCourseDos = new ArrayList<>();
            if (courseDos != null && courseDos.size() > 0){
                for (int i=0;i<courseDos.size();i++){
                    if(courseDos.get(i).courseName.toLowerCase().contains(courseName.toLowerCase())){
                        filteredCourseDos.add(courseDos.get(i));
                    }
                }
            }
            if(filteredCourseDos.size() > 0){
                rvCourseList.setVisibility(View.VISIBLE);
                tvNoCoursesFound.setVisibility(View.GONE);
                rvCourseList.setAdapter(coursesListAdapter = new CoursesListAdapter(CoursesListActivity.this, filteredCourseDos, favouriteDos));
            }
            else {
                rvCourseList.setVisibility(View.GONE);
                tvNoCoursesFound.setVisibility(View.VISIBLE);
            }
        }
        else {
            rvCourseList.setVisibility(View.VISIBLE);
            tvNoCoursesFound.setVisibility(View.GONE);
            coursesListAdapter.refreshAdapter(courseDos, favouriteDos);
        }
    }

    private void initializeControls(){
        etSearchCourse              = llCourses.findViewById(R.id.etSearchCourse);
        flSearch                    = llCourses.findViewById(R.id.flSearch);
        ivAddCourse                 = llCourses.findViewById(R.id.ivAddCourse);
        ivSearchCourse              = llCourses.findViewById(R.id.ivSearchCourse);
        rvCourseList                = llCourses.findViewById(R.id.rvCourseList);
        tvNoCoursesFound            = llCourses.findViewById(R.id.tvNoCoursesFound);
        rvCourseList.setLayoutManager(new LinearLayoutManager(CoursesListActivity.this, LinearLayoutManager.VERTICAL, false));
    }

    private ArrayList<CourseDo> courseDos = new ArrayList<>();
    private ArrayList<FavouriteDo> favouriteDos = new ArrayList<>();

    @Override
    public void getData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference(AppConstants.Table_Course);
        showLoader();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                hideLoader();
                Log.e("Courses Count " ,""+snapshot.getChildrenCount());
                courseDos = new ArrayList<>();
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    CourseDo courseDo = postSnapshot.getValue(CourseDo.class);
                    courseDos.add(courseDo);
                    Log.e("Get Data", courseDo.toString());
                }
                if(courseDos!=null && courseDos.size() > 0){
                    loadFavouriteCourses(courseDos);
                    flSearch.setVisibility(View.VISIBLE);
                    rvCourseList.setVisibility(View.VISIBLE);
                    tvNoCoursesFound.setVisibility(View.GONE);
                    rvCourseList.setAdapter(coursesListAdapter = new CoursesListAdapter(CoursesListActivity.this, courseDos, favouriteDos));
                }
                else {
                    flSearch.setVisibility(View.GONE);
                    rvCourseList.setVisibility(View.GONE);
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

    private void loadFavouriteCourses(final ArrayList<CourseDo> courseDos) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference(AppConstants.Table_Favourite);
        showLoader();
        databaseReference.orderByChild("userId").equalTo(preferenceUtils.getStringFromPreference(PreferenceUtils.EmailId, ""))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        hideLoader();
                        favouriteDos = new ArrayList<>();
                        Log.e("Fav Courses Count " ,""+snapshot.getChildrenCount());
                        for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                            FavouriteDo favouriteDo = postSnapshot.getValue(FavouriteDo.class);
                            favouriteDos.add(favouriteDo);
                            Log.e("Get Data", favouriteDo.toString());
                        }
                        coursesListAdapter.refreshAdapter(courseDos, favouriteDos);
//                        else {
//                            flSearch.setVisibility(View.GONE);
//                            rvCourseList.setVisibility(View.GONE);
//                            tvNoCoursesFound.setVisibility(View.VISIBLE);
//                        }
//                        getData();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        hideLoader();
                        Log.e("The read failed: " ,databaseError.getMessage());
//                        getData();
                    }
                });
    }

    private boolean isFavouriteCourse(String courseId, ArrayList<FavouriteDo> favouriteDos){
        if(favouriteDos !=null && favouriteDos.size() > 0){
            for (int i=0; i<favouriteDos.size(); i++){
                if(favouriteDos.get(i).courseId.equalsIgnoreCase(courseId)){
                    return true;
                }
            }
        }
        return false;
    }

    private String getFavouriteCourseId(String courseId, ArrayList<FavouriteDo> favouriteDos){
        if(favouriteDos !=null && favouriteDos.size() > 0){
            for (int i=0; i<favouriteDos.size(); i++){
                if(favouriteDos.get(i).courseId.equalsIgnoreCase(courseId)){
                    return favouriteDos.get(i).favouriteId;
                }
            }
        }
        return "";
    }

    @Override
    public void onBackPressed() {
        if (userType.equalsIgnoreCase(AppConstants.Guest)){
            super.onBackPressed();
        }
        else {
            showAppCompatAlert("", "Do you want ot exit userType app?", "Exit", "Cancel", "Exit", true);
        }
    }

    @Override
    public void onButtonYesClick(String from) {
        super.onButtonYesClick(from);
        if (from.equalsIgnoreCase("Exit")){
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        else if (from.equalsIgnoreCase("DeleteCourse")){
//            getData();
        }
    }

    private class CoursesListAdapter extends RecyclerView.Adapter<CourseHolder>{

        private Context context;
        private ArrayList<CourseDo> courseDos;
        private ArrayList<FavouriteDo> favouriteDos;

        public CoursesListAdapter(Context context, ArrayList<CourseDo> courseDos, ArrayList<FavouriteDo> favouriteDos){
            this.context = context;
            this.courseDos = courseDos;
            this.favouriteDos = favouriteDos;
        }

        private void refreshAdapter(ArrayList<CourseDo> courseDos, ArrayList<FavouriteDo> favouriteDos){
            this.courseDos = courseDos;
            this.favouriteDos = favouriteDos;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CourseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View convertVIew = LayoutInflater.from(context).inflate(R.layout.course_list_item_cell, parent, false);
            return new CourseHolder(convertVIew);
        }

        @Override
        public void onBindViewHolder(@NonNull final CourseHolder holder, int position) {
            try {
                final CourseDo courseDo = courseDos.get(position);
                holder.tvCourseName.setText(courseDo.courseName);
                if(userType.equalsIgnoreCase(AppConstants.Guest)
                        || userType.equalsIgnoreCase(AppConstants.Tutor)){
                    holder.ivFav.setVisibility(View.GONE);
                }
                else {
                    holder.ivFav.setVisibility(View.VISIBLE);
                }
                if(userType.equalsIgnoreCase(AppConstants.Admin)){
                    holder.ivFav.setImageResource(R.drawable.delete_icon);
                    holder.ivFav.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            deleteCourse(courseDo.courseId);
                        }
                    });
                }
                else {
                    final boolean isFav = isFavouriteCourse(courseDo.courseId, favouriteDos);
                    if(isFav){
                        holder.ivFav.setImageResource(R.drawable.favourite_icon);
                    }
                    else {
                        holder.ivFav.setImageResource(R.drawable.unfavourite_icon);
                    }
                    holder.ivFav.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            favouriteCourse(courseDo, isFav, holder.ivFav, getFavouriteCourseId(courseDo.courseId, favouriteDos));
                        }
                    });
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(userType.equalsIgnoreCase(AppConstants.Tutor)){
                            Intent intent = new Intent(context, TutorCourseDetailsActivity.class);
                            intent.putExtra("From", userType);
                            intent.putExtra("CourseDo", courseDo);
                            startActivity(intent);
                            overridePendingTransition(R.anim.enter, R.anim.exit);
                        }
                        else {
                            Intent intent = new Intent(context, CourseDetailsActivity.class);
                            intent.putExtra("From", userType);
                            intent.putExtra("CourseDo", courseDo);
                            startActivity(intent);
                            overridePendingTransition(R.anim.enter, R.anim.exit);
                        }
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

        private void deleteCourse(String courseId){
            showLoader();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            Query applesQuery = ref.child(AppConstants.Table_Course).orderByChild("courseId").equalTo(courseId);
            applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    hideLoader();
                    for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                        appleSnapshot.getRef().removeValue();
                    }
                    showAppCompatAlert("", "Successfully deleted course.", "OK", "", "DeleteCourse", true);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    hideLoader();
                    Log.e("CourseList", "onCancelled", databaseError.toException());
                }
            });
        }

        private void favouriteCourse(CourseDo courseDo, boolean add, final ImageView ivFav, String favId){
            if(!add){
                showLoader();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference databaseReference = database.getReference(AppConstants.Table_Favourite);
                String favouriteId = ""+System.currentTimeMillis();
                FavouriteDo favouriteDo = new FavouriteDo(favouriteId, courseDo.courseId, courseDo.courseName,
                        preferenceUtils.getStringFromPreference(PreferenceUtils.EmailId, ""));
                databaseReference.child(favouriteId).setValue(favouriteDo).
                        addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                hideLoader();
                                ivFav.setImageResource(R.drawable.favourite_icon);
                                showAppCompatAlert("", "Added course into your favourite list.", "OK", "", "", true);
                            }
                        });
            }
            else {
                showLoader();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                Query applesQuery = ref.child(AppConstants.Table_Favourite).orderByChild("favouriteId").equalTo(favId);
                applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        hideLoader();
                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                            appleSnapshot.getRef().removeValue();
                        }
                        ivFav.setImageResource(R.drawable.unfavourite_icon);
                        showAppCompatAlert("", "Removed course userType your favourite list.", "OK", "", "", true);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        hideLoader();
                        Log.e("CourseList", "onCancelled", databaseError.toException());
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return courseDos!=null ? courseDos.size():0;
        }
    }

    private static class CourseHolder extends RecyclerView.ViewHolder{

        private ImageView ivCourse,ivFav;
        private TextView tvCourseName;
        public CourseHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseName         = itemView.findViewById(R.id.tvCourseName);
            ivCourse             = itemView.findViewById(R.id.ivCourse);
            ivFav                = itemView.findViewById(R.id.ivFav);
        }
    }
}
