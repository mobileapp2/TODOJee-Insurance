package com.insurance.todojee.utilities;

public class ParamsPojo {

    private String Param_Key;
    private String Param_Value;

    public ParamsPojo(String key, String value) {
        this.Param_Key = key;
        this.Param_Value = value;
    }

    public String getParam_Key() {
        return Param_Key;
    }

    public void setParam_Key(String param_Key) {
        Param_Key = param_Key;
    }

    public String getParam_Value() {
        return Param_Value;
    }

    public void setParam_Value(String param_Value) {
        Param_Value = param_Value;
    }
}
