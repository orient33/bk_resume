package cn.zzu.ie;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import cn.zzu.ie.cloud.Util;

public class Preferences extends PreferenceActivity implements Preference.OnPreferenceClickListener
    ,Preference.OnPreferenceChangeListener{

	private static final String TAG = "Preferences";
    static final String key="displaymode",key_about="pref1";
    Preference mP;
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.pref);
        mP =  this.findPreference(key);
        mP.setOnPreferenceChangeListener(this);
        this.findPreference(key_about).setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference arg0, Object arg1) {
        Util.logd(TAG, "onPreferenceChange "+ arg0.getKey()+" , " + arg1.toString());
        FloatService.setDisplayType(arg1.toString().equals("1"));
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference p) {
        Util.logd(TAG, 	"onPreferenceClick " +p.getKey());
        if(p.getKey().equals(key_about)){

            AlertDialog.Builder ab = new AlertDialog.Builder(this);
            ab.setTitle(R.string.about);
            ab.setMessage(R.string.about_msg);
            ab.create().show();   
        }
        return true;
    }
}
