package com.sunteam.massage;


import com.sunteam.massage.utils.Global;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class GetDbInfo {
	private DBOpenHelper helper;// 新建DBOpenHelper
	private SQLiteDatabase db;// 新建SQLiteDatabase
	
	private static final String USER_DBNAME = "/mnt/sdcard/massage.db";// 内部保存数据
	//private static final String USER_DBNAME = "massage.db";// 用户数据库
	//private static final String TF_DBNAME = "mnt/sdcard/massage/massage.db";// 存储卡路径
	@SuppressWarnings("unused")
	private final String USER_PATH = "/mnt/extsd/"; // 外部 sd卡
	private static final String TF_DBNAME = "/mnt/extsd/" + "/massage.db";//Environment.getExternalStorageDirectory() + "//massage.db";
	//private static final String TF_DBNAME = Environment.getExternalStorageDirectory() + "//massage.db";
	private static final int USER_PATH_FLAG = 0;// 内存数据标志
	private static final int TF_PATH_FLAG = 1;// 存储卡数据标志
	
	public GetDbInfo(Context context, int flag)// 打开数据库
	{
		if(USER_PATH_FLAG == flag)
		{
			helper = new DBOpenHelper(context, USER_DBNAME);
		}
		else if(TF_PATH_FLAG == flag)
		{
//			File mfFile = new File( USER_PATH);
//			if(mfFile.exists()){
				helper = new DBOpenHelper(context, TF_DBNAME);
//			}
//			else{
//				Global.getTts().speak("er");
//			}
		}
	}

	/**
	 * 增加数据
	 * 
	 * @param userinfo
	 */
	public void add(userinfo user, int userid) {
		Global.debug("DB add =========userid =="+ userid);
		db = helper.getWritableDatabase();
		// 增加数据命令
		String cmd = "insert into user"+ userid +" "+"(_id, year, month, day , forwork, overwork, money) values (?,?,?,?,?,?,?)";
		db.execSQL(	cmd, new Object[] { user.getid(),user.getYear(),user.getMonth(),user.getDay(), user.getforwork(), user.getoverwork(),user.getMoney() });
	}

	/**
	 * 更新数据
	 * 
	 * @param userinfo
	 */
	public void update(userinfo user, int userid) {
		Global.debug("[****]update === id  " + user.getid());
		
		Global.debug("ID= "+user.getid() + " year + "+user.getYear()+ " month ="+user.getMonth()+" day = "+user.getDay());
		Global.debug(" forwork= "+user.getforwork() + " overwork + "+user.getoverwork() + " money ="+user.getMoney());
		
		db = helper.getWritableDatabase();
		//更新数据命令
		String cmd = "update user"+ userid +" "+"set year = ?,month = ?,day = ?,forwork = ?,overwork = ?, money = ? where _id = ?";
		db.execSQL(cmd,
				new Object[] { user.getYear(),user.getMonth(),user.getDay(), user.getforwork(), user.getoverwork(),user.getMoney(), user.getid() });
	}

	/**
	 * 查找数据
	 * 
	 * @param userid->用户ＩＤ     num--- > 数据号
	 * @return
	 */
	public userinfo find(int userid, int num) {
		Global.debug("find === num = " + num);
		if(num <= 0)
		{
			num = 1;
		}
		db = helper.getWritableDatabase();
		String cmd = "select _id, year, month, day, forwork, overwork, money from user"+ userid +" "+"where _id = ?";
		Cursor cursor = db.rawQuery(cmd, new String[] { String.valueOf(num) });// 查找命令字
		if (cursor.moveToNext())
		{
			// 获取用户数据
			return new userinfo(
					cursor.getInt(cursor.getColumnIndex("_id")),
					cursor.getInt(cursor.getColumnIndex("year")),
					cursor.getInt(cursor.getColumnIndex("month")),
					cursor.getInt(cursor.getColumnIndex("day")),
					cursor.getDouble(cursor.getColumnIndex("forwork")),
					cursor.getDouble(cursor.getColumnIndex("overwork")),
					cursor.getInt(cursor.getColumnIndex("money")));
		}
		return null;// 或找到返回空
	}

	/**
	 * 删除数据
	 * 
	 * @param ids
	 */
	public void detele(int id) {
		db = helper.getWritableDatabase();// ��ʼ��SQLiteDatabase����
		// 删除数据命令
		String cmd = "delete from user"+ id;
		db.execSQL(cmd);
				
	}


	/**
	 * 获取 列数
	 * 
	 * @return
	 */
	public long getCount(int id) {
		db = helper.getWritableDatabase();
		String cmd = "select count(_id) from user"+id;
		Cursor cursor = db.rawQuery(cmd,null);//获取列数 命令
		if (cursor.moveToNext())
		{
			return cursor.getLong(0);// 获取列数
		}
		return 0;// 未查找
	}

	/**
	 * 获取表的最大数据个数
	 * 
	 * @return
	 */
	public int getMaxId(int userID) {
		Global.debug("[*******]getMaxId    userID = " + userID);
		if(userID < 0)
		{
			userID = 0;
		}
		db = helper.getWritableDatabase();
		String cmd = "select max(_id) from user"+userID;
		Cursor cursor = db.rawQuery(cmd, null);// 获取最大数  命令
		while (cursor.moveToLast()) {
			return cursor.getInt(0);// 获取最大数据个数
		}
		return 0;// 没有数据
	}
	
	public int closeDb() {
		db = helper.getWritableDatabase();
		db.close();
		return 0;//关闭数据库
	}
}
