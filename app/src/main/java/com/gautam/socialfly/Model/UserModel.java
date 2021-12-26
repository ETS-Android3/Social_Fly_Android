package com.gautam.socialfly.Model;

public class UserModel {
    public String name;
    public String username;
    public String profilepic;

    public UserModel(){}

    public UserModel(String name, String username, String profilepic) {
        this.name = name;
        this.username = username;
        this.profilepic = profilepic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilepic() {
        return profilepic;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }
}
