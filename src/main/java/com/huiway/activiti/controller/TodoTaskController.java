package com.huiway.activiti.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.huiway.activiti.common.bean.RestResponse;
import com.huiway.activiti.common.constant.CodeConstant;
import com.huiway.activiti.dto.ActivitiDto;
import com.huiway.activiti.dto.todotask.GetRequestDTO;
import com.huiway.activiti.dto.todotask.NextTaskNodeRequestDTO;
import com.huiway.activiti.dto.todotask.NextTaskNodeResponseDTO;
import com.huiway.activiti.entity.BpmActRuTask;
import com.huiway.activiti.exception.MyExceptions;
import com.huiway.activiti.exception.ValidationError;
import com.huiway.activiti.service.BpmActivityInterface;
import com.huiway.activiti.utils.CommonUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

//@Profile({"dev","test"})
@Slf4j
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
	@Autowired
	private ProcessEngine processEngine;
	
	@ApiOperation(value = "完成任务",notes = "完成任务")
	@RequestMapping(value = "/complete", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void completeTask(HttpServletRequest request,HttpServletResponse response) {
		JSONObject jsonParam=null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "完成任务失败!");
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
    				throw new MyExceptions("完成任务失败,tenantId不能为空！");
    			}
    			String assignee=jsonParam.getString("assignee");
    			String groupIds=jsonParam.getString("groupIds");
