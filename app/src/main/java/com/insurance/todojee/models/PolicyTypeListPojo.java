package com.insurance.todojee.models;

import java.io.Serializable;
import java.util.ArrayList;

public class PolicyTypeListPojo implements Serializable {

    private ArrayList<Policy_details> policy_details;

    private String id;

    private String insurance_type;

    private String company_name;

    public ArrayList<Policy_details> getPolicy_details() {
        return policy_details;
    }

    public void setPolicy_details(ArrayList<Policy_details> policy_details) {
        this.policy_details = policy_details;
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

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public static class Policy_details implements Serializable {
        private String id;

        private String alias;

        private String type;

        private boolean isChecked;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }
    }


}
