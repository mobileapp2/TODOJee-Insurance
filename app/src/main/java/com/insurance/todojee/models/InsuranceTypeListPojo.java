package com.insurance.todojee.models;

public class InsuranceTypeListPojo {

    private String id;
    private String insuranceType;
    private boolean isChecked;

    public InsuranceTypeListPojo(String id, String insuranceType, boolean isChecked) {
        this.id = id;
        this.insuranceType = insuranceType;
        this.isChecked = isChecked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInsuranceType() {
        return insuranceType;
    }

    public void setInsuranceType(String insuranceType) {
        this.insuranceType = insuranceType;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
