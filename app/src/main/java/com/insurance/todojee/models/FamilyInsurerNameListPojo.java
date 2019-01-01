package com.insurance.todojee.models;

public class FamilyInsurerNameListPojo {

    private String id;

    private String dob;

    private String name;

    private String relation;

    private String lic_client_id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getLic_client_id() {
        return lic_client_id;
    }

    public void setLic_client_id(String lic_client_id) {
        this.lic_client_id = lic_client_id;
    }

}
