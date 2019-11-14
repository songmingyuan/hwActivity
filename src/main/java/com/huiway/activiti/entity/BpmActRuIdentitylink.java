package com.huiway.activiti.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huiway.activiti.dto.BaseDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * The persistent class for the act_ru_identitylink database table.
 * 
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Accessors(chain = true)
@TableName("ACT_RU_IDENTITYLINK")
public class BpmActRuIdentitylink extends BaseDTO {
    private static final long serialVersionUID = 1L;

    @TableId(value="ID_")
    private String id;

    @TableId(value="GROUP_ID_")
    private String groupId;

    @TableId(value="REV_")
    private int rev;

    @TableId(value="TYPE_")
    private String type;

    @TableId(value="USER_ID_")
    private String userId;
    
    @TableId(value="TASK_ID_")
    private String taskId;
    
    @TableId(value="PROC_INST_ID_")
    private String procInstId;
    
    @TableId(value="PROC_DEF_ID")
    private String procDefId;

    public BpmActRuIdentitylink() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getRev() {
        return this.rev;
    }

    public void setRev(int rev) {
        this.rev = rev;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}