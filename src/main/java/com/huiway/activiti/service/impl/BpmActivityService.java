package com.huiway.activiti.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huiway.activiti.entity.BpmActRuTask;
import com.huiway.activiti.mapper.BpmActRuTaskMapper;
import com.huiway.activiti.service.BpmActivityInterface;

@Service
public class BpmActivityService extends ServiceImpl<BpmActRuTaskMapper, BpmActRuTask> implements BpmActivityInterface {
	
	@Resource
	private BpmActRuTaskMapper bpmActRuTaskMapper;
}