//    			if(StringUtils.isBlank(assignee)){
//    				if(StringUtils.isBlank(groupIds)){
//        				throw new MyExceptions("完成任务失败,groupIds不能为空！");
//        			}
//    			}
    			if(StringUtils.isEmpty(groupIds)){
    				if(StringUtils.isBlank(assignee)){
        				throw new MyExceptions("完成任务失败,assignee不能为空！");
        			}
    			}
    			String nodeCode=jsonParam.getString("nodeCode");
    			String taskId=jsonParam.getString("taskId");
    			if(StringUtils.isBlank(taskId)){
    				throw new MyExceptions("完成任务失败,taskId不能为空！");
    			}
    			String judge=jsonParam.getString("judge");
    			if(StringUtils.isBlank(judge)){
    				throw new MyExceptions("完成任务失败,judge不能为空！");
    			}
    			String userId=jsonParam.getString("userId");
    			if(StringUtils.isBlank(userId)){
    				throw new MyExceptions("完成任务失败,userId不能为空！");
    			}
    			
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
    		               // throw new ValidationError("This activity model has already be suspended.");
    		            	throw new ValidationError("已挂起");
    		            }

    		            boolean instSuspended = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId)
    		                    .singleResult().isSuspended();
    		            if (instSuspended) {
    		               // throw new ValidationError("This activity instance has already be suspended.");
    		            	throw new ValidationError("已挂起");
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
    		          if(StringUtils.isNoneBlank(nodeCode)){
    		        	  if(nodeCode.startsWith("endevent")){

      		                List<HistoricActivityInstance> htiList =historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().desc().list();
      		            	
      		                HistoricActivityInstance hh=htiList.get(0);
      	    		            BpmActRuTask bb =new BpmActRuTask();
      	    		            bb.setName(hh.getActivityName());
      	    		            bb.setTaskDefKey(hh.getActivityId());
      	    		            bb.setProcDefId(hh.getProcessDefinitionId());
      	    		            bb.setProcInstId(hh.getProcessInstanceId());
      	    		            bpmActRuTaskList.add(bb);	
      		            }
    		        	
    		        	processEngine.getRuntimeService().deleteProcessInstance(processInstanceId, "结束");
    		        	  
    		          }
    		           
    		          
    		            dto.setProcDefId(processDefinitionId);
    		            dto.setProcInstId(processInstanceId);
    		            result.put("rtnCode", "1");
        				result.put("rtnMsg", "完成任务成功!");
        				result.put("bean", dto);
        				result.put("beans", bpmActRuTaskList);
        				 log.info("完成任务成功"+result.toString());
    		        } else {
//    		            response.setRtnCode(CodeConstant.FAIL);
//    		            response.setMessage("task not found");
//    		            return response;
    		            
    		            result.put("rtnCode", "-1");
        				result.put("rtnMsg", "完成任务失败，找不到任务!");
        				throw new MyExceptions("完成任务失败，找不到任务!");
    		        }
    			
    		       
    		        
    		}
    		
    		
    		// 直接将json信息打印出来
    		//System.out.println(jsonParam.toJSONString());
    	} catch (Exception e) {
    		e.printStackTrace();
    		log.info("完成任务失败"+e.getMessage());
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
    			log.info("完成任务失败"+e.getMessage());
    		}
    		
    	}
		
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
    @RequestMapping(value = "/next-node", method=RequestMethod.POST,produces="application/json;charset=utf-8")
    public void getNextTaskNode(HttpServletRequest request,HttpServletResponse response) {
    	JSONObject jsonParam=null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "获取下一步任务节点失败!");
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
    			String taskId=jsonParam.getString("taskId");
    			if(StringUtils.isBlank(taskId)){
    				throw new MyExceptions("获取下一步任务节点,taskId不能为空！");
    			}
    	    	Task task = taskService.createTaskQuery() // 创建任务查询
    	                .taskId(taskId) // 根据任务id查询
    	                .singleResult();
    	        String processInstanceId = "";
    	        String processDefinitionId="";
    	        // 当前流程节点Id信息
    	        String activityId = "";
    	        if (task != null) {
    	            processInstanceId = task.getProcessInstanceId();
    	            activityId = task.getTaskDefinitionKey();
    	             processDefinitionId = task.getProcessDefinitionId();
    	        }  else {
    	        	result.put("rtnCode", "-1");
    	    		result.put("rtnMsg", "获取下一步任务节点失败，taskId不可用");
    	    		throw new MyExceptions("获取下一步任务节点失败,taskId不可用！");
    	        }

    	        // 获取流程发布Id信息
    	        String definitionId = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId)
    	                .singleResult().getProcessDefinitionId();

    	        BpmnModel model = repositoryService.getBpmnModel(definitionId);
    	        List<SequenceFlow> sequenceFlowList;
    	        FlowElement activeEl = model.getMainProcess().getFlowElement(activityId);
    	        List<Map<String,String>>  maplist=new ArrayList<>();
    	        BpmnModel model2 = repositoryService.getBpmnModel(processDefinitionId);
 		       if(model2 != null) {
 		           Collection<FlowElement> flowElements = model2.getMainProcess().getFlowElements();
 		           for(FlowElement e : flowElements) {
 		        	   Map<String,String> map=new HashMap<>();
 		        	   map.put( "id", e.getId());
 		        	   map.put( "name", e.getName());
 		        	   maplist.add(map);
 		           }
 		        //   System.out.println("flowelement id:" + e.getId() + "  name:" + e.getName() + "   class:" + e.getClass().toString());
 		        }
    	        List<Map<String,String>> list = new ArrayList<Map<String,String>>();
    	        if (activeEl instanceof org.activiti.bpmn.model.UserTask) { // 节点
    	            sequenceFlowList = ((org.activiti.bpmn.model.UserTask) activeEl).getOutgoingFlows();// 流出信息
    	        	for(SequenceFlow sequenceFlow : sequenceFlowList) {
    	        		 Map<String,String> maps=new HashMap<>();
    	        		 maps.put( "condition", sequenceFlow.getConditionExpression());
    	        		 maps.put( "targetRef", sequenceFlow.getTargetRef());
    	        		 
    	        		 for(Map<String,String> m :maplist){
    	        			 if(sequenceFlow.getTargetRef().equals(m.get("id").toString())){
    	        				 maps.put( "name", m.get("name").toString());
    	        			 }
    	        			 
    	        		 }
    	        		 list.add(maps);
    	        		 
    	        	}
    	        	
    	        	
    	        	
    	        	
    	        	
    	        }  
    	        
 		        result.put("rtnCode", "1");
				result.put("rtnMsg", "获取下一步任务节点成功!");
				result.put("bean", null);
				result.put("beans", list);
				 log.info("获取下一步任务节点成功"+result.toString());
    	        
    		        
    		}
    		
    		
    		// 直接将json信息打印出来
    		//System.out.println(jsonParam.toJSONString());
    	} catch (Exception e) {
    		e.printStackTrace();
    		log.info("获取下一步任务节点失败"+e.getMessage());
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
    			log.info("获取下一步任务节点失败"+e.getMessage());
    		}
    		
    	}
    	
    	
    	

    }
	
    @ApiOperation(value = "获取所有流程节点", notes = "获取所有流程节点")
     //@RequestMapping(value = "/get-procdef", method=RequestMethod.GET)
    @RequestMapping(value = "/get-procdef", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void getTodoTaskNode(HttpServletRequest request,HttpServletResponse response){
    	JSONObject jsonParam=null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "获取所有流程节点失败!");
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
    		List<Map<String,String>>  list=new ArrayList<>();
    		if(jsonParam!=null){
    			String procdefId=jsonParam.getString("procdefId");
    			if(StringUtils.isBlank(procdefId)){
    				result.put("rtnCode", "-1");
    				result.put("rtnMsg", "获取所有流程节点失败,procdefId不能为空");
    				throw new MyExceptions("获取下一步任务节点失败,procdefId不能为空！");
    			}
    			
    			BpmnModel model = repositoryService.getBpmnModel(procdefId);
    		       if(model != null) {
    		           Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
    		           for(FlowElement e : flowElements) {
    		        	   Map<String,String> map=new HashMap<>();
    		        	   map.put( "id", e.getId());
    		        	   map.put( "name", e.getName());
    		        	   list.add(map);
    		           }
    		        //   System.out.println("flowelement id:" + e.getId() + "  name:" + e.getName() + "   class:" + e.getClass().toString());
    		        }
    		        
    		}
    		
    		    result.put("rtnCode", "1");
				result.put("rtnMsg", "获取所有流程节点成功!");
				result.put("bean", null);
				result.put("beans", list);
				 log.info("获取所有流程节点"+result.toString());
    		
    		
    		// 直接将json信息打印出来
    		//System.out.println(jsonParam.toJSONString());
    	} catch (Exception e) {
    		e.printStackTrace();
    		log.info("获取所有流程节点失败"+e.getMessage());
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
    			log.info("获取所有流程节点失败"+e.getMessage());
    		}
    		
    	}
    	
    	
    	
    	
    	
    }    
       
}
