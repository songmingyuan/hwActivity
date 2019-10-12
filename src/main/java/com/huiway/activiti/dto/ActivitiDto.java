package com.huiway.activiti.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ActivitiDto extends BaseDTO implements Serializable{

	private static final long serialVersionUID = -9182709895581136523L;
    
    @ApiModelProperty("流程定义ID")
    private String procId;
    
    @ApiModelProperty("流程定义key")
    private String procDefId;
    
    @ApiModelProperty("流程名称")
    private String processName;
    
    @ApiModelProperty("流程实例ID")
    private String procInstId;
    
	@ApiModelProperty("业务主键")
	public String businessKey;
	
}
