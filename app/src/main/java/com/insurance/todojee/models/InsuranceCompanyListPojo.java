package com.insurance.todojee.models;

public class InsuranceCompanyListPojo {

    private String created_by;

    private String id;

    private String insurance_type;

    private String updated_at;

    private String company_name;

    private String created_at;

    private String updated_by;

    private String sync_status;

    private String company_alias;

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

    public String getInsurance_type() {
        return insurance_type;
    }

    public void setInsurance_type(String insurance_type) {
        this.insurance_type = insurance_type;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
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

    public String getCompany_alias() {
        return company_alias;
    }

    public void setCompany_alias(String company_alias) {
        this.company_alias = company_alias;
    }

    @Override
    public String toString() {
        return "ClassPojo [created_by = " + created_by + ", id = " + id + ", insurance_type = " + insurance_type + ", updated_at = " + updated_at + ", company_name = " + company_name + ", created_at = " + created_at + ", updated_by = " + updated_by + ", sync_status = " + sync_status + ", company_alias = " + company_alias + "]";
    }
}
