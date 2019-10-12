package com.huiway.activiti.dto;

import java.io.Serializable;

import com.huiway.activiti.dto.BaseDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ActTaskNodeDTO extends BaseDTO implements Serializable{

	private static final long serialVersionUID = 7646807787369511662L;

	private String taskCategory;
    
    private String taskDefKey;
    
    private String taskNodeName;
    
    private int orderNo;

}
