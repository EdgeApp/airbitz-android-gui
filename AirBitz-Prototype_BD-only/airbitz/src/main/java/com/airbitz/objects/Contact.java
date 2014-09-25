package com.airbitz.objects;

/**
 * Created by tom on 9/25/14.
 */
public class Contact {
    String name;
    String email;
    String phone;
    String thumbnail;

    public Contact(String name, String email, String phone, String uri) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.thumbnail = uri;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getThumbnail() {
        return thumbnail;
    }

}

