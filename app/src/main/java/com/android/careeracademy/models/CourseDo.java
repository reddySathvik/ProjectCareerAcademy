package com.android.careeracademy.models;

public class CourseDo extends BaseDo {

    public String courseId = "";
    public String courseName = "";
    public String courseImgPath = "";
    public String courseDescription = "";
    public float courseRating;
    public String tutorName = "";
    public double coursePrice;
    public int courseDuration;

    public CourseDo(){}

    public CourseDo(String courseId, String courseName, String courseImgPath, String courseDescription, float courseRating,
                    String tutorName, double coursePrice, int courseDuration){
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseImgPath = courseImgPath;
        this.courseDescription = courseDescription;
        this.courseRating = courseRating;
        this.tutorName = tutorName;
        this.coursePrice = coursePrice;
        this.courseDuration = courseDuration;
    }
}
