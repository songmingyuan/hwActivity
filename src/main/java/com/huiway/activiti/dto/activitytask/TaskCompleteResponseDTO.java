package com.huiway.activiti.dto.activitytask;



import java.util.List;

import com.huiway.activiti.dto.BaseDTO;
import com.huiway.activiti.entity.BpmActHiActinst;
import com.huiway.activiti.entity.BpmActHiTaskinst;
import com.huiway.activiti.entity.BpmActRuTask;

/**
 * 任务完成返回 数据传输对象
 */
public class TaskCompleteResponseDTO extends BaseDTO {

    /**
     * 
     */
    private static final long serialVersionUID = 4517659110867097693L;

    private String procInstId;
    
    private String attachments;
    
    private String comment;
 
    private String pictures;

    private List<TaskAssigneeInfo> assigneeInfos;
    
    private BpmActHiTaskinst actHiTaskinst;
    
    private BpmActHiActinst actHiActinst;
    
    private List<BpmActRuTask> actRuTasks;
    
    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPictures() {
        return pictures;
    }

    public void setPictures(String pictures) {
        this.pictures = pictures;
    }
    
    
    
    public static class TaskAssigneeInfo {
        
        private String taskDefKey;
        
        private String taskName;

        public String getTaskDefKey() {
            return taskDefKey;
        }

        public void setTaskDefKey(String taskDefKey) {
            this.taskDefKey = taskDefKey;
        }

        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }
        
    }

    public List<TaskAssigneeInfo> getAssigneeInfos() {
        return assigneeInfos;
    }

    public void setAssigneeInfos(List<TaskAssigneeInfo> assigneeInfos) {
        this.assigneeInfos = assigneeInfos;
    }

    public String getProcInstId() {
        return procInstId;
    }

    public void setProcInstId(String procInstId) {
        this.procInstId = procInstId;
    }

    public BpmActHiTaskinst getActHiTaskinst() {
        return actHiTaskinst;
    }

    public void setActHiTaskinst(BpmActHiTaskinst actHiTaskinst) {
        this.actHiTaskinst = actHiTaskinst;
    }

    public List<BpmActRuTask> getActRuTasks() {
        return actRuTasks;
    }

    public void setActRuTasks(List<BpmActRuTask> actRuTasks) {
        this.actRuTasks = actRuTasks;
    }

    public BpmActHiActinst getActHiActinst() {
        return actHiActinst;
    }

    public void setActHiActinst(BpmActHiActinst actHiActinst) {
        this.actHiActinst = actHiActinst;
    }

}
