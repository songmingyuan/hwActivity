package com.huiway.activiti.controller;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.huiway.activiti.common.bean.RestResponse;
import com.huiway.activiti.dto.ActivitiDto;
import com.huiway.activiti.dto.activitytask.ClaimRequestDTO;
import com.huiway.activiti.dto.activitytask.CreateRequestDTO;
import com.huiway.activiti.dto.activitytask.DiagramRequestDTO;
import com.huiway.activiti.dto.activitytask.DiagramResponseDTO;
import com.huiway.activiti.dto.activitytask.RevocationRequestDTO;
import com.huiway.activiti.entity.BpmActRuTask;
import com.huiway.activiti.exception.MyExceptions;
import com.huiway.activiti.exception.ValidationError;
import com.huiway.activiti.service.BpmActivityInterface;
import com.huiway.activiti.utils.BpmsActivityTypeEnum;
import com.huiway.activiti.utils.CommonUtils;
import com.huiway.activiti.utils.StringUtils;
import com.huiway.activiti.utils.UtilMisc;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

//@Profile({"dev","test"})
@Slf4j
@Api(value="流程任务管理")
@RestController
@RequestMapping("/activiti/task")
public class ActivityTaskController {

	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	private ProcessEngine processEngine;
	
	@Autowired
	BpmActivityInterface bpmActivityService;
	
	@ApiOperation(value = "启动流程实例")
	//@RequestMapping(value = "/startUp", method=RequestMethod.GET)
	@RequestMapping(value = "/startUp", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void create(HttpServletRequest request,HttpServletResponse response) {
		
		
		JSONObject jsonParam=null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "部署失败!");
		result.put("procDefId", null);
		BufferedReader streamReader=null;
		response.setContentType("application/json;charset=utf-8");
		try {
    		// 获取输入流
    		 streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));

    		// 写入数据到Stringbuilder
    		StringBuilder sb = new StringBuilder();
    		String line = null;
    		while ((line = streamReader.readLine()) != null) {
    			sb.append(line);
    		}
    		log.info("参数"+sb);
    		jsonParam = JSONObject.parseObject(sb.toString());
    		if(jsonParam!=null){
    			String tenantId=jsonParam.getString("tenantId");
    			if(StringUtils.isBlank(tenantId)){
    				throw new MyExceptions("启动流程失败,tenantId不能为空！");
    			}
    			String assignee=jsonParam.getString("assignee");
    			if(StringUtils.isBlank(assignee)){
    				throw new MyExceptions("启动流程失败,assignee不能为空！");
    			}
    			String procDefId=jsonParam.getString("procDefId");
    			if(StringUtils.isBlank(procDefId)){
    				throw new MyExceptions("启动流程失败,procDefId不能为空！");
    			}
    			String businessKey=jsonParam.getString("businessKey");
    			if(StringUtils.isBlank(businessKey)){
    				throw new MyExceptions("启动流程失败,businessKey不能为空！");
    			}
    			ActivitiDto dto = new ActivitiDto();
    			Map<String,Object> map = new HashMap<String,Object>();
    			map.put("assignee",assignee);
    			ProcessInstance pi = runtimeService
    					.startProcessInstanceByKeyAndTenantId(procDefId, 
    							businessKey, 
    							map, 
    							tenantId);
    					
    	        dto.setProcDefId(procDefId);
    	        dto.setProcInstId(pi.getId());
    	        
    	        List<BpmActRuTask> actRuTasks = (List<BpmActRuTask>) bpmActivityService.listByMap
    	        		(ImmutableMap.of("PROC_INST_ID_", pi.getId()));
    			
    			
    			
    			
    		       
    		        result.put("rtnCode", "1");
    				result.put("rtnMsg", "启动流程成功!");
    				result.put("bean", dto);
    				result.put("beans", actRuTasks);
    				 log.info("启动流程成功"+result.toString());
    		}
    		
    		
    		// 直接将json信息打印出来
    		//System.out.println(jsonParam.toJSONString());
    	} catch (Exception e) {
    		e.printStackTrace();
    		log.info("启动流程"+e.getMessage());
    	}finally{
    		try{
    			if(null!=streamReader){
    				streamReader.close();
    			}
    			String result2=result.toString();
    			PrintWriter p=response.getWriter();
    			p.println(result2);
    			p.flush();
    			p.close();
    		}catch(Exception e){
    			e.getStackTrace();
    			log.info("启动流程"+e.getMessage());
    		}
    		
    	}
		
	}
	
	@ApiOperation(value = "获取流程图",notes = "根据流程实例id获取流程图")
	@RequestMapping(value = "/diagram", method=RequestMethod.GET)
	public RestResponse diagram(@Valid DiagramRequestDTO requestDTO,BindingResult results) {
		if (results.hasErrors()) {
			return CommonUtils.initErrors(results);
		}
		
		RestResponse response = new RestResponse();
		DiagramResponseDTO dto = new DiagramResponseDTO();
        dto.setDiagramResource(getDiagram(requestDTO.getProcInstId()));
        response.setBean(dto);
		return response;
	}
	
    
	@ApiOperation(value = "认领任务",notes = "认领任务")
	@RequestMapping(value = "/claim", method=RequestMethod.GET)
    public RestResponse claim(@Valid ClaimRequestDTO requestDTO,BindingResult results) {
		if (results.hasErrors()) {
			return CommonUtils.initErrors(results);
		}
		
		RestResponse response = new RestResponse();
        Task acttask = taskService.createTaskQuery().taskId(requestDTO.getTaskId())
                .singleResult();
        if (acttask != null) {
        	taskService.claim(acttask.getId(), requestDTO.getAssignee());
        }
        return response;

    }
	
	@ApiOperation(value = "撤回任务到指定的流程节点",notes = "撤回任务到指定的流程节点")
	@RequestMapping(value = "/revocation", method=RequestMethod.GET)
	public RestResponse revocation(@Valid RevocationRequestDTO requestDTO,BindingResult results) {
		if (results.hasErrors()) {
			return CommonUtils.initErrors(results);
		}
		String processInstanceId = requestDTO.getProcInstId();
		String taskKey = requestDTO.getTaskDefKey();
		String userId = requestDTO.getUserId();
		
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();

        if (task == null) {
            throw new ValidationError("流程未启动或已执行完成，无法撤回");
        }

        RepositoryService repositoryService = processEngine.getRepositoryService();
        List<HistoricTaskInstance> htiList = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId).orderByTaskCreateTime().desc().list();
        String myTaskId = null;
        HistoricTaskInstance myTask = null;

        for(HistoricTaskInstance hti : htiList) {
            if (hti.getTaskDefinitionKey().equals(taskKey)) {
                myTaskId = hti.getId();
                myTask = hti;
            }
        }

        if (null == myTaskId) {
            throw new ValidationError("找不到节点，无法撤回");
        }

        String processDefinitionId = myTask.getProcessDefinitionId();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);

        // 变量
