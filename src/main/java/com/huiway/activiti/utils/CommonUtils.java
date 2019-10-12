package com.huiway.activiti.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.springframework.validation.BindingResult;

import com.huiway.activiti.common.bean.RestResponse;
import com.huiway.activiti.common.constant.CodeConstant;

public class CommonUtils {
	
    public static String getImageStr(InputStream in) {
        byte[] data = null;
        // 读取图片字节数组
        try {
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        // 对字节数组Base64编码 返回Base64编码过的字节数组字符串
        Base64 base64 = new Base64();
        return new String(base64.encode(data));
    }
    
    public static RestResponse initErrors(BindingResult results) {
    	RestResponse restResponse = new RestResponse();
		restResponse.setRtnCode(CodeConstant.FAIL);
		restResponse.setMessage(results.getFieldError().getDefaultMessage());
		return restResponse;
    }
    
}
