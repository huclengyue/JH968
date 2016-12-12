package com.sunteam.dao;

import com.sunteam.receiver.Alarmpublic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// 新建数据库
public class DBOpenHelper extends SQLiteOpenHelper {
	private static final int VERSION = 1;// 版本信息
	
	public DBOpenHelper(Context context, String path){// 
		
		super(context, path, null, VERSION);// 建立数据库
	}

	@Override
	public void onCreate(SQLiteDatabase db){// 首先建立数据表
	
		Alarmpublic.debug("\r\n   ====DBOpenHelper onCreate     ==== creat===");
		db.execSQL("create table " + Alarmpublic.ALARM_TABLE + Alarmpublic.CMD_TABLE);
		db.execSQL("create table " + Alarmpublic.ANNIVERSARY_TABLE + Alarmpublic.CMD_TABLE);
		db.execSQL("create table " + Alarmpublic.REMIND_TABLE + Alarmpublic.CMD_TABLE);
		//db.execSQL("create table alarm (_id integer primary key, year integer, month integer,day integer, hour integer, minute integer, filename string, path string, onoff integer)"); // 创建 表

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
	}
}
