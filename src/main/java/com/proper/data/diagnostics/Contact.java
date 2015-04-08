package com.proper.data.diagnostics;

import java.io.Serializable;

/**
 * Created by Lebel on 27/02/14.
 */
public class Contact implements Serializable {
    private static final long serialVersionUID = 1L;
    private int ContactId;
    private String Firstname;
    private String Lastname;
    private String Email;

    public Contact() {
    }

    public Contact(int contactId, String firstname, String lastname, String email) {
        ContactId = contactId;
         Firstname = firstname;
        Lastname = lastname;
        Email = email;
    }

    public int getContactId() {
        return ContactId;
    }

    public void setContactId(int contactId) {
        ContactId = contactId;
    }

    public String getFirstname() {
        return Firstname;
    }

    public void setFirstname(String firstname) {
        Firstname = firstname;
    }

    public String getLastname() {
        return Lastname;
    }

    public void setLastname(String lastname) {
        Lastname = lastname;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @Override
    public String toString() {
        return Firstname.trim() + " " + Lastname.trim().substring(0,1).toUpperCase() + ".";
    }
}
