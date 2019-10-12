package com.huiway.activiti.common.bean;

import lombok.Data;

import javax.validation.Valid;
import java.io.Serializable;

@Data
public class RestRequest<T> implements Serializable {

    private static final long serialVersionUID = -8963393665876763035L;

    /**
     * 请求消息体(泛型)
     */
    @Valid
    private T body = null;

    /**
     * 构造方法
     */
    public RestRequest() {
        super();
    }

    /**
     * 构造方法
     *
     * @Param body 请求消息体(泛型)
     */
    private RestRequest(T body) {
        super();
        this.body = body;
    }

    /**
     * 静态构造方法
     */
    public static RestRequest instance() {
        return new RestRequest();
    }

    /**
     * 静态构造方法
     *
     * @Param body 请求消息体(泛型)
     */
    public static <T> RestRequest<T> instance(T body) {
        return new RestRequest<T>(body);
    }
}
