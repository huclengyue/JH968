package com.sunteam.massage;

import com.sunteam.massage.utils.Global;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// 新建数据库
public class DBOpenHelper extends SQLiteOpenHelper {
	private static final int VERSION = 1;// 版本信息
//	private static final String DBNAME = "massage.db";// 数据库名称
	
	public DBOpenHelper(Context context, String path){// 
		super(context, path, null, VERSION);// 建立数据库
	}

	@Override
	public void onCreate(SQLiteDatabase db){// 首先建立每个用户的数据表
	
		Global.debug("DBOpenHelper onCreate     ==== creat===");
		db.execSQL("create table user1 (_id integer primary key, year integer, month integer,day integer, forwork Double, overwork Double, money integer)"); // �����û�1��Ϣ��
		db.execSQL("create table user2 (_id integer primary key, year integer, month integer,day integer, forwork Double, overwork Double, money integer)"); // �����û�2��Ϣ��
		db.execSQL("create table user3 (_id integer primary key, year integer, month integer,day integer, forwork Double, overwork Double, money integer)"); // �����û�3��Ϣ��
		db.execSQL("create table user4 (_id integer primary key, year integer, month integer,day integer, forwork Double, overwork Double, money integer)"); // �����û�4��Ϣ��
		db.execSQL("create table user5 (_id integer primary key, year integer, month integer,day integer, forwork Double, overwork Double, money integer)"); // �����û�5��Ϣ��
		db.execSQL("create table user6 (_id integer primary key, year integer, month integer,day integer, forwork Double, overwork Double, money integer)"); // �����û�6��Ϣ��
		db.execSQL("create table user7 (_id integer primary key, year integer, month integer,day integer, forwork Double, overwork Double, money integer)"); // �����û�7��Ϣ��
		db.execSQL("create table user8 (_id integer primary key, year integer, month integer,day integer, forwork Double, overwork Double, money integer)"); // �����û�8��Ϣ��
		db.execSQL("create table user9 (_id integer primary key, year integer, month integer,day integer, forwork Double, overwork Double, money integer)"); // �����û�9��Ϣ��
		db.execSQL("create table user0 (_id integer primary key, year integer, month integer,day integer, forwork Double, overwork Double, money integer)"); // ������10��Ϣ��
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)// ��д�����onUpgrade�������Ա����ݿ�汾����
	{
	}
}
