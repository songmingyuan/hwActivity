package com.huiway.activiti.dto.todotask;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import com.google.common.base.MoreObjects;
import com.huiway.activiti.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import springfox.documentation.annotations.ApiIgnore;

@ApiModel(value="获取下一个任务节点入参")
@Data
@EqualsAndHashCode(callSuper = true)
public class NextTaskNodeRequestDTO extends BaseDTO implements Serializable{

	private static final long serialVersionUID = 8399484619739357303L;

	@ApiModelProperty("节点ID")
	@NotBlank(message = "taskId不能为空")
	private String taskId;
	
	@Override
	public String toString() {
		 return MoreObjects.toStringHelper(this).omitNullValues()
	                .add("tenantId", tenantId)
	                .add("userId", userId)
	                .add("taskId", taskId)
	                .toString();
	}
}
