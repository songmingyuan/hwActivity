package com.huiway.activiti.dto.model;

import java.io.Serializable;

import com.huiway.activiti.dto.BaseDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ModelDiagramDTO extends BaseDTO implements Serializable{
	
	private static final long serialVersionUID = -5726179082654239536L;
	private String diagramResource;
    private String diagramResourceName;

}
