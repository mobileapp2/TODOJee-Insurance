package com.insurance.todojee.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class ClientMainListPojo implements Serializable {

    private ArrayList<ClientFamilyDetailsPojo> relation_details;

    private String created_by;

    private String whats_app_no;

    private String alias;

    private String middle_name;

    private String sync_status;

    private String family_code;

    private String id;

    private String first_name;

    private String updated_at;

    private String email;

    private String dob;

    private String last_name;

    private ArrayList<ClientFirmDetailsPojo> firm_details;

    private String created_at;

    private String updated_by;

    private String anniversary_date;

    private String is_main;

    private String mobile;

    private String family_code_id;

    private boolean isChecked;

    public ArrayList<ClientFamilyDetailsPojo> getRelation_details() {
        return relation_details;
    }

    public void setRelation_details(ArrayList<ClientFamilyDetailsPojo> relation_details) {
        this.relation_details = relation_details;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public String getWhats_app_no() {
        return whats_app_no;
    }

    public void setWhats_app_no(String whats_app_no) {
        this.whats_app_no = whats_app_no;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getMiddle_name() {
        return middle_name;
    }

    public void setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
    }

    public String getSync_status() {
        return sync_status;
    }

    public void setSync_status(String sync_status) {
        this.sync_status = sync_status;
    }

    public String getFamily_code() {
        return family_code;
    }

    public void setFamily_code(String family_code) {
        this.family_code = family_code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public ArrayList<ClientFirmDetailsPojo> getFirm_details() {
        return firm_details;
    }

    public void setFirm_details(ArrayList<ClientFirmDetailsPojo> firm_details) {
        this.firm_details = firm_details;
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

    public String getAnniversary_date() {
        return anniversary_date;
    }

    public void setAnniversary_date(String anniversary_date) {
        this.anniversary_date = anniversary_date;
    }

    public String getIs_main() {
        return is_main;
    }

    public void setIs_main(String is_main) {
        this.is_main = is_main;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getFamily_code_id() {
        return family_code_id;
    }

    public void setFamily_code_id(String family_code_id) {
        this.family_code_id = family_code_id;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public static Comparator<ClientMainListPojo> NameComparator = new Comparator<ClientMainListPojo>() {

        @Override
        public int compare(ClientMainListPojo e1, ClientMainListPojo e2) {
            return e1.getFirst_name().toLowerCase().compareTo(e2.getFirst_name().toLowerCase());
        }
    };

    public static class ClientFamilyDetailsPojo implements Serializable {

        private String dob;

        private String name;

        private String relation;

        private String mobile;

        private String family_details_id;

        private boolean isChecked;

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
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

        public String getFamily_details_id() {
            return family_details_id;
        }

        public void setFamily_details_id(String family_details_id) {
            this.family_details_id = family_details_id;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }
    }

    public static class ClientFirmDetailsPojo implements Serializable {

        private String firm_name;

        private String firm_id;

        private boolean isChecked;

        public String getFirm_name() {
            return firm_name;
        }

        public void setFirm_name(String firm_name) {
            this.firm_name = firm_name;
        }

        public String getFirm_id() {
            return firm_id;
        }

        public void setFirm_id(String firm_id) {
            this.firm_id = firm_id;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }
    }
}
