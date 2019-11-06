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
 * The persistent class for the act_hi_varinst database table.
 * 
 */

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Accessors(chain = true)
@TableName("ACT_HI_VARINST")
public class BpmActHiVarinst extends BaseDTO {
    private static final long serialVersionUID = 1L;

    @TableId(value="ID_")
    private String id;

    @TableId(value="BYTEARRAY_ID_")
    private String bytearrayId;

    @TableId(value="CREATE_TIME_")
    private Date createTime;

    private double double_;

    @TableId(value="EXECUTION_ID_")
    private String executionId;

    @TableId(value="LAST_UPDATED_TIME_")
    private Date lastUpdatedTime;

    private BigInteger long_;

    @TableId(value="value_")
    private String value;

    @TableId(value="PROC_INST_ID_")
    private String procInstId;

    @TableId(value="REV_")
    private int rev;

    @TableId(value="TASK_ID_")
    private String taskId;

    @TableId(value="TEXT_")
    private String text;

    @TableId(value="TEXT2_")
    private String text2;

    @TableId(value="VAR_TYPE_")
    private String varType;

    public BpmActHiVarinst() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBytearrayId() {
        return this.bytearrayId;
    }

    public void setBytearrayId(String bytearrayId) {
        this.bytearrayId = bytearrayId;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public double getDouble_() {
        return this.double_;
    }

    public void setDouble_(double double_) {
        this.double_ = double_;
    }

    public String getExecutionId() {
        return this.executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public Date getLastUpdatedTime() {
        return this.lastUpdatedTime;
    }

    public void setLastUpdatedTime(Date lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public BigInteger getLong_() {
        return this.long_;
    }

    public void setLong_(BigInteger long_) {
        this.long_ = long_;
    }

    public String getvalue() {
        return this.value;
    }

    public void setvalue(String value) {
        this.value = value;
    }

    public String getProcInstId() {
        return this.procInstId;
    }

    public void setProcInstId(String procInstId) {
        this.procInstId = procInstId;
    }

    public int getRev() {
        return this.rev;
    }

    public void setRev(int rev) {
        this.rev = rev;
    }

    public String getTaskId() {
        return this.taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText2() {
        return this.text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    public String getVarType() {
        return this.varType;
    }

    public void setVarType(String varType) {
        this.varType = varType;
    }

}