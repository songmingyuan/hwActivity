package com.huiway.activiti.controller;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.image.ProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.huiway.activiti.common.bean.RestResponse;
import com.huiway.activiti.dto.ActTaskNodeDTO;
import com.huiway.activiti.dto.ActivitiDto;
import com.huiway.activiti.dto.model.ModelDiagramDTO;
import com.huiway.activiti.exception.MyExceptions;
import com.huiway.activiti.utils.CommonUtils;
import com.huiway.activiti.utils.StringUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

//@Profile({"dev","test"})
@Slf4j
@Api(value="流程模型管理")
@RestController
@RequestMapping("/activiti/models")
public class ModelController {

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private StandaloneProcessEngineConfiguration processEngineConfiguration;
	@ApiOperation(value = "部署流程模型")
	@RequestMapping(value = "/deploy", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void deploy(HttpServletRequest request,HttpServletResponse response) {
		response.setContentType("application/json;charset=utf-8");
		JSONObject jsonParam=null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "部署失败!");
		result.put("procDefId", null);
		BufferedReader streamReader=null;
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
    		InputStream inputStream =null;
    		if(jsonParam!=null){
    			String tenantId=jsonParam.getString("tenantId");
    			if(StringUtils.isBlank(tenantId)){
    				throw new MyExceptions("部署失败,tenantId不能为空！");
    			}
    			String file=jsonParam.getString("file");
    			if(StringUtils.isBlank(file)){
    				throw new MyExceptions("部署失败,file不能为空！");
    			}
    			 inputStream = new ByteArrayInputStream(file.getBytes("ISO-8859-1"));
    			 String resourceName =jsonParam.getString("fileName");
    			 if(StringUtils.isBlank(tenantId)){
     				throw new MyExceptions("部署失败,fileName不能为空！");
     			}
    			 //根据bpmn文件部署流程  
    		        Deployment deploy = repositoryService.createDeployment().addInputStream(resourceName, inputStream)
    		        		.tenantId(tenantId)
    		        		.deploy();  
    		        //获取流程定义  
    		        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
    		        		.deploymentId(deploy.getId()).singleResult();  
    		        
    		        //
    		        if(StringUtils.isBlank(processDefinition.getKey())){
    		        	throw new MyExceptions("部署失败,key不能为空！");
    		        }
    		       
    		        result.put("rtnCode", "1");
    				result.put("rtnMsg", "部署成功!");
    				result.put("procDefId", processDefinition.getKey());
    				 log.info("部署成功"+result.toString());
    		}
    		
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    		log.info("部署失败"+e.getMessage());
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
    			log.info("部署失败"+e.getMessage());
    		}
    		
    	}
		
		

		
	}

	
	@ApiOperation(value = "获取流程图",notes = "根据流程定义id获取流程图")
	@RequestMapping(value = "/diagram", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void diagram(HttpServletRequest request,HttpServletResponse response) {
		response.setContentType("application/json;charset=utf-8");
		JSONObject jsonParam=null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "部署失败!");
		result.put("procDefId", null);
		BufferedReader streamReader=null;
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
    		InputStream inputStream =null;
    		if(jsonParam!=null){
    			String processDefinitionId=jsonParam.getString("processDefinitionId");
    			if(StringUtils.isBlank(processDefinitionId)){
    				throw new MyExceptions("获取流程图失败,processDefinitionId不能为空！");
    			}

    			
    	        ProcessDiagramGenerator processDiagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
    	        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
    	        inputStream = processDiagramGenerator.generateDiagram(bpmnModel, "png", new ArrayList<String>(),
    	                new ArrayList<String>(), "宋体", "微软雅黑", "黑体", null, 2.0); 
    	            result.put("rtnCode", "1");
    	        	result.put("file","data:image/jpeg;base64," + CommonUtils.getImageStr(inputStream));
    				result.put("rtnMsg", "获取流程图成功!");
    			
    				 log.info("获取流程图成功"+result.toString());
    		}
    		
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    		log.info("获取流程图失败"+e.getMessage());
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
    			log.info("获取流程图失败"+e.getMessage());
    		}
    		
    	}
	}
	
	
	    @ApiOperation(value = "移除已部署的流程模型", notes = "根据流程部署id移除已经部署的模型")
	    @RequestMapping(value = "/delete", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	    public void delete(HttpServletRequest request,HttpServletResponse response){
	    	
	    	response.setContentType("application/json;charset=utf-8");
			JSONObject jsonParam=null;
			JSONObject result = new JSONObject();
			result.put("rtnCode", "-1");
			result.put("rtnMsg", "移除已部署的流程失败!");
			result.put("procDefId", null);
			BufferedReader streamReader=null;
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
	    		InputStream inputStream =null;
	    		if(jsonParam!=null){
	    			String procdefId=jsonParam.getString("procdefId");
	    			if(StringUtils.isBlank(procdefId)){
	    				throw new MyExceptions("移除已部署的流程失败,procdefId不能为空！");
	    			}
	    			String deploymentId = repositoryService.getProcessDefinition(procdefId).getDeploymentId();
	    			

	    			  if(!StringUtils.isEmpty(deploymentId)){
	    				  
	    				  repositoryService.deleteDeployment(deploymentId,true);
		    			  result.put("rtnCode", "1");
		    				result.put("rtnMsg", "删除成功!");
		    				result.put("bean", null);
		    				result.put("beans", null);
		    				 log.info("删除流程成功"+result.toString());
	    			  }else{
	    				  result.put("rtnMsg", "删除失败!");
		    				result.put("bean", null);
		    				result.put("beans", null);
	    			  }
	    			 
	    		}
	    		
	    		
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    		log.info("移除已部署的流程失败"+e.getMessage());
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
	    			log.info("移除已部署的流程失败"+e.getMessage());
	    		}
	    		
	    	}
	    	
	      
	       
	    }
	
	
	
	@ApiOperation(value = "获取流程节点",notes = "根据流程定义id获取流程节点")
	@RequestMapping(value = "/nodes", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void getModelNodes(HttpServletRequest request,HttpServletResponse response) {
		
		response.setContentType("application/json;charset=utf-8");
		JSONObject jsonParam=null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "获取流程节点失败!");
		BufferedReader streamReader=null;
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
    		InputStream inputStream =null;
    		if(jsonParam!=null){
    			String processDefId=jsonParam.getString("procdefId");
    			if(StringUtils.isBlank(processDefId)){
    				throw new MyExceptions("获取流程节点,procdefId不能为空！");
    			}
    			  List<ActTaskNodeDTO> responseList = new ArrayList<ActTaskNodeDTO>();
    		        BpmnModel model = repositoryService.getBpmnModel(processDefId);
    		        if (model != null) {
    		            Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
    		            int index = 1;
    		            for (FlowElement e : flowElements) {
    		                if (e instanceof org.activiti.bpmn.model.UserTask) { // 节点
    		                	ActTaskNodeDTO node = new ActTaskNodeDTO();
    		                    node.setTaskDefKey(e.getId());
    		                    node.setTaskNodeName(e.getName());
    		                    node.setTaskCategory(((org.activiti.bpmn.model.UserTask) e).getCategory());
    		                    node.setOrderNo(index++);
    		                    responseList.add(node);
    		                }
    		            }
    		            result.put("rtnCode", "1");
	    				result.put("rtnMsg", "查询成功!");
	    				result.put("bean", null);
	    				result.put("beans", responseList);
	    				 log.info("获取流程节点成功"+result.toString());
    		        }else {
    		        	throw new MyExceptions("获取流程节点失败！");
    		        }

    			  
    			 
    		}
    		
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    		log.info("获取流程节点失败"+e.getMessage());
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
    			log.info("获取流程节点失败"+e.getMessage());
    		}
    		
    	}
    	
	}
	
	
	
	
	    @ApiOperation(value = "挂起任务", notes = "根据流程定义id挂起任务")
	    @RequestMapping(value = "/procDefId/suspend", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	    public void updateStateSuspend(HttpServletRequest request,HttpServletResponse response) {
	    	response.setContentType("application/json;charset=utf-8");
			JSONObject jsonParam=null;
			JSONObject result = new JSONObject();
			result.put("rtnCode", "-1");
			result.put("rtnMsg", "挂起任务失败!");
			BufferedReader streamReader=null;
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
	    		InputStream inputStream =null;
	    		if(jsonParam!=null){
	    			String procDefId=jsonParam.getString("procDefId");
	    			if(StringUtils.isBlank(procDefId)){
	    				throw new MyExceptions("挂起任务,procDefId不能为空！");
	    			}
	    		    String deploymentId = repositoryService.getProcessDefinition(procDefId).getDeploymentId();
		            List<ProcessDefinition> defines = repositoryService.createProcessDefinitionQuery()
		                    .deploymentId(deploymentId).list();
		            if (defines.size() > 0) {
		                ProcessDefinition define = defines.get(0);
		                if (define.isSuspended()) {
		                	throw new MyExceptions("挂起任务 ：process define is already suspended");
		                }
		            }
		            
	    		        repositoryService.suspendProcessDefinitionById(procDefId, true, null);
	    		        result.put("rtnCode", "1");
	    				result.put("rtnMsg", "挂起任务成功!");
	    				result.put("bean", null);
	    				result.put("beans", null);
	    			 
	    		}
	    		
	    		
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    		log.info("挂起任务失败"+e.getMessage());
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
	    			log.info("挂起任务失败"+e.getMessage());
	    		}
	    		
	    	}
	    	
	    	

	    }

	    @ApiOperation(value = "激活任务", notes = "根据流程定义id激活任务")
	    @RequestMapping(value = "/procDefId/active", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	    public void updateStateActive(HttpServletRequest request,HttpServletResponse response) {
	    	response.setContentType("application/json;charset=utf-8");
			JSONObject jsonParam=null;
			JSONObject result = new JSONObject();
			result.put("rtnCode", "-1");
			result.put("rtnMsg", "激活任务失败!");
			BufferedReader streamReader=null;
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
	    			String procDefId=jsonParam.getString("procDefId");
	    			if(StringUtils.isBlank(procDefId)){
	    				throw new MyExceptions("激活任务失败,procDefId不能为空！");
	    			}
	    			String deploymentId = repositoryService.getProcessDefinition(procDefId).getDeploymentId();
	    			List<ProcessDefinition> defines = repositoryService.createProcessDefinitionQuery()
	    	                    .deploymentId(deploymentId).list();
	    	            if (defines.size() > 0) {
	    	                ProcessDefinition define = defines.get(0);
	    	                if (!define.isSuspended()) {
	    	                    throw new MyExceptions("激活任务失败 ：process define is already actived");
	    	                }
	    	            }

	    	            repositoryService.activateProcessDefinitionById(procDefId, true, null);
		            
	    		        repositoryService.suspendProcessDefinitionById(procDefId, true, null);
	    		        result.put("rtnCode", "1");
	    				result.put("rtnMsg", "激活任务成功!");
	    				result.put("bean", null);
	    				result.put("beans", null);
	    			 
	    		}
	    		
	    		
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    		log.info("激活任务失败"+e.getMessage());
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
	    			log.info("激活任务失败"+e.getMessage());
	    		}
	    		
	    	}
	    	
	    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
