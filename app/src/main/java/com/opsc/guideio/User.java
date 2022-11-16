package com.opsc.guideio;

public class User {public String getName() {
    return name;
}

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String name;
    private String surname;
    private String email;
    private String password;
    String [] Mesurements= {"Kilometers","Miles"};
    String [] places= {"none","airport","bakery","bank","police","university","zoo"};
}
