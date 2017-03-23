package com.sunteam.fmradio.dao;

import com.sunteam.fmradio.utils.Global;

public class FmInfo {
	public int _id;// 存储 编号
	public int chanel;// 文件名 
	
	public FmInfo()// 默认构造函数
	{
		super();
	}

	// 定义有参构造函数，用来初始化支出信息实体类中的各个字段
	public FmInfo(int id, int chanel) {
		super();
		this._id = id;// 为支出编号赋值
		this.chanel = chanel;
		
//		Global.debug("alarminfo ===============111===");
	}

	public int getid()// 获取编号
	{
		return _id;
	}

	public void setid(int id)// 设置编号
	{
		this._id = id;
	}

	
	public int getChanel()// 获取文件路径
	{
		return chanel;
	}

	public void setPath(int chanel)// 设置文件路径
	{
		this.chanel = chanel;
	}


	
}
