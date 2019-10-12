package com.huiway.activiti.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huiway.activiti.entity.BpmActRuTask;
import com.huiway.activiti.service.BpmActivityInterface;

import lombok.extern.slf4j.Slf4j;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
@RequestMapping("/activiti")  
@Slf4j
public class ActivitiTestController {

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private TaskService taskService;
	
	@Autowired
	BpmActivityInterface bpmActivityService;
	
	@RequestMapping("helloWorld")  
    public void helloWorld() {  
		Collection<BpmActRuTask> bpmActRuTaskList = new ArrayList<BpmActRuTask>();
        Map<String, Object> columnMap = new HashMap<String, Object>();
        columnMap.put("PROC_INST_ID_", "7505");
        bpmActRuTaskList = bpmActivityService.listByMap(columnMap);
		
        //根据bpmn文件部署流程  
        Deployment deploy = repositoryService.createDeployment()
        									.addClasspathResource("MyProcess-test.bpmn")
        									.deploy();  
        //获取流程定义  
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deploy.getId()).singleResult();  
        //启动流程定义，返回流程实例  
        ProcessInstance pi = runtimeService.startProcessInstanceById(processDefinition.getId());  
        String processId = pi.getId();  
        log.info("流程创建成功，当前流程实例ID："+processId);  
          
        Task task=taskService.createTaskQuery().processInstanceId(processId).singleResult();  
        log.info("执行前，任务名称："+task.getName());  
        taskService.complete(task.getId());  
  
        task = taskService.createTaskQuery().processInstanceId(processId).singleResult();  
        log.info("task为null，任务执行完毕："+task);  
	}
	
	@RequestMapping("singleAssignee")  
	public void setSingleAssignee() {  
		
		//根据bpmn文件部署流程  
		repositoryService.createDeployment().addClasspathResource("singleAssignee.bpmn").deploy();
		// 设置User Task1受理人变量
		Map<String, Object> variables = new HashMap<>();
		variables.put("user1", "007");
		//采用key来启动流程定义并设置流程变量，返回流程实例  
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("singleAssignee", variables);  
		String processId = pi.getId();  
		log.info("流程创建成功，当前流程实例ID："+processId);
		
		List<Task> list = taskService.createTaskQuery().taskAssignee("007").list();
		if(list!=null && list.size()>0){ 
            for(org.activiti.engine.task.Task task:list){  
                log.info("任务ID："+task.getId());  
                log.info("任务的办理人："+task.getAssignee());  
                log.info("任务名称："+task.getName());  
                log.info("任务的创建时间："+task.getCreateTime());  
                log.info("流程实例ID："+task.getProcessInstanceId());  
                log.info("#######################################");
            }
        }
		
		// 设置User Task2的受理人变量
		Map<String, Object> variables1 = new HashMap<>();
		variables1.put("user2", "Kevin");
		taskService.complete(list.get(0).getId(), variables1);
		log.info("User Task1被完成了，此时流程已流转到User Task2");
	}
	
	@RequestMapping("multiAssignee")  
	public void setMultiAssignee() {  
		
		//根据bpmn文件部署流程  
		repositoryService.createDeployment().addClasspathResource("MultiAssignee.bpmn").deploy();
		Map<String, Object> variables = new HashMap<>();
		List<String> userList = new ArrayList<>();
		userList.add("user1");
		userList.add("user2");
		userList.add("user3");
		variables.put("userList", userList);
		//采用key来启动流程定义并设置流程变量，返回流程实例  
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("multiAssigneeProcess", variables);  
		String processId = pi.getId();  
		log.info("流程创建成功，当前流程实例ID："+processId);
		
		// 查看user1的任务
		List<Task> list = taskService.createTaskQuery().taskAssignee("user1").list();
		if(list!=null && list.size()>0){ 
            for(org.activiti.engine.task.Task task:list){  
                log.info("任务ID："+task.getId());  
                log.info("任务的办理人："+task.getAssignee());  
                log.info("任务名称："+task.getName());  
                log.info("任务的创建时间："+task.getCreateTime());  
                log.info("流程实例ID："+task.getProcessInstanceId());  
                log.info("#######################################");
            }
        }
		
		// 查看user2的任务
		List<Task> list2 = taskService.createTaskQuery().taskAssignee("user2").list();
		if(list2!=null && list2.size()>0){ 
            for(org.activiti.engine.task.Task task:list2){  
                log.info("任务ID："+task.getId());  
                log.info("任务的办理人："+task.getAssignee());  
                log.info("任务名称："+task.getName());  
                log.info("任务的创建时间："+task.getCreateTime());  
                log.info("流程实例ID："+task.getProcessInstanceId());  
                log.info("#######################################");
            }
        }
				
		// 查看user3的任务
		List<Task> list3 = taskService.createTaskQuery().taskAssignee("user3").list();
		if(list3!=null && list3.size()>0){ 
            for(org.activiti.engine.task.Task task:list3){  
                log.info("任务ID："+task.getId());  
                log.info("任务的办理人："+task.getAssignee());  
                log.info("任务名称："+task.getName());  
                log.info("任务的创建时间："+task.getCreateTime());  
                log.info("流程实例ID："+task.getProcessInstanceId());  
                log.info("#######################################");
            }
        }
		
	}
	
	@RequestMapping("exclusiveGateway")  
	public void exclusiveGateway() {  
		
		//根据bpmn文件部署流程  
		repositoryService.createDeployment().addClasspathResource("exclusiveGateway.bpmn").deploy();
		// 设置User Task1受理人变量
		Map<String, Object> variables = new HashMap<>();
		variables.put("user1", "007");
		//采用key来启动流程定义并设置流程变量，返回流程实例  
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("exclusiveGatewayAndTimerBoundaryEventProcess", variables);  
		String processId = pi.getId();  
		log.info("流程创建成功，当前流程实例ID："+processId);
		
		List<Task> list = taskService.createTaskQuery().taskAssignee("007").list();
		Map<String, Object> variables1 = new HashMap<>();
		variables1.put("user2", "lili"); // 设置User Task2的受理人变量
		variables1.put("operate", ""); // 设置用户的操作 为空 表示走flow3的默认路线
		taskService.complete(list.get(0).getId(), variables1);
		log.info("User Task1被完成了，此时流程已流转到User Task2");
		
		List<Task> list1 = taskService.createTaskQuery().taskAssignee("lili").list();
		Map<String, Object> variables2 = new HashMap<>();
		variables2.put("user4", "bobo");
		variables2.put("startTime", "2018-6-11T14:22:00"); // 设置定时边界任务的触发时间 注意：后面的时间必须是ISO 8601时间格式的字符串！！！
		taskService.complete(list1.get(0).getId(), variables2);
		
		List<Task> list2 = taskService.createTaskQuery().taskAssignee("bobo").list();
		if(list2!=null && list2.size()>0){ 
            for(org.activiti.engine.task.Task task:list2){  
                log.info("任务ID："+task.getId());  
                log.info("任务的办理人："+task.getAssignee());  
                log.info("任务名称："+task.getName());  
                log.info("任务的创建时间："+task.getCreateTime());  
                log.info("流程实例ID："+task.getProcessInstanceId());  
                log.info("#######################################");
            }
        }
	}
	
}