//      Map<String, VariableInstance> variables = runtimeService.getVariableInstances(currentTask.getExecutionId());
        String myActivityId = null;
        List<HistoricActivityInstance> haiList = historyService.createHistoricActivityInstanceQuery()
                .executionId(myTask.getExecutionId()).finished().list();
        for (HistoricActivityInstance hai : haiList) {
            if (myTaskId.equals(hai.getTaskId())) {
                myActivityId = hai.getActivityId();
                break;
            }
        }
        FlowNode myFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(myActivityId);

        Execution execution = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
        String activityId = execution.getActivityId();
        log.info("revocation------->> activityId:" + activityId);
        FlowNode flowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(activityId);

        // 记录原活动方向
        List<SequenceFlow> oriSequenceFlows = new ArrayList<SequenceFlow>();
        oriSequenceFlows.addAll(flowNode.getOutgoingFlows());

        // 清理活动方向
        flowNode.getOutgoingFlows().clear();
        // 建立新方向
        List<SequenceFlow> newSequenceFlowList = new ArrayList<SequenceFlow>();
        SequenceFlow newSequenceFlow = new SequenceFlow();
        newSequenceFlow.setId("newSequenceFlowId");
        newSequenceFlow.setSourceFlowElement(flowNode);
        newSequenceFlow.setTargetFlowElement(myFlowNode);
        newSequenceFlowList.add(newSequenceFlow);
        flowNode.setOutgoingFlows(newSequenceFlowList);

//        Authentication.setAuthenticatedUserId(userId);
//        taskService.addComment(task.getId(), task.getProcessInstanceId(), "撤回");

