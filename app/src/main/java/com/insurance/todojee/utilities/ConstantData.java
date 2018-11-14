package com.insurance.todojee.utilities;

import com.insurance.todojee.models.ClientMainListPojo;
import com.insurance.todojee.models.FamilyCodePojo;
import com.insurance.todojee.models.FrequencyListPojo;
import com.insurance.todojee.models.InsuranceTypeListPojo;
import com.insurance.todojee.models.PolicyStatusListPojo;
import com.insurance.todojee.models.PolicyTypeListPojo;

import java.util.ArrayList;

public class ConstantData {

    public static ConstantData _instance;
    private ArrayList<FamilyCodePojo> familyCodeList;
    private ArrayList<ClientMainListPojo> clientList;
    private ArrayList<InsuranceTypeListPojo> insuranceTypeList;
    private ArrayList<PolicyStatusListPojo> policyStatusList;
    private ArrayList<FrequencyListPojo> frequencyList;
    private ArrayList<PolicyTypeListPojo> policyTypeList;

    private ConstantData() {
    }

    public static ConstantData getInstance() {
        if (_instance == null) {
            _instance = new ConstantData();
        }
        return _instance;
    }

    public static ConstantData get_instance() {
        return _instance;
    }

    public static void set_instance(ConstantData _instance) {
        ConstantData._instance = _instance;
    }

    public ArrayList<FamilyCodePojo> getFamilyCodeList() {
        return familyCodeList;
    }

    public void setFamilyCodeList(ArrayList<FamilyCodePojo> familyCodeList) {
        this.familyCodeList = familyCodeList;
    }

    public ArrayList<ClientMainListPojo> getClientList() {
        return clientList;
    }

    public void setClientList(ArrayList<ClientMainListPojo> clientList) {
        this.clientList = clientList;
    }

    public ArrayList<InsuranceTypeListPojo> getInsuranceTypeList() {
        return insuranceTypeList;
    }

    public void setInsuranceTypeList(ArrayList<InsuranceTypeListPojo> insuranceTypeList) {
        this.insuranceTypeList = insuranceTypeList;
    }

    public ArrayList<PolicyStatusListPojo> getPolicyStatusList() {
        return policyStatusList;
    }

    public void setPolicyStatusList(ArrayList<PolicyStatusListPojo> policyStatusList) {
        this.policyStatusList = policyStatusList;
    }

    public ArrayList<FrequencyListPojo> getFrequencyList() {
        return frequencyList;
    }

    public void setFrequencyList(ArrayList<FrequencyListPojo> frequencyList) {
        this.frequencyList = frequencyList;
    }

    public ArrayList<PolicyTypeListPojo> getPolicyTypeList() {
        return policyTypeList;
    }

    public void setPolicyTypeList(ArrayList<PolicyTypeListPojo> policyTypeList) {
        this.policyTypeList = policyTypeList;
    }
}