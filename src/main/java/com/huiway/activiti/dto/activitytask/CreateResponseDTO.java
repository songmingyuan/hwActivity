package com.huiway.activiti.dto.activitytask;

import java.io.Serializable;
import java.util.Date;

import com.huiway.activiti.dto.ActivitiDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CreateResponseDTO extends ActivitiDto implements Serializable{

	private static final long serialVersionUID = -393017482769285109L;
	
    @ApiModelProperty("流程启动时间")
    private Date startDate;
    
    @ApiModelProperty("流程是否暂停")
    private boolean suspended;
    
    @ApiModelProperty("下一节点Id")
    private String taskId;
    
    @ApiModelProperty("下一节点名称")
    private String taskName;
    
    @ApiModelProperty("下一节点处理权限")
    private String taskCategory;

}
