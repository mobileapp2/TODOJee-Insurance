package com.insurance.todojee.models;

import java.util.ArrayList;

public class PremiumPlanPojo {

    private ArrayList<PremiumPlanModel> result;

    private String type;

    private String message;

    public ArrayList<PremiumPlanModel> getResult ()
    {
        return result;
    }

    public void setResult (ArrayList<PremiumPlanModel> result)
    {
        this.result = result;
    }

    public String getType ()
    {
        return type;
    }

    public void setType (String type)
    {
        this.type = type;
    }

    public String getMessage ()
    {
        return message;
    }

    public void setMessage (String message)
    {
        this.message = message;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [result = "+result+", type = "+type+", message = "+message+"]";
    }
}
