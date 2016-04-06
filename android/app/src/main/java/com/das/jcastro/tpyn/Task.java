package com.das.jcastro.tpyn;

import java.util.Date;

public class Task {
    private int code;
    private String title;
    private String content;
    private Date when;
    private String where;

    public Task(int code, String title, String content, Date when, String where) {
        this.code = code;
        this.title = title;
        this.content = content;
        this.when = when;
        this.where = where;
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

    public Date getWhen() {
        return this.when;
    }

    public void setWhen(Date when) {
        this.when = when;
    }

    public String getWhere() {
        return this.where;
    }

    public void setWhere(String where) {
        this.where = where;
    }
}
