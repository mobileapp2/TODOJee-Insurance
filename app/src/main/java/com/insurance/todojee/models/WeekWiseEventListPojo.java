package com.insurance.todojee.models;

import java.util.ArrayList;

public class WeekWiseEventListPojo {

    private String Date;

    private ArrayList<EventListPojo> eventListPojos;

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public ArrayList<EventListPojo> getEventListPojos() {
        return eventListPojos;
    }

    public void setEventListPojos(ArrayList<EventListPojo> eventListPojos) {
        this.eventListPojos = eventListPojos;
    }

    public static class EventListPojo {

        private String id;

        private String status;

        private String description;

        private String date;

        private String client_id;

        public boolean isChecked;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getClient_id() {
            return client_id;
        }

        public void setClient_id(String client_id) {
            this.client_id = client_id;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }
    }

}
