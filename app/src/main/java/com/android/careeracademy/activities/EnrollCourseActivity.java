package com.android.careeracademy.activities;

import android.app.DatePickerDialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.careeracademy.R;
import com.android.careeracademy.common.AppConstants;
import com.android.careeracademy.models.CourseDo;
import com.android.careeracademy.models.EnrollCourseDo;
import com.android.careeracademy.utils.PreferenceUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import androidx.annotation.NonNull;

public class EnrollCourseActivity extends BaseActivity {

    private LinearLayout llEnroll;
    private TextView tvCourseName, tvCourseFee, tvPay;
    private EditText etCardNumber, etCVV;
    private TextView etExpire;
    private CourseDo courseDo;

    @Override
    public void initialise() {
        llEnroll = (LinearLayout) inflater.inflate(R.layout.enroll_course_layout, null);
        addBodyView(llEnroll);
        lockMenu();
        llToolbar.setVisibility(View.VISIBLE);
        ivMenu.setVisibility(View.GONE);
        ivBack.setVisibility(View.VISIBLE);
        if(getIntent().hasExtra("CourseDo")){
            courseDo = (CourseDo) getIntent().getExtras().getSerializable("CourseDo");
        }
        tvTitle.setText("Enroll Course");
        initializeControls();
        tvCourseName.setText(courseDo.courseName);
        tvCourseFee.setText(courseDo.coursePrice+"$");
        tvPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enrollCourse();
            }
        });
        etExpire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                final DecimalFormat decimalFormat = new DecimalFormat("00");
                DatePickerDialog datePickerDialog = new DatePickerDialog(EnrollCourseActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                String shortYear = (year+"").substring(2, 4);
                                etExpire.setText(decimalFormat.format(monthOfYear + 1) + "" + shortYear);
                            }
                        }, mYear, mMonth, mDay);

//                c.add(Calendar.YEAR, -2); // subtract 2 years from now
                datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
                c.add(Calendar.YEAR, 4); // add 4 years to min date to have 2 years after now
                datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());

                datePickerDialog.show();
            }
        });
    }

    private void initializeControls(){
        tvCourseName                    = llEnroll.findViewById(R.id.tvCourseName);
        tvCourseFee                     = llEnroll.findViewById(R.id.tvCourseFee);
        tvPay                           = llEnroll.findViewById(R.id.tvPay);
        etCardNumber                    = llEnroll.findViewById(R.id.etCardNumber);
        etExpire                        = llEnroll.findViewById(R.id.etExpire);
        etCVV                           = llEnroll.findViewById(R.id.etCVV);
    }

    private void enrollCourse(){
        if(etCardNumber.getText().toString().trim().length() != 16){
            showAppCompatAlert("", "Please enter valid card number", "Ok", "", "", true);
        }
        else if(etExpire.getText().toString().trim().length() != 4
                || !isValidMonth(etExpire.getText().toString().trim())
                || isExpired(etExpire.getText().toString().trim())){
            showAppCompatAlert("", "Please enter valid card expiry date", "Ok", "", "", true);
        }
        else if(etCVV.getText().toString().trim().length() != 3){
            showAppCompatAlert("", "Please enter valid CVV number", "Ok", "", "", true);
        }
        else {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference databaseReference = database.getReference(AppConstants.Table_Enroll);
            showLoader();
            String enrollId = ""+System.currentTimeMillis();
            EnrollCourseDo enrollCourseDo = new EnrollCourseDo(enrollId, courseDo.courseId, courseDo.courseName,
                    preferenceUtils.getStringFromPreference(PreferenceUtils.EmailId, ""), courseDo.coursePrice);

            databaseReference.child(enrollId).setValue(enrollCourseDo).
                    addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            hideLoader();
                            showAppCompatAlert("", "Congratulations! You have successfully enrolled a course.", "OK", "", "Enrolled", false);
                        }
                    });
        }
    }

    private boolean isValidMonth(String expire){
        int month = Integer.parseInt((""+expire).substring(0, 2));
        return month <= 12;
    }
    private boolean isExpired(String expire){
        try {
            Calendar calendar = Calendar.getInstance();
            DateFormat dateFormat = new SimpleDateFormat("yy"); // Just the year, with 2 digits
            int expireMonth = Integer.parseInt((""+expire).substring(0, 2));
            int expireYear = Integer.parseInt((""+expire).substring(2, 4));
            int curYear = Integer.parseInt(dateFormat.format(calendar.getTime()));
            int curMonth = calendar.get(Calendar.MONTH)+1;
            if(expireYear < curYear){
                return true;
            }
            else if(expireYear == expireMonth){
                return expireMonth < curMonth;
            }
            else {
                return false;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return true;
        }
    }

    @Override
    public void onButtonYesClick(String from) {
        super.onButtonYesClick(from);
        if (from.equalsIgnoreCase("Enrolled")){
            setResult(101);
            finish();
        }
    }

    @Override
    public void getData() {

    }
}
