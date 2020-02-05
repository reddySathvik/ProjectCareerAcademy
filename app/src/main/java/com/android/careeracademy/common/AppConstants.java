package com.android.careeracademy.common;

import android.graphics.Typeface;
import android.os.Environment;

import com.android.careeracademy.R;

import java.io.File;

public class AppConstants
{
	public static int DEVICE_DISPLAY_WIDTH;
	public static int DEVICE_DISPLAY_HEIGHT;
	public static final String courseNamesList[] = {"SELECT COURSE", "JAVA", "PYTHON", "SQL", "HTML"};
	public static final int courseImagesList[] = {R.drawable.course_placeholder, R.drawable.java, R.drawable.python,
			R.drawable.sql, R.drawable.oracle};

	public static final String GILL_SANS_TYPE_FACE			= "Gill Sans";
	public static final String MONTSERRAT_MEDIUM_TYPE_FACE  = "montserrat_medium";
	public static Typeface tfMedium;
	public static Typeface tfRegular;
	public static String SDCARD_ROOT = Environment.getExternalStorageDirectory().toString() + File.separator;
	public static final String INTERNET_CHECK			 	 = "InternetCheck";

	public static final String Guest					 = "G";
	public static final String User	     				 = "U";
	public static final String Tutor					 = "T";
	public static final String Admin					 = "A";
	//tables
    public static final String Table_Users				 = "Users";
    public static final String Table_Course				 = "Course";
    public static final String Table_Rating				 = "Rating";
    public static final String Table_Favourite			 = "Favourite";
    public static final String Table_Enroll  			 = "Enroll";
    public static final String Table_Docs  			     = "Dcos";
    public static final String Table_Chat  			     = "Chat";
    public static String UserType					     = User;
	public static String UserId							 = "";
}
