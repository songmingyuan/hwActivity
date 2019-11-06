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
 * The persistent class for the act_hi_actinst database table.
 * 
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Accessors(chain = true)
@TableName("ACT_HI_ACTINST")
public class BpmActHiActinst extends BaseDTO {
    private static final long serialVersionUID = 1L;

    @TableId(value="ID_")
    private String id;

    @TableId(value="ACT_ID_")
    private String actId;

    @TableId(value="ACT_NAME_")
    private String actName;

    @TableId(value="ACT_TYPE_")
    private String actType;

    @TableId(value="ASSIGNEE_")
    private String assignee;

    @TableId(value="CALL_PROC_INST_ID_")
    private String callProcInstId;

    @TableId(value="DELETE_REASON_")
    private String deleteReason;

    @TableId(value="DURATION_")
    private BigInteger duration;

    @TableId(value="END_TIME_")
    private Date endTime;

    @TableId(value="EXECUTION_ID_")
    private String executionId;

    @TableId(value="PROC_DEF_ID_")
    private String procDefId;

    @TableId(value="PROC_INST_ID_")
    private String procInstId;

    @TableId(value="START_TIME_")
    private Date startTime;

    @TableId(value="TASK_ID_")
    private String taskId;

    @TableId(value="TENANT_ID_")
    private String tenantId;

    public BpmActHiActinst() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActId() {
        return this.actId;
    }

    public void setActId(String actId) {
        this.actId = actId;
    }

    public String getActName() {
        return this.actName;
    }

    public void setActName(String actName) {
        this.actName = actName;
    }

    public String getActType() {
        return this.actType;
    }

    public void setActType(String actType) {
        this.actType = actType;
    }

    public String getAssignee() {
        return this.assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getCallProcInstId() {
        return this.callProcInstId;
    }

    public void setCallProcInstId(String callProcInstId) {
        this.callProcInstId = callProcInstId;
    }

    public String getDeleteReason() {
        return this.deleteReason;
    }

    public void setDeleteReason(String deleteReason) {
        this.deleteReason = deleteReason;
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

    public String getTaskId() {
        return this.taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

}