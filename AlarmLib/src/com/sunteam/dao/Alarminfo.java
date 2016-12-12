package com.sunteam.dao;

public class Alarminfo {
	public int _id;// 存储 编号
	public int year; // 年
	public int month; // 月
	public int day; // 日
	
	public int hour;// 时
	public int minute;// 分
	public String filename;// 文件名 
	public String path;// 文件名
	public int type;// 闹钟烈性
	public int onoff;// 开关 标志  1 打开  0 关闭
	
	//private String time; // 日期
	
	public Alarminfo()// 默认构造函数
	{
		super();
	}

	// 定义有参构造函数，用来初始化支出信息实体类中的各个字段
	public Alarminfo(int id, int year, int month, int day, int hour, int minute, String filename, String path, int type, int onoff) {
		super();
		this._id = id;// 为支出编号赋值
		
		this.year = year;// 时间赋值
		this.month = month;// 时间赋值
		this.day = day;// 
		this.hour = hour;// 
		this.minute = minute;// 
		this.filename = filename;// 
		this.path = path;
		this.onoff = onoff;
		this.type = type;
		
	//	Global.debug("alarminfo ===============111===");
	}

	public int getid()// 设置编号的可读属性
	{
		return _id;
	}

	public void setid(int id)// 设置支出编号的可写属性
	{
		this._id = id;
	}

	public int getHour()// 读取小时
	{
		return hour;
	}

	public void setHour(int hour)// 设置小时
	{
		this.hour = hour;
	}

	public int getMinute()// 读取分
	{
		return minute;
	}

	public void setMinute(int minute)// 设置分
	{
		this.minute = minute;
	}
	
	public int getYear()// 获取年
	{
		return this.year;
	}
	public void setYear(int year)// 设置支 年
	{
		 this.year = year;
	}
	
	public void setMonth(int month)// 设置月
	{
		 this.month = month;
	}
	
	public int getMonth()// 获取月
	{
		return this.month;
	}
	
	
	public void setDay(int day)// 设置 日
	{
		 this.day = day;
	}
	
	public int getDay()// 获取 日
	{
		return this.day;
	}	
	
	public String getPath()// 获取文件路径
	{
		return path;
	}

	public void setPath(String path)// 设置文件路径
	{
		this.path = path;
	}

	public String getFileName()// 获取文件名 
	{
		return filename;
	}

	public void setFileName(String filename)// 设置文件名
	{
		this.filename = filename;
	}
	
	public int getOnoff()// 获取开关标志
	{
		//Global.debug("\r\n getOnoff == onoff = " + onoff);
		return onoff;
	}

	public void setOnoff(int onoff)//  设置开关标志
	{
		this.onoff = onoff;
	}
	
	public int getType()// 获取闹钟列别
	{
		return type;
	}

	public void setType(int type)// 设置闹钟列别
	{
		this.type = type;
	}

}
