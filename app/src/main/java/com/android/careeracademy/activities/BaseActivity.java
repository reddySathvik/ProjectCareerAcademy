package com.android.careeracademy.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.careeracademy.R;
import com.android.careeracademy.common.AppConstants;
import com.android.careeracademy.utils.PreferenceUtils;

import java.util.regex.Pattern;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;


public abstract class BaseActivity extends AppCompatActivity {

    public Context context;
    public LayoutInflater inflater;
    public PreferenceUtils preferenceUtils;
    private Dialog dialog;
    private AlertDialog.Builder alertDialog;
    public FrameLayout llToolbar;
    public LinearLayout llDrawerLayoutPrent, llDrawerLayout, llBody;
    private DrawerLayout dlCareer;
    public ImageView ivBack, ivMenu;
    public TextView tvTitle, tvSignup;
    private TextView tvMyProfile, tvEnrolledCourses, tvFavourites, tvChat, tvLogout;
    public static BaseActivity mInstance;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.career_base_layout);
        mInstance = this;
        inflater = getLayoutInflater();
        context = BaseActivity.this;
        preferenceUtils = new PreferenceUtils(context);
        initialiseControls();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        if(AppConstants.UserType.equalsIgnoreCase(AppConstants.User)){
            tvMyProfile.setVisibility(View.VISIBLE);
            tvEnrolledCourses.setVisibility(View.VISIBLE);
            tvFavourites.setVisibility(View.VISIBLE);
            tvChat.setVisibility(View.VISIBLE);
            tvLogout.setVisibility(View.VISIBLE);
        }
        else if(AppConstants.UserType.equalsIgnoreCase(AppConstants.Tutor)){
            tvMyProfile.setVisibility(View.VISIBLE);
            tvEnrolledCourses.setVisibility(View.GONE);
            tvFavourites.setVisibility(View.GONE);
            tvChat.setVisibility(View.VISIBLE);
            tvLogout.setVisibility(View.VISIBLE);
        }
        else {
            tvMyProfile.setVisibility(View.GONE);
            tvEnrolledCourses.setVisibility(View.GONE);
            tvFavourites.setVisibility(View.GONE);
            tvChat.setVisibility(View.GONE);
            tvLogout.setVisibility(View.GONE);
        }

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuOperates();
            }
        });

        tvSignup.setOnClickListener(menuListener);
        tvMyProfile.setOnClickListener(menuListener);
        tvEnrolledCourses.setOnClickListener(menuListener);
        tvFavourites.setOnClickListener(menuListener);
        tvChat.setOnClickListener(menuListener);
        tvLogout.setOnClickListener(menuListener);

        llDrawerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuOperates();
            }
        });
        llDrawerLayoutPrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuOperates();
            }
        });
        initialise();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void lockMenu() {
        dlCareer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
    }

    public int getCourseImage(String courseName){
        switch (courseName.toLowerCase()){
            case "java":
                return R.drawable.java;
            case "python":
                return R.drawable.python;
            case "sql":
                return R.drawable.sql;
            case "html":
                return R.drawable.oracle;
        }
        return R.drawable.course_placeholder;
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private View.OnClickListener menuListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.tvSignup:
                    if(AppConstants.UserType.equalsIgnoreCase(AppConstants.Admin)) {
                        Intent intent = new Intent(context, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        overridePendingTransition(R.anim.enter, R.anim.exit);
                    }
                    else {
                        Intent intent = new Intent(context, RegistrationActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.enter, R.anim.exit);
                    }
                    break;

                case R.id.tvMyProfile:
                    menuOperates();
                    if(!(context instanceof MyProfileActivity)){
                        Intent intent = new Intent(context, MyProfileActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.enter, R.anim.exit);
                    }
                    break;

                case R.id.tvEnrolledCourses:
                    menuOperates();
                    if(!(context instanceof EnrolledCoursesListActivity)){
                        Intent intent = new Intent(context, EnrolledCoursesListActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.enter, R.anim.exit);
                    }
                    break;

                case R.id.tvFavourites:
                    menuOperates();
                    if(!(context instanceof FavouriteCoursesListActivity)){
                        Intent intent = new Intent(context, FavouriteCoursesListActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.enter, R.anim.exit);
                    }
                    break;

                case R.id.tvChat:
                    menuOperates();
                    if(!(context instanceof ChatHistoryActivity)){
                        Intent intent = new Intent(context, ChatHistoryActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.enter, R.anim.exit);
                    }
                    break;

                case R.id.tvLogout:
                    menuOperates();
                    Intent intent = new Intent(context, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                    break;
            }
        }
    };

    private void initialiseControls() {
        dlCareer                    = findViewById(R.id.dlCareer);
        llDrawerLayout              = findViewById(R.id.llDrawerLayout);
        llDrawerLayoutPrent         = findViewById(R.id.llDrawerLayoutPrent);
        llBody                      = findViewById(R.id.llBody);
        llToolbar                   = findViewById(R.id.llToolbar);
        ivBack                      = findViewById(R.id.ivBack);
        ivMenu                      = findViewById(R.id.ivMenu);
        tvSignup                    = findViewById(R.id.tvSignup);
        tvTitle                     = findViewById(R.id.tvTitle);
        tvMyProfile                 = findViewById(R.id.tvMyProfile);
        tvEnrolledCourses           = findViewById(R.id.tvEnrolledCourses);
        tvFavourites                = findViewById(R.id.tvFavourites);
        tvChat                      = findViewById(R.id.tvChat);
        tvLogout                    = findViewById(R.id.tvLogout);

    }

    protected void addBodyView(View body) {
        llBody.addView(body, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void menuOperates() {
        hideKeyBoard(llBody);
        if (dlCareer.isDrawerOpen(Gravity.RIGHT)) {
            dlCareer.closeDrawer(Gravity.RIGHT);
        } else {
            dlCareer.openDrawer(Gravity.RIGHT);
        }
    }

    public abstract void initialise();

    public abstract void getData();

    public void showToast(String strMsg) {
        Toast.makeText(BaseActivity.this, strMsg, Toast.LENGTH_LONG).show();
    }

    public static BaseActivity getInstance() {
        return mInstance;
    }

    public void showAppCompatAlert(String mTitle, String mMessage, String posButton, String negButton, final String from, boolean isCancelable) {
        if (alertDialog == null)
            alertDialog = new AlertDialog.Builder(BaseActivity.this, R.style.AppCompatAlertDialogStyle);
        alertDialog.setTitle(mTitle);
        alertDialog.setMessage(mMessage);
        alertDialog.setPositiveButton(posButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onButtonYesClick(from);
            }
        });
        if (negButton != null && !negButton.equalsIgnoreCase(""))
            alertDialog.setNegativeButton(negButton, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onButtonNoClick(from);
                }
            });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public void onButtonNoClick(String from) {

    }

    public void onButtonYesClick(String from) {

    }

    public void hideKeyBoard(View view) {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSoftKeyboard(View view) {
        try {
            if (view.requestFocus()) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showInternetDialog(String from) {
        showAppCompatAlert("", getResources().getString(R.string.network_error), "Ok", "Cancel", from, false);

    }

    protected boolean isValidEmail(String email) {
        Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
                "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                        "\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                        "(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+");
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
    }

    //Method to Show loader without text
    public void showLoader() {
        runOnUiThread(new RunShowLoader(""));
    }

    //Method to show loader with text
    public void showLoader(String msg) {
        runOnUiThread(new RunShowLoader(msg));
    }

    public void hideLoader() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (dialog != null && dialog.isShowing())
                        dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setFont(ViewGroup group, Typeface tf) {
        int count = group.getChildCount();
        View v;
        for (int i = 0; i < count; i++) {
            v = group.getChildAt(i);
            if (v instanceof TextView || v instanceof Button /* etc. */)
                ((TextView) v).setTypeface(tf);
            else if (v instanceof ViewGroup)
                setFont((ViewGroup) v, tf);
        }

    }

    private class RunShowLoader implements Runnable {
        private String strMsg;

        public RunShowLoader(String strMsg) {
            this.strMsg = strMsg;
        }

        @Override
        public void run() {
            try {
                dialog = new Dialog(BaseActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                View view = LayoutInflater.from(context).inflate(R.layout.loader_custom, null);
                ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
                progressBar.setIndeterminate(true);
                progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);
                dialog.setContentView(view);
                if (!dialog.isShowing())
                    dialog.show();
            } catch (Exception e) {
            }
        }
    }

    public boolean isNetworkConnectionAvailable(Context context) {
        boolean isNetworkConnectionAvailable = false;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

            if (activeNetworkInfo != null) {
                isNetworkConnectionAvailable = activeNetworkInfo.getState() == NetworkInfo.State.CONNECTED;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isNetworkConnectionAvailable;
    }

    public void applyFont(final View root, String fontName) {
        try {
            fontName = fontStyleName(fontName);
            if (root instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) root;
                for (int i = 0; i < viewGroup.getChildCount(); i++)
                    applyFont(viewGroup.getChildAt(i), fontName);
            }
            else if (root instanceof TextView){
                ((TextView) root).setTypeface(Typeface.createFromAsset(getAssets(), fontName));
            }
        } catch (Exception e) {
            Log.e("KARYA", String.format("Error occured when trying to apply %s font for %s view", fontName, root));
            e.printStackTrace();
        }
    }

    public void applyFontFromSuccess(final View root, String fontName) {
        try {
            fontName = fontStyleName(fontName);
            if (root instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) root;
                for (int i = 0; i < viewGroup.getChildCount(); i++)
                    applyFont(viewGroup.getChildAt(i), fontName);
            }
            else if (root instanceof TextView){
                ((TextView) root).setTypeface(Typeface.createFromAsset(getAssets(), fontName));
                ((TextView) root).setTypeface(((TextView) root).getTypeface(), Typeface.BOLD);
            }
        } catch (Exception e) {
            Log.e("KARYA", String.format("Error occured when trying to apply %s font for %s view", fontName, root));
            e.printStackTrace();
        }
    }

    private String fontStyleName(String name){
        String fontStyleName = "";
        switch (name){
            case AppConstants.GILL_SANS_TYPE_FACE:
                fontStyleName =  "gillsansstd.otf";
                break;
            case AppConstants.MONTSERRAT_MEDIUM_TYPE_FACE:
                fontStyleName =  "montserrat_medium.ttf";
                break;
            case "":
                fontStyleName =  "gillsansstd.otf";
                break;
        }
        return fontStyleName;
    }
}

