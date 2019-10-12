package com.huiway.activiti.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
	
	/**
	 * yyyyMMdd 20150101
	 */
	public static final String FMT_YYYYMMDD = "yyyyMMdd";
	
	/**
	 * yyyy-MM-dd 2015-01-01
	 */
	public static final String FMT_YYYY_MM_DD = "yyyy-MM-dd";
	
	/**
	 * yyyy-MM-dd HH:mm:ss 2015-01-01 23:59:01
	 */
	public static final String FMT_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
	
	
	
	public static final String FMT_SOLR = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	
	public static String formatDate(Date date,String fmt){
		SimpleDateFormat format = new SimpleDateFormat(fmt);
		return format.format(date);
	}
	
	public static Integer formatY(Date date){
		if(date.after(new Date())){
			date = new Date();
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy");
		try{
			return Integer.parseInt(format.format(date));
		}catch (Exception e) {
			e.printStackTrace();
			return Integer.parseInt(format.format(new Date()));
		}
	}
	
	public static Integer formatYM(Date date){
		if(date.after(new Date())){
			date = new Date();
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
		try{
			return Integer.parseInt(format.format(date));
		}catch (Exception e) {
			e.printStackTrace();
			return Integer.parseInt(format.format(new Date()));
		}
	}
	
	public static Integer formatYMD(Date date){
		if(date.after(new Date())){
			date = new Date();
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		try{
			return Integer.parseInt(format.format(date));
		}catch (Exception e) {
			e.printStackTrace();
			return Integer.parseInt(format.format(new Date()));
		}
	}
	
	public static Integer formatYMDH(Date date){
		if(date.after(new Date())){
			date = new Date();
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH");
		try{
			return Integer.parseInt(format.format(date));
		}catch (Exception e) {
			e.printStackTrace();
			return Integer.parseInt(format.format(new Date()));
		}
	}
	
	public static Long formatYMDHm(Date date){
		if(date.after(new Date())){
			date = new Date();
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
		try{
			return Long.parseLong(format.format(date));
		}catch (Exception e) {
			e.printStackTrace();
			return Long.parseLong(format.format(new Date()));
		}
	}
	
	public static Date parseDate(String dateStr,String fmt){
		SimpleDateFormat format = new SimpleDateFormat(fmt);
		try {
			return format.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 获取当前日期
	 * @return
	 */
	public static Date getCurrentDate() {

		return new Date(System.currentTimeMillis());
	}
	
	/**
	 * 返回日期中的年份
	 * 
	 * @param date
	 *             日期
	 * @return 返回年份
	 */
	public static int getYear(Date date) {

		Calendar c = Calendar.getInstance();
		c.setTime(date);

		return c.get(Calendar.YEAR);
	}

	/**
	 * 返回日期中的月份
	 * 
	 * @param date
	 *             日期
	 * @return 返回月份
	 */
	public static int getMonth(Date date) {

		Calendar c = Calendar.getInstance();
		c.setTime(date);

		return c.get(Calendar.MONTH) + 1;
	}

	/**
	 * 返回日期中的日
	 * 
	 * @param date
	 *             日期
	 * @return 返回日
	 */
	public static int getDay(Date date) {

		Calendar c = Calendar.getInstance();
		c.setTime(date);

		return c.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 返回日期中的小时
	 * 
	 * @param date
	 *             日期
	 * @return 返回小时
	 */
	public static int getHour(Date date) {

		Calendar c = Calendar.getInstance();
		c.setTime(date);

		return c.get(Calendar.HOUR_OF_DAY);
	}

	/**
	 * 返回日期中的分钟
	 * 
	 * @param date
	 *             日期
	 * @return 返回分钟
	 */
	public static int getMinute(Date date) {

		Calendar c = Calendar.getInstance();
		c.setTime(date);

		return c.get(Calendar.MINUTE);
	}

	/**
	 * 返回日期中的秒钟
	 * 
	 * @param date
	 *             日期
	 * @return 返回秒钟
	 */
	public static int getSecond(Date date) {

		Calendar c = Calendar.getInstance();
		c.setTime(date);

		return c.get(Calendar.SECOND);
	}

	/**
	 * 返回日期代表的毫秒
	 * 
	 * @param date
	 *             日期
	 * @return 返回毫秒
	 */
	public static long getMillis(Date date) {

		Calendar c = Calendar.getInstance();
		c.setTime(date);

		return c.getTimeInMillis();
	}

	/**
	 * 返回当前日期代表的毫秒
	 * 
	 * @return
	 */
	public static long getCurrentTimeMillis() {

		return System.currentTimeMillis();
	}
	
	public static int getCompareDate(Date comDate){
		long s1=comDate.getTime();//将时间转为毫秒
		long s2=System.currentTimeMillis();//得到当前的毫秒
		int  day=(int) ((s2-s1)/1000/60/60/24);
		return day;
	}
	
	/**
	 * 两个日期date1-date2相减，相差的天数
	 * 
	 * @param date1
	 *             日期
	 * @param date2
	 *             日期
	 * @return 返回相减后的日期
	 */
	public static int betweenTwoDates(Date date1, Date date2) {

		return (int) ((getMillis(date1) - getMillis(date2)) / (24 * 3600 * 1000));
	}
	
	/**
	 * 两个日期date1-date2相减，相差的小时
	 * 
	 * @param date1
	 *             日期
	 * @param date2
	 *             日期
	 * @return 返回相减后的日期
	 */
	public static int betweenTwoDatesH(Date date1, Date date2) {

		return (int) ((getMillis(date1) - getMillis(date2)) / (3600 * 1000));
	}
	
	public static void main(String[] args) {
		
		while (true) {
			System.out.println(DateUtil.formatDate(DateUtil.getCurrentDate(), "yyyy-MM-dd : hh-mm-ss"));
			System.out.println("--------------等待5秒钟执行--------------------");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}
		
	}
}
