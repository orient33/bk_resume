
package cn.zzu.ie;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageDataObserver;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

/**
 * UI to display six View that act as six operations
 */
public class MainActivity extends Activity/* implements View.OnClickListener */{

    // private View v[];
    private static ProgressDialog mProgressDialog, mPd;
    private String mDbFileName;
    private MyDB mdb;
    private Handler mHandler;

    // handler to show different toasts depend on @msg
    static class MyHandler extends Handler {
        private static MainActivity mAct;

        MyHandler(Activity a) {
            mAct = (MainActivity) a;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (mProgressDialog != null)
                mProgressDialog.hide();

            switch (msg.what) {
                case R.id.view1: // backup contact
                    mAct.showToast(getStrings(mAct, R.string.find,
                            (int[]) msg.obj, R.string.con_sum,
                            R.string.backup));
                    break;
                case R.id.view2: // resume contacts
                    mAct.showToast(getStrings(mAct, R.string.find,
                            (int[]) msg.obj, R.string.con_sum,
                            R.string.resume));
                    break;
                case R.id.view3: // backup sms
                    mAct.showToast(getStrings(mAct, R.string.find,
                            (int[]) msg.obj, R.string.sms_sum,
                            R.string.backup));
                    break;
                case R.id.view4: // resume sms
                    mAct.showToast(getStrings(mAct, R.string.find,
                            (int[]) msg.obj, R.string.sms_sum,
                            R.string.resume));
                    break;
                case R.id.view5: // backup call log
                    mAct.showToast(getStrings(mAct, R.string.find,
                            (int[]) msg.obj, R.string.calllog_sum,
                            R.string.backup));
                    break;
                case R.id.view6: // resume call log
                    mAct.showToast(getStrings(mAct, R.string.find,
                            (int[]) msg.obj, R.string.calllog_sum,
                            R.string.resume));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int ids[] = {
                R.id.view1, R.id.view2, R.id.view3,
                R.id.view4, R.id.view5, R.id.view6
        };
        for (int id : ids) { // set listener for six view
            this.findViewById(id).setOnClickListener(lis);
        }
        // create or location database file. sdcard/download/myData.db
        String mPath = Environment.getExternalStorageDirectory() + "/download";
        mDbFileName = mPath + "/myData.db";
        File dir = new File(mPath);
        if (!dir.exists())
            dir.mkdir();
        if (!(new File(mDbFileName).exists())) {
            try {
                FileOutputStream fos = new FileOutputStream(mDbFileName);
                fos.close();
            } catch (IOException e) {
                Log.e("dfdun", "onCreate();e = " + e.toString());
            }
        }
        // handler to display process dialog
        mHandler = new MyHandler(this);

        mPd = new ProgressDialog(this);
        mPd.setCancelable(false);
        mPd.setIndeterminate(true);
        mPd.setTitle(getString(R.string.clear) + "...");

        mdb = new MyDB(this, mDbFileName, null, 1);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mPd.isShowing())
            mPd.dismiss();
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit:
                stopService(new Intent(this, FloatService.class));
                finish();
                break;
            case R.id.clear:
                new ClearCache(this).execute("");
                break;
            case R.id.setting:
                startActivity(new Intent(this, Preferences.class));
                break;
        }
        return true;
    }

