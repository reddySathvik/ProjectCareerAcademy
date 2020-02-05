package com.android.careeracademy.activities;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.careeracademy.R;

public class AboutUsActivity extends BaseActivity {

    private View llAbout;
    private TextView tvDescription, tvClose;
    @Override
    public void initialise() {
        llAbout = inflater.inflate(R.layout.about_us_layout, null);
        addBodyView(llAbout);
        llToolbar.setVisibility(View.VISIBLE);
        ivBack.setVisibility(View.VISIBLE);
        ivMenu.setVisibility(View.GONE);
        tvSignup.setVisibility(View.GONE);
        tvTitle.setText("About Us");
        lockMenu();
        initialiseControls();
        getData();
        tvDescription.setText(getResources().getString(R.string.about_us_desc));// add test in string file
        tvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initialiseControls(){
        tvDescription           = llAbout.findViewById(R.id.tvDescription);
        tvClose                 = llAbout.findViewById(R.id.tvClose);
    }

    @Override
    public void getData() {

    }
}
