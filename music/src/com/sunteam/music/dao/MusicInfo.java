package com.sunteam.music.dao;

import com.sunteam.music.utils.Global;

public class MusicInfo {
	public int _id;// 存储 编号
	public String filename;// 文件名 
	public String path;// 文件名
	public int playtime;   // 播放时长
	
	public MusicInfo()// 默认构造函数
	{
		super();
	}

	// 定义有参构造函数，用来初始化支出信息实体类中的各个字段
	public MusicInfo(int id, String filename, String path, int playtime) {
		super();
		this._id = id;// 为支出编号赋值
		this.filename = filename;// 
		this.path = path;
		this.playtime = playtime;
		
		Global.debug("alarminfo ===============111===");
	}

	public int getid()// 获取编号
	{
		return _id;
	}

	public void setid(int id)// 设置编号
	{
		this._id = id;
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
	
	public int getPlayTime()// 获取文件名
	{
		return playtime;
	}

	public void setPlayTime(int playtime)// 设置文件名
	{
		this.playtime = playtime;
	}
	
}
