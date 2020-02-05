package com.android.careeracademy.activities;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.careeracademy.R;
import com.android.careeracademy.common.AppConstants;
import com.android.careeracademy.utils.PreferenceUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import androidx.annotation.NonNull;

public class LoginActivity extends BaseActivity {

    private FrameLayout flLogin;
    private LinearLayout llRegistration;
    private EditText etEmail, etPassword;
    private TextView tvLogin, tvRegister;
    private String userType = "";

    @Override
    public void initialise() {
        flLogin = (FrameLayout) inflater.inflate(R.layout.login_layout, null);
        addBodyView(flLogin);
        lockMenu();
        ivBack.setVisibility(View.VISIBLE);
        ivMenu.setVisibility(View.GONE);
        tvTitle.setVisibility(View.GONE);
        tvSignup.setVisibility(View.GONE);
        llToolbar.setVisibility(View.VISIBLE);
        initializeControls();
//        etEmail.setText("sathvik@gmail.com");
//        etPassword.setText("123456");
        if(getIntent().hasExtra("From")){
            userType = getIntent().getExtras().getString("From");
        }
        tvTitle.setText("Login");
        if(userType.equalsIgnoreCase(AppConstants.Admin)){
            llRegistration.setVisibility(View.GONE);
        }
        else {
            llRegistration.setVisibility(View.VISIBLE);
        }
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doLogin();
            }
        });
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
    }

    private void initializeControls(){
        llRegistration              = flLogin.findViewById(R.id.llRegistration);
        etEmail                     = flLogin.findViewById(R.id.etEmail);
        etPassword                  = flLogin.findViewById(R.id.etPassword);
        tvLogin                     = flLogin.findViewById(R.id.tvLogin);
        tvRegister                  = flLogin.findViewById(R.id.tvRegister);
    }

    private void doLogin(){
        if (etEmail.getText().toString().trim().equalsIgnoreCase("")){
            showAppCompatAlert("", "Please enter your email", "OK", "", "", true);
        }
        else if(!isValidEmail(etEmail.getText().toString().trim())){
            showAppCompatAlert("", "Please enter valid email", "OK", "", "", true);
        }

        else if(!isValidEmail(etEmail.getText().toString().trim())){
            showAppCompatAlert("", "Please enter valid email", "OK", "", "", true);
        }

        else if(etPassword.getText().toString().trim().length()<6){
            showAppCompatAlert("", "Please enter minimum 6 char password", "OK", "", "", true);
        }
        else {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference databaseReference = database.getReference(AppConstants.Table_Users);
            showLoader();
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot!=null && dataSnapshot.exists()){
                                HashMap hashMap = (HashMap) ((HashMap) dataSnapshot.getValue()).get(etEmail.getText().toString().trim().split("@")[0]);
                                if (hashMap != null && hashMap.get("email").toString().equalsIgnoreCase(etEmail.getText().toString().trim())
                                        && hashMap.get("password").toString().equalsIgnoreCase(etPassword.getText().toString().trim())) {
                                    preferenceUtils.saveString(PreferenceUtils.EmailId, etEmail.getText().toString().trim());
                                    preferenceUtils.saveString(PreferenceUtils.Password, etPassword.getText().toString().trim());
                                    String userType = hashMap.get("userType").toString();
                                    String userName = hashMap.get("firstName").toString();
                                    userName = userName +" "+ hashMap.get("lastName").toString();
                                    Intent intent = null;
                                    if(userType.equalsIgnoreCase("A")){
                                        intent = new Intent(LoginActivity.this, CoursesListActivity.class);
                                    }
                                    else if(userType.equalsIgnoreCase("U")){
                                        intent = new Intent(LoginActivity.this, CoursesListActivity.class);
                                    }
                                    else if(userType.equalsIgnoreCase("T")){//tutor
                                        intent = new Intent(LoginActivity.this, CoursesListActivity.class);
                                    }

                                    try {
                                        AppConstants.UserId = etEmail.getText().toString().trim().split("@")[0];
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    AppConstants.UserType = userType;
                                    preferenceUtils.saveString(PreferenceUtils.UserName, userName);
                                    intent.putExtra("From", userType);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.enter, R.anim.exit);
                                    finish();
                                }
                                else {
                                    //Username does not exist
                                    hideLoader();
                                    databaseReference.orderByChild("email")
                                            .equalTo(etEmail.getText().toString().trim()).removeEventListener(this);
                                    showAppCompatAlert("", "The entered email and password are not exist.", "OK", "", "", true);
                                }
                            }
                            else {
                                //Username does not exist
                                hideLoader();
                                databaseReference.child(AppConstants.Table_Users).orderByChild("email")
                                        .equalTo(etEmail.getText().toString().trim()).removeEventListener(this);
                                showAppCompatAlert("", "The entered email and password are not exist.", "OK", "", "", true);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            hideLoader();
                            Log.e("LoginActivity", "Failed to reading email.", databaseError.toException());
                        }
                    });
        }
    }

    @Override
    public void getData() {

    }
}
