package com.pygabo.rabbfinance;

public class TodoItem {
    public int id;
    public String description;
    public boolean ready;

    public TodoItem(int id, String description, boolean ready){
        this.id = id;
        this.description = description;
        this.ready = ready;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }
}
