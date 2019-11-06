package com.huiway.activiti.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.HistoricVariableInstanceQuery;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.huiway.activiti.dto.ActivitiDto;
import com.huiway.activiti.entity.BpmActRuTask;
import com.huiway.activiti.exception.MyExceptions;
import com.huiway.activiti.exception.ValidationError;
import com.huiway.activiti.service.BpmActivityInterface;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;


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
	@Autowired
	private IdentityService identityService;
	@ApiOperation(value = "完成任务",notes = "完成任务")
	@RequestMapping(value = "/complete", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void completeTask(HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "完成任务失败!");
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
				String tenantId = jsonParam.getString("tenantId");
				if (StringUtils.isBlank(tenantId)) {
					throw new MyExceptions("完成任务失败,tenantId不能为空！");
				}
				String assignee = jsonParam.getString("assignee");
				String assigneeKey = jsonParam.getString("assigneeKey");
				// if(StringUtils.isBlank(assignee)){
				// throw new MyExceptions("完成任务失败,assignee不能为空！");
				// }
				String taskId = jsonParam.getString("taskId");
				if (StringUtils.isBlank(taskId)) {
					throw new MyExceptions("完成任务失败,taskId不能为空！");
				}
				String judge = jsonParam.getString("judge");
				if (StringUtils.isBlank(judge)) {
					throw new MyExceptions("完成任务失败,judge不能为空！");
				}
				String userId = jsonParam.getString("userId");
				if (StringUtils.isBlank(userId)) {
					throw new MyExceptions("完成任务失败,userId不能为空！");
				}

				String condition = jsonParam.getString("condition");
				String conditionValue = jsonParam.getString("conditionValue");
				// String candidateUsers=jsonParam.getString("candidateUsers");
				// String
				// candidateGroups=jsonParam.getString("candidateGroups");
				String isJoinTask = jsonParam.getString("isJoinTask");
				ActivitiDto dto = new ActivitiDto();
				List<BpmActRuTask> bpmActRuTaskList = new ArrayList<BpmActRuTask>();
				Task task = taskService.createTaskQuery().taskId(taskId) // 根据任务id查询
						.singleResult();
				if (task != null) {
					String processInstanceId = task.getProcessInstanceId();
					String processDefinitionId = task.getProcessDefinitionId();

					// if (task.getOwner() != null &&
					// !task.getOwner().equals("null")) {
					// DelegationState delegationState =
					// task.getDelegationState();
					// if (delegationState.toString().equals("RESOLVED")) {
					// System.out.println("此委托任务已是完结状态");
					// } else if (delegationState.toString().equals("PENDING"))
					// {
					// taskService.resolveTask(taskId);// 解决委托
					// } else {
					// System.out.println("此任务不是委托任务");
					// }
					// }

					boolean modelSuspended = repositoryService.createProcessDefinitionQuery()
							.processDefinitionId(processDefinitionId).singleResult().isSuspended();
					if (modelSuspended) {
						// throw new ValidationError("");
						throw new ValidationError("已挂起This activity model has already be suspended.");
					}

					boolean instSuspended = runtimeService.createProcessInstanceQuery()
							.processInstanceId(processInstanceId).singleResult().isSuspended();
					if (instSuspended) {
						// throw new ValidationError("");
						throw new ValidationError("已挂起This activity instance has already be suspended.");
					}

					Map<String, Object> map = new HashMap<String, Object>();
					if (!StringUtils.isEmpty(judge)) {
						map.put("judge", judge);
					}
					if (!StringUtils.isEmpty(conditionValue) && !StringUtils.isEmpty(condition)) {
						map.put(condition, conditionValue);
					}

					if (!StringUtils.isEmpty(assigneeKey)) {
						if (!StringUtils.isEmpty(assignee)) {
							map.put(assigneeKey, assignee);
						} else {
							List<HistoricVariableInstance> list = processEngine.getHistoryService()
									.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).list();
							if (!list.isEmpty()) {
								for (HistoricVariableInstance hti : list) {
									String name = hti.getVariableName();
									if (assigneeKey.equals(name)) {
										assignee = (String) hti.getValue();
										break;
									}
								}
							}

							if (StringUtils.isEmpty(assignee)) {
								throw new MyExceptions("完成任务失败,查不到任务处理人！");
							}
							map.put(assigneeKey, assignee);
						}

					}

					String inputDataItem = jsonParam.getString("inputDataItem");
					String inputDataItemValue = jsonParam.getString("inputDataItemValue");

					if (!StringUtils.isEmpty(isJoinTask) && "true".equals(isJoinTask)) {
						boolean flag = isLastTask(processInstanceId, task.getName());
						if (flag) {
							if (!StringUtils.isEmpty(assigneeKey)) {
								if (!StringUtils.isEmpty(assignee)) {
									map.put(assigneeKey, assignee);
								} else {
									throw new MyExceptions("完成任务失败,assignee不能为空！");
								}
							} else {
								throw new MyExceptions("完成任务失败,assigneeKey不能为空！");
							}
						}
					}
					if (!StringUtils.isEmpty(inputDataItem) && !StringUtils.isEmpty(inputDataItemValue)) {
						String[] userCodes = inputDataItemValue.split(",");
						List<String> newList = new ArrayList<String>();
						for (String s : userCodes) {
							newList.add(s);
						}

						map = new HashMap<String, Object>();
						map.put(inputDataItem, newList);
					}

					taskService.complete(taskId, map);
					Map<String, Object> paramMap = new HashMap<>();
					paramMap.put("PROC_INST_ID_", processInstanceId);
					bpmActRuTaskList = (List<BpmActRuTask>) bpmActivityService.listByMap(paramMap);
					List<HistoricActivityInstance> htiList = historyService.createHistoricActivityInstanceQuery()
							.processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().desc()
							.list();
					if (!htiList.isEmpty()) {
						if (htiList.get(0).getActivityId().startsWith("endevent")) {
							HistoricActivityInstance hh = htiList.get(0);
							BpmActRuTask bb = new BpmActRuTask();
							bb.setName(hh.getActivityName());
							bb.setTaskDefKey(hh.getActivityId());
							bb.setProcDefId(hh.getProcessDefinitionId());
							bb.setProcInstId(hh.getProcessInstanceId());
							bpmActRuTaskList.add(bb);
							List<BpmActRuTask> bpmActRuTaskList2 = (List<BpmActRuTask>) bpmActivityService
									.listByMap(paramMap);
							if (!bpmActRuTaskList2.isEmpty()) {
								processEngine.getRuntimeService().deleteProcessInstance(processInstanceId, "结束");
							}

						}

					}

					dto.setProcDefId(processDefinitionId);
					dto.setProcInstId(processInstanceId);
					result.put("rtnCode", "1");
					result.put("rtnMsg", "完成任务成功!");
					result.put("bean", dto);
					result.put("beans", bpmActRuTaskList);
					log.info("完成任务成功" + result.toString());
				} else {
					// response.setRtnCode(CodeConstant.FAIL);
					// response.setMessage("task not found");
					// return response;

					result.put("rtnCode", "-1");
					result.put("rtnMsg", "完成任务失败，找不到任务!");
					throw new MyExceptions("完成任务失败，找不到任务!");
				}

			}

			// 直接将json信息打印出来
			// System.out.println(jsonParam.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
			log.info("完成任务失败" + e.getMessage());
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
				log.info("完成任务失败" + e.getMessage());
			}

		}

	}
	/**
	 * 是否是会签的最后一个任务
	 * @param processInstanceId
	 * @param taskName
	 * @return
	 */
	private boolean isLastTask(String processInstanceId, String taskName) {
		boolean flag = false;
		List<Task> list = taskService.createTaskQuery().processInstanceId(processInstanceId).taskName(taskName).list();
		if (list.isEmpty() || list.size() > 1) {
			flag = false;
		} else if (list.size() == 1) {
			flag = true;
			
		}
		return flag;
	}

	@ApiOperation(value = "是否是会签的最后一个任务", notes = "根据流程实例id获取待办任务")
	@RequestMapping(value = "/get/multiInstance", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	public void isLastTaskList(HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "获取任务失败!");
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
			List<Map<String, String>> list = new ArrayList<>();
			if (jsonParam != null) {

				String taskId = jsonParam.getString("taskId");
				if (StringUtils.isBlank(taskId)) {
					throw new MyExceptions("获取任务失败,taskId不能为空！");
				}
				String isJoinTask = jsonParam.getString("isJoinTask");
				if (StringUtils.isBlank(isJoinTask)) {
					throw new MyExceptions("获取任务失败,isJoinTask不能为空！");
				}
				Task task = taskService.createTaskQuery().taskId(taskId) // 根据任务id查询
						.singleResult();
				Map<String, Object> res = new HashMap<>();
				if (task != null) {
					String processInstanceId = task.getProcessInstanceId();
					boolean flag = false;
					if ("true".equals(isJoinTask)) {

						List<Task> tasklist = taskService.createTaskQuery().processInstanceId(processInstanceId)
								.taskName(task.getName()).list();
						if (tasklist.isEmpty() || tasklist.size() > 1) {
							flag = false;
						} else if (list.size() == 1) {
							flag = true;
						}
					}
					res.put("isLastJoinTask", flag);

				} else {
					result.put("rtnCode", "-1");
					result.put("rtnMsg", "获取任务失败，找不到任务!");
					throw new MyExceptions("获取任务失败，找不到任务!");
				}
				result.put("rtnCode", "1");
				result.put("rtnMsg", "查询成功!");
				result.put("bean", null);
				result.put("beans", null);
				result.put("map", res);
				log.info("获取任务成功" + result.toString());
			} else {
				result.put("rtnCode", "-1");
				result.put("rtnMsg", "获取任务失败，找不到参数!");
				throw new MyExceptions("获取任务失败，找不到参数!");
			}

			// 直接将json信息打印出来
			// System.out.println(jsonParam.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
			log.info("获取任务失败" + e.getMessage());
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
				log.info("获取任务失败" + e.getMessage());
			}

		}

	}

	@ApiOperation(value = "获取组待办任务", notes = "根据流程实例id获取待办任务")
	@RequestMapping(value = "/get/group", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	public void getTodoTaskGroup(HttpServletRequest request, HttpServletResponse response) {

		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "获取组待办任务失败!");
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
			List<Map<String, String>> list = new ArrayList<>();
			if (jsonParam != null) {

				String userId = jsonParam.getString("userId");
				if (StringUtils.isBlank(userId)) {
					throw new MyExceptions("获取组待办任务失败,userId不能为空！");
				}
				List<Task> tasks = taskService.createTaskQuery().taskCandidateUser(userId).orderByTaskCreateTime()
						.desc().list();
				List<BpmActRuTask> ruList = new ArrayList<>();
				if (!tasks.isEmpty()) {
					for (Task task : tasks) {
						String taskId = task.getId();
						Task t = taskService.createTaskQuery().taskId(taskId).singleResult();
						String processInstanceId = t.getProcessInstanceId();
						Map<String, Object> paramMap = new HashMap<>();
						paramMap.put("PROC_INST_ID_", processInstanceId);
						paramMap.put("ID_", taskId);
						List<BpmActRuTask> bpmActRuTaskList = (List<BpmActRuTask>) bpmActivityService
								.listByMap(paramMap);
						ruList.addAll(bpmActRuTaskList);
					}

				} else {
					throw new MyExceptions("获取组待办任务失败,查不到任务信息！");
				}

				result.put("rtnCode", "1");
				result.put("rtnMsg", "获取组待办任务成功!");
				result.put("bean", null);
				result.put("beans", ruList);
				log.info("获取组待办任务成功" + result.toString());
			}

			// 直接将json信息打印出来
			// System.out.println(jsonParam.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
			log.info("获取组待办任务失败" + e.getMessage());
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
				log.info("获取组待办任务失败" + e.getMessage());
			}

		}

	}
	
 
    
    
    
    @ApiOperation(value = "获取待办任务", notes = "根据流程实例id获取待办任务")
    @RequestMapping(value = "/get", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void getTodoTask(HttpServletRequest request, HttpServletResponse response) {

		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "获取待办任务失败!");
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
			List<Map<String, String>> list = new ArrayList<>();
			if (jsonParam != null) {
				String userId = jsonParam.getString("userId");
				if (StringUtils.isBlank(userId)) {
					throw new MyExceptions("获取待办任务失败,userId不能为空！");
				}

				List<Task> tasks = taskService.createTaskQuery().taskAssignee(userId).orderByTaskCreateTime().desc()
						.list();

				List<BpmActRuTask> ruList = new ArrayList<>();
				if (!tasks.isEmpty()) {
					for (Task task : tasks) {
						String taskId = task.getId();
						Task t = taskService.createTaskQuery().taskId(taskId).singleResult();
						String processInstanceId = t.getProcessInstanceId();
						Map<String, Object> paramMap = new HashMap<>();
						paramMap.put("PROC_INST_ID_", processInstanceId);
						paramMap.put("ID_", taskId);
						List<BpmActRuTask> bpmActRuTaskList = (List<BpmActRuTask>) bpmActivityService
								.listByMap(paramMap);
						ruList.addAll(bpmActRuTaskList);
					}

				} else {
					throw new MyExceptions("获取组待办任务失败,查不到任务信息！");
				}

				result.put("rtnCode", "1");
				result.put("rtnMsg", "获取待办任务成功!");
				result.put("bean", null);
				result.put("beans", ruList);
				log.info("获取待办任务成功" + result.toString());
			}

			// 直接将json信息打印出来
			// System.out.println(jsonParam.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
			log.info("获取待办任务失败" + e.getMessage());
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
				log.info("获取待办任务失败" + e.getMessage());
			}

		}

	}
    
    
    
    
    
    @ApiOperation(value = "获取下一步任务节点", notes = "获取下一步任务节点")
    @RequestMapping(value = "/next-node", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void getNextTaskNode(HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "获取下一步任务节点失败!");
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
					throw new MyExceptions("获取下一步任务节点,taskId不能为空！");
				}
				Task task = taskService.createTaskQuery() // 创建任务查询
						.taskId(taskId) // 根据任务id查询
						.singleResult();
				String processInstanceId = "";
				String processDefinitionId = "";
				// 当前流程节点Id信息
				String activityId = "";
				if (task != null) {
					processInstanceId = task.getProcessInstanceId();
					activityId = task.getTaskDefinitionKey();
					processDefinitionId = task.getProcessDefinitionId();
				} else {
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
				List<Map<String, String>> maplist = new ArrayList<>();
				BpmnModel model2 = repositoryService.getBpmnModel(processDefinitionId);
				if (model2 != null) {
					Collection<FlowElement> flowElements = model2.getMainProcess().getFlowElements();
					for (FlowElement e : flowElements) {
						Map<String, String> map = new HashMap<>();
						map.put("activityId", e.getId());
						map.put("activityName", e.getName());
						maplist.add(map);
					}
					// System.out.println("flowelement id:" + e.getId() + "
					// name:" + e.getName() + " class:" +
					// e.getClass().toString());
				}
				List<Map<String, String>> list = new ArrayList<Map<String, String>>();
				if (activeEl instanceof org.activiti.bpmn.model.UserTask) { // 节点
					sequenceFlowList = ((org.activiti.bpmn.model.UserTask) activeEl).getOutgoingFlows();// 流出信息
					for (SequenceFlow sequenceFlow : sequenceFlowList) {
						Map<String, String> maps = new HashMap<>();
						maps.put("condition", sequenceFlow.getConditionExpression());
						maps.put("activityId", sequenceFlow.getTargetRef());

						for (Map<String, String> m : maplist) {
							if (sequenceFlow.getTargetRef().equals(m.get("activityId").toString())) {
								maps.put("activityName", m.get("activityName").toString());
							}

						}
						list.add(maps);

					}

				}

				result.put("rtnCode", "1");
				result.put("rtnMsg", "获取下一步任务节点成功!");
				result.put("bean", null);
				result.put("beans", list);
				log.info("获取下一步任务节点成功" + result.toString());

			}

			// 直接将json信息打印出来
			// System.out.println(jsonParam.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
			log.info("获取下一步任务节点失败" + e.getMessage());
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
				log.info("获取下一步任务节点失败" + e.getMessage());
			}

		}

	}
	
    @ApiOperation(value = "获取所有流程节点", notes = "获取所有流程节点")
    @RequestMapping(value = "/get/procDefId", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void getTodoTaskNode(HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "获取所有流程节点失败!");
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
			List<Map<String, Object>> list = new ArrayList<>();
			if (jsonParam != null) {
				String procDefId = jsonParam.getString("procDefId");
				if (StringUtils.isBlank(procDefId)) {
					result.put("rtnCode", "-1");
					result.put("rtnMsg", "获取所有流程节点失败,procDefId不能为空");
					throw new MyExceptions("获取下一步任务节点失败,procDefId不能为空！");
				}

				BpmnModel model = repositoryService.getBpmnModel(procDefId);
				if (model != null) {
					Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
					for (FlowElement e : flowElements) {
						Map<String, Object> map = new HashMap<>();
						if (e instanceof StartEvent) {
							StartEvent startEvent = (StartEvent) e;
							map.put("initiator", startEvent.getInitiator());
							// map.put("startEvent", startEvent);
							map.put("activityId", e.getId());
							map.put("activityName", e.getName());
						} else if (e instanceof UserTask) {
							UserTask userTask = (UserTask) e;
							map.put("activityId", e.getId());
							map.put("activityName", e.getName());
							map.put("assignee", userTask.getAssignee());
							map.put("candidateUsers", userTask.getCandidateUsers());
							map.put("candidateGroups", userTask.getCandidateGroups());
							// map.put("outgoingFlows",
							// userTask.getOutgoingFlows());
							// map.put("incomingFlows",
							// userTask.getIncomingFlows());
							MultiInstanceLoopCharacteristics ll = userTask.getLoopCharacteristics();
							if (ll != null) {
								map.put("isJoinTask", true);
								//map.put("loopCharacteristics", userTask.getLoopCharacteristics());
								
								map.put("sequential", userTask.getLoopCharacteristics().isSequential());
								map.put("completionCondition",
										userTask.getLoopCharacteristics().getCompletionCondition());
								map.put("elementVariable", userTask.getLoopCharacteristics().getElementVariable());
								map.put("inputDataItem", userTask.getLoopCharacteristics().getInputDataItem());
							}


							// map.put("userTask", userTask);
						} else if (e instanceof EndEvent) {
							EndEvent endEvent = (EndEvent) e;
							// map.put("endEvent", endEvent);
							map.put("activityId", e.getId());
							map.put("activityName", e.getName());
						} else if(e instanceof SequenceFlow){
							SequenceFlow sequenceFlow = (SequenceFlow) e;
							// map.put("sequenceFlow", sequenceFlow);
							map.put("conditionExpression", sequenceFlow.getConditionExpression());
							map.put("flowId", e.getId());
							map.put("flowName", e.getName());
							map.put("activityId",sequenceFlow.getTargetRef() );
						}
						
						list.add(map);
					}
					// System.out.println("flowelement id:" + e.getId() + "
					// name:" + e.getName() + " class:" +
					// e.getClass().toString());
				}

			}

			result.put("rtnCode", "1");
			result.put("rtnMsg", "获取所有流程节点成功!");
			result.put("bean", null);
			result.put("beans", list);
			log.info("获取所有流程节点" + result.toString());

			// 直接将json信息打印出来
			// System.out.println(jsonParam.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
			log.info("获取所有流程节点失败" + e.getMessage());
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
				log.info("获取所有流程节点失败" + e.getMessage());
			}

		}

	}
 
    @ApiOperation(value = "获取可以驳回节点", notes = "根据任务id获取可以驳回的节点")
    @RequestMapping(value = "/actiivty/reject", method=RequestMethod.POST,produces="application/json;charset=utf-8")
	public void getActivityRuTask(HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "获取可以驳回节点失败!");
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
					throw new MyExceptions("获取可以驳回节点失败,taskId不能为空！");
				}
				Task task = taskService.createTaskQuery().taskId(taskId) // 根据任务id查询
						.singleResult();
				List<Map<String,Object>> resultList=new ArrayList<>();
				if (task != null) {
					String processInstanceId = task.getProcessInstanceId();
					String processDefinitionId = task.getProcessDefinitionId();
					List<HistoricActivityInstance> list = processEngine.getHistoryService() // 历史相关Service
							.createHistoricActivityInstanceQuery() // 创建历史活动实例查询
							.processInstanceId(processInstanceId) // 执行流程实例id
							.orderByHistoricActivityInstanceStartTime().desc().list();
					
					List<Map<String,Object>>  userTaskList=getUserTask(processDefinitionId);
					if(userTaskList.isEmpty()){
						throw new MyExceptions("获取可以驳回节点失败,根据流程定义Id查询不到流程节点信息！");
					}
					
					boolean flag=false;
					if(!list.isEmpty()){
						for(HistoricActivityInstance hai:list){
							Map<String,Object> resmap =new HashMap<>();
							if("userTask".equals(hai.getActivityType())){
								resmap.put("activityId", hai.getActivityId());
								resmap.put("activityName", hai.getActivityName());
							}
							for(Map<String,Object> map:userTaskList){
								String isJoinTask=map.get("isJoinTask")==null?"":map.get("isJoinTask").toString();
								String id=map.get("activityId")==null?"":map.get("activityId").toString();
								if(StringUtils.isEmpty(isJoinTask)){
									if(hai.getActivityId().equals(id)){
										flag=true;
										break;
									}
								}else{
									break;
								}
								
							}
							
							if(flag){
								resultList.add(resmap);
							}
							
							
						}
					}
					
					
					
					result.put("rtnCode", "1");
					result.put("rtnMsg", "获取可以驳回节点成功");
					result.put("bean", null);
					result.put("beans", resultList);
					log.info("获取可以驳回节点成功" + result.toString());
				}

			}

			// 直接将json信息打印出来
		} catch (Exception e) {
			e.printStackTrace();
			log.info("获取可以驳回节点失败" + e.getMessage());
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
				log.info("获取可以驳回节点失败" + e.getMessage());
			}

		}
	}
		
		private List<Map<String,Object>> getUserTask(String procDefId){
			List<Map<String, Object>> list = new ArrayList<>();
			BpmnModel model = repositoryService.getBpmnModel(procDefId);
			if (model != null) {
				Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
				for (FlowElement e : flowElements) {
					Map<String, Object> map = new HashMap<>();
					if (e instanceof UserTask) {
						UserTask userTask = (UserTask) e;
//						map.put("assignee", userTask.getAssignee());
//						map.put("candidateUsers", userTask.getCandidateUsers());
//						map.put("candidateGroups", userTask.getCandidateGroups());
						MultiInstanceLoopCharacteristics ll = userTask.getLoopCharacteristics();
						if (ll != null) {
							map.put("isJoinTask", true);
//							map.put("loopCharacteristics", userTask.getLoopCharacteristics());
//							map.put("completionCondition",
//									userTask.getLoopCharacteristics().getCompletionCondition());
//							map.put("elementVariable", userTask.getLoopCharacteristics().getElementVariable());
//							map.put("inputDataItem", userTask.getLoopCharacteristics().getInputDataItem());
						}
					}
					map.put("activityId", e.getId());
					map.put("activityName", e.getName());
					list.add(map);
				}
			}
			
			return list;
		}
}
