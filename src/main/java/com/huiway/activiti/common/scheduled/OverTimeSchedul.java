package com.huiway.activiti.common.scheduled;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.huiway.activiti.common.constant.Constant;
import com.huiway.activiti.entity.TimeOutInfo;
import com.huiway.activiti.service.TimeOutInterface;
import com.huiway.activiti.utils.DateUtil;
import com.huiway.activiti.utils.SendEmail;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OverTimeSchedul {
	
	@Autowired
	TimeOutInterface timeOutService;
	
	//每15s执行一次
    //@Scheduled(cron = "*/30 * * * * *")
    public void fetchTask() {
    	log.info("OverTimeSchedul 执行定时更新任务...");
        
        List<TimeOutInfo> listMap = timeOutService.selectOverTimeByState();
        
        for(TimeOutInfo timeOutInfo : listMap) {
        	String create = timeOutInfo.getCreateDate();
        	int configDate = Integer.parseInt(timeOutInfo.getTimeOut());
        	String type = timeOutInfo.getType();
        	Date dateCreate = DateUtil.parseDate(create, DateUtil.FMT_YYYY_MM_DD_HH_MM_SS);
        	int difference = DateUtil.betweenTwoDatesH(new Date(),dateCreate);
        	if(difference > configDate) {
        		if(Constant.EMAIL.equals(type)) {
        			try {
						SendEmail.send(timeOutInfo.getEmail());
						
						timeOutInfo.setState(Constant.PROCESSED);
						timeOutService.update(timeOutInfo, new UpdateWrapper<TimeOutInfo>().eq("id", timeOutInfo.getId()));
					} catch (Exception e) {
						log.info("邮件发送失败");
						e.printStackTrace();
					}
        		}
        		
        	}
        }
    }
}
