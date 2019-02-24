package com.insurance.todojee.models;

public class PremiumPlanModel {

    private String end_date;

    private String amount;

    private String policies;

    private String created_at;

    private String is_default;

    private String whtasApp_msg;

    private String created_by;

    private String space;

    private String updated_at;

    private String sms;

    private String updated_by;

    private String id;

    private String customers;

    private String validity;

    private String promotional_plan;

    private String plan;

    private String plan_description;

    private String start_date;

    public String getEnd_date ()
    {
        return end_date;
    }

    public void setEnd_date (String end_date)
    {
        this.end_date = end_date;
    }

    public String getAmount ()
    {
        return amount;
    }

    public void setAmount (String amount)
    {
        this.amount = amount;
    }

    public String getPolicies ()
    {
        return policies;
    }

    public void setPolicies (String policies)
    {
        this.policies = policies;
    }

    public String getCreated_at ()
    {
        return created_at;
    }

    public void setCreated_at (String created_at)
    {
        this.created_at = created_at;
    }

    public String getIs_default ()
    {
        return is_default;
    }

    public void setIs_default (String is_default)
    {
        this.is_default = is_default;
    }

    public String getWhtasApp_msg ()
    {
        return whtasApp_msg;
    }

    public void setWhtasApp_msg (String whtasApp_msg)
    {
        this.whtasApp_msg = whtasApp_msg;
    }

    public String getCreated_by ()
    {
        return created_by;
    }

    public void setCreated_by (String created_by)
    {
        this.created_by = created_by;
    }

    public String getSpace ()
    {
        return space;
    }

    public void setSpace (String space)
    {
        this.space = space;
    }

    public String getUpdated_at ()
    {
        return updated_at;
    }

    public void setUpdated_at (String updated_at)
    {
        this.updated_at = updated_at;
    }

    public String getSms ()
    {
        return sms;
    }

    public void setSms (String sms)
    {
        this.sms = sms;
    }

    public String getUpdated_by ()
    {
        return updated_by;
    }

    public void setUpdated_by (String updated_by)
    {
        this.updated_by = updated_by;
    }

    public String getId ()
    {
        return id;
    }

    public void setId (String id)
    {
        this.id = id;
    }

    public String getCustomers ()
    {
        return customers;
    }

    public void setCustomers (String customers)
    {
        this.customers = customers;
    }

    public String getValidity ()
    {
        return validity;
    }

    public void setValidity (String validity)
    {
        this.validity = validity;
    }

    public String getPromotional_plan ()
    {
        return promotional_plan;
    }

    public void setPromotional_plan (String promotional_plan)
    {
        this.promotional_plan = promotional_plan;
    }

    public String getPlan ()
    {
        return plan;
    }

    public void setPlan (String plan)
    {
        this.plan = plan;
    }

    public String getPlan_description ()
    {
        return plan_description;
    }

    public void setPlan_description (String plan_description)
    {
        this.plan_description = plan_description;
    }

    public String getStart_date ()
    {
        return start_date;
    }

    public void setStart_date (String start_date)
    {
        this.start_date = start_date;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [end_date = "+end_date+", amount = "+amount+", policies = "+policies+", created_at = "+created_at+", is_default = "+is_default+", whtasApp_msg = "+whtasApp_msg+", created_by = "+created_by+", space = "+space+", updated_at = "+updated_at+", sms = "+sms+", updated_by = "+updated_by+", id = "+id+", customers = "+customers+", validity = "+validity+", promotional_plan = "+promotional_plan+", plan = "+plan+", plan_description = "+plan_description+", start_date = "+start_date+"]";
    }
}
