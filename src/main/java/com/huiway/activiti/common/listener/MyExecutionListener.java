package  com.huiway.activiti.common.listener;

import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("serial")
public abstract class MyExecutionListener implements ExecutionListener  {
	@Autowired
	private TaskService taskService;
	@Override
	public void notify(DelegateExecution execution) {

    String eventName = execution.getEventName();
    if("end".equals(eventName)){
    	// 完成任务
		taskService.complete(execution.getId());
	}
	}
}