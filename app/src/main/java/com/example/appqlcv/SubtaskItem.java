package com.example.appqlcv;

public class SubtaskItem {
    private String taskId;
    private String subtaskId;
    private String description;
    private String attachmentUrl;

    public SubtaskItem() {
        // Required default constructor for Firebase
    }

    public SubtaskItem(String taskId, String subtaskId, String description, String attachmentUrl) {
        this.taskId = taskId;
        this.subtaskId = subtaskId;
        this.description = description;
        this.attachmentUrl = attachmentUrl;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    public String getSubTaskId() {
        return subtaskId;
    }

    public void setSubTaskId(String subtaskId) {
        this.subtaskId = subtaskId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }
}