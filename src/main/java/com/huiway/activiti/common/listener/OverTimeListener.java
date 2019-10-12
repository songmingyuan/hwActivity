package com.huiway.activiti.common.listener;

import java.util.Date;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.el.FixedValue;
import org.springframework.context.ConfigurableApplicationContext;

import com.huiway.activiti.Application;
import com.huiway.activiti.entity.TimeOutInfo;
import com.huiway.activiti.service.TimeOutInterface;
import com.huiway.activiti.utils.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OverTimeListener implements TaskListener{

	private static final long serialVersionUID = -9066786418012781008L;
	
	//同流程图中fieldName
	private FixedValue timeOut; 
	private FixedValue type; 

	@Override
	public void notify(DelegateTask delegateTask) {
		
		ConfigurableApplicationContext app = Application.getContext();
		
		TimeOutInterface timeOutService = (TimeOutInterface)app.getBean("timeOutService");
		
		String eventName = delegateTask.getEventName();
        if ("create".endsWith(eventName)) {
        	delegateTask.getAssignee();
        	Object timeOut_ = timeOut.getValue(delegateTask); 
        	Object type_ = type.getValue(delegateTask); 
        	
        	TimeOutInfo timeOutInfo = new TimeOutInfo();
        	timeOutInfo.setTaskId(delegateTask.getId());
        	timeOutInfo.setAssignee(delegateTask.getAssignee());
        	timeOutInfo.setTimeOut(timeOut_.toString());
        	timeOutInfo.setType(type_.toString());
        	timeOutInfo.setCreateDate(DateUtil.formatDate(new Date(), DateUtil.FMT_YYYY_MM_DD_HH_MM_SS) );
        	timeOutService.save(timeOutInfo);
        	
            log.info("create=========");
        }
        if("complete".endsWith(eventName)) {
        	//timeOutService.removeById("");
        	log.info("complete=========");
        }
        
	}

	public FixedValue getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(FixedValue timeOut) {
		this.timeOut = timeOut;
	}

	public FixedValue getType() {
		return type;
	}

	public void setType(FixedValue type) {
		this.type = type;
	}
	
	
	
}
