package com.skylark.tripadviser;

/**
 * Created by bochi on 10-12-2017.
 */

public class UserDetails
{
    private String balance;
    private String username;
    private String lname;
    private String email;
    private String pendingAmount;
    private String password;
    private String fname;
    private String mobile;

    public String getBalance () {
        return balance;
    }

    public void setBalance (String balance) {
        this.balance = balance;
    }

    public String getUsername () {
        return username;
    }

    public void setUsername (String username) {
        this.username = username;
    }

    public String getLname () {
        return lname;
    }

    public void setLname (String lname) {
        this.lname = lname;
    }

    public String getEmail () {
        return email;
    }

    public void setEmail (String email) {
        this.email = email;
    }

    public String getPendingAmount () {
        return pendingAmount;
    }

    public void setPendingAmount (String pendingAmount) {
        this.pendingAmount = pendingAmount;
    }

    public String getPassword () {
        return password;
    }

    public void setPassword (String password) {
        this.password = password;
    }

    public String getFname () {
        return fname;
    }

    public void setFname (String fname) {
        this.fname = fname;
    }

    public String getMobile () {
        return mobile;
    }

    public void setMobile (String mobile) {
        this.mobile = mobile;
    }
}
