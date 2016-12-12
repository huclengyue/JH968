package com.sunteam.dao;


import java.util.ArrayList;

import com.sunteam.receiver.Alarmpublic;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


public class GetDbInfo {
	private DBOpenHelper helper;// 新建DBOpenHelper
	private SQLiteDatabase db;// 新建SQLiteDatabase
	
	private static final String USER_DBNAME = Alarmpublic.DB_ALARM; //Environment.getExternalStorageDirectory() + "//alarm.db";
	//private static final String USER_DBNAME = Environment.getExternalStorageDirectory() + "//alarm.db";

	
	public GetDbInfo(Context context)// 打开数据库
	{
//		Global.debug("\r\nGetDbInfo ===============111==USER_DBNAME="+ USER_DBNAME);
		helper = new DBOpenHelper(context, USER_DBNAME);		
	}

	/**
	 * 增加数据
	 * 
	 * @param alarminfo
	 */
	public void add(Alarminfo alarm, String table ) {	
/*		
		Alarmpublic.debug("\r\n add ============ table = " + table);
		Alarmpublic.debug("\r\n add =alarm.year = " + alarm.year);
		Alarmpublic.debug("\r\n add =alarm.month = " + alarm.month);
		Alarmpublic.debug("\r\n add =alarm.day = " + alarm.day);
		Alarmpublic.debug("\r\n add =alarm.hour = " + alarm.hour);
		Alarmpublic.debug("\r\n add =alarm.minute = " + alarm.minute);
		
		Alarmpublic.debug("\r\n add =alarm.onoff = " + alarm.onoff);
		Alarmpublic.debug("\r\n add =alarm.filename = " + alarm.filename);
		*/
		db = helper.getWritableDatabase();		
		// 增加数据命令
		String cmd = "insert into "+ table +" (_id, year, month, day , hour, minute, filename, path, type, onoff) values (?,?,?,?,?,?,?,?,?,?)";
		db.execSQL(	cmd, new Object[] { alarm.getid(),alarm.getYear(),alarm.getMonth(),alarm.getDay(), alarm.getHour(), alarm.getMinute(),alarm.getFileName(),alarm.getPath(), alarm.getType(), alarm.getOnoff()});
	}

	/**
	 * 更新数据
	 * 
	 * @param alarminfo
	 */
	public void update(Alarminfo alarm , String table) {
/*
		Alarmpublic.debug("\r\n update ============ table = " + table);
		Alarmpublic.debug("\r\n update =alarm.year = " + alarm.year);
		Alarmpublic.debug("\r\n update =alarm.month = " + alarm.month);
		Alarmpublic.debug("\r\n update =alarm.day = " + alarm.day);
		Alarmpublic.debug("\r\n update =alarm.hour = " + alarm.hour);
		Alarmpublic.debug("\r\n update =alarm.minute = " + alarm.minute);
		
		Alarmpublic.debug("\r\n update =alarm.onoff = " + alarm.onoff);
		Alarmpublic.debug("\r\n update =alarm.filename = " + alarm.filename);
		*/
		///Alarmpublic.debug("    update ======= \r\n");
		db = helper.getWritableDatabase();
		//更新数据命令
		String cmd = "update "+ table +" set year = ?,month = ?,day = ?,hour = ?,minute = ?, filename = ?,path = ?, type = ?,onoff = ? where _id = ?";
		db.execSQL(cmd,
				new Object[] { alarm.getYear(),alarm.getMonth(),alarm.getDay(), alarm.getHour(), alarm.getMinute(), alarm.getFileName(), alarm.getPath(), alarm.getType(), alarm.getOnoff(), alarm.getid() });
	}

	/**
	 * 查找数据
	 * 
	 * @param    num--- > 数据号
	 * @return
	 */
	public Alarminfo find(int num, String table) {
		//Global.debug("find === num = " + num);
		if(num <= 0)
		{
			num = 1;
		}
		db = helper.getWritableDatabase();
		String cmd = "select _id, year, month, day, hour, minute, filename,  path, type, onoff  from "+ table +" where _id = ?";
		Cursor cursor = db.rawQuery(cmd, new String[] { String.valueOf(num) });// 查找命令字
		if (cursor.moveToNext())
		{
			// 获取用户数据
			return new Alarminfo(
					cursor.getInt(cursor.getColumnIndex("_id")),
					cursor.getInt(cursor.getColumnIndex("year")),
					cursor.getInt(cursor.getColumnIndex("month")),
					cursor.getInt(cursor.getColumnIndex("day")),
					cursor.getInt(cursor.getColumnIndex("hour")),
					cursor.getInt(cursor.getColumnIndex("minute")),
					cursor.getString(cursor.getColumnIndex("filename")),
					cursor.getString(cursor.getColumnIndex("path")),
					cursor.getInt(cursor.getColumnIndex("type")),
					cursor.getInt(cursor.getColumnIndex("onoff"))
					);	
		}
		return null;// 或找到返回空
	}

