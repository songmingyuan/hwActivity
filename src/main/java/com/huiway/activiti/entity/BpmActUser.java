package com.huiway.activiti.entity;

import java.io.Serializable;

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
@TableName("ACT_ID_INFO")
public class BpmActUser implements Serializable{

	private static final long serialVersionUID = 8774884204835870369L;

	@TableId(value = "ID_")
    private String id;

    @TableField("REV_")
    private int rev;

    @TableField("FIRST_")
    private String first;

    @TableField("LAST_")
    private String last;

    @TableField("EMAIL_")
    private String email;

    @TableField("PWD_")
    private String pwd;

    @TableField("PICTURE_ID_")
    private String pictureId;

}