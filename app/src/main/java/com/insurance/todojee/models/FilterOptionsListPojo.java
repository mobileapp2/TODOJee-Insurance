package com.insurance.todojee.models;

public class FilterOptionsListPojo {

    private String filterName;
    private int filterIcon;
    private boolean isChecked;

    public FilterOptionsListPojo(String filterName, int filterIcon, boolean isChecked) {
        this.filterName = filterName;
        this.filterIcon = filterIcon;
        this.isChecked = isChecked;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public int getFilterIcon() {
        return filterIcon;
    }

    public void setFilterIcon(int filterIcon) {
        this.filterIcon = filterIcon;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
