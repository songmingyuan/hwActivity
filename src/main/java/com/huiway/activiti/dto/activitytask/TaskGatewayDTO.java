package com.huiway.activiti.dto.activitytask;

import java.util.List;

import com.huiway.activiti.dto.BaseDTO;


public class TaskGatewayDTO extends BaseDTO {

    public TaskGatewayDTO(String name, String condition) {
        super();
        this.name = name;
        this.condition = condition;
    }

    public TaskGatewayDTO() {
        super();
    }

    /**
     * 
     */
    private static final long serialVersionUID = -5758427728588980301L;

    private boolean mutiSelectFlag = false;

    private String name;

    private String condition;

    private List<TaskGatewayDTO> items;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public boolean isMutiSelectFlag() {
        return mutiSelectFlag;
    }

    public void setMutiSelectFlag(boolean mutiSelectFlag) {
        this.mutiSelectFlag = mutiSelectFlag;
    }

    public List<TaskGatewayDTO> getItems() {
        return items;
    }

    public void setItems(List<TaskGatewayDTO> items) {
        this.items = items;
    }

}
