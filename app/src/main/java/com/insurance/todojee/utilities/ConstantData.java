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
    private ArrayList<ClientMainListPojo.ClientFamilyDetailsPojo> clientFamilyList;
    private ArrayList<ClientMainListPojo.ClientFirmDetailsPojo> clientFirmList;
    private ArrayList<InsuranceTypeListPojo> insuranceTypeList;
    private ArrayList<PolicyStatusListPojo> policyStatusList;
    private ArrayList<FrequencyListPojo> frequencyList;
    private ArrayList<PolicyTypeListPojo.Policy_details> policyTypeList;

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

    public ArrayList<ClientMainListPojo.ClientFamilyDetailsPojo> getClientFamilyList() {
        return clientFamilyList;
    }

    public void setClientFamilyList(ArrayList<ClientMainListPojo.ClientFamilyDetailsPojo> clientFamilyList) {
        this.clientFamilyList = clientFamilyList;
    }

    public ArrayList<ClientMainListPojo.ClientFirmDetailsPojo> getClientFirmList() {
        return clientFirmList;
    }

    public void setClientFirmList(ArrayList<ClientMainListPojo.ClientFirmDetailsPojo> clientFirmList) {
        this.clientFirmList = clientFirmList;
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

    public ArrayList<PolicyTypeListPojo.Policy_details> getPolicyTypeList() {
        return policyTypeList;
    }

    public void setPolicyTypeList(ArrayList<PolicyTypeListPojo.Policy_details> policyTypeList) {
        this.policyTypeList = policyTypeList;
    }
}