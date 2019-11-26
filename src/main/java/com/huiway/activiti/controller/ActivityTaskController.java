package com.huiway.activiti.controller;

import java.io.BufferedReader;
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

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.huiway.activiti.dto.ActHiTaskNodeDTO;
import com.huiway.activiti.dto.ActivitiDto;
import com.huiway.activiti.dto.activitytask.TaskGatewayDTO;
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

@Slf4j
@Api(value="流程任务管理")
@RestController
@RequestMapping("/activiti/task")
public class ActivityTaskController {
	private String strEXCLUSIVE_GATEWAY_RESULT = "RESULT";  // RESULT
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
	private IdentityService identityService;
	@Autowired
	BpmActivityInterface bpmActivityService;
	
	
	@ApiOperation(value = "启动流程实例")
	@RequestMapping(value = "/startUp", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void create(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("application/json;charset=utf-8");
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "启动流程失败!");
		result.put("procDefId", null);
		BufferedReader streamReader = null;
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
			log.info("参数" + sb);
			jsonParam = JSONObject.parseObject(sb.toString());
			if (jsonParam != null) {
//				String tenantId = jsonParam.getString("tenantId");
//				if (StringUtils.isBlank(tenantId)) {
//					throw new MyExceptions("启动流程失败,tenantId不能为空！");
//				}
				String assignee = jsonParam.getString("assignee");
				// if(StringUtils.isBlank(assignee)){
				// throw new MyExceptions("启动流程失败,assignee不能为空！");
				// }
				String assigneeKey = jsonParam.getString("assigneeKey");

				String procDefId = jsonParam.getString("procDefId");
				if (StringUtils.isBlank(procDefId)) {
					result.put("rtnMsg", "启动流程失败,参数procDefId不能为空！");
					throw new MyExceptions("启动流程失败,procDefId不能为空！");
				}
				String businessKey = jsonParam.getString("businessKey");
				if (StringUtils.isBlank(businessKey)) {
					result.put("rtnMsg", "启动流程失败,参数businessKey不能为空！");
					throw new MyExceptions("启动流程失败,businessKey不能为空！");
				}
				String userId = jsonParam.getString("userId");
				if (StringUtils.isBlank(userId)) {
					result.put("rtnMsg", "启动流程失败,参数userId不能为空！");
					throw new MyExceptions("启动流程失败,userId不能为空！");
				}

				ActivitiDto dto = new ActivitiDto();
				Map<String, Object> map = new HashMap<String, Object>();
				if (!StringUtils.isEmpty(assigneeKey) && !StringUtils.isEmpty(assignee)) {
					map.put(assigneeKey, assignee);
				}
				// map.put("", value)
				processEngine.getIdentityService().setAuthenticatedUserId(userId);
				ProcessInstance pi = runtimeService.startProcessInstanceById(procDefId, businessKey, map);
				// .startProcessInstanceByKeyAndTenantId(procDefId,
				// businessKey,
				// map,
				// tenantId);

				dto.setProcDefId(procDefId);
				dto.setProcInstId(pi.getId());
				Map<String, Object> paramMap = new HashMap<>();
				paramMap.put("PROC_INST_ID_", pi.getId());
				List<BpmActRuTask> actRuTasks = (List<BpmActRuTask>) bpmActivityService.listByMap(paramMap);

				result.put("rtnCode", "1");
				result.put("rtnMsg", "启动流程成功!");
				result.put("bean", dto);
				result.put("beans", actRuTasks);
				log.info("启动流程成功" + result.toString());
			}

			// 直接将json信息打印出来
			// System.out.println(jsonParam.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
			log.info("启动流程" + e.getMessage());
		} finally {
			try {
				if (null != streamReader) {
					streamReader.close();
				}
				String result2 = result.toString();
				PrintWriter p = response.getWriter();
				p.println(result2);
				p.flush();
				p.close();
			} catch (Exception e) {
				e.getStackTrace();
				log.info("启动流程" + e.getMessage());
			}

		}

	}
	@ApiOperation(value = "删除用户")
	@RequestMapping(value = "/deleteUser", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void deleteUser(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("application/json;charset=utf-8");
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "删除失败!");
		result.put("procDefId", null);
		BufferedReader streamReader = null;
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
			log.info("参数" + sb);
			jsonParam = JSONObject.parseObject(sb.toString());
			if (jsonParam != null) {
				String userList = jsonParam.getString("userList");
				if (StringUtils.isBlank(userList)) {
					result.put("rtnMsg", "删除失败，参数userList不能为空！");
					throw new MyExceptions("删除失败,获取不到参数！");
				}
				List<Map> li = JSONObject.parseArray(userList, Map.class);
				if (li.isEmpty()) {
					throw new MyExceptions("删除失败,获取不到参数！");
				}
				for (Map map : li) {
					String userId = map.get("userId")==null?"": map.get("userId").toString();
					if(!StringUtils.isEmpty(userId)){
						identityService.deleteUser(userId);
					}
				}
				result.put("rtnCode", "1");
				result.put("rtnMsg", "删除成功!");
				result.put("bean", null);
				result.put("beans", null);
				log.info("删除成功" + result.toString());
			}
			// 直接将json信息打印出来
			// System.out.println(jsonParam.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
			log.info("删除失败" + e.getMessage());
		} finally {
			try {
				if (null != streamReader) {
					streamReader.close();
				}
				String result2 = result.toString();
				PrintWriter p = response.getWriter();
				p.println(result2);
				p.flush();
				p.close();
			} catch (Exception e) {
				e.getStackTrace();
				log.info("删除失败" + e.getMessage());
			}

		}

	}
	
	@ApiOperation(value = "删除组下的人员")
	@RequestMapping(value = "/deleteGroupUser", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void deleteGroudUser(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("application/json;charset=utf-8");
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "删除失败!");
		result.put("procDefId", null);
		BufferedReader streamReader = null;
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
			log.info("参数" + sb);
			jsonParam = JSONObject.parseObject(sb.toString());
			if (jsonParam != null) {
				String groupsAndUserList = jsonParam.getString("groupsAndUserList");
				if (StringUtils.isEmpty(groupsAndUserList)) {
					result.put("rtnMsg", "删除失败,参数groupsAndUserList不能为空！");
					throw new MyExceptions("删除失败,获取不到参数！");
				}
				List<Map> li = JSONObject.parseArray(groupsAndUserList, Map.class);
				if (li.isEmpty()) {
					throw new MyExceptions("删除失败,获取不到参数！");
				}
				for (Map map : li) {
					String groupsId = map.get("groupsId")==null?"":map.get("groupsId").toString();
					String userList = map.get("userList")==null?"":map.get("userList").toString();
					if (StringUtils.isBlank(userList)) {
						throw new MyExceptions("添加,userList不能为空！");
					}
					String[] userIds = userList.split(",");
					for (String userId : userIds) {
						if (!StringUtils.isEmpty(userId) && !StringUtils.isEmpty(groupsId)) {
							identityService.deleteMembership(userId, groupsId);
						}
					}
					
				}
				result.put("rtnCode", "1");
				result.put("rtnMsg", "删除成功!");
				result.put("bean", null);
				result.put("beans", null);
				log.info("删除成功" + result.toString());
			}

			// 直接将json信息打印出来
			// System.out.println(jsonParam.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
			log.info("删除失败" + e.getMessage());
		} finally {
			try {
				if (null != streamReader) {
					streamReader.close();
				}
				String result2 = result.toString();
				PrintWriter p = response.getWriter();
				p.println(result2);
				p.flush();
				p.close();
			} catch (Exception e) {
				e.getStackTrace();
				log.info("删除失败" + e.getMessage());
			}

		}

	}
	@ApiOperation(value = "删除組")
	@RequestMapping(value = "/deleteGroup", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void deleteGroud(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("application/json;charset=utf-8");
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "删除失败!");
		result.put("procDefId", null);
		BufferedReader streamReader = null;
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
			log.info("参数" + sb);
			jsonParam = JSONObject.parseObject(sb.toString());
			if (jsonParam != null) {
				String groupsIdList = jsonParam.getString("groupsIdList");
				if (StringUtils.isBlank(groupsIdList)) {
					result.put("rtnMsg", "删除失败,参数groupsIdList不能为空！");
					throw new MyExceptions("删除失败,获取不到参数！");
				}
				List<Map> li = JSONObject.parseArray(groupsIdList, Map.class);
				if (li.isEmpty()) {
					throw new MyExceptions("删除失败,获取不到参数！");
				}
				for (Map map : li) {
					String groupsId =  map.get("groupsId")==null?"":map.get("groupsId").toString();
					if (!StringUtils.isEmpty(groupsId)) {
						identityService.deleteGroup(groupsId);
					}

				}
				result.put("rtnCode", "1");
				result.put("rtnMsg", "删除成功!");
				result.put("bean", null);
				result.put("beans", null);
				log.info("删除成功" + result.toString());
			}

			// 直接将json信息打印出来
			// System.out.println(jsonParam.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
			log.info("删除失败" + e.getMessage());
		} finally {
			try {
				if (null != streamReader) {
					streamReader.close();
				}
				String result2 = result.toString();
				PrintWriter p = response.getWriter();
				p.println(result2);
				p.flush();
				p.close();
			} catch (Exception e) {
				e.getStackTrace();
				log.info("删除失败" + e.getMessage());
			}

		}

	}
	
	
	@ApiOperation(value = "添加用户")
	@RequestMapping(value = "/addUser", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void createUser(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("application/json;charset=utf-8");
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "添加失败!");
		result.put("procDefId", null);
		BufferedReader streamReader = null;
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
			log.info("参数" + sb);
			jsonParam = JSONObject.parseObject(sb.toString());
			if (jsonParam != null) {
				String userList = jsonParam.getString("userList");
				if (StringUtils.isBlank(userList)) {
					result.put("rtnMsg", "添加失败,参数userList不能为空！");
					throw new MyExceptions("添加失败,获取不到参数！");
				}
				List<Map> li = JSONObject.parseArray(userList, Map.class);
				if (li.isEmpty()) {
					throw new MyExceptions("添加失败,获取不到参数！");
				}
				for (Map map : li) {
					String uId = map.get("userId") == null ? "" : map.get("userId").toString();
					if (!StringUtils.isBlank(userList)) {
						User u = identityService.createUserQuery().userId(uId).singleResult();
						if (u == null) {
							User user = identityService.newUser(uId);
							identityService.saveUser(user);
						}

					}

				}
				result.put("rtnCode", "1");
				result.put("rtnMsg", "添加成功!");
				result.put("bean", null);
				result.put("beans", null);
				log.info("添加成功" + result.toString());
			}
			// 直接将json信息打印出来
			// System.out.println(jsonParam.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
			log.info("添加失败" + e.getMessage());
		} finally {
			try {
				if (null != streamReader) {
					streamReader.close();
				}
				String result2 = result.toString();
				PrintWriter p = response.getWriter();
				p.println(result2);
				p.flush();
				p.close();
			} catch (Exception e) {
				e.getStackTrace();
				log.info("添加失败" + e.getMessage());
			}

		}

	}
	@ApiOperation(value = "添加組")
	@RequestMapping(value = "/addGroup", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void createGroup(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("application/json;charset=utf-8");
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "添加失败!");
		result.put("procDefId", null);
		BufferedReader streamReader = null;
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
			log.info("参数" + sb);
			jsonParam = JSONObject.parseObject(sb.toString());
			if (jsonParam != null) {
				String groupsList = jsonParam.getString("groupsList");
				if (StringUtils.isBlank(groupsList)) {
					result.put("rtnMsg", "添加失败,groupsList不能为空！");
					throw new MyExceptions("添加失败,获取不到参数！");
				}
				List<Map> li = JSONObject.parseArray(groupsList, Map.class);
				if (li.isEmpty()) {
					throw new MyExceptions("添加失败,获取不到参数！");
				}
				for (Map map : li) {
					String groupsId =  map.get("groupsId")==null?"":map.get("groupsId").toString();
					if (StringUtils.isBlank(groupsId)) {
						result.put("rtnMsg", "添加失败,groupsId不能为空！");
						throw new MyExceptions("添加失败,groupsId不能为空！");
					}
					String groupsName =  map.get("groupsName")==null?"":map.get("groupsName").toString();
					if (StringUtils.isBlank(groupsName)) {
						result.put("rtnMsg", "添加失败,groupsName不能为空！");
						throw new MyExceptions("添加失败,groupsName不能为空！");
					}
					// String userList=(String) map.get("userList");
					// if(StringUtils.isBlank(userList)){
					// throw new MyExceptions("添加,userList不能为空！");
					// }
					
					
					Group g=identityService.createGroupQuery().groupId(groupsId).singleResult();
					if(g==null){
						Group group = identityService.newGroup(groupsId);
						group.setName(groupsName);
						identityService.saveGroup(group);
					}
					//
					// String[] userIds=userList.split(",");
					//
					// for(String uId:userIds){
					// if(!StringUtils.isBlank(uId)){
					// User user=identityService.newUser(uId);
					// identityService.saveUser(user);
					// identityService.createMembership(uId,groupsId);
					// }
					//
					// }

				}
				result.put("rtnCode", "1");
				result.put("rtnMsg", "添加成功!");
				result.put("bean", null);
				result.put("beans", null);
				log.info("添加成功" + result.toString());
			}
			// 直接将json信息打印出来
			// System.out.println(jsonParam.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
			log.info("添加失败" + e.getMessage());
		} finally {
			try {
				if (null != streamReader) {
					streamReader.close();
				}
				String result2 = result.toString();
				PrintWriter p = response.getWriter();
				p.println(result2);
				p.flush();
				p.close();
			} catch (Exception e) {
				e.getStackTrace();
				log.info("添加失败" + e.getMessage());
			}

		}

	}
	
	@ApiOperation(value = "添加組下的人员")
	@RequestMapping(value = "/addGroupUser", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void createGroupUser(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("application/json;charset=utf-8");
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "添加失败!");
		result.put("procDefId", null);
		BufferedReader streamReader = null;
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
			log.info("参数" + sb);
			jsonParam = JSONObject.parseObject(sb.toString());
			if (jsonParam != null) {
				String groupsAndUser = jsonParam.getString("groupsAndUser");
				if (StringUtils.isBlank(groupsAndUser)) {
					result.put("rtnMsg", "添加失败,groupsAndUser不能为空！");
					throw new MyExceptions("添加失败,获取不到参数！");
				}
				List<Map> li = JSONObject.parseArray(groupsAndUser, Map.class);
				if (li.isEmpty()) {
					throw new MyExceptions("添加失败,获取不到参数！");
				}
				for (Map map : li) {
					String groupsId = map.get("groupsId")==null?"":map.get("groupsId").toString();
					if (StringUtils.isBlank(groupsId)) {
						result.put("rtnMsg", "添加失败,groupsId不能为空！");
						throw new MyExceptions("添加失败,groupsId不能为空！");
					}
					String userList = map.get("userList")==null?"":map.get("userList").toString();
					if (StringUtils.isBlank(userList)) {
						result.put("rtnMsg", "添加失败,userList不能为空！");
						throw new MyExceptions("添加,userList不能为空！");
					}
					String[] userIds = userList.split(",");
					for (String uId : userIds) {
						if (!StringUtils.isBlank(uId)) {
							boolean flag = false;
							List<User> uList = identityService.createUserQuery().memberOfGroup("groupsId").listPage(0,
									100);
							if (!uList.isEmpty()) {
								for (User uu : uList) {
									if (uu.getId().equals(uId)) {
										flag = true;
										break;
									}
								}
							}
							if (!flag) {
								identityService.createMembership(uId, groupsId);
							}

						}
					}

				}
				result.put("rtnCode", "1");
				result.put("rtnMsg", "添加成功!");
				result.put("bean", null);
				result.put("beans", null);
				log.info("添加成功" + result.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.info("添加失败" + e.getMessage());
		} finally {
			try {
				if (null != streamReader) {
					streamReader.close();
				}
				String result2 = result.toString();
				PrintWriter p = response.getWriter();
				p.println(result2);
				p.flush();
				p.close();
			} catch (Exception e) {
				e.getStackTrace();
				log.info("添加失败" + e.getMessage());
			}

		}

	}
	
	
	@ApiOperation(value = "获取流程图",notes = "根据流程实例id获取流程图")
	@RequestMapping(value = "/diagram", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void diagram(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("application/json;charset=utf-8");
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "获取流程图失败!");
		result.put("procDefId", null);
		BufferedReader streamReader = null;
		try {
			// 获取输入流
			streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));

			// 写入数据到Stringbuilder
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = streamReader.readLine()) != null) {
				sb.append(line);
			}
			log.info("参数" + sb);
			jsonParam = JSONObject.parseObject(sb.toString());
			InputStream inputStream = null;
			if (jsonParam != null) {
				String procInstId = jsonParam.getString("procInstId");
				if (StringUtils.isBlank(procInstId)) {
					result.put("rtnMsg", "获取流程图失败,procInstId不能为空！");
					throw new MyExceptions("获取流程图失败,procInstId不能为空！");
				}
				String str = getDiagram(procInstId);
				result.put("rtnCode", "1");
				result.put("file", str);
				result.put("rtnMsg", "获取流程图成功!");

				// result.put("file", CommonUtils.getImageStr(inputStream));
				log.info("获取流程图成功" + result.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.info("获取流程图失败" + e.getMessage());
		} finally {
			try {
				if (null != streamReader) {
					streamReader.close();
				}
				String result2 = result.toString();
				PrintWriter p = response.getWriter();
				p.println(result2);
				p.flush();
				p.close();
			} catch (Exception e) {
				e.getStackTrace();
				log.info("获取流程图失败" + e.getMessage());
			}

		}

	}
	
	
    @ApiOperation(value = "获取任务历史", notes = "根据流程实例id获取任务历史节点")
    @RequestMapping(value = "/history/tasks", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void getHiTask(HttpServletRequest request, HttpServletResponse response) {

		response.setContentType("application/json;charset=utf-8");
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "任务失败!");
		result.put("procDefId", null);
		BufferedReader streamReader = null;
		try {
			// 获取输入流
			streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));

			// 写入数据到Stringbuilder
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = streamReader.readLine()) != null) {
				sb.append(line);
			}
			log.info("参数" + sb);
			jsonParam = JSONObject.parseObject(sb.toString());
			InputStream inputStream = null;
			if (jsonParam != null) {
				String procInstId = jsonParam.getString("procInstId");
				if (StringUtils.isBlank(procInstId)) {
					result.put("rtnMsg", "任务失败,procInstId不能为空！");
					throw new MyExceptions("任务失败,procInstId不能为空！");
				}
				List<ActHiTaskNodeDTO> responseDTOList = new ArrayList<ActHiTaskNodeDTO>();
				List<HistoricActivityInstance> hai = historyService.createHistoricActivityInstanceQuery()
						.processInstanceId(procInstId).orderByHistoricActivityInstanceStartTime().asc().list(); // 获取流程中已经执行的节点，按照执行先后顺序排序
				for (HistoricActivityInstance activityInstance : hai) {
					if (activityInstance.getActivityType().equals("userTask")) {
						ActHiTaskNodeDTO dto = new ActHiTaskNodeDTO();
						dto.setUserId(activityInstance.getAssignee());
						dto.setTaskDefKey(activityInstance.getActivityId());
						dto.setTaskName(activityInstance.getActivityName());
						dto.setStartTime(activityInstance.getStartTime());
						dto.setEndTime(activityInstance.getEndTime());
						dto.setTaskId(activityInstance.getTaskId());

						responseDTOList.add(dto);
					}
				}

				result.put("rtnCode", "1");
				result.put("rtnMsg", "任务成功!");
				result.put("beans", responseDTOList);
				// result.put("file", CommonUtils.getImageStr(inputStream));
				log.info("任务成功" + result.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.info("任务失败" + e.getMessage());
		} finally {
			try {
				if (null != streamReader) {
					streamReader.close();
				}
				String result2 = result.toString();
				PrintWriter p = response.getWriter();
				p.println(result2);
				p.flush();
				p.close();
			} catch (Exception e) {
				e.getStackTrace();
				log.info("任务失败" + e.getMessage());
			}

		}

	}
	
    
	@ApiOperation(value = "认领任务",notes = "认领任务")
	@RequestMapping(value = "/claim", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void claim(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("application/json;charset=utf-8");
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "认领任务失败!");
		result.put("procDefId", null);
		BufferedReader streamReader = null;
		try {
			// 获取输入流
			streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));

			// 写入数据到Stringbuilder
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = streamReader.readLine()) != null) {
				sb.append(line);
			}
			log.info("参数" + sb);
			jsonParam = JSONObject.parseObject(sb.toString());
			InputStream inputStream = null;
			if (jsonParam != null) {
				String taskId = jsonParam.getString("taskId");
				if (StringUtils.isBlank(taskId)) {
					result.put("rtnMsg", "认领任务失败,taskId不能为空！");
					throw new MyExceptions("认领任务失败,taskId不能为空！");
				}
				String userId = jsonParam.getString("userId");
				if (StringUtils.isBlank(userId)) {
					result.put("rtnMsg", "认领任务失败,userId不能为空！");
					throw new MyExceptions("认领任务失败,userId不能为空！");
				}
				Task acttask = taskService.createTaskQuery().taskId(taskId).singleResult();
				if (acttask != null) {
					taskService.claim(acttask.getId(), userId);
				}

				result.put("rtnCode", "1");

				result.put("rtnMsg", "认领任务成功!");
				result.put("rtnCode", "1");
				result.put("bean", null);
				result.put("beans", null);
				log.info("认领任务成功" + result.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.info("认领任务失败" + e.getMessage());
		} finally {
			try {
				if (null != streamReader) {
					streamReader.close();
				}
				String result2 = result.toString();
				PrintWriter p = response.getWriter();
				p.println(result2);
				p.flush();
				p.close();
			} catch (Exception e) {
				e.getStackTrace();
				log.info("认领任务失败" + e.getMessage());
			}

		}

	}
	
	
    @ApiOperation(value = "只撤回到上一步流程节点", notes = "只撤回到上一步流程节点")
    @RequestMapping(value = "/revocation/up", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void revocationUp(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("application/json;charset=utf-8");
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "只撤回到上一步流程节点任务失败!");
		result.put("procDefId", null);
		BufferedReader streamReader = null;
		try {
			// 获取输入流
			streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));

			// 写入数据到Stringbuilder
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = streamReader.readLine()) != null) {
				sb.append(line);
			}
			log.info("参数" + sb);
			jsonParam = JSONObject.parseObject(sb.toString());
			InputStream inputStream = null;
			if (jsonParam != null) {
				String taskId = jsonParam.getString("taskId");
				if (StringUtils.isBlank(taskId)) {
					result.put("rtnMsg", "只撤回到上一步流程节点任务失败,taskId不能为空！");
					throw new MyExceptions("只撤回到上一步流程节点任务失败,taskId不能为空！");
				}
				String assigneeKey = jsonParam.getString("assigneeKey");
				String userId = jsonParam.getString("userId");
				if (StringUtils.isBlank(userId)) {
					throw new MyExceptions("只撤回到上一步流程节点失败,userId不能为空！");
				}
				TaskService taskService = processEngine.getTaskService();

				Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

				if (task == null) {
					throw new ValidationError("流程未启动或已执行完成，无法撤回");
				}
                String procInstId=task.getProcessInstanceId();
				HistoryService historyService = processEngine.getHistoryService();
				RepositoryService repositoryService = processEngine.getRepositoryService();
				List<HistoricTaskInstance> htiList = historyService.createHistoricTaskInstanceQuery()
						.processInstanceId(procInstId).orderByTaskCreateTime().desc().list();
				String myTaskId = null;
				HistoricTaskInstance myTask = null;

				if (htiList.size() >= 2) {
					HistoricTaskInstance hti = htiList.get(1);
					if (userId.equals(hti.getAssignee())) {
						myTaskId = hti.getId();
						myTask = hti;
					}
				}

				if (null == myTaskId) {
					throw new ValidationError("该任务非当前用户提交，无法撤回");
				}

				String processDefinitionId = myTask.getProcessDefinitionId();
				ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) repositoryService
						.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
				BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);

				// 变量
				// Map<String, VariableInstance> variables =
				// runtimeService.getVariableInstances(currentTask.getExecutionId());
				String myActivityId = null;
				String assignee="";
				List<HistoricActivityInstance> haiList = historyService.createHistoricActivityInstanceQuery()
						.executionId(myTask.getExecutionId()).finished().list();
				for (HistoricActivityInstance hai : haiList) {
					if (myTaskId.equals(hai.getTaskId())) {
						myActivityId = hai.getActivityId();
						assignee=hai.getAssignee();
						break;
					}
				}
				FlowNode myFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(myActivityId);

				RuntimeService runtimeService = processEngine.getRuntimeService();

				Execution execution = runtimeService.createExecutionQuery().executionId(task.getExecutionId())
						.singleResult();
				String activityId = execution.getActivityId();
				System.out.println("revocation------->> activityId:" + activityId);
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

				Authentication.setAuthenticatedUserId(userId);
				taskService.addComment(task.getId(), task.getProcessInstanceId(), "撤回");
				//
				// Map<String, Object> currentVariables = new HashMap<String,
				// Object>();
				// currentVariables.put("applier", userId);
				// 完成任务
				// taskService.complete(task.getId(), currentVariables);
				taskService.complete(task.getId(), ImmutableMap.of(assigneeKey, userId));
				// 恢复原方向
				flowNode.setOutgoingFlows(oriSequenceFlows);

				Task newCurrentTask = taskService.createTaskQuery().processInstanceId(procInstId).singleResult();
				taskService.setAssignee(newCurrentTask.getId(), assignee);

				Map<String, Object> paramMap = new HashMap<>();
				paramMap.put("PROC_INST_ID_", procInstId);
				List<BpmActRuTask> bpmActRuTaskList = (List<BpmActRuTask>) bpmActivityService.listByMap(paramMap);

				// RestResponse response = new RestResponse();
				// response.setBeans(bpmActRuTaskList);
				// return response;
				//

				result.put("rtnCode", "1");

				result.put("rtnMsg", "任务完成成功!");
				result.put("rtnCode", "1");
				result.put("bean", null);
				result.put("beans", bpmActRuTaskList);
				log.info("任务完成成功" + result.toString());

			}
		} catch (Exception e) {
			e.printStackTrace();
			log.info("只撤回到上一步流程节点失败" + e.getMessage());
		} finally {
			try {
				if (null != streamReader) {
					streamReader.close();
				}
				String result2 = result.toString();
				PrintWriter p = response.getWriter();
				p.println(result2);
				p.flush();
				p.close();
			} catch (Exception e) {
				e.getStackTrace();
				log.info("只撤回到上一步流程节点失败" + e.getMessage());
			}

		}

	}
	
	
	@ApiOperation(value = "撤回任务到指定的流程节点",notes = "撤回任务到指定的流程节点")
	@RequestMapping(value = "/revocation", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void revocation(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("application/json;charset=utf-8");
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "任务失败!");
		result.put("procDefId", null);
		BufferedReader streamReader = null;
		try {
			// 获取输入流
			streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));

			// 写入数据到Stringbuilder
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = streamReader.readLine()) != null) {
				sb.append(line);
			}
			log.info("参数" + sb);
			jsonParam = JSONObject.parseObject(sb.toString());
			InputStream inputStream = null;
			if (jsonParam != null) {
				String taskId = jsonParam.getString("taskId");
				if (StringUtils.isBlank(taskId)) {
					result.put("rtnMsg", "任务失败,taskId不能为空！");
					throw new MyExceptions("任务失败,taskId不能为空！");
				}
				String taskKey = jsonParam.getString("taskDefKey");
				if (StringUtils.isBlank(taskKey)) {
					result.put("rtnMsg", "任务失败,taskDefKey不能为空！");
					throw new MyExceptions("任务失败,taskDefKey不能为空！");
				}
				String userId = jsonParam.getString("userId");
				if (StringUtils.isBlank(userId)) {
					result.put("rtnMsg", "任务失败,userId不能为空！");
					throw new MyExceptions("任务失败,userId不能为空！");
				}
				String assigneeKey = jsonParam.getString("assigneeKey");
				Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

				if (task == null) {
					throw new MyExceptions("流程未启动或已执行完成，无法撤回");
				}
				String processInstanceId=task.getProcessInstanceId();
				RepositoryService repositoryService = processEngine.getRepositoryService();
				List<HistoricTaskInstance> htiList = historyService.createHistoricTaskInstanceQuery()
						.processInstanceId(processInstanceId).orderByTaskCreateTime().desc().list();
				String myTaskId = null;
				HistoricTaskInstance myTask = null;

				for (HistoricTaskInstance hti : htiList) {
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
				// Map<String, VariableInstance> variables =
				// runtimeService.getVariableInstances(currentTask.getExecutionId());
				String myActivityId = null;
				List<HistoricActivityInstance> haiList = historyService.createHistoricActivityInstanceQuery()
						.executionId(myTask.getExecutionId()).finished().list();
				String assignee="";
				for (HistoricActivityInstance hai : haiList) {
					if (myTaskId.equals(hai.getTaskId())) {
						myActivityId = hai.getActivityId();
						assignee=hai.getAssignee();
						break;
					}
				}
				FlowNode myFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(myActivityId);

				Execution execution = runtimeService.createExecutionQuery().executionId(task.getExecutionId())
						.singleResult();
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

				Authentication.setAuthenticatedUserId(userId);
				taskService.addComment(task.getId(), task.getProcessInstanceId(), "撤回");

				// Map<String, Object> currentVariables = new HashMap<String,
				// Object>();
				// currentVariables.put("applier", userId);

				// 完成任务
				taskService.complete(task.getId(), ImmutableMap.of(assigneeKey, userId));
				// 恢复原方向
				flowNode.setOutgoingFlows(oriSequenceFlows);

				Task newCurrentTask = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
				taskService.setAssignee(newCurrentTask.getId(), assignee);

				Map<String, Object> paramMap = new HashMap<>();
				paramMap.put("PROC_INST_ID_", processInstanceId);
				List<BpmActRuTask> bpmActRuTaskList = (List<BpmActRuTask>) bpmActivityService.listByMap(paramMap);

				// RestResponse response = new RestResponse();
				// response.setBeans(bpmActRuTaskList);
				// return response;
				//

				result.put("rtnCode", "1");

				result.put("rtnMsg", "任务完成成功!");
				result.put("rtnCode", "1");
				result.put("bean", null);
				result.put("beans", bpmActRuTaskList);
				log.info("任务完成成功" + result.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.info("任务失败" + e.getMessage());
		} finally {
			try {
				if (null != streamReader) {
					streamReader.close();
				}
				String result2 = result.toString();
				PrintWriter p = response.getWriter();
				p.println(result2);
				p.flush();
				p.close();
			} catch (Exception e) {
				e.getStackTrace();
				log.info("任务失败" + e.getMessage());
			}

		}
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

							for (int j = i; j < historicActivityInstances.size(); j++) {

								HistoricActivityInstance historicActivityInstanceJ = historicActivityInstances.get(j);

								FlowNode sourceFlowNode = (FlowNode) bpmnModel.getMainProcess()
										.getFlowElement(sequenceFlow.getSourceRef(), true);

								if (sourceFlowNode
										.getBehavior() instanceof org.activiti.engine.impl.bpmn.behavior.ExclusiveGatewayActivityBehavior) {
									if (i < historicActivityInstances.size() - 1 && historicActivityInstances.get(i + 1)
											.getActivityId().equals(sequenceFlow.getTargetRef())) {
										tempMapList.add(UtilMisc.toMap("flowId", sequenceFlow.getId(),
												"activityStartTime",
												String.valueOf(historicActivityInstance.getStartTime().getTime())));
									}
								} else {
									if (historicActivityInstanceJ.getActivityId().equals(sequenceFlow.getTargetRef())) {
										tempMapList.add(UtilMisc.toMap("flowId", sequenceFlow.getId(),
												"activityStartTime",
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
	
    
    
    

    @ApiOperation(value = "删除流程任务", notes = "删除流程任务")
	@RequestMapping(value = "/delete", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void deleted(HttpServletRequest request, HttpServletResponse response) {

		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "删除流程任务失败!");
		result.put("procDefId", null);
		BufferedReader streamReader = null;
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
			log.info("参数" + sb);
			jsonParam = JSONObject.parseObject(sb.toString());
			if (jsonParam != null) {
				String procInstId = jsonParam.getString("procInstId");
				if (StringUtils.isBlank(procInstId)) {
					result.put("rtnMsg", "删除流程任务失败,procInstId不能为空！");
					throw new MyExceptions("删除流程任务,procInstId不能为空！");
				}
				processEngine.getRuntimeService().deleteProcessInstance(procInstId, "删除");

				result.put("rtnCode", "1");
				result.put("rtnMsg", "删除成功!");
				result.put("bean", null);
				result.put("beans", null);
				log.info("删除流程任务成功" + result.toString());
			}

			// 直接将json信息打印出来
			// System.out.println(jsonParam.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
			log.info("删除流程任务失败" + e.getMessage());
		} finally {
			try {
				if (null != streamReader) {
					streamReader.close();
				}
				String result2 = result.toString();
				PrintWriter p = response.getWriter();
				p.println(result2);
				p.flush();
				p.close();
			} catch (Exception e) {
				e.getStackTrace();
				log.info("删除流程任务失败" + e.getMessage());
			}

		}
	}

    
    /**
     * 获任务网关信息
     * 
     * @return
     */
    @ApiOperation(value = "获取任务网关信息", notes = "获任务网关信息")
    @RequestMapping(value = "/actTaskId/gateway", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void getTaskGateway(HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "获取任务网关信息失败!");
		result.put("procDefId", null);
		BufferedReader streamReader = null;
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
			log.info("参数" + sb);
			jsonParam = JSONObject.parseObject(sb.toString());
			if (jsonParam != null) {

				String taskId = jsonParam.getString("taskId");
				if (StringUtils.isBlank(taskId)) {
					result.put("rtnMsg", "获取任务网关信息失败,taskId不能为空！");
					throw new MyExceptions("获取任务网关信息失败,taskId不能为空！");
				}
				Task task = taskService.createTaskQuery() // 创建任务查询
						.taskId(taskId) // 根据任务id查询
						.singleResult();
				List<TaskGatewayDTO> gateWays = new ArrayList<TaskGatewayDTO>();
				if (task != null) {

					String processInstanceId = task.getProcessInstanceId();

					// 获取流程发布Id信息
					String definitionId = runtimeService.createProcessInstanceQuery()
							.processInstanceId(processInstanceId).singleResult().getProcessDefinitionId();
					// 当前流程节点Id信息
					String activityId = task.getTaskDefinitionKey();

					BpmnModel model = processEngine.getRepositoryService().getBpmnModel(definitionId);
					List<SequenceFlow> sequenceFlowList;
					FlowElement activeEl = model.getMainProcess().getFlowElement(activityId);
					if (activeEl instanceof org.activiti.bpmn.model.UserTask) { // 节点
						sequenceFlowList = ((org.activiti.bpmn.model.UserTask) activeEl).getOutgoingFlows();// 流出信息
						FlowElement targetEl = sequenceFlowList.get(0).getTargetFlowElement();

						gateWays = getTaskGateWayDepth(targetEl, 0);
					}
				}

				result.put("rtnCode", "1");
				result.put("rtnMsg", "获取任务网关信息成功!");
				result.put("bean", null);
				result.put("beans", gateWays);
				log.info("获取任务网关信息成功" + result.toString());
			}

			// 直接将json信息打印出来
			// System.out.println(jsonParam.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
			log.info("获取任务网关信息失败" + e.getMessage());
		} finally {
			try {
				if (null != streamReader) {
					streamReader.close();
				}
				String result2 = result.toString();
				PrintWriter p = response.getWriter();
				p.println(result2);
				p.flush();
				p.close();
			} catch (Exception e) {
				e.getStackTrace();
				log.info("获取任务网关信息失败" + e.getMessage());
			}

		}

	}

	/**
	 * 递归取网管出口信息
	 * 
	 * @param targetEl
	 * @param depth
	 * @return
	 */
	private List<TaskGatewayDTO> getTaskGateWayDepth(FlowElement targetEl, int depth) {
		List<TaskGatewayDTO> gateWays = new ArrayList<TaskGatewayDTO>();
		depth++;
		if (depth > 2) {
			return null;
		}

		List<SequenceFlow> sequenceFlowList;
		if (targetEl instanceof org.activiti.bpmn.model.ExclusiveGateway
				|| targetEl instanceof org.activiti.bpmn.model.InclusiveGateway) {
			sequenceFlowList = ((org.activiti.bpmn.model.Gateway) targetEl).getOutgoingFlows();// 流出信息
			for (SequenceFlow flow : sequenceFlowList) {
				String name = flow.getName();
				System.out.println("name=" + name);
				String condition = flow.getConditionExpression();
				TaskGatewayDTO gateWay = new TaskGatewayDTO();
				if (targetEl instanceof org.activiti.bpmn.model.ExclusiveGateway) {
					if (condition != null) {
						condition = condition.replace(" ", "").replace("{", "").replace("}", "").replace("$", "")
								.replace("\"", "").replace(strEXCLUSIVE_GATEWAY_RESULT, "").replace("=", "");
						gateWay.setName(name);
						gateWay.setCondition(condition);
						gateWays.add(gateWay);
					}
				} else {
					if (condition != null && name != null) {
						gateWay.setName(name);
						condition = condition.replace("${", "").replace("}", "");
						gateWay.setCondition(condition);
						gateWay.setMutiSelectFlag(true);
						gateWays.add(gateWay);
					}
				}
				FlowElement targetElSub = flow.getTargetFlowElement();
				gateWay.setItems(getTaskGateWayDepth(targetElSub, depth));
			} // ExclusiveGateway
		}
		return gateWays;
	}
    
    @ApiOperation(value = "暂停任务", notes = "暂停任务")
    @RequestMapping(value = "/actTaskId/suspend", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void updateStateSuspend(HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "暂停任务失败!");
		result.put("procDefId", null);
		BufferedReader streamReader = null;
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
			log.info("参数" + sb);
			jsonParam = JSONObject.parseObject(sb.toString());
			if (jsonParam != null) {

				String procInstId = jsonParam.getString("procInstId");
				if (StringUtils.isBlank(procInstId)) {
					result.put("rtnMsg", "暂停任务失败,procInstId不能为空！");
					throw new MyExceptions("暂停任务失败,procInstId不能为空！");
				}
				//挂起流程实例
				boolean suspended = runtimeService.createProcessInstanceQuery().processInstanceId(procInstId)
						.singleResult().isSuspended();
				if (!suspended) {
		            runtimeService.suspendProcessInstanceById(procInstId);
		            
				} else {
					throw new MyExceptions("This activity instance has already be suspened.");
				}
				result.put("rtnCode", "1");
				result.put("rtnMsg", "暂停任务成功!");
				result.put("bean", null);
				result.put("beans", null);
				log.info("暂停任务成功" + result.toString());
			}

			// 直接将json信息打印出来
		} catch (Exception e) {
			e.printStackTrace();
			log.info("暂停任务失败" + e.getMessage());
		} finally {
			try {
				if (null != streamReader) {
					streamReader.close();
				}
				String result2 = result.toString();
				PrintWriter p = response.getWriter();
				p.println(result2);
				p.flush();
				p.close();
			} catch (Exception e) {
				e.getStackTrace();
				log.info("暂停任务失败" + e.getMessage());
			}

		}

	}
    @ApiOperation(value = "转派任务", notes = "转派任务")
    @RequestMapping(value = "/change/active", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void updateChangeActive(HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "转派任务失败!");
		BufferedReader streamReader = null;
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
			log.info("参数" + sb);
			jsonParam = JSONObject.parseObject(sb.toString());
			if (jsonParam != null) {

				String taskId = jsonParam.getString("taskId");
				if (StringUtils.isBlank(taskId)) {
					result.put("rtnMsg", "转派任务失败,taskId不能为空！");
					throw new MyExceptions("转派任务失败,taskId不能为空！");
				}
//				String userId = jsonParam.getString("userId");
//				if (StringUtils.isBlank(userId)) {
//					throw new MyExceptions("转派任务失败,userId不能为空！");
//				}
				String receiver = jsonParam.getString("receiver");
				if (StringUtils.isBlank(receiver)) {
					result.put("rtnMsg", "转派任务失败,receiver不能为空！");
					throw new MyExceptions("转派任务失败,receiver不能为空！");
				}
				taskService.setAssignee(taskId, receiver);
				result.put("rtnCode", "1");
				result.put("rtnMsg", "转派任务成功!");
				result.put("bean", null);
				result.put("beans", null);
			}

			// 直接将json信息打印出来
		} catch (Exception e) {
			e.printStackTrace();
			log.info("转派任务失败" + e.getMessage());
		} finally {
			try {
				if (null != streamReader) {
					streamReader.close();
				}
				String result2 = result.toString();
				PrintWriter p = response.getWriter();
				p.println(result2);
				p.flush();
				p.close();
			} catch (Exception e) {
				e.getStackTrace();
				log.info("转派任务" + e.getMessage());
			}

		}
	}
    
    @ApiOperation(value = "激活任务", notes = "激活任务")
    @RequestMapping(value = "/actTaskId/active", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void updateStateActive(HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "激活任务失败!");
		result.put("procDefId", null);
		BufferedReader streamReader = null;
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
			log.info("参数" + sb);
			jsonParam = JSONObject.parseObject(sb.toString());
			if (jsonParam != null) {

				String procInstId = jsonParam.getString("procInstId");
				if (StringUtils.isBlank(procInstId)) {
					result.put("rtnMsg", "激活任务失败,procInstId不能为空！");
					throw new MyExceptions("激活任务失败,procInstId不能为空！");
				}
				boolean suspended = runtimeService.createProcessInstanceQuery().processInstanceId(procInstId)
						.singleResult().isSuspended();
			    if (suspended) {
			            runtimeService.activateProcessInstanceById(procInstId);
			   }else{
				   throw new MyExceptions("激活任务失败,This activity instance has already be activated.");
			   }
				Map<String, Object> paramMap = new HashMap<>();
				paramMap.put("PROC_INST_ID_", procInstId);
				List<BpmActRuTask> bpmActRuTaskList = (List<BpmActRuTask>) bpmActivityService.listByMap(paramMap);
			    
			    
			    
				result.put("rtnCode", "1");
				result.put("rtnMsg", "激活任务成功!");
				result.put("bean", null);
				result.put("beans", bpmActRuTaskList);
				log.info("激活任务成功" + result.toString());
			}

			// 直接将json信息打印出来
		} catch (Exception e) {
			e.printStackTrace();
			log.info("激活任务失败" + e.getMessage());
		} finally {
			try {
				if (null != streamReader) {
					streamReader.close();
				}
				String result2 = result.toString();
				PrintWriter p = response.getWriter();
				p.println(result2);
				p.flush();
				p.close();
			} catch (Exception e) {
				e.getStackTrace();
				log.info("激活任务" + e.getMessage());
			}

		}
	}
    
    
    @ApiOperation(value = "获取历史任务", notes = "根据流程实例id获取历史任务")
    @RequestMapping(value = "/ru/tasks", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void getRuTask(HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "获取历史任务失败!");
		result.put("procDefId", null);
		BufferedReader streamReader = null;
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
			log.info("参数" + sb);
			jsonParam = JSONObject.parseObject(sb.toString());
			if (jsonParam != null) {

				String procInstId = jsonParam.getString("procInstId");
				if (StringUtils.isBlank(procInstId)) {
					result.put("rtnMsg", "获取历史任务失败,procInstId不能为空！");
					throw new MyExceptions("获取历史任务失败,procInstId不能为空！");
				}
				List<HistoricTaskInstance> list = processEngine.getHistoryService() // 历史相关Service
						.createHistoricTaskInstanceQuery() // 创建历史任务实例查询
						.processInstanceId(procInstId) // 用流程实例id查询
						.orderByTaskCreateTime().asc()
						.list();

				result.put("rtnCode", "1");
				result.put("rtnMsg", "获取历史任务成功!");
				result.put("bean", null);
				result.put("beans", list);
				log.info("获取历史任务成功" + result.toString());
			}

			// 直接将json信息打印出来
		} catch (Exception e) {
			e.printStackTrace();
			log.info("获取历史任务失败" + e.getMessage());
		} finally {
			try {
				if (null != streamReader) {
					streamReader.close();
				}
				String result2 = result.toString();
				PrintWriter p = response.getWriter();
				p.println(result2);
				p.flush();
				p.close();
			} catch (Exception e) {
				e.getStackTrace();
				log.info("获取历史任务失败" + e.getMessage());
			}

		}
	}
    
    @ApiOperation(value = "获取任务历史节点", notes = "根据流程实例id获取历史节点")
    @RequestMapping(value = "/actiivty/tasks", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void getActivityRuTask(HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "获取任务历史节点失败!");
		result.put("procDefId", null);
		BufferedReader streamReader = null;
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
			log.info("参数" + sb);
			jsonParam = JSONObject.parseObject(sb.toString());
			if (jsonParam != null) {

				String procInstId = jsonParam.getString("procInstId");
				if (StringUtils.isBlank(procInstId)) {
					result.put("rtnMsg", "获取任务历史节点失败,procInstId不能为空！");
					throw new MyExceptions("任务历史节点失败,procInstId不能为空！");
				}
				List<HistoricActivityInstance> list = processEngine.getHistoryService() // 历史相关Service
						.createHistoricActivityInstanceQuery() // 创建历史活动实例查询
						.processInstanceId(procInstId) // 执行流程实例id
						.list();
				result.put("rtnCode", "1");
				result.put("rtnMsg", "任务历史节点!");
				result.put("bean", null);
				result.put("beans", list);
				log.info("任务历史节点成功" + result.toString());
			}

			// 直接将json信息打印出来
		} catch (Exception e) {
			e.printStackTrace();
			log.info("任务历史节点失败" + e.getMessage());
		} finally {
			try {
				if (null != streamReader) {
					streamReader.close();
				}
				String result2 = result.toString();
				PrintWriter p = response.getWriter();
				p.println(result2);
				p.flush();
				p.close();
			} catch (Exception e) {
				e.getStackTrace();
				log.info("任务历史节点失败" + e.getMessage());
			}

		}
	}
    
}
