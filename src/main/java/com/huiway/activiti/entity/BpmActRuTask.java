package com.huiway.activiti.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Accessors(chain = true)
@TableName("ACT_RU_TASK")
public class BpmActRuTask implements Serializable{

	private static final long serialVersionUID = 7292875544516856331L;

    @TableId(value = "ID_")
    private String id;

    @TableField("ASSIGNEE_")
    private String assignee;

    @TableField("CATEGORY_")
    private String category;

    @TableField("CLAIM_TIME_")
    private Date claimTime;

    @TableField("CREATE_TIME_")
    private Timestamp createTime;

    @TableField("DELEGATION_")
    private String delegation;

    @TableField("DESCRIPTION_")
    private String description;

    @TableField("DUE_DATE_")
    private Date dueDate;

    @TableField("FORM_KEY_")
    private String formKey;

    @TableField("NAME_")
    private String name;

    @TableField("OWNER_")
    private String owner;

    @TableField("PARENT_TASK_ID_")
    private String parentTaskId;

    @TableField("PRIORITY_")
    private int priority;

    @TableField("REV_")
    private int rev;

    @TableField("SUSPENSION_STATE_")
    private int suspensionState;

    @TableField("TASK_DEF_KEY_")
    private String taskDefKey;

    @TableField("TENANT_ID_")
    private String tenantId;
    
    @TableField("PROC_INST_ID_")
    private String procInstId;

    @TableField("PROC_DEF_ID_")
    private String procDefId;

}