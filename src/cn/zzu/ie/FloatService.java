package cn.zzu.ie;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;

public class FloatService extends Service implements OnClickListener {

    static boolean mClick = true;
    private static boolean displayType = false;
    TelephonyManager mTm;
    private static TextView mFloatWin;
    private static WindowManager.LayoutParams mParams;
    private static String mState = " ",mXG=" ", mType = " ";
    private static boolean mShow = false;
    private static WindowManager wm;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        log("onDestroy");
        if (mShow)
            wm.removeViewImmediate(mFloatWin);
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        log("onCreate");
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        createFloatWin();
        mTm = (TelephonyManager) this.getSystemService("phone");
        mTm.listen(new PhoneStateListener() {
            public void onDataActivity(int direction) {
//                switch (direction) {
//                case 0: // none
//                    mDirect = "<||>";
//                    break;
//                case 1:// in
//                    mDirect = "<°˝°˝>";
//                    break;
//                case 2:// out
//                    mDirect = "<°¸°¸>";
//                    break;
//                case 3:// in & out
//                    mDirect = "<°¸°˝>";
//                    break;
//                case 4:// suspend
//                    mDirect = "<%%>";
//                    break;
//                default:// -1 unknown;
//                    mDirect = "<??>";
//                }
//                updateView();
            }

            public void onDataConnectionStateChanged(int state, int networkType) {
                // mState = state; mType = networkType;
                switch (state) {
                case 0: // return "disconnected";
                    // mState = "(~~)";
                    mState = getMyString(R.string.disconnected);
                    break;
                case 1: // return "connecting";
                    // mState = "(--)";
                    mState = getMyString(R.string.connecting);
                    break;
                case 2: // return "connected";
                    // mState = "(==)";
                    mState = getMyString(R.string.connected);
                    break;
                case 3: // "suspended,(connect is up, but IP traffic is unavailable)";
                    // mState = "(=~)";
                    mState = getMyString(R.string.suspended);
                    break;
                default: // return "unknow";
                    // mState = "(~~)";
                    mState = getMyString(R.string.unknow);
                }
                mXG=PhoneInfo.getNetworkClass(networkType);
                mType=PhoneInfo.getNetworkTypeName(networkType);
                updateView();
            }
        }, 64 | 128);
    }

    private String getMyString(int resId) {
        return getString(resId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand: mShow="+mShow);
        String mode=(getSharedPreferences(getPackageName()+"_preferences", 0).getString("displaymode", "0"));
        if (mShow) {
            wm.removeViewImmediate(mFloatWin);
            mShow = false;
        } else {
            wm.addView(mFloatWin, mParams);
            displayType=mode.equals("1");
            mShow = true;
            updateView();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private static void updateView() {
        if(!mShow) return;
        if (displayType)
            mFloatWin.setText(mType + " " + mState);
        else
            mFloatWin.setText(mXG + " " + mState);
        wm.updateViewLayout(mFloatWin, mParams);
    }
    static void setDisplayType(boolean b){
        displayType=b;
        if (mShow)
            updateView();
    }

    private void createFloatWin() {
        mFloatWin = (TextView) LayoutInflater.from(this).inflate(
                R.layout.float_view, null);// .findViewById(R.id.float_view);//
                                           // all ok ,whether //
        mParams = new WindowManager.LayoutParams();
        mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;// ”≈œ»º∂
        mParams.format = PixelFormat.RGBA_8888;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.x = 1;
        mParams.y = 1;
        mParams.alpha = 0.7f;
        mFloatWin.setOnTouchListener(new OnTouchListener() {
            int lastX, lastY, x, y;

            public boolean onTouch(View v, MotionEvent e) {
                if (!mClick) return false;
                switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) e.getRawX();
                    lastY = (int) e.getRawY();
                    x = mParams.x;
                    y = mParams.y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    int dx = (int) e.getRawX() - lastX;
                    int dy = (int) e.getRawY() - lastY;
                    mParams.x = x + dx;
                    mParams.y = y + dy;
                    wm.updateViewLayout(mFloatWin, mParams);
                    break;
                }
                return false;
            }
        });
        mFloatWin.setOnClickListener(this);
    }

    static void log(String msg) {
        android.util.Log.i("dfdun", msg);
    }

    @Override
    public void onClick(View v) {
        if (!mClick) return;
        if (v.getId() == R.id.float_view) {
            Intent i = new Intent(this, PhoneInfo.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(i);
        }
    }
}
