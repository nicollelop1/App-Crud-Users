package com.nalr.appcrudusers.model;

public class CreateUser {
    public String id;
    public String name;
    public String email;
    public String password;
    public String role;

    public CreateUser() {
    }

    public CreateUser(String id, String name, String email, String password, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