    /* id : for string resource */
    private Dialog getProgessDialog(int id) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.setMessage(getString(id));
        return mProgressDialog;
    }

    private class SubThread extends Thread {
        private int arm;

        protected SubThread(int id) {
            super();
            arm = id;
        }

        @Override
        public void run() {
            Message msg = new Message();
            final int[] result;
            msg.what = arm;
            switch (arm) {
                case R.id.view1:
                    result = mdb.backupContact();
                    msg.obj = result;
                    break;
                case R.id.view2:
                    result = mdb.resumeContact();
                    msg.obj = result;
                    break;
                case R.id.view3:
                    result = mdb.backupSms(); // return {sum, backup}
                    msg.obj = result;
                    break;
                case R.id.view4:
                    result = mdb.resumeSms(); // return {sum, resume}
                    msg.obj = result;
                    break;
                case R.id.view5:
                    result = mdb.backupCalllog();
                    msg.obj = result;
                    break;
                case R.id.view6:
                    result = mdb.resumeCalllog();
                    msg.obj = result;
                    break;
            }
            // send message after operation is completed
            mHandler.sendMessageDelayed(msg, 500);
        }
    }

    private static String getStrings(Context c, int id1, int r[], int id2, int id3) {
        return c.getString(id1) + " " + r[0] + " " + c.getString(id2) + "\n"
                + c.getString(id3) + " " + r[1] + " " + c.getString(id2) + "\n"
                + r[2] / 1000.0 + " s ";
    }

    private void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    View.OnClickListener lis = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            // Log.i("dfdun", " click id = " + id);
            if (!Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                (new AlertDialog.Builder(MainActivity.this)
                        .setMessage(R.string.sd_not_ready)).create().show();
                return;
            }
            switch (id) {
                case R.id.view1: /* backup contacts , from handset to SD card */
                    // getProgessDialog(R.string.backup_con).show();
                    startActivity(new Intent(MainActivity.this, PhoneInfo.class));
                    return;
                case R.id.view2: /* resume contacts ,from SD card to handset */
                    // getProgessDialog(R.string.resume_con).show();
                    new ClearEmptyFolder().execute();
                    return;
                case R.id.view3: /* backup Sms , from handset to SD card */
                    getProgessDialog(R.string.backup_sms).show();
                    break;
                case R.id.view4: /* resume Sms ,from SD card to handset */
                    getProgessDialog(R.string.resume_sms).show();
                    break;
                case R.id.view5:
                    getProgessDialog(R.string.backup_calllog).show();
                    break;
                case R.id.view6:
                    getProgessDialog(R.string.resume_calllog).show();
                    break;
            }
            new SubThread(id).start();
        }
    };

    class ClearEmptyFolder extends AsyncTask<Void, Void, Void> {
        int emptyFile = 0, emptyDir = 0;

        @Override
        protected void onPreExecute() {
            getProgessDialog(R.string.clear).show();
        }

        int size;

        @Override
        protected Void doInBackground(Void... params) {
            final String path = Environment.getExternalStorageDirectory().getPath();
            String subs[] = new File(path).list();
            if (subs == null)
                return null;
            for (String sub : subs) {
                size = 0;
                getSize(path + "/" + sub);
                if (size == 0) {
                    android.util.Log.i("dd", "find empty folder :" + path + "/" + sub);
                    android.util.Log.i("dd", "del ?" + delPath(path + "/" + sub));
                }
            }
            return null;
        }

        private boolean delPath(String path) {
            File file = new File(path);
            if (file.isDirectory() && file.list() != null && file.list().length > 0) {
                for (File sub : file.listFiles()) {
                    if (!delPath(sub.getPath()))
                        return false;
                }
                return true;
            } else {
                if (file.isDirectory())
                    ++emptyDir;
                else
                    ++emptyFile;
                return file.delete();
            }
        }

        private void getSize(String path) {
            if (isCancelled())
                return;
            File file = new File(path);
            if (file.isDirectory()) {
                File[] listFiles = file.listFiles();
                if (listFiles == null)
                    return;

                for (File f : listFiles) {
                    if (isCancelled())
                        return;

                    getSize(f.getPath());
                }
            } else {
                size += file.length();
                // onSize(size);
            }
        }

        protected void onPostExecute(Void result) {
            if (mProgressDialog != null) {
                mProgressDialog.setMessage(MainActivity.this.getString(R.string.clear_empty,
                        emptyDir, emptyFile));
                mProgressDialog.setCancelable(true);
                mProgressDialog.setCanceledOnTouchOutside(true);
                mProgressDialog = null;
            }
        }
    }

    class ClearCache extends AsyncTask<Object, Object, Object> {
        Context con;
        PackageManager pm;
        Long time;
        ArrayList<String> pns = new ArrayList<String>();
        String temp = " - ";

        ClearCache(Context context) {
            super();
            con = context;
            pm = con.getPackageManager();
            pns.clear();
        }

        @Override
        protected void onPreExecute() {
            time = System.currentTimeMillis();
            mPd.show();
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            mPd.setMessage(temp);
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected Object doInBackground(Object... o) {
            Class[] arrayOfClass = new Class[2];
            Class c2 = Long.TYPE;
            arrayOfClass[0] = c2;
            arrayOfClass[1] = IPackageDataObserver.class;
            Long len = Long.valueOf(getEnvironmentSize() - 1L);
            Method m;
            try {
                m = pm.getClass().getMethod("freeStorageAndNotify", arrayOfClass);
                m.invoke(pm, len, new IPackageDataObserver.Stub() {
                    public void onRemoveCompleted(String packageName, boolean succeeded) {
                        pns.add(packageName + "(" + succeeded + ")");
                        temp = (packageName + "(" + succeeded + ")");
                        Log.i("dfdun", "onRemoveCompleted;" + temp);
                        // publishProgress(null);
                    }
                });
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                PhoneInfo.log(e.toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                PhoneInfo.log(e.toString());
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                PhoneInfo.log(e.toString());
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                PhoneInfo.log(e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            time = System.currentTimeMillis() - time;
            mPd.hide();
            showToast(con.getString(R.string.clear_cache_notice, time / 1000f));
        }

        private long getEnvironmentSize() {
            File localFile = Environment.getDataDirectory();
            long length;
            if (localFile == null)
                length = 0L;
            String str = localFile.getPath();
            StatFs localStatFs = new StatFs(str);
            long l2 = localStatFs.getBlockSize();
            length = localStatFs.getBlockCount() * l2;
            Log.i("dfdun", "size of EnvironmentSize is " + length);
            return length;
        }
    }
}
