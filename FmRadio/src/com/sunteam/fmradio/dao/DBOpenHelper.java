package com.sunteam.fmradio.dao;

import com.sunteam.fmradio.utils.Global;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



// 新建数据库
public class DBOpenHelper extends SQLiteOpenHelper {
	private static final int VERSION = 1;// 版本信息
//	private static final String DBNAME = "massage.db";// 数据库名称
	private static final String table_cmd = " (_id integer primary key, chanel integer)";   // 最近播放 
		
	public DBOpenHelper(Context context, String path){// 

		super(context, path, null, VERSION);// 建立数据库
	}

	@Override
	public void onCreate(SQLiteDatabase db){// 首先建立每个数据的数据表
	
		try {
			Global.debug("   ====DBOpenHelper onCreate     ==== creat===");
			String cmd = "create table "+ Global.FM_LIST + table_cmd; // 创建 最近播放表
			db.execSQL(cmd); // 创建 表	
		}catch (SQLException e) {
			e.printStackTrace();
		}
		//db.execSQL("create table alarm (_id integer primary key, year integer, month integer,day integer, hour integer, minute integer, filename string, path string, onoff integer)"); // 创建 表
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
	}
}
