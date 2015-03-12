package cn.zzu.ie;


import android.app.AlertDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

public class Preferences extends PreferenceActivity implements Preference.OnPreferenceClickListener
    ,Preference.OnPreferenceChangeListener{

    static final String key="displaymode",key_about="pref1";
    Preference mP;
    String version="";
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.pref);
        mP =  this.findPreference(key);
        mP.setOnPreferenceChangeListener(this);
        this.findPreference(key_about).setOnPreferenceClickListener(this);
        String ver="unknow";
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            ver = pi.versionName;
        } catch (NameNotFoundException e) {
            throw new RuntimeException("....hmm. not find self...");
        }
        version=this.getString(R.string.about_msg,ver);
    }

    @Override
    public boolean onPreferenceChange(Preference arg0, Object arg1) {
        Log.i("dfdun","onPreferenceChange "+ arg0.getKey()+" , " + arg1.toString());
        FloatService.setDisplayType(arg1.toString().equals("1"));
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference p) {
        Log.i("dfdun", " onPreferenceClick " +p.getKey());
        if(p.getKey().equals(key_about)){

            AlertDialog.Builder ab = new AlertDialog.Builder(this);
            ab.setTitle(R.string.about);
            ab.setMessage(version);
            ab.create().show();   
        }
        return true;
    }
}
