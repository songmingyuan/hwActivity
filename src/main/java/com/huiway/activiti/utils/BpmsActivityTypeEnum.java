package com.huiway.activiti.utils;

public enum BpmsActivityTypeEnum {

    START_EVENT("startEvent", "开始事件"),
    END_EVENT("endEvent", "结束事件"),
    USER_TASK("userTask", "用户任务"),
    EXCLUSIVE_GATEWAY("exclusiveGateway", "排他网关"),
    PARALLEL_GATEWAY("parallelGateway", "并行网关"),
    INCLUSIVE_GATEWAY("inclusiveGateway", "包含网关");

    private String type;
    private String displayName;

    private BpmsActivityTypeEnum(String type, String displayName) {
        this.type = type;
        this.displayName = displayName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

}
