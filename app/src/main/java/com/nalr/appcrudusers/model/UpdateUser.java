package com.nalr.appcrudusers.model;

public class UpdateUser {
    public String name;
    public String email;
    public String password;
    public String role;
    public String status;

    public UpdateUser() {
    }

    public UpdateUser(String name, String email, String password, String role, String status) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
    }
}
