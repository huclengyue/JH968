package com.sunteam.massage;

import java.util.ArrayList;
import java.util.Calendar;

import com.sunteam.common.menu.MenuActivity;
import com.sunteam.common.utils.ArrayUtils;
import com.sunteam.massage.utils.Global;

import android.content.Intent;
import android.os.Bundle;

public class UserInfoActivity extends MenuActivity {
	private int gUserID = 0; // 用户 ID

	private double gfortime = 0.0; // 排钟时间
	private double govertime = 0.0; // 点钟时间
	private int gmoney = 0; // 金额

	private double gMonth_forwork = 0; // 月 排钟时间
	private double gMonth_overwork = 0; // 月点钟时间
	private int gMonth_money = 0; // 月金额

	private double gYear_forwork = 0; // 年排钟时间
	private double gYear_overwork = 0; // 年点钟时间
	private int gYear_money = 0; // 年金额

	// 变量
	public int gyear = 0;
	public int gmonth = 0;
	public int gday = 0;
	//private int FLAG_CODE = 0x505;

//	private static final int TF_PATH = 1;// 存储卡数据
	// private MyHandler mHandler = new MyHandler(); // 句柄
	String[] guser_List = new String[6]; // list信息

	//private int userId = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		initView();

		super.onCreate(savedInstanceState);
	}
	// 初始化
	private void initView() {
		// 获取传入参数
		Intent intent = getIntent(); // 获取 Intent
		Bundle bundle=intent.getExtras();	//获取 Bundle
				
		gUserID = bundle.getInt("ID"); // 获取用户ID
		//mTitle = intent.getStringExtra("title");
		mTitle = bundle.getString("title");

		// 获取时间
		Calendar calendar = Calendar.getInstance(); // 获取日历

		gyear = calendar.get(Calendar.YEAR);
		gmonth = calendar.get(Calendar.MONTH) + 1; // 月份从0开始，需要+1
		gday = calendar.get(Calendar.DAY_OF_MONTH);
		
		mMenuList = UserUpDateList();
	}

	// 获取菜单列表
	private ArrayList<String> UserUpDateList() {

		GetDbInfo dbInfo = new GetDbInfo(UserInfoActivity.this, Global.USER_PATH); // 开打数据库

		gMonth_forwork = 0; // 月排钟
		gMonth_overwork = 0; // 月点钟
		gMonth_money = 0; // 月金额

		gYear_forwork = 0; // 年排钟
		gYear_overwork = 0; // 年点钟
		gYear_money = 0; // 年金额

		gfortime = 0.0; // 排钟
		govertime = 0.0; // 点钟
		gmoney = 0; // 金额

		userinfo tempinfo;

		if (gUserID >= Global.MAX_USER_NUM) // 全部用户
		{
			for (int i = 0; i < Global.MAX_USER_NUM; i++) // 计算全部用户
			{

				int maxid = dbInfo.getMaxId(i); // 获取用户最大数据个数
				
				for (int j = 1; j <= maxid; j++) {
					tempinfo = dbInfo.find(i, j); // 获取 数据
					
					Global.debug("i = " + i + " j = " + j + " "+tempinfo.getYear() + "-" + tempinfo.getMonth() + "-"
							+ tempinfo.getDay());
					Global.debug("");
					if ((tempinfo.getYear() == gyear) && (tempinfo.getMonth() == gmonth) && (tempinfo.getDay() == gday)) // 同一天数据
					{
						gfortime = gfortime + tempinfo.getforwork();  // 当天的 排钟
						govertime = govertime + tempinfo.getoverwork();  // 当天的 点钟
						gmoney = gmoney + tempinfo.getMoney();		// 当天的 金额

						gMonth_forwork = gMonth_forwork + tempinfo.getforwork();
						gMonth_overwork = gMonth_overwork + tempinfo.getoverwork();
						gMonth_money = gMonth_money + tempinfo.getMoney();

						gYear_forwork = gYear_forwork + tempinfo.getforwork();
						gYear_overwork = gYear_overwork + tempinfo.getoverwork();
						gYear_money = gYear_money + tempinfo.getMoney();

					}else if ((tempinfo.getYear() == gyear) && (tempinfo.getMonth() == gmonth)) {
						gMonth_forwork = gMonth_forwork + tempinfo.getforwork();
						gMonth_overwork = gMonth_overwork + tempinfo.getoverwork();
						gMonth_money = gMonth_money + tempinfo.getMoney();

						gYear_forwork = gYear_forwork + tempinfo.getforwork();
						gYear_overwork = gYear_overwork + tempinfo.getoverwork();
						gYear_money = gYear_money + tempinfo.getMoney();
					}else if (tempinfo.getYear() == gyear) {
						gYear_forwork = gYear_forwork + tempinfo.getforwork();
						gYear_overwork = gYear_overwork + tempinfo.getoverwork();
						gYear_money = gYear_money + tempinfo.getMoney();
					}
					
	/*				Global.debug("gfortime =" + gfortime);
					Global.debug("govertime =" + govertime);
					Global.debug("gmoney =" + gmoney);
					
					Global.debug("gMonth_forwork =" + gMonth_forwork);
					Global.debug("gMonth_overwork =" + gMonth_overwork);
					Global.debug("gMonth_money =" + gMonth_money);
					
					Global.debug("gYear_forwork =" + gYear_forwork);
					Global.debug("gYear_overwork =" + gYear_overwork);
					Global.debug("gYear_money =" + gYear_money);*/
				}
			}
		} else {
			int maxid = dbInfo.getMaxId(gUserID);
			Global.debug("===========maxid=" + maxid);

			if (maxid > 0) {
				for (int j = 1; j <= maxid; j++) //
				{
					tempinfo = dbInfo.find(gUserID, j); // 获取数据
					
					/*  Global.debug("tempinfo year ="+tempinfo.getYear());
					  Global.debug("tempinfo month ="+tempinfo.getMonth());
					  Global.debug("tempinfo day ="+tempinfo.getDay());
					  
					  Global.debug("tempinfo forwork ="+tempinfo.getforwork());
					  Global.debug("tempinfo overwork ="+tempinfo.getoverwork());
					  Global.debug("tempinfo money ="+tempinfo.getMoney());*/
					  //lobal.debug("tempinfo year ="+tempinfo.getYear());
					 
					if ((tempinfo.getYear() == gyear) && (tempinfo.getMonth() == gmonth) && (tempinfo.getDay() == gday)) // 同一天数据
					{
						gfortime = gfortime + tempinfo.getforwork();
						govertime = govertime + tempinfo.getoverwork();
						gmoney = gmoney + tempinfo.getMoney();

						gMonth_forwork = gMonth_forwork + tempinfo.getforwork();
						gMonth_overwork = gMonth_overwork + tempinfo.getoverwork();
						gMonth_money = gMonth_money + tempinfo.getMoney();

						gYear_forwork = gYear_forwork + tempinfo.getforwork();
						gYear_overwork = gYear_overwork + tempinfo.getoverwork();
						gYear_money = gYear_money + tempinfo.getMoney();
						Global.debug("year = " + gyear + "month = " + gmonth + "day = " + gday);
						Global.debug("forwork = " + gfortime + "overwork = " + govertime + "money = " + gmoney);
					} else if ((tempinfo.getYear() == gyear) && (tempinfo.getMonth() == gmonth)) {
						gMonth_forwork = gMonth_forwork + tempinfo.getforwork();
						gMonth_overwork = gMonth_overwork + tempinfo.getoverwork();
						gMonth_money = gMonth_money + tempinfo.getMoney();

						gYear_forwork = gYear_forwork + tempinfo.getforwork();
						gYear_overwork = gYear_overwork + tempinfo.getoverwork();
						gYear_money = gYear_money + tempinfo.getMoney();
					} else if (tempinfo.getYear() == gyear) {
						gYear_forwork = gYear_forwork + tempinfo.getforwork();
						gYear_overwork = gYear_overwork + tempinfo.getoverwork();
						gYear_money = gYear_money + tempinfo.getMoney();
					}
				}

			} else { // 没有数据

				gMonth_forwork = 0; // 月排钟
				gMonth_overwork = 0; // 月点钟
				gMonth_money = 0; // 月金额

				gYear_forwork = 0; // 年排钟
				gYear_overwork = 0; // 年点钟
				gYear_money = 0; // 年金额

				gfortime = 0.0; // 排钟
				govertime = 0.0; // 点钟
				gmoney = 0; // 金额
			}
		}
		dbInfo.closeDb();

		java.text.DecimalFormat df=new java.text.DecimalFormat("##.#");   // 限制有效位数
	//	String temp = df.format(gOverTime);
		
		guser_List[0] = getResources().getString(R.string.cur_date) + gyear + "-" + gmonth + "-" + gday; // ����
																											// ��һ��
		guser_List[1] = getResources().getString(R.string.cur_forTime) + df.format(gfortime)
				+ getResources().getString(R.string.hour);
		// 点钟
		guser_List[2] = getResources().getString(R.string.cur_overTime) + df.format(govertime)
				+ getResources().getString(R.string.hour);
		// 金额
		guser_List[3] = getResources().getString(R.string.cur_money) + gmoney + getResources().getString(R.string.yuan);
		// 月
		String[] mon = getResources().getStringArray(R.array.month);
		guser_List[4] = mon[gmonth] + " " + getResources().getString(R.string.forTime) + df.format(gMonth_forwork)
				+ getResources().getString(R.string.hour) + " " + getResources().getString(R.string.overTime)
				+ df.format(gMonth_overwork) + getResources().getString(R.string.hour) + " "
				+ getResources().getString(R.string.workTime) + df.format(gMonth_forwork + gMonth_overwork)
				+ getResources().getString(R.string.hour) + " " + getResources().getString(R.string.money)
				+ gMonth_money + getResources().getString(R.string.yuan);
		// 年
		guser_List[5] = gyear + getResources().getString(R.string.year) + " "
				+ getResources().getString(R.string.forTime) + df.format(gYear_forwork) + getResources().getString(R.string.hour)
				+ " " + getResources().getString(R.string.overTime) + df.format(gYear_overwork)
				+ getResources().getString(R.string.hour) + " " + getResources().getString(R.string.workTime)
				+ df.format(gYear_forwork + gYear_overwork) + getResources().getString(R.string.hour) + " "
				+ getResources().getString(R.string.money) + gYear_money + getResources().getString(R.string.yuan);

		@SuppressWarnings("unchecked")
		ArrayList<String> tempList = ArrayUtils.strArray2List(guser_List);
		return tempList;

	}
	// 从写setResultCode 函数
	@Override
	public void setResultCode(int resultCode, int selectItem, String menuItem) {
		Global.debug("\r\n[setResultCode] setResultCode == gUserID = " + gUserID + " selectItem == " + selectItem);
		if (((gUserID >= Global.MAX_USER_NUM) && (selectItem != Global.DATE_SET)) || ((gUserID < Global.MAX_USER_NUM) && (selectItem > Global.MONEY_SET))) {
			return;
		}
		

		Intent intent = new Intent();
		Bundle bundle = new Bundle();//

		bundle.putInt("ID", selectItem); // 修改项
		bundle.putInt("USERID", gUserID); // 用户ID

		bundle.putInt("YEAR", gyear);
		bundle.putInt("MONTH", gmonth);
		bundle.putInt("DAY", gday);

		bundle.putDouble("FORTIME", gfortime);
		bundle.putDouble("OVERTIME", govertime);
		
		Global.debug("[***]setResultCode gForTime = " + gfortime + " gOverTime = "+ govertime);
		
		
		bundle.putInt("MONEY", gmoney);

		Global.debug(
				" [onEnterCompleted] ID= " + selectItem + " year + " + gyear + " month =" + gmonth + " day = " + gday);
		Global.debug(" [onEnterCompleted] forwork= " + gfortime + " overwork + " + govertime + " money =" + gmoney);

		// intent.setClass(MainActivity.this, MenuActivity.class);
		intent.putExtras(bundle); // 传入参数
		intent.setAction("set_action");// 启动设置界面

		startActivityForResult(intent, Global.FLAG_CODE); // 设置标志

	}

	// 参数返回 从设置界面返回
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Global.FLAG_CODE && resultCode == Global.FLAG_CODE) {

			Bundle bundle = data.getExtras();
			long temp_id = bundle.getLong("SELECID");

			Global.debug("[*********]onActivityResult temp_id =" + temp_id);

			if (temp_id == Global.DATE_SET) { // 修改日期

				int year = bundle.getInt("YEAR");
				int month = bundle.getInt("MONTH");
				int day = bundle.getInt("DAY");

				gyear = year;
				gmonth = month;
				gday = day;
				Global.debug("[user] onActivityResult== year = " + gyear);
				Global.debug("[user] onActivityResult== month = " + gmonth);
				Global.debug("[user] onActivityResult== day = " + gday);

				mMenuList = UserUpDateList();
				setListData(mMenuList);
			} else if (temp_id == Global.FORWORK_SET) // 修改 排钟
			{
				Global.debug("FORWORK_SET temp_id =" + temp_id);
				mMenuList = UserUpDateList();
				setListData(mMenuList);
			} else if (temp_id == Global.OVERWORK_SET) // 修改点钟
			{
				mMenuList = UserUpDateList();
				setListData(mMenuList);
			} else if (temp_id == Global.MONEY_SET) // 金额
			{
				mMenuList = UserUpDateList();
				setListData(mMenuList);
			}
		}
	}

}
