package com.huiway.activiti.dto.activitytask;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import com.google.common.base.MoreObjects;
import com.huiway.activiti.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@ApiModel(value="流程启动入参")
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateRequestDTO extends BaseDTO implements Serializable{

	private static final long serialVersionUID = 8399484619739357303L;

	@ApiModelProperty("流程定义key")
	@NotBlank(message = "procDefId不能为空")
	private String procDefId;
	
	@ApiModelProperty("流程业务标识")
	@NotBlank(message = "businessKey不能为空")
	private String businessKey;
	
	@ApiModelProperty("受理人")
	@NotBlank(message = "assignee不能为空")
	private String assignee;
	
	@Override
	public String toString() {
		 return MoreObjects.toStringHelper(this).omitNullValues()
	                .add("tenantId", tenantId)
	                .add("userId", userId)
	                .add("procDefId", procDefId)
	                .add("businessKey", businessKey)
	                .toString();
	}
}
