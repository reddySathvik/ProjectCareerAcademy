package com.android.careeracademy.models;

public class FavouriteDo extends BaseDo {

    public String courseId = "";
    public String favouriteId = "";
    public String userId = "";
    public String courseName = "";

    public FavouriteDo(){}

    public FavouriteDo(String favouriteId, String courseId, String courseName, String userId){
        this.courseId = courseId;
        this.favouriteId = favouriteId;
        this.userId = userId;
        this.courseName = courseName;
    }
}
