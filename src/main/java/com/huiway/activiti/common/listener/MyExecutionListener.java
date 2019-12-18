package  com.huiway.activiti.common.listener;

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
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.history.HistoricTaskInstance;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huiway.activiti.utils.SpringUtil;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class MyExecutionListener implements ExecutionListener  {

	/**
	 * 调用接口的文档
	 */
	private String url = "http://127.0.0.1:8088/hwsystem/task/audit/person";
	
	@Override
	 public void notify(DelegateExecution execution) {
		log.info("进入MyExecutionListener执行监听器方法------------");
		HistoryService historyService=SpringUtil.getObject(HistoryService.class);
		FlowElement e= execution.getCurrentFlowElement();
		List<String> strList=new ArrayList<>();
		
		if(e instanceof UserTask){
			UserTask userTask = (UserTask) e;
			strList=userTask.getCandidateUsers();
		}
        String procInstId=execution.getProcessInstanceId();
		String procDefId = execution.getProcessDefinitionId();
		String taskDefKey = execution.getCurrentActivityId();
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("procInstId", procInstId);
		paramMap.put("procDefId", procDefId);
		paramMap.put("nodeCode", taskDefKey);
		String paramJson = JSON.toJSONString(paramMap);
		String auditUserIds = "";
		String code = httpReuestData(url, paramJson);
		if (!StringUtils.isEmpty(code) && !"0".equals(code)) {
			JSONObject jsonParam = JSONObject.parseObject(code);
			if (jsonParam != null) {
				String rtnCode = jsonParam.getString("rtnCode");
				if ("1".equals(rtnCode)) {
					String lists = jsonParam.getString("beans");
					List li = JSONObject.parseArray(lists, Map.class);
					List<Map<String, Object>> list = li;
					for (int i = 0; i < list.size(); i++) {
						Map<String, Object> map = list.get(i);
						String userName = map.get("userName") == null ? "" : map.get("userName").toString();
						
							if (i == list.size() - 1) {
								auditUserIds = auditUserIds + userName;
							} else {
								auditUserIds = auditUserIds+userName + "," ;
							}
					

					}
				}
			}
		}
		if(StringUtils.isEmpty(auditUserIds)){
			List<HistoricTaskInstance> halist = historyService // 历史相关Service
					.createHistoricTaskInstanceQuery() // 创建历史任务实例查询
					.processInstanceId(procInstId).finished()
					.orderByHistoricTaskInstanceEndTime().desc().list();
			if(!halist.isEmpty()){
				HistoricTaskInstance hai=halist.get(0);
				auditUserIds=hai.getAssignee();
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