package  com.huiway.activiti.common.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

@SuppressWarnings("serial")
public class TaskListenerImpl implements TaskListener {
 
	/**用来指定任务的办理人*/
	@Override
	public void notify(DelegateTask delegateTask) {
		//指定个人任务的办理人，也可以指定组任务的办理人
		//个人任务：通过类去查询数据库，将下一个任务的办理人查询获取，然后通过setAssignee()的方法指定任务的办理人
		
		String taskDefKey=delegateTask.getTaskDefinitionKey();
		if("usertask6".equals(taskDefKey)){
			delegateTask.setAssignee("zhangsan");
		}else if("usertask10".equals(taskDefKey)){
			delegateTask.setAssignee("hwsystem");
		}
		
		
	}
}