package com.huiway.activiti.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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

import com.huiway.activiti.common.bean.RestResponse;
import com.huiway.activiti.dto.ActTaskNodeDTO;
import com.huiway.activiti.dto.ActivitiDto;
import com.huiway.activiti.dto.model.ModelDiagramDTO;
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
	//@RequestMapping(value = "/deploy", method=RequestMethod.GET)
	@PostMapping (value = "/deploy")
	public RestResponse deploy(@RequestParam(value="file",required = false) MultipartFile file,HttpServletRequest request) {
        RestResponse response2 = new RestResponse("-1","部署失败！");
		
		// ActivitiDto response = new ActivitiDto();
		String tenantId = request.getParameter("tenantId");
		if(StringUtils.isBlank(tenantId)){
			return response2;
		}
		InputStream inputStream = null;
		try {
			inputStream = file.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String resourceName = file.getOriginalFilename();
		 //根据bpmn文件部署流程  
        Deployment deploy = repositoryService.createDeployment().addInputStream(resourceName, inputStream)
        		.tenantId(tenantId)
        		.deploy();  
        //获取流程定义  
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
        		.deploymentId(deploy.getId()).singleResult();  
        
       // response.setProcDefId(processDefinition.getKey());
        //
        if(StringUtils.isBlank(processDefinition.getKey())){
        	return response2;
        }
        log.info(response2.toString());
        response2.setRtnCode("1");
		response2.setMessage("部署成功！");
        
		return response2;
	}
	
	@ApiOperation(value = "获取流程图",notes = "根据流程定义id获取流程图")
	@RequestMapping(value = "/diagram", method=RequestMethod.GET)
	public ModelDiagramDTO diagram(@ApiParam("流程定义id") @RequestParam String processDefinitionId) {
		ModelDiagramDTO dto = new ModelDiagramDTO();
        ProcessDiagramGenerator processDiagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        InputStream imageStream = processDiagramGenerator.generateDiagram(bpmnModel, "png", new ArrayList<String>(),
                new ArrayList<String>(), "宋体", "微软雅黑", "黑体", null, 2.0); 
        dto.setDiagramResource("data:image/jpeg;base64," + CommonUtils.getImageStr(imageStream));
		return dto;
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
