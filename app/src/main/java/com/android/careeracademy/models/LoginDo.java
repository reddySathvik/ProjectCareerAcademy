package com.android.careeracademy.models;

public class LoginDo extends BaseDo {

    public String firstName = "";
    public String lastName = "";
    public String email = "";
    public String password = "";
    public String profileImgPath = "";
    public String userType = "";

    public LoginDo(){}

    public LoginDo(String firstName, String lastName, String email, String password, String profileImgPath, String userType){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.profileImgPath = profileImgPath;
        this.userType = userType;
    }
}
