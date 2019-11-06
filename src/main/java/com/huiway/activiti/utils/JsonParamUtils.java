package com.huiway.activiti.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;

public class JsonParamUtils {
    /**
     * 功能描述:通过request来获取到json数据<br/>
     * @param request
     * @return
     */
	 public static JSONObject getJSONParam(HttpServletRequest req) {
    	JSONObject jsonParam = null;
    	try {
    		// 获取输入流
    		BufferedReader streamReader = new BufferedReader(new InputStreamReader(req.getInputStream(), "UTF-8"));

    		// 写入数据到Stringbuilder
    		StringBuilder sb = new StringBuilder();
    		String line = null;
    		while ((line = streamReader.readLine()) != null) {
    			sb.append(line);
    		}
    		jsonParam = JSONObject.parseObject(sb.toString());
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return jsonParam;
	}
}
