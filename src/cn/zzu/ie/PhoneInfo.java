package cn.zzu.ie;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class PhoneInfo extends Activity {

    TelephonyManager mTm;
    TextView mCallStateView;
    TextView mDataConView;
    TextView mLocationView;
    TextView mServiceView;
    TextView mSignalView;
    PhoneState mPS;

    int mCallState;
    CellLocation mLoc;
    int mDataDirection;
    int mDataState;
    int mNetworkType;
    ServiceState mSS;
    SignalStrength mSignalStrength;
    
    /** Network type is unknown */
    public static final int NETWORK_TYPE_UNKNOWN = 0;
    /** Current network is GPRS */
    public static final int NETWORK_TYPE_GPRS = 1;
    /** Current network is EDGE */
    public static final int NETWORK_TYPE_EDGE = 2;
    /** Current network is UMTS */
    public static final int NETWORK_TYPE_UMTS = 3;
    /** Current network is CDMA: Either IS95A or IS95B*/
    public static final int NETWORK_TYPE_CDMA = 4;
    /** Current network is EVDO revision 0*/
    public static final int NETWORK_TYPE_EVDO_0 = 5;
    /** Current network is EVDO revision A*/
    public static final int NETWORK_TYPE_EVDO_A = 6;
    /** Current network is 1xRTT*/
    public static final int NETWORK_TYPE_1xRTT = 7;
    /** Current network is HSDPA */
    public static final int NETWORK_TYPE_HSDPA = 8;
    /** Current network is HSUPA */
    public static final int NETWORK_TYPE_HSUPA = 9;
    /** Current network is HSPA */
    public static final int NETWORK_TYPE_HSPA = 10;
    /** Current network is iDen */
    public static final int NETWORK_TYPE_IDEN = 11;
    /** Current network is EVDO revision B*/
    public static final int NETWORK_TYPE_EVDO_B = 12;
    /** Current network is LTE */
    public static final int NETWORK_TYPE_LTE = 13;
    /** Current network is eHRPD */
    public static final int NETWORK_TYPE_EHRPD = 14;
    /** Current network is HSPA+ */
    public static final int NETWORK_TYPE_HSPAP = 15;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.phone_info);
        mCallStateView = (TextView) findViewById(R.id.info_call_state);
        mDataConView   = (TextView) findViewById(R.id.info_data_connect);
        mLocationView  = (TextView) findViewById(R.id.info_location);
        mServiceView   = (TextView) findViewById(R.id.info_service_state);
        mSignalView    = (TextView) findViewById(R.id.info_signal);
        mTm = (TelephonyManager) this.getSystemService("phone");
        mPS=new PhoneState();
        mTm.listen(mPS, PhoneStateListener.LISTEN_SERVICE_STATE     // 1
                | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS        // 256
                | PhoneStateListener.LISTEN_CALL_STATE              // 32
                | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE   // 64
//                | PhoneStateListener.LISTEN_CELL_LOCATION           // 16
                | PhoneStateListener.LISTEN_DATA_ACTIVITY);         // 128
        
    }

    protected void onStart() {
        super.onStart();
        android.util.Log.d("dfdun", "PhoneInfo. onStart()  listen OK");
    }
    
    protected void onResume(){
        super.onResume();
        updateCallState();
        updateLocation();
        updateDataConnect();
        updateSignal();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.phoneinfo, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.float_window:
            startService(new Intent(this, FloatService.class));
            break;
        case R.id.float_window2:
            if (FloatService.mClick)
                FloatService.mClick = false;
            else
                FloatService.mClick = true;
            break;
        case R.id.net_help:
            AlertDialog.Builder ab = new AlertDialog.Builder(this);
            ab.setTitle(R.string.about);
            ab.setMessage(R.string.net_help);
            ab.create().show();
            break;
        }
        return true;
    }
    
    private void updateCallState(){
        mCallStateView.setText(getString(R.string.call_state)+"\n"+mCallState);
    }
    
    private void updateLocation(){}{
        if(mLocationView!=null)
        mLocationView.setText(getString(R.string.location)+"\n"+mLoc==null?"null":mLoc.toString());
//        else 
//            log("mLocationView ==null");
    }

    private void updateDataConnect() {
        mDataConView.setText(getString(R.string.data_con) + "\n"
                + mNetworkType + " , "+ getNetworkTypeName(mNetworkType)+" , " +
                getNetworkClass(mNetworkType)+"\n" 
                + mDataState +" , "+ getDataState(mDataState)+"\n" 
                + mDataDirection+ " , "+getDataActivity(mDataDirection));
    }
    
    private void updateSignal(){
        mSignalView.setText(getString(R.string.signal_state) + "\n"
                  + (mSignalStrength == null ? "null" : mSignalStrength.toString()));
    }
    
    public static String getNetworkClass(int networkType) {
        switch (networkType) {
            case NETWORK_TYPE_GPRS:// 1
            case NETWORK_TYPE_EDGE://2
            case NETWORK_TYPE_CDMA://4
            case NETWORK_TYPE_1xRTT://7
            case NETWORK_TYPE_IDEN://11
                return "2G";
            case NETWORK_TYPE_UMTS://3
            case NETWORK_TYPE_EVDO_0://5
            case NETWORK_TYPE_EVDO_A://6
            case NETWORK_TYPE_HSDPA://8
            case NETWORK_TYPE_HSUPA://9
            case NETWORK_TYPE_HSPA://10
            case NETWORK_TYPE_EVDO_B://12
            case NETWORK_TYPE_EHRPD://14
            case NETWORK_TYPE_HSPAP://15
                return "3G";
            case NETWORK_TYPE_LTE://13
                return "4G";
            default:
                return "?G ";
        }
    }
    
    public static String getNetworkTypeName(int type) {
        switch (type) {
            case NETWORK_TYPE_GPRS:
                return "GPRS";
            case NETWORK_TYPE_EDGE:
                return "EDGE";
            case NETWORK_TYPE_UMTS:
                return "UMTS";
            case NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case NETWORK_TYPE_HSPA:
                return "HSPA";
            case NETWORK_TYPE_CDMA:
                return "CDMA";
            case NETWORK_TYPE_EVDO_0:
                return "CDMA - EvDo rev. 0";
            case NETWORK_TYPE_EVDO_A:
                return "CDMA - EvDo rev. A";
            case NETWORK_TYPE_EVDO_B:
                return "CDMA - EvDo rev. B";
            case NETWORK_TYPE_1xRTT:
                return "CDMA - 1xRTT";
            case NETWORK_TYPE_LTE:
                return "LTE";
            case NETWORK_TYPE_EHRPD:
                return "CDMA - eHRPD";
            case NETWORK_TYPE_IDEN:
                return "iDEN";
            case NETWORK_TYPE_HSPAP:
                return "HSPA+";
            default:
                return "?? ";
        }
    }
    
    public static String getDataActivity(int a){
        switch (a){
        case 0:
            return "none";
        case 1:
            return "in";
        case 2:
            return "out";
        case 3:
            return "in & out";
        case 4:
            return "activity, but physical link is down";
        }
        return "unknow";
    }
    
    public static String getDataState(int state){
        switch(state){
        case 0:
            return "disconnected";
        case 1:
            return "connecting";
        case 2:
            return "connected";
        case 3:
            return "suspended,(connect is up, but IP traffic is unavailable)";
        default: // -1
            return "unknow";
        }
    }
    class PhoneState extends PhoneStateListener {

        @Override
        public void onCallForwardingIndicatorChanged(boolean cfi) {
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            mCallState = state;
            updateCallState();
        }

        @Override
        public void onCellLocationChanged(CellLocation location) {
            mLoc = location;
            updateLocation();
        }

        @Override
        public void onDataActivity(int direction) {
            mDataDirection = direction;
            updateDataConnect();
        }

        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            mDataState = state;
            mNetworkType = networkType;
            updateDataConnect();
        }

        @Override
        public void onMessageWaitingIndicatorChanged(boolean mwi) {
            super.onMessageWaitingIndicatorChanged(mwi);
        }

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            mSS = serviceState;
            mServiceView.setText(getString(R.string.service_state)+"\n"+serviceState.toString());
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            mSignalStrength = signalStrength;
            updateSignal();
        }
    }
    
    static void log(String s) {
        Log.i("dfdun", "PhoneInfo] " + s);
    }
}
