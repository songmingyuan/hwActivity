package com.huiway.activiti.dto.activitytask;

import java.io.Serializable;

import com.google.common.base.MoreObjects;
import com.huiway.activiti.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@ApiModel(value="获取下一个任务节点入参")
@Data
@EqualsAndHashCode(callSuper = true)
public class DiagramResponseDTO extends BaseDTO implements Serializable{

	private static final long serialVersionUID = 8399484619739357303L;

	@ApiModelProperty("图片资源")
	private String diagramResource;
	
	@ApiModelProperty("资源名称")
	private String diagramResourceName;
	
	
	@Override
	public String toString() {
		 return MoreObjects.toStringHelper(this).omitNullValues()
	                .add("tenantId", tenantId)
	                .add("userId", userId)
	                .add("diagramResource", diagramResource)
	                .add("diagramResourceName", diagramResourceName)
	                .toString();
	}
}
