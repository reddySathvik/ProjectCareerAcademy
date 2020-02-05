package com.android.careeracademy.models;

public class CourseDocDo extends BaseDo {

    public String docId = "";
    public String courseId = "";
    public String courseName = "";
    public String courseDocPath = "";
    private String fileExt = "";
    public String tutorId = "";

    public CourseDocDo(){}

    public CourseDocDo(String docId, String courseId, String courseName, String courseDocPath, String fileExt, String tutorId){
        this.docId = docId;
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseDocPath = courseDocPath;
        this.fileExt = fileExt;
        this.tutorId = tutorId;
    }
}
