package com.huiway.activiti.dto.activitytask;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import com.google.common.base.MoreObjects;
import com.huiway.activiti.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@ApiModel(value="获取流程图入参")
@Data
@EqualsAndHashCode(callSuper = true)
public class DiagramRequestDTO extends BaseDTO implements Serializable{

	private static final long serialVersionUID = 8399484619739357303L;

	@ApiModelProperty("流程实例ID")
	@NotBlank(message = "procInstId不能为空")
	private String procInstId;
	
	@Override
	public String toString() {
		 return MoreObjects.toStringHelper(this).omitNullValues()
	                .add("tenantId", tenantId)
	                .add("userId", userId)
	                .add("procInstId", procInstId)
	                .toString();
	}
}
