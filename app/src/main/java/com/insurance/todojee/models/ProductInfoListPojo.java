package com.insurance.todojee.models;

import java.io.Serializable;

public class ProductInfoListPojo implements Serializable {

    private String created_by;

    private String id;

    private String document;

    private String text;

    private String updated_at;

    private String created_at;

    private String updated_by;

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_by() {
        return updated_by;
    }

    public void setUpdated_by(String updated_by) {
        this.updated_by = updated_by;
    }

    @Override
    public String toString() {
        return "ClassPojo [created_by = " + created_by + ", id = " + id + ", document = " + document + ", text = " + text + ", updated_at = " + updated_at + ", created_at = " + created_at + ", updated_by = " + updated_by + "]";
    }
}
