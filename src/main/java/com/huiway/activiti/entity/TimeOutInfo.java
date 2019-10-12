package com.huiway.activiti.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Accessors(chain = true)
@TableName("TB_TIMEOUT_INFO")
public class TimeOutInfo implements Serializable{
	
	private static final long serialVersionUID = -7104267459357670097L;
	
    @TableField("ID")
    private int id;
	
    @TableField("TASKID")
    private String taskId;
	
    @TableField("ASSIGNEE")
    private String assignee;

    @TableField("TIMEOUT")
    private String timeOut;

    @TableField("TYPE")
    private String type;
    
    @TableField("CREATEDATE")
    private String createDate;
    
    @TableField("STATE")
    private String state;
    
    @TableField(exist = false)
    private String email;
    
}

