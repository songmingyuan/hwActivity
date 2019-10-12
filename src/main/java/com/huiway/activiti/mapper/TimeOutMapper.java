package com.huiway.activiti.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huiway.activiti.entity.TimeOutInfo;

@Mapper
public interface TimeOutMapper extends BaseMapper<TimeOutInfo> {
	
	List<TimeOutInfo> selectOverTimeByState();
	
}
