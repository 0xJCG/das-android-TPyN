package com.das.jcastro.tpyn;

public class Note {
    private int code;
    private String title;
    private String content;
    private String location;
    private byte[] image;

    public Note(int code, String title, String content, String location, byte[] image) {
        this.code = code;
        this.title = title;
        this.content = content;
        this.location = location;
        this.image = image;
    }

    public Note(int code, String title, String content, String location) {
        this.code = code;
        this.title = title;
        this.content = content;
        this.location = location;
        this.image = null;
    }

    public Note(int code, String title, String content, byte[] image) {
        this.code = code;
        this.title = title;
        this.content = content;
        this.location = "";
        this.image = image;
    }

    public Note(int code, String title, String content) {
        this.code = code;
        this.title = title;
        this.content = content;
        this.location = "";
        this.image = null;
    }


    public int getCode() {
        return this.code;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String where) {
        this.location = where;
    }

    public byte[] getImage() {
        return this.image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
