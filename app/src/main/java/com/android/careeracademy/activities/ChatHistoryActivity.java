package com.android.careeracademy.activities;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.careeracademy.R;
import com.android.careeracademy.common.AppConstants;
import com.android.careeracademy.models.LoginDo;
import com.android.careeracademy.utils.PreferenceUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ChatHistoryActivity extends BaseActivity {

    private LinearLayout llChatList;
    private TextView tvNoChatFound;
    private RecyclerView rvChatList;
    private ChatHistoryListAdapter chatHistoryListAdapter;

    @Override
    public void initialise() {
        llChatList = (LinearLayout) inflater.inflate(R.layout.chat_history_list_layout, null);
        addBodyView(llChatList);
        llToolbar.setVisibility(View.VISIBLE);
        tvSignup.setVisibility(View.GONE);
        tvTitle.setVisibility(View.VISIBLE);
        ivBack.setVisibility(View.VISIBLE);
        ivMenu.setVisibility(View.GONE);
        lockMenu();
        tvTitle.setText("Chat");
        initialiseControls();
        rvChatList.setLayoutManager(new LinearLayoutManager(ChatHistoryActivity.this, LinearLayoutManager.VERTICAL, false));
        chatHistoryListAdapter = new ChatHistoryListAdapter(ChatHistoryActivity.this, new ArrayList<LoginDo>());
        rvChatList.setAdapter(chatHistoryListAdapter);
        getData();
    }

    private void initialiseControls(){
        rvChatList              = llChatList.findViewById(R.id.rvChatList);
        tvNoChatFound           = llChatList.findViewById(R.id.tvNoChatFound);
    }
    @Override
    public void getData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference(AppConstants.Table_Users);
        showLoader();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                hideLoader();
                if (snapshot != null){
                    Log.e("Courses Count " ,""+snapshot.getChildrenCount());
                    ArrayList<LoginDo>  loginDos = new ArrayList<>();
                    for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                        LoginDo loginDo = postSnapshot.getValue(LoginDo.class);
                        if(!loginDo.email.equalsIgnoreCase(preferenceUtils.getStringFromPreference(PreferenceUtils.EmailId, ""))){
                            loginDos.add(loginDo);
                        }
                        Log.e("Get Data", loginDo.toString());
                    }


//                    HashMap hashMap = (HashMap) ((HashMap) snapshot.getValue()).get(AppConstants.UserId);
//                    if(hashMap != null ){
//                        ArrayList<String> listChated = new ArrayList<>(hashMap.keySet());
        //                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
        //                    ChatMessageDo chatMessageDo = postSnapshot.getValue(ChatMessageDo.class);
        //                    chatMessageDos.add(chatMessageDo);
        //                    Log.e("Get Data", chatMessageDo.toString());
        //                }
                        if(loginDos!=null && loginDos.size() > 0){
//                    loadFavouriteCourses(courseDos);
//                    flSearch.setVisibility(View.VISIBLE);
//                    rvCourseList.setVisibility(View.VISIBLE);
//                    tvNoCoursesFound.setVisibility(View.GONE);
                            rvChatList.setAdapter(chatHistoryListAdapter = new ChatHistoryListAdapter(ChatHistoryActivity.this, loginDos));
                        }
                        else {
//                    flSearch.setVisibility(View.GONE);
//                    rvCourseList.setVisibility(View.GONE);
//                    tvNoCoursesFound.setVisibility(View.VISIBLE);
                        }
//                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hideLoader();
                Log.e("The read failed: " ,databaseError.getMessage());
            }
        });

    }

    private class ChatHistoryListAdapter extends RecyclerView.Adapter<ChatHolder>{

        private Context context;
        private ArrayList<LoginDo> loginDos;

        public ChatHistoryListAdapter(Context context, ArrayList<LoginDo> loginDos){
            this.context = context;
            this.loginDos = loginDos;
        }

        @NonNull
        @Override
        public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View convertView = LayoutInflater.from(context).inflate(R.layout.chat_history_item_cell, parent, false);
            return new ChatHolder(convertView);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatHolder holder, final int position) {
            holder.tvUser.setText(""+loginDos.get(position).firstName+" "+loginDos.get(position).lastName);
            if (loginDos.get(position).userType.equalsIgnoreCase("A")){
                holder.tvUserType.setText("Admin");
            }
            else if (loginDos.get(position).userType.equalsIgnoreCase("T")){
                holder.tvUserType.setText("Tutor");
            }
            else {
                holder.tvUserType.setText("User");
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, NewChatActivity.class);
                    intent.putExtra("ChatWith", loginDos.get(position).email.split("@")[0]);
                    startActivity(intent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                }
            });
        }

        @Override
        public int getItemCount() {
            return loginDos!=null ? loginDos.size() : 0;
        }
    }

    private class ChatHolder extends RecyclerView.ViewHolder{

        private TextView tvUser, tvUserType;
        public ChatHolder(@NonNull View itemView) {
            super(itemView);
            tvUser              = itemView.findViewById(R.id.tvUser);
            tvUserType          = itemView.findViewById(R.id.tvUserType);

        }
    }
}
