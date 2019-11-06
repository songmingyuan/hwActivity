package com.huiway.activiti.dto.activitytask;


import java.util.List;
import java.util.Map;

import com.huiway.activiti.dto.BaseDTO;

/**
 * 任务完成 数据传输对象
 */
public class TaskCompleteDTO extends BaseDTO {

    /**
     * 
     */
    private static final long serialVersionUID = 4517659110867097693L;

    private String orgId;
    
    private String projectId;
    
    private List<String> attachFiles;
    
    private String comment;
 
    private List<String> pictures;
    
    private Map<String,Object> command;
    
    private String operator;

    public List<String> getAttachFiles() {
        return attachFiles;
    }

    public void setAttachFiles(List<String> attachFiles) {
        this.attachFiles = attachFiles;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<String> getPictures() {
        return pictures;
    }

    public void setPictures(List<String> pictures) {
        this.pictures = pictures;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Map<String, Object> getCommand() {
        return command;
    }

    public void setCommand(Map<String, Object> command) {
        this.command = command;
    }

}
