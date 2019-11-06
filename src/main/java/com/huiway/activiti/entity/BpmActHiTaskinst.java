package com.huiway.activiti.entity;

import java.math.BigInteger;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huiway.activiti.dto.BaseDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;


/**
 * The persistent class for the act_hi_taskinst database table.
 * 
 */

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Accessors(chain = true)
@TableName("ACT_HI_TACKINST")
public class BpmActHiTaskinst extends BaseDTO {
    private static final long serialVersionUID = 1L;

    @TableId(value="ID_")
    private String id;

    @TableId(value="ASSIGNEE_")
    private String assignee;

    @TableId(value="CATEGORY_")
    private String category;

    @TableId(value="CLAIM_TIME_")
    private Date claimTime;

    @TableId(value="DELETE_REASON_")
    private String deleteReason;

    @TableId(value="DESCRIPTION_")
    private String description;

    @TableId(value="DUE_DATE_")
    private Date dueDate;

    @TableId(value="DURATION_")
    private BigInteger duration;

    @TableId(value="END_TIME_")
    private Date endTime;

    @TableId(value="EXECUTION_ID_")
    private String executionId;

    @TableId(value="FORM_KEY_")
    private String formKey;

    @TableId(value="NAME_")
    private String name;

    @TableId(value="OWNER_")
    private String owner;

    @TableId(value="PARENT_TASK_ID_")
    private String parentTaskId;

    @TableId(value="PRIORITY_")
    private int priority;

    @TableId(value="PROC_DEF_ID_")
    private String procDefId;

    @TableId(value="PROC_INST_ID_")
    private String procInstId;

    @TableId(value="START_TIME_")
    private Date startTime;

    @TableId(value="TASK_DEF_KEY_")
    private String taskDefKey;

    @TableId(value="TENANT_ID_")
    private String tenantId;

    public BpmActHiTaskinst() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAssignee() {
        return this.assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getClaimTime() {
        return this.claimTime;
    }

    public void setClaimTime(Date claimTime) {
        this.claimTime = claimTime;
    }

    public String getDeleteReason() {
        return this.deleteReason;
    }

    public void setDeleteReason(String deleteReason) {
        this.deleteReason = deleteReason;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDueDate() {
        return this.dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public BigInteger getDuration() {
        return this.duration;
    }

    public void setDuration(BigInteger duration) {
        this.duration = duration;
    }

    public Date getEndTime() {
        return this.endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getExecutionId() {
        return this.executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getFormKey() {
        return this.formKey;
    }

    public void setFormKey(String formKey) {
        this.formKey = formKey;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getParentTaskId() {
        return this.parentTaskId;
    }

    public void setParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getProcDefId() {
        return this.procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }

    public String getProcInstId() {
        return this.procInstId;
    }

    public void setProcInstId(String procInstId) {
        this.procInstId = procInstId;
    }

    public Date getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getTaskDefKey() {
        return this.taskDefKey;
    }

    public void setTaskDefKey(String taskDefKey) {
        this.taskDefKey = taskDefKey;
    }

    public String getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

}