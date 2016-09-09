package ru.dealerpoint.redmine;

import java.io.Serializable;

public class User implements Serializable {
    private Long id;
    private String login;
    private String firstname;
    private String lastname;
    private String mail;
//    private String created_on; "2016-03-08T17:22:19Z",
//    private String last_login_on; "2016-09-05T05:48:27Z",
    private String apiKey;
    private Integer status;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    @Override
    public String toString()
    {
        return "User {" + this.id + ", " + this.login + '}';
    }
}
