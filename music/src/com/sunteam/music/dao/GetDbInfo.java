package com.sunteam.music.dao;


import java.util.ArrayList;

import com.sunteam.music.utils.Global;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class GetDbInfo {
	private DBOpenHelper helper;// 新建DBOpenHelper
	private SQLiteDatabase db;// 新建SQLiteDatabase
		
	private static final String USER_DBNAME = Global.DB_PATH ;//Environment.getExternalStorageDirectory() + "//music.db";

	
	public GetDbInfo(Context context)// 打开数据库
	{
		Global.debug("GetDbInfo ===============111===");
		helper = new DBOpenHelper(context, USER_DBNAME);	
	}

	/**
	 * 增加数据    
	 * musicInfo -- 需要保存的数据
	 * tableType -- 添加的表类型
	 * @param MusicInfo
	 */
	public void add(MusicInfo musicInfo, int tableType) {	
		Global.debug("add ============");
		db = helper.getWritableDatabase();
		if(Global.PLAY_LIST_ID == tableType){
		// 增加数据命令
			String cmd = "insert into "+ Global.PLAY_LIST +" (_id, filename, path, playtime) values (?,?,?,?)";
			db.execSQL(	cmd, new Object[] { musicInfo.getid(), musicInfo.getFileName(),musicInfo.getPath(), musicInfo.getPlayTime()});
		}
		else if(Global.SAVE_LIST_ID == tableType){
			String cmd = "insert into "+ Global.SAVE_LIST +" (_id, filename, path, playtime) values (?,?,?,?)";
			db.execSQL(	cmd, new Object[] { musicInfo.getid(), musicInfo.getFileName(),musicInfo.getPath(), musicInfo.getPlayTime()});
		}
	}

	/**
	 * 更新数据
	 * musicInfo -- 需要保存的数据
	 * tableType -- 添加的表类型
	 * @param musicinfo
	 */
	public void update(MusicInfo musicInfo, int tableType) {
			
		db = helper.getWritableDatabase();
		//更新数据命令
		String cmd = "update ";
		if(Global.PLAY_LIST_ID == tableType){
			cmd = cmd + Global.PLAY_LIST;
		}
		else if(Global.SAVE_LIST_ID == tableType){
			cmd = cmd + Global.SAVE_LIST;
		}
		cmd += " set filename = ?,path = ?, playtime = ?where _id = ?";
		Global.debug("\r\n [update] ==" + cmd);
		db.execSQL(cmd,
				new Object[] { musicInfo.getFileName(), musicInfo.getPath() , musicInfo.getPlayTime(), musicInfo.getid() });
	}

	/**
	 * 查找数据
	 * 
	 * @param    num--- > 数据号
	 * tableType -- 查找的表类型
	 * @return
	 */
	public MusicInfo find(int num, int tableType) {
		Global.debug("find === num = " + num);
		if(num <= 0)
		{
			num = 1;
		}
		db = helper.getWritableDatabase();
		//String cmd = "select _id, year, month, day, hour, minute, filename,  path, onoff  from alarm" +" "+"where _id = ?";
		String cmd = "select _id, filename,  path , playtime from ";//alarm" +" "+"where _id = ?";
		if(Global.PLAY_LIST_ID == tableType){
			cmd += Global.PLAY_LIST;
		}
		else if(Global.SAVE_LIST_ID == tableType)
		{
			cmd += Global.SAVE_LIST;
		}
		cmd +=" where _id = ?";
		Global.debug("find === cmd =" + cmd);	
		try{
			Cursor cursor = db.rawQuery(cmd, new String[] { String.valueOf(num) });// 查找命令字
			if (cursor.moveToNext())
			{
				// 获取用户数据
				return new MusicInfo(
						cursor.getInt(cursor.getColumnIndex("_id")),
						cursor.getString(cursor.getColumnIndex("filename")),
						cursor.getString(cursor.getColumnIndex("path")),
						cursor.getInt(cursor.getColumnIndex("playtime"))
						);	
			}
		}
		catch (SQLException ex) {
	        Global.debug("没有此数据++++++++++++++++++++++++++++++++\r\n");
	   }
		
		return null;// 或找到返回空
	}

	/**
	 * 删除表全部数据
	 * 
	 * @param 
	 */
	public void detele(int tableType) {
		db = helper.getWritableDatabase();// ��ʼ��SQLiteDatabase����
		// 删除数据命令
		//String cmd = "delete from alarm";
		String cmd = "delete from ";
		if(Global.PLAY_LIST_ID == tableType){
			cmd += Global.PLAY_LIST;
		}
		else if(Global.SAVE_LIST_ID == tableType)
		{
			cmd += Global.SAVE_LIST;
		}
		try{
			db.execSQL(cmd);
		}catch (SQLException ex) {
	        Global.debug(" 删除数据失败+++++++++++++++++++++++++++\r\n");
	   }
	}
	/**
	 * 删除一条数据
	 * 
	 * @param ids
	 */
	public void deteleForOne(int id, int tableType) {
		db = helper.getWritableDatabase();//  获取数据库操作
		// 删除数据命令
		String cmd = "delete from ";//person where _id =?"+ id;
		// db.execSQL("delete from person where name=?", new Object[]{name});
		if(Global.PLAY_LIST_ID == tableType){
			cmd += Global.PLAY_LIST;
		}
		else if(Global.SAVE_LIST_ID == tableType)
		{
			cmd += Global.SAVE_LIST;
		}
		cmd += " where _id=?";
		try{
			db.execSQL(cmd, new Object[]{ id });
		}catch (SQLException ex) {
	        Global.debug(" 删除数据失败+++++++++++++++++++++++++++\r\n");
	   }
				
	}
	
	public void deteleForOneByPath(String path, int tableType) {
		db = helper.getWritableDatabase();//  获取数据库操作
		// 删除数据命令
		String cmd = "delete from ";//person where _id =?"+ id;
		// db.execSQL("delete from person where name=?", new Object[]{name});
		if(Global.PLAY_LIST_ID == tableType){
			cmd += Global.PLAY_LIST;
		}
		else if(Global.SAVE_LIST_ID == tableType)
		{
			cmd += Global.SAVE_LIST;
		}
		cmd += " where path=?";
		try{
			db.execSQL(cmd, new Object[]{ path });
		}catch (SQLException ex) {
	        Global.debug(" 删除数据失败+++++++++++++++++++++++++++\r\n");
	   }
				
	}
	// 查询数据
	public ArrayList<MusicInfo> GetAllData(String table) {
		db = helper.getWritableDatabase();
		String sql = "select * from " + table + " order by " + "_id" + " desc";  // id 倒序
		Cursor cursor = db.rawQuery(sql, null);
		ArrayList<MusicInfo> orderList = new ArrayList<MusicInfo>();
		try {
			if (null != cursor) {
				if (cursor.getCount() > 0) {
					while (cursor.moveToNext()) {
						MusicInfo musicInfo = new MusicInfo();
						musicInfo._id = cursor.getInt(cursor.getColumnIndex("_id"));
						musicInfo.filename = cursor.getString(cursor.getColumnIndex("filename"));
						musicInfo.path = cursor.getString(cursor.getColumnIndex("path"));
						musicInfo.playtime = cursor.getInt(cursor.getColumnIndex("playtime"));
						orderList.add(musicInfo);
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

	// 查找数据库中是否已经存在某一条数据
	public boolean hasRecord(String table, MusicInfo info) {
		Cursor cursor = null;
		db = helper.getWritableDatabase();
		String selection = "path" + "=?";
		String[] selectionArgs = new String[] { info.path };
		cursor = db.query(table, null, selection, selectionArgs, null, null, null);
		int count = 0;
		if (null != cursor) {
			count = cursor.getCount();
			cursor.close();
		}
		db.close();
		if (count != 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * 获取 列数
	 * 
	 * @return
	 */
	public int getCount( int tableType) {
		db = helper.getWritableDatabase();
		//String cmd = "select count(_id) from alarm";
		String cmd = "select count(*) from ";
		if(Global.PLAY_LIST_ID == tableType){
			cmd += Global.PLAY_LIST;
		}
		else if(Global.SAVE_LIST_ID == tableType)
		{
			cmd += Global.SAVE_LIST;
		}
		Global.debug("\r\n getCount cmd =="+ cmd);
		try{
			Cursor cursor = db.rawQuery(cmd,null);//获取列数 命令
			if (cursor.moveToNext())
			{
				return cursor.getInt(0);// 获取列数
			}
		}catch (SQLException ex) {
	        Global.debug(" 获取条数失败+++++++++++++++++++++++++++\r\n");
	   }
		return 0;// 未查找
	}

	/**
	 * 获取表的最大数据个数
	 * 
	 * @return
	 */
	public int getMaxId(int tableType) {

		db = helper.getWritableDatabase();
		//String cmd = "select max(_id) from alarm";
		String cmd = "select max(_id) from ";
		if(Global.PLAY_LIST_ID == tableType){
			cmd += Global.PLAY_LIST;
		}
		else if(Global.SAVE_LIST_ID == tableType)
		{
			cmd += Global.SAVE_LIST;
		}
		try{
			Cursor cursor = db.rawQuery(cmd, null);// 获取最大数  命令
			while (cursor.moveToLast()) {
				return cursor.getInt(0);// 获取最大数据个数
			}
		}catch (SQLException ex) {
		        Global.debug(" 获取最大Id 失败+++++++++++++++++++++++++++\r\n");
		}
		return 0;// 没有数据
	}

	public int getMinId(int tableType) {

		db = helper.getWritableDatabase();
		//String cmd = "select max(_id) from alarm";
		String cmd = "select min(_id) from ";
		if(Global.PLAY_LIST_ID == tableType){
			cmd += Global.PLAY_LIST;
		}
		else if(Global.SAVE_LIST_ID == tableType)
		{
			cmd += Global.SAVE_LIST;
		}
		try{
			Cursor cursor = db.rawQuery(cmd, null);// 获取最大数  命令
			while (cursor.moveToLast()) {
				return cursor.getInt(0);// 获取最大数据个数
			}
		}catch (SQLException ex) {
		        Global.debug(" 获取最小Id 失败+++++++++++++++++++++++++++\r\n");
		}
		return 0;// 没有数据
	}
	
	// 关闭数据库
	public int closeDb() {
		db = helper.getWritableDatabase();
		db.close();
		Global.debug("closeDb ++++++++++++++++++++");
		return 0;//关闭数据库
	}
}
