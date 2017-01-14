package com.sunteam.music.utils;

import java.util.ArrayList;

import com.sunteam.music.dao.GetDbInfo;
import com.sunteam.music.dao.MusicInfo;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class Global {
	public static final String TAG = "zbc";
	
	public static final String MENU_PATH_EXTSD = "/mnt/extsd"; // 扩展SD卡
	public static final String MENU_PATH_UDISK = "/mnt/usbhost1"; // U盘
	
	public static final String DIRECTORY_PATH = "/directory"; // 目录浏览
	public static final String FAVORITE_PATH = "/favorite"; // 我的最爱
	public static final String RECENTPLAY_PATH = "/Recentplay"; // 最近播放
		
	public static final String PLAY_LIST = "Play";   // 最近播放 
	public static final String SAVE_LIST = "Save";   // 我的收藏

	public static final int MAIN_DIR_ID = 0;   // 主界面选择界面  目录浏览
	public static final int MAIN_SAVE_ID = 1;   // 主界面选择界面  我的收藏
	public static final int MAIN_PLAY_ID = 2;   // 主界面选择界面 最近播放
	
	public static final int MAIN_LOCAL_ID = 0;   // 目录浏览 内存
	public static final int MAIN_TF_ID = 1;   // 目录浏览 tf卡
	public static final int MAIN_UDISK_ID = 2;   // 目录浏览 u盘
	
	public static final String ROOT_PATH = Environment.getExternalStorageDirectory().toString();// java.io.File.separator;
	public static final String USER_PATH = "/mnt/extsd"; // 外部 sd卡
	public static final String USB_PATH = "/mnt/usbhost1"; // 优盘
	
	public static final String ROOT_PATH_FLAG = "/root";
	
	public static final int PLAY_LIST_ID = 1;   // 最近播放 
	public static final int SAVE_LIST_ID = 2;   // 我的收藏
	
	public static final int MAX_SAVE_LIST_NUM = 100;   // 最近播放 保存最大的条数
	
	public static final int MENU_FLAG = 0x500;   // 功能菜单界面
	public static final int PLAY_FLAG = 0x501;   // 播放界面
	public static final int PLAY_MENU_FLAG = 0x502;   // 播放界面
	public static final int PLAY_MENU_PLAY_FLAG = 0x503;   // 播放界面
	
	public static final int MENU_DEL_FILE = 0;   // 删除一个文件
	public static final int MENU_DEL_ALL = 1;   // 删除所有文件
	public static final int MENU_ADD = 2;   // 删除所有文件
	
	public static final int MENU_PLAY_ADD_FILE = 3;   // 添加到我的收藏
	public static final int MENU_PLAY_MODE_ALL = 0;   // 全部循环
	public static final int MENU_PLAY_MODE_SINGLE = 1;   // 单曲循环
	public static final int MENU_PLAY_MODE_RAND = 2;   // 随机播放
	
	public static final int MSG_NO_FILE = 1;   // 无文件
	public static final int MSG_DEL_OK = 2;   // 文件 已删除
	public static final int MSG_DELALL_OK = 3;   // 文件 已清空
	public static final int MSG_RESUME = 4;   // 刷新
	
	public static final int MSG_PLAY_A = 5;   // 刷新 A点选定
	public static final int MSG_PLAY_B = 6;   // 刷新 B点选定
	public static final int MSG_PLAY_AB_CANEL = 7;   // 刷新 B点选定
	
	public static final int MSG_PLAY_SWITCH = 8;   // 文件播放错误  MSG_PLAY_NEXT
	public static final int MSG_PLAY_NEXT = 9;
	public static final int MSG_PLAY_ERROR = 10;
	public static final int MSG_PLAY_BACK = 11;
	
	
	public static final int MSG_MENU_BACK = 12;  // 返回界面
	
	public static final int MSG_SHOW_MAIN = 13;  // 显示主界面
	
	public static int MAX_RANK = 128;// 最大级数
	
	public static final String MUSIC_CONFIG_FILE = "music_cfg.xml";   // 保存播放模式
	public static final String MUSIC_MODE = "music_mode";   //
	public static final String MUSIC_SELECT = "music_select";   //
	
	public static final String DB_PATH = Environment.getExternalStorageDirectory() + "//music.db"; // 数据库路径

	public static final int SEEK_LEN = 10000;   // 一次seek 时间
	
	public static String FristString = null;   // 最后一次的浏览
	
	public static void debug(String s) {
		Log.d(TAG, s);
	}
	// 
	public static String GetPalyFristData(Context mContext){
		GetDbInfo dbMusicInfo = new GetDbInfo( mContext ); // 打开数据库

		ArrayList<MusicInfo> musicInfos = new ArrayList<MusicInfo>();		

		musicInfos = dbMusicInfo.GetAllData(Global.PLAY_LIST);
		dbMusicInfo.closeDb();
		if(musicInfos.size() > 0){
			//return musicInfos.get(0).path+"/"+musicInfos.get(0).filename;
			return musicInfos.get(0).path;
		}
		else{
			return null;
		}		
	}
	
	public static int GetPalyFristSeekTime(Context mContext, String filename){
		GetDbInfo dbMusicInfo = new GetDbInfo( mContext ); // 打开数据库

		ArrayList<MusicInfo> musicInfos = new ArrayList<MusicInfo>();		

		musicInfos = dbMusicInfo.GetAllData(Global.PLAY_LIST);
		dbMusicInfo.closeDb();
		Global.debug("\r\n GetPalyFristSeekTime  musicInfos.size() ==="  + musicInfos.size());
		int playtime = 0;
		
		for(int i = 0; i < musicInfos.size(); i++)
		{
			Global.debug("\r\n  musicInfos.get("+i+").playtime ==="  + musicInfos.get(i).playtime);
			Global.debug("  musicInfos.get("+i+").path ==="  + musicInfos.get(i).path);
			Global.debug("  filename ==="  + filename);
			
			if(musicInfos.get(i).path.equals(filename)){
				playtime = musicInfos.get(i).playtime;
				break;
			}
		}
		Global.debug("\r\n GetPalyFristSeekTi playtime ==="  + playtime);
		if(playtime == 0){
			playtime = 1;
		}
		return playtime;
	}
}
