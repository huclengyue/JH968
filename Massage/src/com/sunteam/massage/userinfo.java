package com.sunteam.massage;

public class userinfo {
	int _id;// 存储 编号
	double forwork;// 排钟时间
	double overwork;// 点钟时间
	int money;// 收入金额间
	//private String time; // 日期
	int year; // 年
	int month; // 月
	int day; // 日
	
	public userinfo()// 默认构造函数
	{
		super();
	}

	// 定义有参构造函数，用来初始化支出信息实体类中的各个字段
	public userinfo(int id, int year, int month, int day, double forwork, double overwork, int money) {
		super();
		this._id = id;// 为支出编号赋值
		this.money = money;// 金额赋值
		this.year = year;// 时间赋值
		this.month = month;// 时间赋值
		this.day = day;// 时间赋值
		this.forwork = forwork;// 为排钟类别赋值
		this.overwork = overwork;// 为点钟赋值
	}

	public int getid()// 设置支出编号的可读属性
	{
		return _id;
	}

	public void setid(int id)// 设置支出编号的可写属性
	{
		this._id = id;
	}

	public int getMoney()// 设置支出金额的可读属性
	{
		return money;
	}

	public void setMoney(int money)// 设置支出金额的可写属性
	{
		this.money = money;
	}

	public int getYear()// 设置支出时间的可读属性
	{
		return this.year;
	}
	public void setYear(int year)// 设置支出时间的可读属性
	{
		 this.year = year;
	}
	
	public void setMonth(int month)// 设置支出时间的可读属性
	{
		 this.month = month;
	}
	
	public int getMonth()// 设置支出时间的可读属性
	{
		return this.month;
	}
	
	
	public void setDay(int day)// 设置支出时间的可读属性
	{
		 this.day = day;
	}
	
	public int getDay()// 设置支出时间的可读属性
	{
		return this.day;
	}	
	
	public double getforwork()// 获取排钟
	{
		return forwork;
	}

	public void setforwork(double forwork)// 设置支出类别的可写属性
	{
		this.forwork = forwork;
	}

	public double getoverwork()// 设置支出地点的可读属性
	{
		return overwork;
	}

	public void setoverwork(double overwork)// 设置支出地点的可写属性
	{
		this.overwork = overwork;
	}

}
