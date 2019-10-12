package com.huiway.activiti.common.bean;

import java.io.Serializable;

import com.huiway.activiti.common.constant.CodeConstant;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import springfox.documentation.annotations.ApiIgnore;

@Data
public class RestResponse implements Serializable {

    private static final long serialVersionUID = 5238209809300311580L;

    @ApiModelProperty("状态码")
    private String rtnCode;

    @ApiModelProperty("信息描述")
    private String message;
    
    @ApiModelProperty("可以转化为map")
    private Object bean;
    
    @ApiModelProperty("可以转化为List<map>")
    private Object beans;
    

    /**
     * 构造方法
     */
    public RestResponse() {
        super();
        this.rtnCode = CodeConstant.SUCCESS;
    }

    /**
     * 构造方法
     *
     * @param code    状态码
     * @param message 状态描述
     */
    public RestResponse(String rtnCode, String message) {
        super();
        this.rtnCode = rtnCode;
        this.message = message;
    }
    
    public static RestResponse instance(String rtnCode, String message) {
        return new RestResponse(rtnCode,message);
    }
}
