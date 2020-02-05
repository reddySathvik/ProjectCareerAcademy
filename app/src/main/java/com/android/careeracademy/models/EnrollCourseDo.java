package com.android.careeracademy.models;

public class EnrollCourseDo extends BaseDo {

    public String enrollId = "";
    public String courseId = "";
    public String courseName = "";
    public String userId = "";
    public double coursePrice;

    public EnrollCourseDo(){}

    public EnrollCourseDo(String enrollId, String courseId, String courseName, String userId, double coursePrice){
        this.enrollId = enrollId;
        this.courseId = courseId;
        this.courseName = courseName;
        this.userId = userId;
        this.coursePrice = coursePrice;
    }
}
