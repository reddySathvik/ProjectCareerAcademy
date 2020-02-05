package com.android.careeracademy.models;

public class RatingDo extends BaseDo {

    public String ratingId = "";
    public String courseId = "";
    public String courseName = "";
    public String userId = "";
    public String comment = "";
    public float courseRating;

    public RatingDo(){}

    public RatingDo(String ratingId, String courseId, String courseName, String userId, String comment, float courseRating){
        this.ratingId = ratingId;
        this.courseId = courseId;
        this.courseName = courseName;
        this.userId = userId;
        this.comment = comment;
        this.courseRating = courseRating;
    }
}
