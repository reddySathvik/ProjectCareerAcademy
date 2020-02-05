package com.android.careeracademy.activities;

import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.careeracademy.R;
import com.android.careeracademy.common.AppConstants;

public class SplashActivity extends BaseActivity {

    private LinearLayout llSplash;
    private TextView tvTypeAdmin, tvTypeUser, tvTypeTutor, tvTypeGuest, tvTypeAboutUs;
    @Override
    public void initialise() {
        llSplash = (LinearLayout) inflater.inflate(R.layout.splash_layout, null);
        addBodyView(llSplash);
        lockMenu();
        ivBack.setVisibility(View.GONE);
        ivMenu.setVisibility(View.GONE);
        llToolbar.setVisibility(View.GONE);
        initializeControls();

        tvTypeAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                intent.putExtra("From", AppConstants.Admin);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
        tvTypeUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                intent.putExtra("From", AppConstants.User);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
        tvTypeTutor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                intent.putExtra("From", AppConstants.Tutor);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
        tvTypeGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SplashActivity.this, CoursesListActivity.class);
                intent.putExtra("From", AppConstants.Guest);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
        tvTypeAboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SplashActivity.this, AboutUsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
    }

    private void initializeControls(){
        tvTypeAdmin             = llSplash.findViewById(R.id.tvTypeAdmin);
        tvTypeUser              = llSplash.findViewById(R.id.tvTypeUser);
        tvTypeTutor             = llSplash.findViewById(R.id.tvTypeTutor);
        tvTypeGuest             = llSplash.findViewById(R.id.tvTypeGuest);
        tvTypeAboutUs           = llSplash.findViewById(R.id.tvTypeAboutUs);
    }

    @Override
    public void getData() {

    }
}
