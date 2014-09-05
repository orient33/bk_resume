package cn.zzu.ie.cloud;

import android.util.Log;

/**
 * 百度云、 以及 本应用 一些常量
 */
public class Util {
	public static final String SP_Name = "sp";
	public static final String DB_Path_Key = "db_file_path";
	static final String Token_Key = "access_token"; // 存储在 sharedPreference中access-token的key
	static final String Name_Key = "username"; // sharedPreference中 当前用户的名称的key
	static final int Upload_Notification_ID = 101;
	// orient33 的 应用 ‘备份恢复’
	static final String ID = "468904";
	static final String API_key = "HOGnbXqiHs22E9lpNvFGGRHo"; // or client_id
	static final String Root_Path = "/apps/baidu_pcs_api/备份恢复"; //在百度云的路径

	public static final void logd(String tag, String msg) {
		Log.d(tag + "", "" + msg);
	}
	public static final void loge(String tag, String msg) {
		Log.e(tag + "", "" + msg);
	}
}
