package cn.zzu.ie.cloud;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.Toast;
import cn.zzu.ie.R;

import com.baidu.oauth.BaiduOAuth;
import com.baidu.oauth.BaiduOAuth.BaiduOAuthResponse;
import com.baidu.pcs.BaiduPCSActionInfo;
import com.baidu.pcs.BaiduPCSClient;
import com.baidu.pcs.BaiduPCSStatusListener;

public class Upload {
	private static final String TAG = "Upload";
	private static Upload mInstance = null;
	private Context mContext;
	SharedPreferences mSp;

	public synchronized static Upload getInstance(Context c) {
		if (mInstance == null)
			mInstance = new Upload(c);
		return mInstance;
	}

	private Upload(Context c) {
		mContext = c;
		mSp = c.getSharedPreferences(Util.SP_Name, 0);
	}

	public void uploadFile() {
		String accessToken = mSp.getString(Util.Token_Key, "");
		if (TextUtils.isEmpty(accessToken)) { // login if has no access_token
			login();
			return;
		}
		uploadBak(accessToken);
	}

	// 上传备份文件 mydata.db
	private void uploadBak(final String token) {
		if (null != token) {
			final String srcLocalPath = mSp.getString(Util.DB_Path_Key, "/");
			final NotificationCompat.Builder b = new NotificationCompat.Builder(
					mContext);
			b.setAutoCancel(false).setContentTitle("Upload Progress")
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentText("uploading...");
			final NotificationManager nm = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			final BaiduPCSStatusListener listener = new BaiduPCSStatusListener() {
				/* bytes - 已经传输的字节数 total - 共有多少字节 */
				public void onProgress(long bytes, long total) {
					Util.logd(TAG, "onProgress() bytes=" + bytes + ", total="
							+ total);
					int progress = (int) (bytes * 100 / total);
					b.setProgress(100, progress, false);
					nm.notify(Util.Upload_Notification_ID, b.build());
				}

			};
			Thread workThread = new Thread(new Runnable() {
				public void run() {
					BaiduPCSClient api = new BaiduPCSClient();
					api.setAccessToken(token);
					BaiduPCSActionInfo.PCSFileInfoResponse res = api
							.uploadFile(srcLocalPath, Util.Root_Path
									+ "/myData.db", listener);

					Util.logd(TAG, "BaiduPCSClient.uploadFile() ! "
							+ srcLocalPath + "; error=" + res.status.errorCode
							+ ",msg=" + res.status.message);
				}
			});
			workThread.start();
			Toast.makeText(mContext, "uploading...", Toast.LENGTH_SHORT).show();
		}
	}

	private void login() {
		SharedPreferences sp = mContext.getSharedPreferences(Util.SP_Name, 0);
		final String token = sp.getString(Util.Token_Key, "");
		if (!TextUtils.isEmpty(token)) {
			String name = sp.getString(Util.Name_Key, "");
			Toast.makeText(mContext, "logined :" + name, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		BaiduOAuth baiduOAuth = new BaiduOAuth();
		BaiduOAuth.OAuthListener lis = new BaiduOAuth.OAuthListener() {
			@Override
			public void onException(String e) {
				Toast.makeText(mContext, "onException() " + e,
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onComplete(BaiduOAuthResponse response) {
				if (null != response) {
					String token0 = response.getAccessToken();
					final String name = response.getUserName();
					storeToken(token0, name);
				}
			}

			@Override
			public void onCancel() {
				Toast.makeText(mContext, "onCancel()", Toast.LENGTH_SHORT)
						.show();
			}
		};
		baiduOAuth.startOAuth(mContext, Util.API_key, new String[] { "basic",
				"netdisk" }, lis);
	}

	private void storeToken(String token, String name) {
		mSp.edit().putString(Util.Token_Key, token)
				.putString(Util.Name_Key, name).commit();
	}

}
