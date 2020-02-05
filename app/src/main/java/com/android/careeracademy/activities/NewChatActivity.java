package com.android.careeracademy.activities;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.careeracademy.R;
import com.android.careeracademy.common.AppConstants;
import com.android.careeracademy.models.ChatMessageDo;
import com.android.careeracademy.utils.PreferenceUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NewChatActivity extends BaseActivity {

    private LinearLayout llChatList;
    private EditText etMessage;
    private ImageView ivSend;
    private RecyclerView rvChatList;
    private ChatMessagesListAdapter chatMessagesListAdapter;
    private String userName = "";
    private String userId = "", chatWith = "";
    private DatabaseReference mReference;

    @Override
    public void initialise() {
        llChatList = (LinearLayout) inflater.inflate(R.layout.new_chat_layout, null);
        addBodyView(llChatList);
        llToolbar.setVisibility(View.VISIBLE);
        tvSignup.setVisibility(View.GONE);
        tvTitle.setVisibility(View.VISIBLE);
        ivBack.setVisibility(View.VISIBLE);
        ivMenu.setVisibility(View.GONE);
        lockMenu();
        userName = preferenceUtils.getStringFromPreference(PreferenceUtils.UserName, "");
        userId = preferenceUtils.getStringFromPreference(PreferenceUtils.EmailId, "");
        tvTitle.setText("Chat");
        if(getIntent().hasExtra("ChatWith")){
            chatWith = getIntent().getStringExtra("ChatWith");
        }
        tvTitle.setText(chatWith);
        initialiseControls();
        rvChatList.setLayoutManager(new LinearLayoutManager(NewChatActivity.this, LinearLayoutManager.VERTICAL, false));
        rvChatList.setAdapter(chatMessagesListAdapter = new ChatMessagesListAdapter(NewChatActivity.this, new ArrayList<ChatMessageDo>()));
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mReference = database.getReference(AppConstants.Table_Chat);
        getData();
        ivSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!etMessage.getText().toString().trim().equalsIgnoreCase("")){
                    ChatMessageDo chatMessageDo = new ChatMessageDo(etMessage.getText().toString(), userId, userName, new Date().getTime());
                    mReference.child(tableName()).child(""+System.currentTimeMillis()).setValue(chatMessageDo).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            getData();
                        }
                    });
                    etMessage.setText("");
                }
            }
        });

    }

    private void initialiseControls(){
        rvChatList              = llChatList.findViewById(R.id.rvChatList);
        etMessage               = llChatList.findViewById(R.id.etMessage);
        ivSend                  = llChatList.findViewById(R.id.ivSend);
    }

    private String tableName(){
        char tableName[] = (AppConstants.UserId+chatWith).toCharArray();
        Arrays.sort(tableName);
        return new String(tableName);
    }
    @Override
    public void getData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference(AppConstants.Table_Chat);
        showLoader();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                hideLoader();
                if(snapshot!=null){
                    Log.e("Chat Count " ,""+snapshot.getChildrenCount());
                    HashMap hashMap = (HashMap) snapshot.getValue();
                    if(hashMap!=null && hashMap.get(tableName()) != null){
                        ArrayList<String> chatWithLlist = new ArrayList<String>(((HashMap)hashMap.get(tableName())).keySet());
                        ArrayList<ChatMessageDo> chatMessageDos = new ArrayList<>();
                        if(chatWithLlist!=null && chatWithLlist.size() > 0){
                            for (int i=0;i<chatWithLlist.size();i++){
                                ChatMessageDo chatMessageDo = new ChatMessageDo();
                                chatMessageDo.messageText = (String) ((HashMap)((HashMap)hashMap.get(tableName())).get(chatWithLlist.get(i))).get("messageText");
                                chatMessageDo.fromUserId = (String) ((HashMap)((HashMap)hashMap.get(tableName())).get(chatWithLlist.get(i))).get("fromUserId");
                                chatMessageDo.messageUserName = (String) ((HashMap)((HashMap)hashMap.get(tableName())).get(chatWithLlist.get(i))).get("messageUserName");
                                chatMessageDo.messageTime = (Long) ((HashMap)((HashMap)hashMap.get(tableName())).get(chatWithLlist.get(i))).get("messageTime");
//                                if(!chatMessageDo.fromUserId.equalsIgnoreCase(userId)){
//                                }
                                chatMessageDos.add(chatMessageDo);
                            }
                        }

                        if(chatMessageDos!=null && chatMessageDos.size() > 0){
                            Collections.sort(chatMessageDos);
                            chatMessagesListAdapter.refreshAdapter(chatMessageDos);
                            rvChatList.post(new Runnable() {
                                @Override
                                public void run() {
                                    rvChatList.smoothScrollToPosition(chatMessagesListAdapter.getItemCount()- 1);
                                }
                            });

//                            rvChatList.setAdapter(chatMessagesListAdapter = new ChatMessagesListAdapter(NewChatActivity.this, chatMessageDos));
                        }
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

    private class ChatMessagesListAdapter extends RecyclerView.Adapter<ChatHolder>{

        private Context context;
        private ArrayList<ChatMessageDo> chatMessageDos;

        public ChatMessagesListAdapter(Context context, ArrayList<ChatMessageDo> chatMessageDos){
            this.context = context;
            this.chatMessageDos = chatMessageDos;
        }

        private void refreshAdapter(ArrayList<ChatMessageDo> chatMessageDos){
            this.chatMessageDos = chatMessageDos;
            notifyDataSetChanged();
        }
        @NonNull
        @Override
        public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View convertView = LayoutInflater.from(context).inflate(R.layout.chat_message_item_cell, parent, false);
            return new ChatHolder(convertView);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatHolder holder, int position) {
            if(!chatMessageDos.get(position).fromUserId.equalsIgnoreCase(userId)){
                holder.tvMyMessage.setText(""+chatMessageDos.get(position).messageText);
                holder.tvMyMessage.setVisibility(View.VISIBLE);
                holder.tvOtherMessage.setVisibility(View.GONE);
            }
            else {
                holder.tvMyMessage.setVisibility(View.GONE);
                holder.tvOtherMessage.setVisibility(View.VISIBLE);
                holder.tvOtherMessage.setText(""+chatMessageDos.get(position).messageText);
            }
        }

        @Override
        public int getItemCount() {
            return chatMessageDos!=null?chatMessageDos.size():0;
        }
    }

    private class ChatHolder extends RecyclerView.ViewHolder {

        private TextView tvMyMessage, tvOtherMessage;
        public ChatHolder(@NonNull View itemView) {
            super(itemView);
            tvMyMessage               = itemView.findViewById(R.id.tvMyMessage);
            tvOtherMessage            = itemView.findViewById(R.id.tvOtherMessage);
        }
    }
}
