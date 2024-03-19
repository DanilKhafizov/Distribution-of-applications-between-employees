package com.kadullin.distributionofrequests.models;
public class Task {
    private final String title;
    private final String createTime;
    private final String status;
    private String employee;
    private String id;
    public Task(String title, String createTime, String status) {
        this.title = title;
        this.createTime = createTime;
        this.status = status;
    }
    public Task(String title, String createTime, String status, String employee) {
        this.title = title;
        this.createTime = createTime;
        this.status = status;
        this.employee = employee;
    }
    public String getTitle() {
        return title;
    }
    public String getCreateTime() {
        return createTime;
    }
    public String getStatus() {
        return status;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getId(){return id;}
    public String getEmployee() {
        return employee;
    }
}
