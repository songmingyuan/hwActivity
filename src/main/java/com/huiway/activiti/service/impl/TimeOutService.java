package com.huiway.activiti.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huiway.activiti.entity.TimeOutInfo;
import com.huiway.activiti.mapper.TimeOutMapper;
import com.huiway.activiti.service.TimeOutInterface;

@Service
public class TimeOutService extends ServiceImpl<TimeOutMapper, TimeOutInfo> implements TimeOutInterface{

	@Resource
	private TimeOutMapper timeOutMapper;

	@Override
	public List<TimeOutInfo> selectOverTimeByState() {
		return timeOutMapper.selectOverTimeByState();
	}
	
}
