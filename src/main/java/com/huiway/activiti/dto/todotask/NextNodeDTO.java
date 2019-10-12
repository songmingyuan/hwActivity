package com.huiway.activiti.dto.todotask;

import java.util.Map;

import com.huiway.activiti.dto.BaseDTO;

public class NextNodeDTO extends BaseDTO {

    /**
     * 
     */
    private static final long serialVersionUID = -5758427728588980301L;

    Map<String,String> nextNode;

    public Map<String, String> getNextNode() {
        return nextNode;
    }

    public void setNextNode(Map<String, String> nextNode) {
        this.nextNode = nextNode;
    }


}
