package com.sgr.sample.simplephonebook;

/**
 * Created by BS158 on 2/13/2017.
 */

public class ContactItem {
    private String id;
    private String name;
    private String phone;
    private byte[] contactImage = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public byte[] getContactImage() {
        return contactImage;
    }

    public void setContactImage(byte[] contactImage) {
        this.contactImage = contactImage;
    }
}