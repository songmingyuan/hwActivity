package com.huiway.activiti.common.listener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.HistoryService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.history.HistoricTaskInstance;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huiway.activiti.utils.SpringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApplyNoExecutionListener implements ExecutionListener{
	@Override
	 public void notify(DelegateExecution execution) {
		log.info("进入ApplyNoExecutionListener执行监听器方法------------");
		HistoryService historyService=SpringUtil.getObject(HistoryService.class);
		FlowElement e= execution.getCurrentFlowElement();
		List<String> strList=new ArrayList<>();
		
		if(e instanceof UserTask){
			UserTask userTask = (UserTask) e;
			strList=userTask.getCandidateUsers();
		}
		 String procInstId=execution.getProcessInstanceId();
		String taskDefKey = execution.getCurrentActivityId();
		List<HistoricTaskInstance> list = historyService // 历史相关Service
				.createHistoricTaskInstanceQuery() // 创建历史任务实例查询
				.processInstanceId(procInstId).finished()
				// 用流程实例id查询
				.orderByHistoricTaskInstanceEndTime().desc().list();
		
		String auditUserIds="";
		for(HistoricTaskInstance hti:list){
			String taskDefKey2= hti.getTaskDefinitionKey();
			if(taskDefKey2.equals(taskDefKey)){
				auditUserIds=hti.getAssignee();
				break;
			}
		}
		
		Map<String, Object>  variables =execution.getVariables();
		String auditActivity="";
		if(!strList.isEmpty()){
			auditActivity=strList.get(0).replace("${", "").replace("}", "");
		}
		if(!StringUtils.isEmpty(auditActivity)){
			variables.put(auditActivity, auditUserIds);
		}
        execution.setVariables(variables);
	 }
	
	private String httpReuestData(String url, String param) {

		URL getUrl;
		OutputStream out = null;
		int responseCode = 0;
		HttpURLConnection connection = null;
		try {
			getUrl = new URL(url);
			connection = (HttpURLConnection) getUrl.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-Type", "applicstion/json;charset=utf-8");
			out = connection.getOutputStream();
			out.write(param.getBytes());
			out.flush();
			out.close();

			responseCode = connection.getResponseCode();
			BufferedReader streamReader = new BufferedReader(
					new InputStreamReader(connection.getInputStream(), "UTF-8"));
			String result = streamReader.readLine();

			return result;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			try {
				if (connection != null) {
					connection.disconnect();
				}
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			}

		}
		return String.valueOf(responseCode);
	}
}
