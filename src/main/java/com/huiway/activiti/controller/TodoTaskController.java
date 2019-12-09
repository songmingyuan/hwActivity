package com.huiway.activiti.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.huiway.activiti.dto.ActivitiDto;
import com.huiway.activiti.entity.BpmActRuTask;
import com.huiway.activiti.exception.MyExceptions;
import com.huiway.activiti.exception.ValidationError;
import com.huiway.activiti.service.BpmActivityInterface;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Api(value = "我的任务管理")
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

	@ApiOperation(value = "完成任务", notes = "完成任务")
	@RequestMapping(value = "/complete", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
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
				Map<String, Object> params = JSONObject.parseObject(jsonParam.toJSONString(),
						new TypeReference<Map<String, Object>>() {
						});
				String assignee = jsonParam.getString("assignee");
				String assigneeKey = jsonParam.getString("assigneeKey");
				String taskId = jsonParam.getString("taskId");
				if (StringUtils.isBlank(taskId)) {
					result.put("rtnMsg", "完成任务失败,参数taskId不能为空！");
					throw new MyExceptions("完成任务失败,taskId不能为空！");
				}
				String userId = jsonParam.getString("userId");
				if (StringUtils.isBlank(userId)) {

					result.put("rtnMsg", "完成任务失败,参数userId不能为空！");
					throw new MyExceptions("完成任务失败,userId不能为空！");
				}

				ActivitiDto dto = new ActivitiDto();
				List<BpmActRuTask> bpmActRuTaskList = new ArrayList<BpmActRuTask>();
				Task task = taskService.createTaskQuery().taskId(taskId) // 根据任务id查询
						.singleResult();
				if (task != null) {
					String processInstanceId = task.getProcessInstanceId();
					String processDefinitionId = task.getProcessDefinitionId();

					if (!userId.equals(task.getAssignee())) {
						result.put("rtnMsg", "完成任务失败,该任务已不在您名下！");
						throw new MyExceptions("完成任务失败,该任务已不在您名下！");
					}

					boolean modelSuspended = repositoryService.createProcessDefinitionQuery()
							.processDefinitionId(processDefinitionId).singleResult().isSuspended();
					if (modelSuspended) {
						throw new ValidationError("已挂起This activity model has already be suspended.");
					}

					boolean instSuspended = runtimeService.createProcessInstanceQuery()
							.processInstanceId(processInstanceId).singleResult().isSuspended();
					if (instSuspended) {
						throw new ValidationError("已挂起This activity instance has already be suspended.");
					}
					taskService.complete(taskId, params);
					if (!StringUtils.isEmpty(assigneeKey)) {
						boolean flag = false;
						List<HistoricVariableInstance> list = processEngine.getHistoryService()
								.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId)
								.orderByProcessInstanceId().asc().list();
						if (!list.isEmpty()) {
							for (HistoricVariableInstance hti : list) {
								String name = hti.getVariableName();
								if (assigneeKey.equals(name)) {

									List<HistoricProcessInstance> list2 = processEngine.getHistoryService()
											.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId)
											.list();
									if (!list2.isEmpty()) {
										for (HistoricProcessInstance hi : list2) {
											assignee = hi.getStartUserId();
											flag = true;
											break;

										}
									}
								}
								if (flag) {
									break;
								}
							}
						}

					}

					Map<String, Object> paramMap = new HashMap<>();
					paramMap.put("PROC_INST_ID_", processInstanceId);
					bpmActRuTaskList = (List<BpmActRuTask>) bpmActivityService.listByMap(paramMap);
					if (!bpmActRuTaskList.isEmpty()) {
						for (BpmActRuTask bp : bpmActRuTaskList) {
							if (!StringUtils.isEmpty(assigneeKey) && !StringUtils.isEmpty(bp.getAssignee())) {
								if (bp.getAssignee().equals(assigneeKey)) {
									taskService.setAssignee(bp.getId(), assignee);
								}

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

					result.put("rtnCode", "-1");
					result.put("rtnMsg", "完成任务失败，找不到任务!");
					throw new MyExceptions("完成任务失败，找不到任务!");
				}

			}

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

	@ApiOperation(value = "完成任务", notes = "完成任务")
	@RequestMapping(value = "/complete/task", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	public void completeTask2(HttpServletRequest request, HttpServletResponse response) {
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
				Map<String, Object> params = JSONObject.parseObject(jsonParam.toJSONString(),
						new TypeReference<Map<String, Object>>() {
						});
				String assignee = jsonParam.getString("assignee");
				String assigneeKey = jsonParam.getString("assigneeKey");
				String taskId = jsonParam.getString("taskId");
				if (StringUtils.isBlank(taskId)) {
					result.put("rtnMsg", "完成任务失败,参数taskId不能为空！");
					throw new MyExceptions("完成任务失败,taskId不能为空！");
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
						throw new ValidationError("已挂起This activity model has already be suspended.");
					}

					boolean instSuspended = runtimeService.createProcessInstanceQuery()
							.processInstanceId(processInstanceId).singleResult().isSuspended();
					if (instSuspended) {
						throw new ValidationError("已挂起This activity instance has already be suspended.");
					}
					taskService.complete(taskId);
					Map<String, Object> paramMap = new HashMap<>();
					paramMap.put("PROC_INST_ID_", processInstanceId);
					bpmActRuTaskList = (List<BpmActRuTask>) bpmActivityService.listByMap(paramMap);
					dto.setProcDefId(processDefinitionId);
					dto.setProcInstId(processInstanceId);
					result.put("rtnCode", "1");
					result.put("rtnMsg", "完成任务成功!");
					result.put("bean", dto);
					result.put("beans", bpmActRuTaskList);
					log.info("完成任务成功" + result.toString());
				} else {

					result.put("rtnCode", "-1");
					result.put("rtnMsg", "完成任务失败，找不到任务!");
					throw new MyExceptions("完成任务失败，找不到任务!");
				}

			}
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

	@ApiOperation(value = "是否是会签的最后一个任务", notes = "根据流程实例id获取任务")
	@RequestMapping(value = "/get/isLastTask", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
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
			boolean flag = false;
			if (jsonParam != null) {

				String taskId = jsonParam.getString("taskId");
				if (StringUtils.isBlank(taskId)) {
					result.put("rtnMsg", "获取任务失败,参数taskId不能为空！");
					throw new MyExceptions("获取任务失败,taskId不能为空！");
				}
				String isJoinTask = jsonParam.getString("isJoinTask");
				if (StringUtils.isBlank(isJoinTask)) {
					throw new MyExceptions("获取任务失败,isJoinTask不能为空！");
				}
				Task task = taskService.createTaskQuery().taskId(taskId) // 根据任务id查询
						.singleResult();
				if (task != null) {
					String processInstanceId = task.getProcessInstanceId();

					if ("true".equals(isJoinTask)) {

						List<Task> tasklist = taskService.createTaskQuery().processInstanceId(processInstanceId)
								.taskName(task.getName()).list();
						if (tasklist.isEmpty() || tasklist.size() > 1) {
							flag = false;
						} else if (tasklist.size() == 1) {
							flag = true;
						}
					}

				} else {
					result.put("rtnCode", "-1");
					result.put("rtnMsg", "获取任务失败，找不到任务!");
					throw new MyExceptions("获取任务失败，找不到任务!");
				}
				Map<String, Object> rtnMap = new HashMap<>();
				rtnMap.put("isLastJoinTask", flag);

				result.put("rtnCode", "1");
				result.put("rtnMsg", "查询成功!");
				result.put("bean", null);
				result.put("beans", null);

				result.put("rtnMap", rtnMap);
				log.info("获取任务成功" + result.toString());
			} else {
				result.put("rtnCode", "-1");
				result.put("rtnMsg", "获取任务失败，找不到参数!");
				throw new MyExceptions("获取任务失败，找不到参数!");
			}

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
					result.put("rtnMsg", "获取组待办任务失败,参数userId不能为空！");
					throw new MyExceptions("获取组待办任务失败,userId不能为空！");
				}
				String procDefId = jsonParam.getString("procDefId");
				if (StringUtils.isBlank(procDefId)) {
					result.put("rtnMsg", "获取组待办任务失败,参数procDefId不能为空！");
					throw new MyExceptions("获取组待办任务失败,procDefId不能为空！");
				}
				List<Task> tasks = taskService.createTaskQuery().processDefinitionId(procDefId).taskCandidateUser(userId).orderByTaskCreateTime()
						.desc().list();
				List<BpmActRuTask> ruList = new ArrayList<>();
				if (!tasks.isEmpty()) {
					for (Task task : tasks) {
						String taskId = task.getId();
						BpmActRuTask bpmActRuTaskList = (BpmActRuTask) bpmActivityService.getById(taskId);
						ruList.add(bpmActRuTaskList);

					}

				}
				Map<String, Object> rtnMap = new HashMap<>();
				rtnMap.put("totalNumber", ruList.size());

				result.put("rtnCode", "1");
				result.put("rtnMsg", "获取组待办任务成功!");
				result.put("bean", null);
				result.put("beans", ruList);
				result.put("rtnMap", rtnMap);
				log.info("获取组待办任务成功" + result.toString());
			}

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

	
	
	
	
	
	@ApiOperation(value = "获取任务明细", notes = "根据流程实例id获取任务明细")
	@RequestMapping(value = "/get/task/info", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	public void getTaskInfo(HttpServletRequest request, HttpServletResponse response) {

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
				String taskId1 = jsonParam.getString("taskId");
				if (StringUtils.isBlank(taskId1)) {
					result.put("rtnMsg", "获取待办任务明细失败,参数taskId不能为空！");
					throw new MyExceptions("获取任务明细失败,taskId不能为空！");
				}

				List<Task> tasks = taskService.createTaskQuery().taskId(taskId1).list();
				List<BpmActRuTask> ruList = new ArrayList<>();
				if (!tasks.isEmpty()) {
					for (Task task : tasks) {
						String taskId = task.getId();
						BpmActRuTask bpmActRuTaskList = (BpmActRuTask) bpmActivityService.getById(taskId);
						ruList.add(bpmActRuTaskList);
					}

				} else {
					throw new MyExceptions("获取任务明细失败,查不到任务信息！");
				}

				result.put("rtnCode", "1");
				result.put("rtnMsg", "获取任务明细成功!");
				result.put("bean", null);
				result.put("beans", ruList);
				log.info("获取任务明细成功" + result.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.info("获取任务明细失败" + e.getMessage());
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
				log.info("获取任务明细失败" + e.getMessage());
			}

		}

	}

	@ApiOperation(value = "获取待办任务信息", notes = "根据流程实例id获取待办任务")
	@RequestMapping(value = "/get/procInstId", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	public void getTodoTaskByprocInstId(HttpServletRequest request, HttpServletResponse response) {

		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "获取待办任务信息失败!");
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
			List<Map<String, Object>> returnList = new ArrayList<>();
			if (jsonParam != null) {
				String procInstId = jsonParam.getString("procInstId");
				if (StringUtils.isBlank(procInstId)) {
					result.put("rtnMsg", "获取待办任务信息失败,参数procInstId不能为空！");
					throw new MyExceptions("获取待办任务信息失败,procInstId不能为空！");
				}

				Map<String, Object> paramMap = new HashMap<>();
				paramMap.put("PROC_INST_ID_", procInstId);
				List<BpmActRuTask> bpmActRuTaskList = (List<BpmActRuTask>) bpmActivityService.listByMap(paramMap);
				if (!bpmActRuTaskList.isEmpty()) {
					for (BpmActRuTask bpmActRuTask : bpmActRuTaskList) {
						Map<String, Object> bpmActRuTaskmap = new HashMap<>();
						// bpmActRuTaskmap.put("bpmActRuTask", bpmActRuTask);
						bpmActRuTaskmap.put("procInstId", bpmActRuTask.getProcInstId());
						bpmActRuTaskmap.put("taskId", bpmActRuTask.getAssignee());
						bpmActRuTaskmap.put("procInstId", bpmActRuTask.getId());
						bpmActRuTaskmap.put("taskDefKey", bpmActRuTask.getTaskDefKey());
						bpmActRuTaskmap.put("name", bpmActRuTask.getName());
						String assignee = bpmActRuTask.getAssignee();
						String taskId = bpmActRuTask.getId();
						if (StringUtils.isEmpty(assignee)) {
							List<IdentityLink> list = taskService.getIdentityLinksForTask(taskId);
							List<Map<String, Object>> identityLinkList = new ArrayList<>();
							for (IdentityLink identityLink : list) {
								Map<String, Object> identityLinkMap = new HashMap<>();
								identityLinkMap.put("type", identityLink.getType());
								identityLinkMap.put("userId", identityLink.getUserId());
								identityLinkMap.put("groupId", identityLink.getGroupId());
								identityLinkMap.put("procInstId", identityLink.getProcessInstanceId());
								identityLinkList.add(identityLinkMap);
							}

							bpmActRuTaskmap.put("identityLink", identityLinkList);
						} else {
							bpmActRuTaskmap.put("assignee", assignee);
						}
						returnList.add(bpmActRuTaskmap);
					}
				}
				Map<String, Object> rtnMap = new HashMap<>();
				rtnMap.put("totalNumber", returnList.size());

				result.put("rtnCode", "1");
				result.put("rtnMsg", "获取待办任务信息成功!");
				result.put("bean", null);
				result.put("beans", returnList);
				result.put("rtnMap", rtnMap);
				log.info("获取待办任务信息成功" + result.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.info("获取待办任务信息失败" + e.getMessage());
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
				log.info("获取待办任务信息失败" + e.getMessage());
			}

		}

	}
	@ApiOperation(value = "完成任务", notes = "完成任务")
	@RequestMapping(value = "/complete/all", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	public void completeTaskAll(HttpServletRequest request, HttpServletResponse response) {
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
				Map<String, Object> params = JSONObject.parseObject(jsonParam.toJSONString(),
						new TypeReference<Map<String, Object>>() {
						});
				String assignee = jsonParam.getString("assignee");
				String assigneeKey = jsonParam.getString("assigneeKey");
				String taskId = jsonParam.getString("taskId");
				if (StringUtils.isBlank(taskId)) {
					result.put("rtnMsg", "完成任务失败,参数taskId不能为空！");
					throw new MyExceptions("完成任务失败,taskId不能为空！");
				}
				String userId = jsonParam.getString("userId");
				if (StringUtils.isBlank(userId)) {

					result.put("rtnMsg", "完成任务失败,参数userId不能为空！");
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
						throw new ValidationError("已挂起This activity model has already be suspended.");
					}

					boolean instSuspended = runtimeService.createProcessInstanceQuery()
							.processInstanceId(processInstanceId).singleResult().isSuspended();
					if (instSuspended) {
						throw new ValidationError("已挂起This activity instance has already be suspended.");
					}
					String person=task.getAssignee();
					if(StringUtils.isEmpty(person)){
						taskService.setAssignee(taskId, userId);
					}else{
						if (!userId.equals(task.getAssignee())) {
							result.put("rtnMsg", "完成任务失败,该任务已不在您名下！");
							throw new MyExceptions("完成任务失败,该任务已不在您名下！");
						}
					}
					
					
					taskService.complete(taskId, params);
					if (!StringUtils.isEmpty(assigneeKey)) {
						boolean flag = false;
						List<HistoricVariableInstance> list = processEngine.getHistoryService()
								.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId)
								.orderByProcessInstanceId().asc().list();
						if (!list.isEmpty()) {
							for (HistoricVariableInstance hti : list) {
								String name = hti.getVariableName();
								if (assigneeKey.equals(name)) {

									List<HistoricProcessInstance> list2 = processEngine.getHistoryService()
											.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId)
											.list();
									if (!list2.isEmpty()) {
										for (HistoricProcessInstance hi : list2) {
											assignee = hi.getStartUserId();
											flag = true;
											break;

										}
									}
								}
								if (flag) {
									break;
								}
							}
						}

					}

					Map<String, Object> paramMap = new HashMap<>();
					paramMap.put("PROC_INST_ID_", processInstanceId);
					bpmActRuTaskList = (List<BpmActRuTask>) bpmActivityService.listByMap(paramMap);
					if (!bpmActRuTaskList.isEmpty()) {
						for (BpmActRuTask bp : bpmActRuTaskList) {
							if (!StringUtils.isEmpty(assigneeKey) && !StringUtils.isEmpty(bp.getAssignee())) {
								if (bp.getAssignee().equals(assigneeKey)) {
									taskService.setAssignee(bp.getId(), assignee);
								}

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

					result.put("rtnCode", "-1");
					result.put("rtnMsg", "完成任务失败，找不到任务!");
					throw new MyExceptions("完成任务失败，找不到任务!");
				}

			}

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
	@ApiOperation(value = "获取待办任务", notes = "根据流程实例id获取待办任务")
	@RequestMapping(value = "/get/all", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	public void getTodoTaskAndGroup(HttpServletRequest request, HttpServletResponse response) {

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
					result.put("rtnMsg", "获取待办任务失败,参数userId不能为空！");
					throw new MyExceptions("获取待办任务失败,userId不能为空！");
				}
				String procDefId = jsonParam.getString("procDefId");
				if (StringUtils.isBlank(procDefId)) {
					result.put("rtnMsg", "获取待办任务失败,参数procDefId不能为空！");
					throw new MyExceptions("获取待办任务失败,procDefId不能为空！");
				}
				List<Task> tasks = taskService.createTaskQuery().processDefinitionId(procDefId)
						.taskAssignee(userId).orderByTaskCreateTime().desc()
						.list();

				List<Task> groupTasks = taskService.createTaskQuery().processDefinitionId(procDefId)
						.taskCandidateUser(userId).orderByTaskCreateTime()
						.desc().list();
				List<BpmActRuTask> ruList = new ArrayList<>();
				if (!tasks.isEmpty()) {
					for (Task task : tasks) {
						String taskId = task.getId();
						BpmActRuTask bpmActRuTaskList = (BpmActRuTask) bpmActivityService.getById(taskId);
						ruList.add(bpmActRuTaskList);

					}

				}
				if (!groupTasks.isEmpty()) {
					for (Task task : groupTasks) {
						String taskId = task.getId();
						BpmActRuTask bpmActRuTaskList = (BpmActRuTask) bpmActivityService.getById(taskId);
						ruList.add(bpmActRuTaskList);

					}

				}
				Map<String, Object> rtnMap = new HashMap<>();
				rtnMap.put("totalNumber", ruList.size());

				result.put("rtnCode", "1");
				result.put("rtnMsg", "获取待办任务成功!");
				result.put("bean", null);
				result.put("beans", ruList);
				result.put("rtnMap", rtnMap);
				log.info("获取待办任务成功" + result.toString());
			}

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
	
	
	
	

	@ApiOperation(value = "获取待办任务", notes = "根据流程实例id获取待办任务")
	@RequestMapping(value = "/get", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
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
					result.put("rtnMsg", "获取待办任务失败,参数userId不能为空！");
					throw new MyExceptions("获取待办任务失败,userId不能为空！");
				}
				String procDefId = jsonParam.getString("procDefId");
				if (StringUtils.isBlank(procDefId)) {
					result.put("rtnMsg", "获取待办任务失败,参数procDefId不能为空！");
					throw new MyExceptions("获取待办任务失败,procDefId不能为空！");
				}
				List<Task> tasks = taskService.createTaskQuery().processDefinitionId(procDefId)
						.taskAssignee(userId).orderByTaskCreateTime().desc()
						.list();

				List<BpmActRuTask> ruList = new ArrayList<>();
				if (!tasks.isEmpty()) {
					for (Task task : tasks) {
						String taskId = task.getId();
						BpmActRuTask bpmActRuTaskList = (BpmActRuTask) bpmActivityService.getById(taskId);
						ruList.add(bpmActRuTaskList);

					}

				}
				Map<String, Object> rtnMap = new HashMap<>();
				rtnMap.put("totalNumber", ruList.size());

				result.put("rtnCode", "1");
				result.put("rtnMsg", "获取待办任务成功!");
				result.put("bean", null);
				result.put("beans", ruList);
				result.put("rtnMap", rtnMap);
				log.info("获取待办任务成功" + result.toString());
			}

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
	@RequestMapping(value = "/next-node", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
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
					result.put("rtnMsg", "获取下一步任务节点失败,参数taskId不能为空！");
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
				boolean flag = false;
				if (model != null) {
					Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
					for (FlowElement e : flowElements) {
						if (e instanceof SubProcess) {
							flag = true;
							break;
						}
					}
				}

				List<SequenceFlow> sequenceFlowList;
				FlowElement activeEl = model.getMainProcess().getFlowElement(activityId);
				if (activeEl == null) {
					activeEl = model.getMainProcess().getFlowElement(activityId, flag);
				}

				List<Map<String, Object>> maplist = new ArrayList<>();
				if (model != null) {
					Collection<FlowElement> flowElements2 = model.getMainProcess().getFlowElements();
					for (FlowElement e : flowElements2) {
						Map<String, Object> map = new HashMap<>();
						map.put("activityId", e.getId());
						map.put("activityName", e.getName());
						maplist.add(map);

					}
				}
				List<Map<String, String>> list = new ArrayList<Map<String, String>>();

				if (activeEl instanceof org.activiti.bpmn.model.UserTask) { // 节点
					sequenceFlowList = ((org.activiti.bpmn.model.UserTask) activeEl).getOutgoingFlows();// 流出信息
					for (SequenceFlow sequenceFlow : sequenceFlowList) {
						Map<String, String> maps = new HashMap<>();
						maps.put("condition", sequenceFlow.getConditionExpression());
						maps.put("activityId", sequenceFlow.getTargetRef());

						for (Map<String, Object> m : maplist) {
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

	@ApiOperation(value = "获取所有流程任务节点", notes = "获取所有流程任务节点")
	@RequestMapping(value = "/get/procDefId/task", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	public void getTodoTaskNode2(HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonParam = null;
		JSONObject result = new JSONObject();
		result.put("rtnCode", "-1");
		result.put("rtnMsg", "获取所有流程任务节点失败!");
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
					result.put("rtnMsg", "获取所有流程任务节点失败,procDefId不能为空");
					throw new MyExceptions("获取所有流程任务节点失败,procDefId不能为空！");
				}

				BpmnModel model = repositoryService.getBpmnModel(procDefId);
				if (model != null) {
					Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
					for (FlowElement e : flowElements) {
						Map<String, Object> map = new HashMap<>();
						if (e instanceof UserTask) {
							UserTask userTask = (UserTask) e;
							map.put("activityId", e.getId());
							map.put("activityName", e.getName());
							map.put("assignee", userTask.getAssignee());
							map.put("candidateUsers", userTask.getCandidateUsers());
							map.put("candidateGroups", userTask.getCandidateGroups());
							MultiInstanceLoopCharacteristics ll = userTask.getLoopCharacteristics();
							if (ll != null) {
								map.put("isJoinTask", true);
								map.put("sequential", userTask.getLoopCharacteristics().isSequential());
								map.put("completionCondition",
										userTask.getLoopCharacteristics().getCompletionCondition());
								map.put("elementVariable", userTask.getLoopCharacteristics().getElementVariable());
								map.put("inputDataItem", userTask.getLoopCharacteristics().getInputDataItem());
							}
						} else if (e instanceof SubProcess) {
							SubProcess sp = (SubProcess) e;
							if (sp != null) {
								List<FlowElement> flowElementList = (List<FlowElement>) sp.getFlowElements();
								for (FlowElement ee : flowElementList) {
									Map<String, Object> maps = new HashMap<>();
									if (ee instanceof UserTask) {
										UserTask userTask = (UserTask) ee;
										maps.put("activityId", ee.getId());
										maps.put("activityName", ee.getName());
										maps.put("assignee", userTask.getAssignee());
										maps.put("candidateUsers", userTask.getCandidateUsers());
										maps.put("candidateGroups", userTask.getCandidateGroups());
										MultiInstanceLoopCharacteristics ll = userTask.getLoopCharacteristics();
										if (ll != null) {
											maps.put("isJoinTask", true);
											// map.put("loopCharacteristics",
											// userTask.getLoopCharacteristics());

											maps.put("sequential", userTask.getLoopCharacteristics().isSequential());
											maps.put("completionCondition",
													userTask.getLoopCharacteristics().getCompletionCondition());
											maps.put("elementVariable",
													userTask.getLoopCharacteristics().getElementVariable());
											maps.put("inputDataItem",
													userTask.getLoopCharacteristics().getInputDataItem());
										}
										// map.put("userTask", userTask);
									}
									if (maps.size() > 0) {
										list.add(maps);
									}

								}

							}

						}
						if (map.size() > 0) {
							list.add(map);
						}

					}
				}

			}

			result.put("rtnCode", "1");
			result.put("rtnMsg", "获取所有流程任务节点成功!");
			result.put("bean", null);
			result.put("beans", list);
			log.info("获取所有流程节点" + result.toString());

			// 直接将json信息打印出来
			// System.out.println(jsonParam.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
			log.info("获取所有流程任务节点失败" + e.getMessage());
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
				log.info("获取所有流程任务节点失败" + e.getMessage());
			}

		}

	}

	@ApiOperation(value = "获取所有流程节点", notes = "获取所有流程节点")
	@RequestMapping(value = "/get/procDefId", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
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
							MultiInstanceLoopCharacteristics ll = userTask.getLoopCharacteristics();
							if (ll != null) {
								map.put("isJoinTask", true);

								map.put("sequential", userTask.getLoopCharacteristics().isSequential());
								map.put("completionCondition",
										userTask.getLoopCharacteristics().getCompletionCondition());
								map.put("elementVariable", userTask.getLoopCharacteristics().getElementVariable());
								map.put("inputDataItem", userTask.getLoopCharacteristics().getInputDataItem());
							}

						} else if (e instanceof EndEvent) {
							EndEvent endEvent = (EndEvent) e;
							map.put("activityId", e.getId());
							map.put("activityName", e.getName());
						} else if (e instanceof SequenceFlow) {
							SequenceFlow sequenceFlow = (SequenceFlow) e;
							map.put("conditionExpression", sequenceFlow.getConditionExpression());
							map.put("flowId", e.getId());
							map.put("flowName", e.getName());
							map.put("activityId", sequenceFlow.getTargetRef());
						} else if (e instanceof SubProcess) {
							SubProcess sp = (SubProcess) e;
							if (sp != null) {
								List<FlowElement> flowElementList = (List<FlowElement>) sp.getFlowElements();
								for (FlowElement ee : flowElementList) {
									Map<String, Object> maps = new HashMap<>();
									if (ee instanceof StartEvent) {
										StartEvent startEvent = (StartEvent) ee;
										maps.put("initiator", startEvent.getInitiator());
										maps.put("activityId", ee.getId());
										maps.put("activityName", ee.getName());
									} else if (ee instanceof UserTask) {
										UserTask userTask = (UserTask) ee;
										maps.put("activityId", ee.getId());
										maps.put("activityName", ee.getName());
										maps.put("assignee", userTask.getAssignee());
										maps.put("candidateUsers", userTask.getCandidateUsers());
										maps.put("candidateGroups", userTask.getCandidateGroups());
										MultiInstanceLoopCharacteristics ll = userTask.getLoopCharacteristics();
										if (ll != null) {
											maps.put("isJoinTask", true);

											maps.put("sequential", userTask.getLoopCharacteristics().isSequential());
											maps.put("completionCondition",
													userTask.getLoopCharacteristics().getCompletionCondition());
											maps.put("elementVariable",
													userTask.getLoopCharacteristics().getElementVariable());
											maps.put("inputDataItem",
													userTask.getLoopCharacteristics().getInputDataItem());
										}

									} else if (ee instanceof EndEvent) {
										EndEvent endEvent = (EndEvent) ee;
										maps.put("activityId", ee.getId());
										maps.put("activityName", ee.getName());
									} else if (ee instanceof SequenceFlow) {
										SequenceFlow sequenceFlow = (SequenceFlow) ee;
										maps.put("conditionExpression", sequenceFlow.getConditionExpression());
										maps.put("flowId", ee.getId());
										maps.put("flowName", ee.getName());
										maps.put("activityId", sequenceFlow.getTargetRef());
									}
									if (maps.size() > 0) {
										list.add(maps);
									}

								}

							}

						} else if (e instanceof CallActivity) {
							CallActivity callActivity = (CallActivity) e;
							if (callActivity != null) {
								map.put("inParameters", callActivity.getInParameters());
								map.put("outParameters", callActivity.getOutParameters());
								map.put("businessKey", callActivity.getBusinessKey());
								if (callActivity.getBehavior() != null) {
									map.put("behavior", callActivity.getBehavior());
								}
							}

						}
						if (map.size() > 0) {
							list.add(map);
						}
					}
				}

			}

			result.put("rtnCode", "1");
			result.put("rtnMsg", "获取所有流程节点成功!");
			result.put("bean", null);
			result.put("beans", list);
			log.info("获取所有流程节点" + result.toString());

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

	@ApiOperation(value = "根据任务id获取历史任务节点", notes = "根据任务id获取历史任务节点")
	@RequestMapping(value = "/actiivty/reject", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	public void getActivityRuTask(HttpServletRequest request, HttpServletResponse response) {
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
				String taskId = jsonParam.getString("taskId");
				if (StringUtils.isBlank(taskId)) {
					throw new MyExceptions("获取历史任务失败,taskId不能为空！");
				}
				Task task = taskService.createTaskQuery().taskId(taskId) // 根据任务id查询
						.singleResult();
				if (task != null) {
					String processInstanceId = task.getProcessInstanceId();
					List<HistoricTaskInstance> list = processEngine.getHistoryService() // 历史相关Service
							.createHistoricTaskInstanceQuery() // 创建历史任务实例查询
							.processInstanceId(processInstanceId).finished()
							// 用流程实例id查询
							.orderByHistoricTaskInstanceEndTime().desc().list();

					result.put("rtnCode", "1");
					result.put("rtnMsg", "获取历史任务成功");
					result.put("bean", null);
					result.put("beans", list);
					log.info("获取历史任务成功" + result.toString());
				}

			}

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
	
	
	@ApiOperation(value = "根据任务id获取历史任务节点", notes = "根据任务id获取历史任务节点")
	@RequestMapping(value = "/end/task", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	public void getActivityRuTask2(HttpServletRequest request, HttpServletResponse response) {
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
					throw new MyExceptions("获取历史任务失败,procInstId不能为空！");
				}
				List<HistoricTaskInstance> list = processEngine.getHistoryService() // 历史相关Service
						.createHistoricTaskInstanceQuery() // 创建历史任务实例查询
						.processInstanceId(procInstId).finished()
						// 用流程实例id查询
						.orderByHistoricTaskInstanceEndTime().desc().list();

				result.put("rtnCode", "1");
				result.put("rtnMsg", "获取历史任务成功");
				result.put("bean", null);
				result.put("beans", list);
				log.info("获取历史任务成功" + result.toString());

			}

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
	
}
