package com.huiway.activiti.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;
import com.huiway.activiti.common.bean.RestResponse;
import com.huiway.activiti.common.constant.CodeConstant;
import com.huiway.activiti.dto.ActivitiDto;
import com.huiway.activiti.dto.activitytask.CompleteRequestDTO;
import com.huiway.activiti.dto.todotask.GetRequestDTO;
import com.huiway.activiti.dto.todotask.NextTaskNodeRequestDTO;
import com.huiway.activiti.dto.todotask.NextTaskNodeResponseDTO;
import com.huiway.activiti.entity.BpmActRuTask;
import com.huiway.activiti.exception.ValidationError;
import com.huiway.activiti.service.BpmActivityInterface;
import com.huiway.activiti.utils.CommonUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

//@Profile({"dev","test"})
@Api(value="我的任务管理")
@RestController
@RequestMapping("/activiti/todo-task")
public class TodoTaskController {
	
	@Autowired
	private TaskService taskService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	BpmActivityInterface bpmActivityService;
	@Autowired
	HistoryService historyService;
	
	@ApiOperation(value = "完成任务",notes = "完成任务")
	@RequestMapping(value = "/complete", method=RequestMethod.GET)
	public RestResponse completeTask(@Valid CompleteRequestDTO requestDTO,BindingResult results) {
		if (results.hasErrors()) {
			return CommonUtils.initErrors(results);
		}
		
		String taskId = requestDTO.getTaskId();
		String judge = requestDTO.getJudge();
		String assignee = requestDTO.getAssignee();
		String groupIds = requestDTO.getGroupIds();
		
		RestResponse response = new RestResponse();
		ActivitiDto dto = new ActivitiDto();
		List<BpmActRuTask> bpmActRuTaskList = new ArrayList<BpmActRuTask>();
		
        Task task = taskService.createTaskQuery().taskId(taskId) // 根据任务id查询
                .singleResult();
        if (task != null) {
            String processInstanceId = task.getProcessInstanceId();
            String processDefinitionId = task.getProcessDefinitionId();

            boolean modelSuspended = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(processDefinitionId).singleResult().isSuspended();
            if (modelSuspended) {
                throw new ValidationError("This activity model has already be suspended.");
            }

            boolean instSuspended = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId)
                    .singleResult().isSuspended();
            if (instSuspended) {
                throw new ValidationError("This activity instance has already be suspended.");
            }
            
            Map<String,Object> map = new HashMap<String,Object>();
            if(StringUtils.isNoneEmpty(judge)) {
            	map.put("judge", judge);
            }
            if(StringUtils.isNoneEmpty(assignee)) {
            	map.put("assignee", assignee);
            }
            if(StringUtils.isNoneEmpty(groupIds)) {
            	map.put("groupIds", groupIds);
            }
            taskService.complete(taskId,map);

            bpmActRuTaskList = (List<BpmActRuTask>) bpmActivityService.listByMap
            		(ImmutableMap.of("PROC_INST_ID_", processInstanceId));
      
            dto.setProcDefId(processDefinitionId);
            dto.setProcInstId(processInstanceId);
            response.setBean(dto);
            response.setBeans(bpmActRuTaskList);
        } else {
            response.setRtnCode(CodeConstant.FAIL);
            response.setMessage("task not found");
            return response;
        }
		
		return response;
	}
	
    @ApiOperation(value = "获取待办任务", notes = "根据流程实例id获取待办任务")
    @RequestMapping(value = "/get", method=RequestMethod.GET)
	public RestResponse getTodoTask(@Valid GetRequestDTO requestDTO,BindingResult results){
		if (results.hasErrors()) {
			return CommonUtils.initErrors(results);
		}
		
		RestResponse response = new RestResponse();
		List<BpmActRuTask> bpmActRuTaskList = new ArrayList<BpmActRuTask>();
       
		bpmActRuTaskList = (List<BpmActRuTask>) bpmActivityService.listByMap
        		(ImmutableMap.of("PROC_INST_ID_", requestDTO.getProcInstId()));
        response.setBeans(bpmActRuTaskList);
        return response;
	}
    
    @ApiOperation(value = "获取下一步任务节点", notes = "获取下一步任务节点")
    @RequestMapping(value = "/next-node", method=RequestMethod.GET)
    public RestResponse getNextTaskNode(@Valid NextTaskNodeRequestDTO requestDTO,BindingResult results) {
		if (results.hasErrors()) {
			return CommonUtils.initErrors(results);
		}
    	
    	RestResponse response = new RestResponse();
    	Task task = taskService.createTaskQuery() // 创建任务查询
                .taskId(requestDTO.getTaskId()) // 根据任务id查询
                .singleResult();
        String processInstanceId = "";
        // 当前流程节点Id信息
        String activityId = "";
        if (task != null) {
            processInstanceId = task.getProcessInstanceId();
            activityId = task.getTaskDefinitionKey();
        }  else {
        	response.setRtnCode(CodeConstant.FAIL);
        	response.setMessage("taskId 不可用");
        	return response;
        }

        // 获取流程发布Id信息
        String definitionId = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId)
                .singleResult().getProcessDefinitionId();

        BpmnModel model = repositoryService.getBpmnModel(definitionId);
        List<SequenceFlow> sequenceFlowList;
        FlowElement activeEl = model.getMainProcess().getFlowElement(activityId);
        
        List<NextTaskNodeResponseDTO> list = new ArrayList<NextTaskNodeResponseDTO>();
        if (activeEl instanceof org.activiti.bpmn.model.UserTask) { // 节点
            sequenceFlowList = ((org.activiti.bpmn.model.UserTask) activeEl).getOutgoingFlows();// 流出信息
        	for(SequenceFlow sequenceFlow : sequenceFlowList) {
        		NextTaskNodeResponseDTO nextTaskNodeResponseDTO = new NextTaskNodeResponseDTO();
        		nextTaskNodeResponseDTO.setConditionExpression(sequenceFlow.getConditionExpression());
        		nextTaskNodeResponseDTO.setTaskDefKey(sequenceFlow.getTargetRef());
        		list.add(nextTaskNodeResponseDTO);
        	}
        	response.setBeans(list);
        }
        
        return response;
    }
    
}
