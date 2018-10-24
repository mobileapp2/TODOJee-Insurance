package com.insurance.todojee.models;

public class ToDoListPojo {

    private String created_by;

    private String id;

    private String updated_at;

    private String is_completed;

    private String created_at;

    private String updated_by;

    private String sync_status;

    private String list;

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

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getIs_completed() {
        return is_completed;
    }

    public void setIs_completed(String is_completed) {
        this.is_completed = is_completed;
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

    public String getSync_status() {
        return sync_status;
    }

    public void setSync_status(String sync_status) {
        this.sync_status = sync_status;
    }

    public String getList() {
        return list;
    }

    public void setList(String list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "ClassPojo [created_by = " + created_by + ", id = " + id + ", updated_at = " + updated_at + ", is_completed = " + is_completed + ", created_at = " + created_at + ", updated_by = " + updated_by + ", sync_status = " + sync_status + ", list = " + list + "]";
    }
}