//        Map<String, Object> currentVariables = new HashMap<String, Object>();
//        currentVariables.put("applier", userId);
        
        // 完成任务
        taskService.complete(task.getId(),ImmutableMap.of("assignee", requestDTO.getAssignee()));
        // 恢复原方向
        flowNode.setOutgoingFlows(oriSequenceFlows);

        Task newCurrentTask = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        taskService.setAssignee(newCurrentTask.getId(), userId);

        List<BpmActRuTask> bpmActRuTaskList = (List<BpmActRuTask>) bpmActivityService.listByMap
        		(ImmutableMap.of("PROC_INST_ID_", processInstanceId));

        RestResponse response = new RestResponse();
        response.setBeans(bpmActRuTaskList);
	    return response;
	}
	
    /**
     * 获取流程图片base64字符串
     * 
     * @param procInctId 流程实例id
     * @return
     */
    private String getDiagram(String procInctId) {
        // 获取当前任务流程图片
        HistoricProcessInstance hip = historyService.createHistoricProcessInstanceQuery().processInstanceId(procInctId)
                .singleResult(); // 获取历史流程实例
        List<HistoricActivityInstance> hai = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(procInctId).orderByHistoricActivityInstanceId().asc().list(); // 获取流程中已经执行的节点，按照执行先后顺序排序
        List<String> executedActivityIdList = new ArrayList<String>(); // 构造已执行的节点ID集合
        for (HistoricActivityInstance activityInstance : hai) {
            executedActivityIdList.add(activityInstance.getActivityId());
        }
        BpmnModel bpmnModel = repositoryService.getBpmnModel(hip.getProcessDefinitionId()); // 获取bpmnModel
        List<String> flowIds = this.getExecutedFlows(bpmnModel, hai); // 获取流程已发生流转的线ID集合
        // List<String> flowIds = new ArrayList<String>();
        ProcessDiagramGenerator processDiagramGenerator = processEngine.getProcessEngineConfiguration()
                .getProcessDiagramGenerator();
        InputStream imageStream = processDiagramGenerator.generateDiagram(bpmnModel, "png", executedActivityIdList,
                flowIds, "宋体", "微软雅黑", "黑体", null, 2.0); // 使用默认配置获得流程图表生成器，并生成追踪图片字符流
        return "data:image/jpeg;base64," + CommonUtils.getImageStr(imageStream);
    }
    
    /**
                * 获取流程已发生流转的线ID集合
     * 
     * @param bpmnModel
     * @param historicActivityInstances //历史流程实例list
     * @return
     */
    private List<String> getExecutedFlows(BpmnModel bpmnModel,
            List<HistoricActivityInstance> historicActivityInstances) {
        List<String> flowIdList = new ArrayList<String>(); // 流转线ID集合
        List<FlowNode> historicFlowNodeList = new LinkedList<FlowNode>(); // 全部活动实例
        List<HistoricActivityInstance> finishedActivityInstanceList = new LinkedList<HistoricActivityInstance>(); // 已完成的历史活动节点
        for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
            historicFlowNodeList.add((FlowNode) bpmnModel.getMainProcess()
                    .getFlowElement(historicActivityInstance.getActivityId(), true));
            if (historicActivityInstance.getEndTime() != null) {
                finishedActivityInstanceList.add(historicActivityInstance);
            }
        }
        /** 遍历已完成的活动实例，从每个实例的outgoingFlows中找到已执行的 */
        FlowNode currentFlowNode = null;
        for (HistoricActivityInstance currentActivityInstance : finishedActivityInstanceList) {
            /** 获得当前活动对应的节点信息及outgoingFlows信息 */
            currentFlowNode = (FlowNode) bpmnModel.getMainProcess()
                    .getFlowElement(currentActivityInstance.getActivityId(), true);
            List<SequenceFlow> sequenceFlowList = currentFlowNode.getOutgoingFlows();
            /**
                                       * 遍历outgoingFlows并找到已流转的 满足如下条件任务已流转：
             * 1.当前节点是并行网关或包含网关，则通过outgoingFlows能够在历史活动中找到的全部节点均为已流转
             * 2.当前节点是以上两种类型之外的，通过outgoingFlows查找到的时间最近的流转节点视为有效流转
             */
            FlowNode targetFlowNode = null;
            if (BpmsActivityTypeEnum.PARALLEL_GATEWAY.getType().equals(currentActivityInstance.getActivityType())
                    || BpmsActivityTypeEnum.INCLUSIVE_GATEWAY.getType()
                            .equals(currentActivityInstance.getActivityType())) {
                for (SequenceFlow sequenceFlow : sequenceFlowList) { // 遍历历史活动节点，找到匹配Flow目标节点的
                    targetFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(sequenceFlow.getTargetRef(),
                            true);
                    if (historicFlowNodeList.contains(targetFlowNode)) {
                        flowIdList.add(sequenceFlow.getId());
                    }
                }
            } else {
                List<Map<String, String>> tempMapList = new LinkedList<Map<String, String>>();
                for (SequenceFlow sequenceFlow : sequenceFlowList) { // 遍历历史活动节点，找到匹配Flow目标节点的
                    for (int i = 0; i < historicActivityInstances.size(); i++) {
                        
                        HistoricActivityInstance historicActivityInstance = historicActivityInstances.get(i);

                        if (historicActivityInstance.getActivityId().equals(sequenceFlow.getSourceRef())) {
                            
                            for(int j = i; j < historicActivityInstances.size(); j++) {
                                
                                HistoricActivityInstance historicActivityInstanceJ = historicActivityInstances.get(j);
                                
                                FlowNode sourceFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(sequenceFlow.getSourceRef(),
                                        true);
                                
                                if(sourceFlowNode.getBehavior() instanceof org.activiti.engine.impl.bpmn.behavior.ExclusiveGatewayActivityBehavior) {
                                    if (i < historicActivityInstances.size() - 1 && historicActivityInstances.get(i + 1)
                                            .getActivityId().equals(sequenceFlow.getTargetRef())) {
                                        tempMapList.add(UtilMisc.toMap("flowId", sequenceFlow.getId(), "activityStartTime",
                                                String.valueOf(historicActivityInstance.getStartTime().getTime())));
                                    }
                                } else {
                                    if (historicActivityInstanceJ.getActivityId().equals(sequenceFlow.getTargetRef())) {
                                        tempMapList.add(UtilMisc.toMap("flowId", sequenceFlow.getId(), "activityStartTime",
                                                String.valueOf(historicActivityInstance.getStartTime().getTime())));
                                    }
                                }
                                
                            }
                            
                        }
                        
                    }
                }
                String flowId = null;
                for (Map<String, String> map : tempMapList) {
                    flowId = map.get("flowId");
                    flowIdList.add(flowId);
                }
            }
        }
        return flowIdList;
    }
	
}
