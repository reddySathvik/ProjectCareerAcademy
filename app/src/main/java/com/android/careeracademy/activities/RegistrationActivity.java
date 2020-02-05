package com.android.careeracademy.activities;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.careeracademy.R;
import com.android.careeracademy.common.AppConstants;
import com.android.careeracademy.models.LoginDo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

public class RegistrationActivity extends BaseActivity {

    private static final String TAG = "RegistrationActivity";
    private FrameLayout llRegister;
    private EditText etFirstName, etLastName, etEmail, etPassword;
    private TextView tvRegister;
    private RadioGroup rgUserType;
    private RadioButton cbUser, cbTutor;
    private String userType = AppConstants.User;

    @Override
    public void initialise() {
        llRegister = (FrameLayout) inflater.inflate(R.layout.registration_layout, null);
        addBodyView(llRegister);
        lockMenu();
        ivMenu.setVisibility(View.GONE);
        llToolbar.setVisibility(View.VISIBLE);
        ivBack.setVisibility(View.VISIBLE);
        tvTitle.setText("Registration");
//        ivBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//                overridePendingTransition(R.anim.enter, R.anim.exit);
//            }
//        });
        initializeControls();
        rgUserType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.cbUser){
                    userType = AppConstants.User;
                }
                else if(checkedId == R.id.cbTutor){
                    userType = AppConstants.Tutor;
                }

            }
        });
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etFirstName.getText().toString().trim().equalsIgnoreCase("")){
                    showAppCompatAlert("", "Please enter First Name", "OK", "", "", true);
                }
                else if(etEmail.getText().toString().trim().equalsIgnoreCase("")){
                    showAppCompatAlert("", "Please enter Email", "OK", "", "", true);
                }
                else if(!isValidEmail(etEmail.getText().toString().trim())){
                    showAppCompatAlert("", "Please enter valid Email", "OK", "", "", true);
                }
                else if(etPassword.getText().toString().trim().length()<6){
                    showAppCompatAlert("", "Please enter minimum 6 character password", "OK", "", "", true);
                }
                else if(userType.equalsIgnoreCase("")){
                    showAppCompatAlert("", "Please select user type", "OK", "", "", true);
                }
                else {
                    doRegistration(etEmail.getText().toString().trim());
                }
            }
        });
    }

    private void initializeControls(){
        etFirstName                     = llRegister.findViewById(R.id.etFirstName);
        etLastName                      = llRegister.findViewById(R.id.etLastName);
        etEmail                         = llRegister.findViewById(R.id.etEmail);
        etPassword                      = llRegister.findViewById(R.id.etPassword);
        rgUserType                      = llRegister.findViewById(R.id.rgUserType);
        cbUser                          = llRegister.findViewById(R.id.cbUser);
        cbTutor                         = llRegister.findViewById(R.id.cbTutor);
        tvRegister                      = llRegister.findViewById(R.id.tvRegister);
    }

    private void doRegistration(final String email){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference(AppConstants.Table_Users);
        showLoader();
        databaseReference.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null && dataSnapshot.exists()) {
                            hideLoader();
                            showAppCompatAlert("", "The entered email already exist, please register with different email", "OK", "", "", true);
                        }
                        else {
                            //Username does not exist
                            databaseReference.orderByChild("email")
                                    .equalTo(email).removeEventListener(this);
                            insertIntoDB(email, databaseReference);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        hideLoader();
                        Log.e(TAG, "Failed to reading email.", databaseError.toException());
                    }
                });
    }

    private void insertIntoDB(String email, DatabaseReference databaseReference){
        LoginDo loginDo = new LoginDo(etFirstName.getText().toString().trim(), etLastName.getText().toString().trim(),
                email, etPassword.getText().toString().trim(), "", userType);
        databaseReference.child(email.split("@")[0]).setValue(loginDo).
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        hideLoader();
                        showAppCompatAlert("", "Congratulations! You have successfully registered.", "OK", "", "Registration", false);
                    }
                });
    }

    @Override
    public void getData() {

    }

    @Override
    public void onButtonYesClick(String from) {
        super.onButtonYesClick(from);
        if(from.equalsIgnoreCase("Registration")){
            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.enter, R.anim.exit);
            finish();
        }
    }
}
