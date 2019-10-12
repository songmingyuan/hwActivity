package com.huiway.activiti.dto.activitytask;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import com.google.common.base.MoreObjects;
import com.huiway.activiti.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@ApiModel(value="认领任务入参")
@Data
@EqualsAndHashCode(callSuper = true)
public class ClaimRequestDTO extends BaseDTO implements Serializable{

	private static final long serialVersionUID = 8399484619739357303L;

	@ApiModelProperty("任务Id")
	@NotBlank(message = "taskId不能为空")
	private String taskId;
	
	@ApiModelProperty("受理人")
	private String assignee;
	
	@Override
	public String toString() {
		 return MoreObjects.toStringHelper(this).omitNullValues()
	                .add("tenantId", tenantId)
	                .add("userId", userId)
	                .add("taskId", taskId)
	                .toString();
	}
}
