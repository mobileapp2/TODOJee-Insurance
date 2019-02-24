package com.insurance.todojee.models;

import java.io.Serializable;
import java.util.ArrayList;

public class LifeGeneralInsuranceMainListPojo implements Serializable {

    private String id;

    private String insurance_type_id;
    private String insurance_company_id;

    private String lic_created_by;

    public String getLic_created_by() {
        return lic_created_by;
    }

    public void setLic_created_by(String lic_created_by) {
        this.lic_created_by = lic_created_by;
    }

    private String insurance_company_name;

    private String insurance_company_alias;

    private String client_id;

    private String client_name;

    private String insurer_type_id;

    private String insurer_id;

    private String insurer_family_name;

    private String insurer_firm_name;

    private String policy_no;

    private String policy_type_id;

    private String policy_type;

    private String start_date;

    private String end_date;

    private String frequency_id;

    private String frequency;

    private String sum_insured;

    private String premium_amount;

    private String policy_status_id;

    private String policy_status;

    private String family_code_id;

    private String link;

    private String remark;

    private String description;

    private String updated_by;

    private String created_by;

    private String created_at;

    private String updated_at;
    private String is_shared;

    public String getIs_shared() {
        return is_shared;
    }

    public void setIs_shared(String is_shared) {
        this.is_shared = is_shared;
    }

    private ArrayList<MaturityDatesListPojo> maturity_date;

    private ArrayList<DocumentListPojo> document;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInsurance_type_id() {
        return insurance_type_id;
    }

    public void setInsurance_type_id(String insurance_type_id) {
        this.insurance_type_id = insurance_type_id;
    }

    public String getInsurance_company_id() {
        return insurance_company_id;
    }

    public void setInsurance_company_id(String insurance_company_id) {
        this.insurance_company_id = insurance_company_id;
    }

    public String getInsurance_company_name() {
        return insurance_company_name;
    }

    public void setInsurance_company_name(String insurance_company_name) {
        this.insurance_company_name = insurance_company_name;
    }

    public String getInsurance_company_alias() {
        return insurance_company_alias;
    }

    public void setInsurance_company_alias(String insurance_company_alias) {
        this.insurance_company_alias = insurance_company_alias;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getClient_name() {
        return client_name;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }

    public String getInsurer_type_id() {
        return insurer_type_id;
    }

    public void setInsurer_type_id(String insurer_type_id) {
        this.insurer_type_id = insurer_type_id;
    }

    public String getInsurer_id() {
        return insurer_id;
    }

    public void setInsurer_id(String insurer_id) {
        this.insurer_id = insurer_id;
    }

    public String getInsurer_family_name() {
        return insurer_family_name;
    }

    public void setInsurer_family_name(String insurer_family_name) {
        this.insurer_family_name = insurer_family_name;
    }

    public String getInsurer_firm_name() {
        return insurer_firm_name;
    }

    public void setInsurer_firm_name(String insurer_firm_name) {
        this.insurer_firm_name = insurer_firm_name;
    }

    public String getPolicy_no() {
        return policy_no;
    }

    public void setPolicy_no(String policy_no) {
        this.policy_no = policy_no;
    }

    public String getPolicy_type_id() {
        return policy_type_id;
    }

    public void setPolicy_type_id(String policy_type_id) {
        this.policy_type_id = policy_type_id;
    }

    public String getPolicy_type() {
        return policy_type;
    }

    public void setPolicy_type(String policy_type) {
        this.policy_type = policy_type;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public String getFrequency_id() {
        return frequency_id;
    }

    public void setFrequency_id(String frequency_id) {
        this.frequency_id = frequency_id;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getSum_insured() {
        return sum_insured;
    }

    public void setSum_insured(String sum_insured) {
        this.sum_insured = sum_insured;
    }

    public String getPremium_amount() {
        return premium_amount;
    }

    public void setPremium_amount(String premium_amount) {
        this.premium_amount = premium_amount;
    }

    public String getPolicy_status_id() {
        return policy_status_id;
    }

    public void setPolicy_status_id(String policy_status_id) {
        this.policy_status_id = policy_status_id;
    }

    public String getPolicy_status() {
        return policy_status;
    }

    public void setPolicy_status(String policy_status) {
        this.policy_status = policy_status;
    }

    public String getFamily_code_id() {
        return family_code_id;
    }

    public void setFamily_code_id(String family_code_id) {
        this.family_code_id = family_code_id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUpdated_by() {
        return updated_by;
    }

    public void setUpdated_by(String updated_by) {
        this.updated_by = updated_by;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public ArrayList<MaturityDatesListPojo> getMaturity_date() {
        return maturity_date;
    }

    public void setMaturity_date(ArrayList<MaturityDatesListPojo> maturity_date) {
        this.maturity_date = maturity_date;
    }

    public ArrayList<DocumentListPojo> getDocument() {
        return document;
    }

    public void setDocument(ArrayList<DocumentListPojo> document) {
        this.document = document;
    }

    public static class MaturityDatesListPojo implements Serializable {
        private String remark;

        private String maturity_date;

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getMaturity_date() {
            return maturity_date;
        }

        public void setMaturity_date(String maturity_date) {
            this.maturity_date = maturity_date;
        }
    }

    public static class DocumentListPojo implements Serializable {
        private String document;

        public String getDocument() {
            return document;
        }

        public void setDocument(String document) {
            this.document = document;
        }

        @Override
        public String toString() {
            return "ClassPojo [document = " + document + "]";
        }
    }


}
