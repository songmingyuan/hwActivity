package com.huiway.activiti.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huiway.activiti.entity.TimeOutInfo;

public interface TimeOutInterface extends IService<TimeOutInfo>{
	
	List<TimeOutInfo> selectOverTimeByState();
	
}

