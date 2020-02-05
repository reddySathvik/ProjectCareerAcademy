package com.android.careeracademy.activities;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
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
import com.android.careeracademy.models.CourseDocDo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CourseDocListActivity extends BaseActivity {

    private LinearLayout llDocList;
    private RecyclerView rvDocumentsList;
    private TextView tvNoDocsFound;
    private CourseDocListAdapter courseDocListAdapter;
    private ArrayList<CourseDocDo> courseDocDos;
    private CourseDo courseDo;
    private String enroll = "";

    @Override
    public void initialise() {
        llDocList = (LinearLayout) inflater.inflate(R.layout.course_doc_list_layout, null);
        addBodyView(llDocList);
        llToolbar.setVisibility(View.VISIBLE);
        tvSignup.setVisibility(View.GONE);
        tvTitle.setVisibility(View.VISIBLE);
        ivBack.setVisibility(View.VISIBLE);
        ivMenu.setVisibility(View.GONE);
        lockMenu();
        tvTitle.setText("Documents");
        if (getIntent().hasExtra("courseDo")){
            courseDo = (CourseDo) getIntent().getSerializableExtra("courseDo");
        }
        if (getIntent().hasExtra("Enroll")){
            enroll = getIntent().getStringExtra("Enroll");
        }
        initializeControls();
        rvDocumentsList.setLayoutManager(new GridLayoutManager(CourseDocListActivity.this, 2, GridLayoutManager.VERTICAL, false));
        courseDocListAdapter = new CourseDocListAdapter(CourseDocListActivity.this, courseDocDos = new ArrayList<>());
        rvDocumentsList.setAdapter(courseDocListAdapter);
        getData();
    }

    private void initializeControls(){
        rvDocumentsList                 = llDocList.findViewById(R.id.rvDocumentsList);
        tvNoDocsFound                   = llDocList.findViewById(R.id.tvNoDocsFound);
    }

    @Override
    public void getData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference(AppConstants.Table_Docs);
        showLoader();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                hideLoader();
                Log.e("Docs Count " ,""+snapshot.getChildrenCount());
                courseDocDos = new ArrayList<>();
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    CourseDocDo courseDocDo = postSnapshot.getValue(CourseDocDo.class);
                    if(courseDocDo!=null && courseDocDo.courseId.equalsIgnoreCase(courseDo.courseId)){
                        courseDocDos.add(courseDocDo);
                    }
                    Log.e("Get Data", courseDocDo.toString());
                }
                if(courseDocDos!=null && courseDocDos.size() > 0){
                    rvDocumentsList.setVisibility(View.VISIBLE);
                    tvNoDocsFound.setVisibility(View.GONE);
                    rvDocumentsList.setAdapter(courseDocListAdapter = new CourseDocListAdapter(CourseDocListActivity.this, courseDocDos));
                }
                else {
                    rvDocumentsList.setVisibility(View.GONE);
                    tvNoDocsFound.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hideLoader();
                Log.e("The read failed: " ,databaseError.getMessage());
            }
        });

    }

    private class CourseDocListAdapter extends RecyclerView.Adapter<DocHolder>{

        private Context context;
        private ArrayList<CourseDocDo> courseDocDos;

        public CourseDocListAdapter(Context context, ArrayList<CourseDocDo> courseDocDos){
            this.context = context;
            this.courseDocDos = courseDocDos;
        }
        private void refreshAdapter(ArrayList<CourseDocDo> courseDocDos){
            this.courseDocDos = courseDocDos;
        }

        @NonNull
        @Override
        public DocHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View convertView = LayoutInflater.from(context).inflate(R.layout.course_doc_item_cell, parent, false);
            return new DocHolder(convertView);
        }

        @Override
        public void onBindViewHolder(@NonNull DocHolder holder, final int position) {
            holder.tvFileName.setText(courseDocDos.get(position).courseName+"_"+courseDocDos.get(position).docId);
            holder.ivDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(enroll.equalsIgnoreCase("Enrolled")){
                        downloadFirebaseFile(courseDocDos.get(position));
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return courseDocDos!=null?courseDocDos.size():0;
        }

        private void downloadFile(CourseDocDo courseDocDo){
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://career-academy-75a9d.appspot.com/Career Academy/Docs");
            StorageReference  islandRef = storageRef.child(courseDocDo.courseDocPath);

            File rootPath = new File(Environment.getExternalStorageDirectory(), "Career Academy");
            if(!rootPath.exists()) {
                rootPath.mkdirs();
            }

            final File localFile = new File(rootPath,courseDocDo.courseDocPath);

            islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.e("firebase ",";local tem file created  created " +localFile.toString());
                    Uri path = Uri.fromFile(localFile);
                    Intent openIntent = new Intent(Intent.ACTION_VIEW);
                    openIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    openIntent.setDataAndType(path, "application/pdf");
                    try {
                        startActivity(openIntent);
                    }
                    catch (Exception e) {

                    }

                    //  updateDb(timestamp,localFile.toString(),position);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("firebase ",";local tem file not created  created " +exception.toString());
                }
            });

        }

        private void downloadFirebaseFile(CourseDocDo courseDocDo){
            DownloadManager downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(courseDocDo.courseDocPath);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(courseDocDo.courseName);
            request.setDescription("Downloading...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            File rootPath = new File(Environment.getExternalStorageDirectory(), "Career Academy");
            if(!rootPath.exists()) {
                rootPath.mkdirs();
            }
            final File localFile = new File(rootPath, "Career.pdf");

            request.setDestinationUri(Uri.fromFile(localFile));
            downloadmanager.enqueue(request);

        }
    }

    private static class DocHolder extends RecyclerView.ViewHolder {

        private ImageView ivDocument, ivDownload;
        private TextView tvFileName;
        public DocHolder(@NonNull View itemView) {
            super(itemView);
            ivDocument              = itemView.findViewById(R.id.ivDocument);
            ivDownload              = itemView.findViewById(R.id.ivDownload);
            tvFileName              = itemView.findViewById(R.id.tvFileName);
        }
    }
}
