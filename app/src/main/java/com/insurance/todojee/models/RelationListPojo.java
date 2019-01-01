package com.insurance.todojee.models;

public class RelationListPojo {

    private String id;

    private String relation;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    @Override
    public String toString() {
        return "ClassPojo [id = " + id + ", relation = " + relation + "]";
    }
}
