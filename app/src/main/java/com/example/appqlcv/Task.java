package com.example.appqlcv;

public class Task {
    private String id;
    private String title;
    private String description;
    private String deadline;

    public Task() {
        // Hàm khởi tạo mặc định rỗng (cần cho Firebase)
    }

    public Task(String id, String title, String description, String deadline) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
    }

    // Thêm các getter và setter cho các thuộc tính của công việc

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }
}
