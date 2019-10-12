package com.huiway.activiti.dto;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import springfox.documentation.annotations.ApiIgnore;

@Data
public class BaseDTO {

	@ApiModelProperty("租户ID/系统ID/项目ID")
	@NotBlank(message = "tenantId不能为空")
	public String tenantId;
	
	@ApiModelProperty("登录用户id")
	@NotBlank(message = "userId不能为空")
	public String userId;
	
}
