package com.huiway.activiti.dto;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

public class ActHiTaskNodeDTO extends BaseDTO {

    private static final long serialVersionUID = 8194574818310660466L;
    
    private String taskId;
    
    private String taskDefKey;

    private String taskName;
    
    private String assignee;
    
    private String assigneeName;
    
    private Date startTime;
    
    private Date endTime;

    private Double hour;
    
    private String comment;
    
    private List attachments;
    
    private List pictures;
    
    private List documents;

    private boolean revocation = false;
    
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Double getHour() {
        return hour;
    }

    public void setHour(Double hour) {
        this.hour = hour;
    }
    
    public List getAttachments() {
        return attachments;
    }

    public void setAttachments(List attachments) {
        this.attachments = attachments;
    }

    public List getPictures() {
        return pictures;
    }

    public void setPictures(List pictures) {
        this.pictures = pictures;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public String getTaskDefKey() {
        return taskDefKey;
    }

    public void setTaskDefKey(String taskDefKey) {
        this.taskDefKey = taskDefKey;
    }

    public boolean isRevocation() {
        return revocation;
    }

    public void setRevocation(boolean revocation) {
        this.revocation = revocation;
    }

    public List getDocuments() {
        return documents;
    }

    public void setDocuments(List documents) {
        this.documents = documents;
    }

}
