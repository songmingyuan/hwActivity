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
	//@PostMapping (value = "/deploy")
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
	//@RequestMapping(value = "/diagram", method=RequestMethod.GET)
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
    	        //dto.setDiagramResource("data:image/jpeg;base64," + CommonUtils.getImageStr(imageStream));
    		        
    	        
 			   ByteArrayOutputStream bos = new ByteArrayOutputStream();
 			   byte[] buffer=new byte[1024*10];
 			   int n=0;
 			 
 				while(-1!=(n=inputStream.read(buffer))){
 					   bos.write(buffer,0,n);
 				   }
 				
 				
 				byte[] data =bos.toByteArray();
 				
 				   String str=new String(data,"ISO-8859-1");
 				//  result.put("fileName", file.getOriginalFilename());
 				    result.put("file", str);
    	        
    	        
    	            result.put("rtnCode", "1");
    				result.put("rtnMsg", "获取流程图成功!");
    				result.put("file",str);
    				//result.put("file", CommonUtils.getImageStr(inputStream));
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
//		
//		ModelDiagramDTO dto = new ModelDiagramDTO();
//        ProcessDiagramGenerator processDiagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
//        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
//       InputStream imageStream = processDiagramGenerator.generateDiagram(bpmnModel, "png", new ArrayList<String>(),
//                new ArrayList<String>(), "宋体", "微软雅黑", "黑体", null, 2.0); 
//        dto.setDiagramResource("data:image/jpeg;base64," + CommonUtils.getImageStr(imageStream));
//		return dto;
	}
	
	@ApiOperation(value = "获取流程节点",notes = "根据流程定义id获取流程节点")
	@RequestMapping(value = "/nodes", method=RequestMethod.GET)
	public List<ActTaskNodeDTO> getModelNodes(@ApiParam("流程定义id") @RequestBody String processDefId) {
	    List<ActTaskNodeDTO> response = new ArrayList<ActTaskNodeDTO>();
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
                    response.add(node);
                }
            }
            return response;
        }else {
        	//todo null
        	return null;
        }
	}
	
}