	/**
	 * 删除表数据
	 * 
	 * @param ids
	 */
	public void detele(String table) {
		db = helper.getWritableDatabase();// ��ʼ��SQLiteDatabase����
		// 删除数据命令
		String cmd = "delete from "+ table;
		db.execSQL(cmd);
				
	}
	/**
	 * 删除一条数据
	 * 
	 * @param ids
	 */
	public void deteleForOne(int id, String table) {
		db = helper.getWritableDatabase();//  获取数据库操作
		
		// 删除数据命令
		String cmd = "delete from " + table +" where _id =?";
		
		try{
			db.execSQL(cmd, new Object[]{ id });
		}catch (SQLException ex) {
	        Alarmpublic.debug(" 删除数据失败+++++++++++++++++++++++++++\r\n");
	   }
				
	}
	/**
	 * 获取 列数
	 * 
	 * @return
	 */
	public int getCount(String table) {
		db = helper.getWritableDatabase();
		//String cmd = "select count(_id) from alarm";
		String cmd = "select count(*) from " + table;
	
		try{
			Cursor cursor = db.rawQuery(cmd,null);//获取列数 命令
			if (cursor.moveToNext())
			{
				return cursor.getInt(0);// 获取列数
			}
		}catch (SQLException ex) {
	        Alarmpublic.debug(" 获取条数失败+++++++++++++++++++++++++++\r\n");
	   }
		return 0;// 未查找
	}
	
	// 查询数据
	public ArrayList<Alarminfo> getAllData(String table) {
		db = helper.getWritableDatabase();
		String sql = "select * from " + table;  // id 倒序
		Cursor cursor = db.rawQuery(sql, null);
		ArrayList<Alarminfo> orderList = new ArrayList<Alarminfo>();
		try {
			if (null != cursor) {
				if (cursor.getCount() > 0) {
					while (cursor.moveToNext()) {
						Alarminfo alarminfo = new Alarminfo();					
						alarminfo._id = cursor.getInt(cursor.getColumnIndex("_id"));
						alarminfo.year = cursor.getInt(cursor.getColumnIndex("year"));
						alarminfo.month = cursor.getInt(cursor.getColumnIndex("month"));
						alarminfo.day = cursor.getInt(cursor.getColumnIndex("day"));
						alarminfo.hour = cursor.getInt(cursor.getColumnIndex("hour"));
						alarminfo.minute = cursor.getInt(cursor.getColumnIndex("minute"));
						alarminfo.filename = cursor.getString(cursor.getColumnIndex("filename"));
						alarminfo.path = cursor.getString(cursor.getColumnIndex("path"));
						alarminfo.type = cursor.getInt(cursor.getColumnIndex("type"));
						alarminfo.onoff = cursor.getInt(cursor.getColumnIndex("onoff"));
						orderList.add(alarminfo);
					}
				}
			}
		} finally {
			if (null != cursor) {
				cursor.close();
			}
			if (null != db) {
				db.close();
			}
		}
		return orderList;
	}	

	/**
	 * 获取表的最大数据个数
	 * 
	 * @return
	 */

	public int getMaxId(String tableType) {

		db = helper.getWritableDatabase();
		//String cmd = "select max(_id) from alarm";
		String cmd = "select max(_id) from " + tableType;
		
		try{
			Cursor cursor = db.rawQuery(cmd, null);// 获取最大数  命令
			while (cursor.moveToLast()) {
				return cursor.getInt(0);// 获取最大数据个数
			}
		}catch (SQLException ex) {
		        Alarmpublic.debug(" 获取最大Id 失败+++++++++++++++++++++++++++\r\n");
		}
		return 0;// 没有数据
	}

	public int getMinId(String tableType) {

		db = helper.getWritableDatabase();
		//String cmd = "select max(_id) from alarm";
		String cmd = "select min(_id) from " + tableType;
		
		try{
			Cursor cursor = db.rawQuery(cmd, null);// 获取最大数  命令
			while (cursor.moveToLast()) {
				return cursor.getInt(0);// 获取最大数据个数
			}
		}catch (SQLException ex) {
		        Alarmpublic.debug(" 获取最小Id 失败+++++++++++++++++++++++++++\r\n");
		}
		return 0;// 没有数据
	}
	
	public int closeDb() {
		db = helper.getWritableDatabase();
		db.close();
		//Global.debug("closeDb ++++++++++++++++++++");
		return 0;//关闭数据库
	}
}
