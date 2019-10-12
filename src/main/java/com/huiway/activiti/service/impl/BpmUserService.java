package com.huiway.activiti.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huiway.activiti.entity.BpmActUser;
import com.huiway.activiti.mapper.BpmActUserMapper;
import com.huiway.activiti.service.BpmUserInterface;

@Service
public class BpmUserService extends ServiceImpl<BpmActUserMapper, BpmActUser> implements BpmUserInterface {
	
	@Resource
	private BpmActUserMapper bpmActUserMapper;
}
