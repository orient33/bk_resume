package cn.zzu.ie;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.text.TextUtils;
import android.util.Log;

/* to create DB file, DB table;
 * and to resume sms,contacts; backup sms,contacts.
 * and return the result for the operation. 
 * */
public class MyDB extends SQLiteOpenHelper {

	private String Table[]={"sms","contact","calls"};
	private Context mContext;
    private static final Uri mSmsUri = Uri.parse("content://sms");
    private static final Uri mContactsUri = ContactsContract.Contacts.CONTENT_URI;
    private static final Uri mCallsUri = CallLog.Calls.CONTENT_URI;
    private static final Uri mContactsRaw=Uri.parse("content://com.android.contacts/raw_contacts");
    private static final Uri mContactsData=Uri.parse("content://com.android.contacts/data");
	
	public MyDB(Context context, String name, CursorFactory factory,
			int version) {
		/* context to use to open or create the database name of the database
		 * file, or null for an in-memory database factory to use for creating
		 * cursor objects, or null for the default version number of the
		 * database (starting at 1);
		 */
		super(context, name, factory, version);
		mContext = context;
        /*
         * About contacts provider Contacts.People.CONTENT_URI; is for 1.6 and lower
         *  content://com.android.contacts/contacts        is for 2.x
         * content://com.android.contacts/data                  is for 2.x
         * content://com.android.contacts/raw_contact           is for 2.x
         * ContactsContract.Contacts.CONTENT_URI                is for 2.x
         * Uri.parse("content://com.android.contacts/contacts");
         * Uri.parse("content://icc/adn"); is contacts for SIM card
         */
		
	}

	@Override
	public void onCreate(SQLiteDatabase db){
		Log.i("dfdun", "MyDB ] create db table ");
		db.execSQL("CREATE TABLE if not exists " +Table[0] +" ( " +
		MyDatabase.SMS[0] + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		MyDatabase.SMS[1] + " INTEGER , " +   // thread_id
		MyDatabase.SMS[2] + " TEXT , " +      // address
		MyDatabase.SMS[3] + " INTEGER , " +   // person
		MyDatabase.SMS[4] + " INTEGER , " +   // unique,Error if 'date'not 
		MyDatabase.SMS[5] + " INTEGER , " +   // protocol
		MyDatabase.SMS[6] + " INTEGER , " +   // read
		MyDatabase.SMS[7] + " INTEGER , " +   // status
		MyDatabase.SMS[8] + " INTEGER , " +   // type
		MyDatabase.SMS[9]+ " TEXT "+" ); ");  // body
		
		String sql="CREATE TABLE if not exists "+Table[1]+" ( "+
		MyDatabase.CON[0] + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		MyDatabase.CON[1] + " text,"+
		MyDatabase.CON[2] + " text,"+
		MyDatabase.CON[3] + " text,"+
		MyDatabase.CON[4] + " text,"+
		MyDatabase.CON[5] + " text );";
		db.execSQL(sql);
		
		sql="create table if not exists "+Table[2]+" ( "+
		MyDatabase.CALLS[0]+ " INTEGER PRIMARY KEY AUTOINCREMENT," +
		MyDatabase.CALLS[1]+" text , " +       //number
		MyDatabase.CALLS[2]+" integer , " +    //date
		MyDatabase.CALLS[3]+" integer , " +//duration
		MyDatabase.CALLS[4]+" integer , " +//type
		MyDatabase.CALLS[5]+" text ) ; " ; //name
		db.execSQL(sql);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer){
		
	}
	
	private void closeDb(SQLiteDatabase db){
	    if(db.isOpen())
	        db.close();
	}
	
	int[] backupContact(){
		int result[]={0,0,0};
		long start = System.currentTimeMillis();
		Cursor cur = mContext.getContentResolver().query(
		        mContactsUri,
				MyDatabase.Pro_con_query,
				null, null, null);
		//logCon(cur);
		SQLiteDatabase sqlDb=this.getWritableDatabase();
		cur.moveToFirst();
		result[0]=cur.getCount();
		int id ; String name;
		ArrayList<String> backupNames = new ArrayList<String>();
	    Cursor bkCur = sqlDb.query(Table[1], new String[]{"name"}, null, null, null, null,
                null);
	    bkCur.moveToFirst();
	    while(!bkCur.isAfterLast()){
	        backupNames.add(bkCur.getString(0));
	        bkCur.moveToNext();
	    }
		bkCur.close();
		while (!cur.isAfterLast()) {
		    id = cur.getInt(0);
		    name = cur.getString(1);
            if (backupNames.contains(name)) {
                cur.moveToNext();
                continue;
            }
            String values[]=getNumbersEmails(id);
            String sql = "insert into " + Table[1] + "("
                    + MyDatabase.toString(MyDatabase.CON) + ") values ("+
					" '"+name+"',"+             //name
					" '"+values[0]+"',"+        //values
					" '"+values[1]+"',"+        //valuesType
					" '"+values[2]+"',"+        //email
					" '"+values[3]+"');" ;      //emailsType
            if(!sqlDb.isOpen())
                sqlDb = this.getWritableDatabase();
            sqlDb.execSQL(sql);
			result[1]++;
			cur.moveToNext();
		}
		cur.close();
		this.closeDb(sqlDb);
		result[2] =(int)(System.currentTimeMillis() - start);
		return result;
	}

	private String[] getNumbersEmails(int id ){
	    StringBuffer numbers=new StringBuffer() , numbersType=new StringBuffer();
	    StringBuffer emails=new StringBuffer(), emailsType=new StringBuffer();
	    Uri uri = Uri.parse(mContactsUri.toString()+"/"+id+"/data");
        Cursor cur = mContext.getContentResolver()
                .query(uri, new String[] { "mimetype", "data1", "data2" },
                        null, null, null);
	    //logCursor(cur);
	    cur.moveToFirst();
	    while(!cur.isAfterLast()){
	        String data1 = cur.getString(cur.getColumnIndex("data1"));
            String mimeType = cur.getString(cur.getColumnIndex("mimetype"));
            if ("vnd.android.cursor.item/name".equals(mimeType)) {
                //sb.append(",name=" + data1);
            } else if ("vnd.android.cursor.item/email_v2".equals(mimeType)) {
                emails.append(data1+",");
            } else if ("vnd.android.cursor.item/phone_v2".equals(mimeType)) {
                numbers.append(data1+",");
            }
            cur.moveToNext();
	    }
	    return new String[]{numbers.toString(),numbersType.toString(),
	            emails.toString(),emailsType.toString()};
	}

	int[] resumeContact() {
		int result[]={0,0,0};
		long start = System.currentTimeMillis();
		Cursor c;
		ContentResolver cr=mContext.getContentResolver();
		SQLiteDatabase sqlDb = getReadableDatabase();
		c = sqlDb.query(Table[1], null, null, null, null, null, null);
		result[0] = c.getCount();
		Log.i("dfdun", "resumeContact: sum is "+result[0]);
		c.moveToFirst();
		while(!c.isAfterLast()){
		    String numbers = c.getString(2),name=c.getString(1)/*,numberstype=c.getString(3)*/;
		    String emails=c.getString(4) /*, emailstype=c.getString(5)*/; 
		    String number = numbers.substring(0, numbers.length()-2);
		    Uri uri=Uri.parse("content://com.android.contacts/data/phones/filter/"+number);
		    Cursor cc = cr.query(uri, null,null, null, null);
            if (cc.getCount() > 0) {
                c.moveToNext();
                continue;
            }
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            ContentProviderOperation op1 = ContentProviderOperation
                    .newInsert(mContactsRaw).withValue("account_name", null).build();
            ops.add(op1);
            ContentProviderOperation op2 = ContentProviderOperation
                    .newInsert(mContactsData).withValueBackReference("raw_contact_id", 0)
                    .withValue("mimetype", "vnd.android.cursor.item/name")
                    .withValue("data2", name).build();
            ops.add(op2);
            ContentProviderOperation op3 = ContentProviderOperation
                    .newInsert(mContactsData).withValueBackReference("raw_contact_id", 0)
                    .withValue("mimetype", "vnd.android.cursor.item/phone_v2")
                    .withValue("data1", number)
                    .withValue("data2", "2")
                    .build();
            ops.add(op3);
            ContentProviderOperation op4 = ContentProviderOperation
                    .newInsert(mContactsData).withValueBackReference("raw_contact_id", 0)
                    .withValue("mimetype", "vnd.android.cursor.item/email_v2")
                    .withValue("data1", emails.substring(0, emails.length()-2))
                    .withValue("data2", "2").build();
            ops.add(op4);
            try {
                cr.applyBatch("com.android.contacts", ops);
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.e("dfdun", " 1 "+e.toString());
            } catch (OperationApplicationException e) {
                e.printStackTrace();
                Log.e("dfdun", " 2 "+e.toString());
            }
		    result[1]++;
		    c.moveToNext();
		}
        c.close();
        this.closeDb(sqlDb);
        result[2] =(int)(System.currentTimeMillis() - start);
		return result;
	}

	int[] backupSms() {
		int result[]={0,0,0};//sum=0, backup=0
		long start = System.currentTimeMillis();
        SQLiteDatabase sqlDb = this.getWritableDatabase();
        
        //��ȡ�ѱ��ݵĶ��ŵ� date  �洢��  ArrayList��
        ArrayList<Long> al=new ArrayList<Long>();
        Cursor cc = sqlDb.query(Table[0], MyDatabase.SMS_1, null, null,
                null, null, "date DESC");
        cc.moveToFirst();
        while (!cc.isAfterLast()) {
            al.add(cc.getLong(1));
            cc.moveToNext();
        }
        cc.close();
        
        //��ѯ���ж���  
        Cursor c = mContext.getContentResolver().query(
		        mSmsUri,
				MyDatabase.SMS,
				"1=1",
				null,//selectionArgs,
				"date DESC");
		c.moveToFirst();
		result[0] = c.getCount();
        while (!c.isAfterLast()) {
		    // whether backup already?
            if (al.indexOf(c.getLong(4)) != -1) {
				c.moveToNext();
				continue;
			}
			String sql = "INSERT INTO "+Table[0]+" ("
			            + MyDatabase.toString(MyDatabase.SMS)+" )  VALUES ( " + 
						c.getInt(1)+","+
						" ' "+c.getString(2)+" ',"+
						c.getInt(3)+","+
						c.getLong(4)+","+
						c.getInt(5)+","+
						c.getInt(6)+","+
						c.getInt(7)+","+
						c.getInt(8)+","+
						" ' "+c.getString(9).replace("'", ",") + " ');"; 	//care about '
			//Log.i("dfdun", "sql= "+sql);
			if(!sqlDb.isOpen())
	            sqlDb = this.getWritableDatabase();
	        sqlDb.execSQL(sql);
			result[1]++;
			c.moveToNext();
		}
		c.close();
		this.closeDb(sqlDb);
		result[2] =(int)(System.currentTimeMillis() - start);
		return result;
	}

	int[] resumeSms(){
		int result[]={0,0,0};
		long start = System.currentTimeMillis();
		SQLiteDatabase sqlDb = getReadableDatabase();
		
		//��ѯ��ǰ���ж���  �洢 ÿ��date�� ArrayList��
		Cursor cc = mContext.getContentResolver().query(mSmsUri,
                MyDatabase.SMS_1, null, null, "date DESC");
		ArrayList<Long> al=new ArrayList<Long>();
		cc.moveToFirst();
        while (!cc.isAfterLast()) {
            al.add(cc.getLong(1));
            cc.moveToNext();
        }
        cc.close();
		
        //ȡ�� �ѱ��ݵ����� ������Ϣ
        ContentValues values = new ContentValues(9);
        Cursor c = sqlDb.query(Table[0], null, null, null, null, null, "date DESC");
		result[0] = c.getCount();
		c.moveToFirst();
        while (!c.isAfterLast()) {
            if (al.contains(c.getLong(4))) {
				c.moveToNext();
				continue;
			}
			//values.put(MyDatabase.SMS[1], c.getInt(1)); //thread_id not use
			values.put(MyDatabase.SMS[2], c.getString(2));
			values.put(MyDatabase.SMS[3], c.getInt(3));
			values.put(MyDatabase.SMS[4], c.getLong(4));
			values.put(MyDatabase.SMS[5], c.getInt(5));
			values.put(MyDatabase.SMS[6], c.getInt(6));
			values.put(MyDatabase.SMS[7], c.getInt(7));
			values.put(MyDatabase.SMS[8], c.getInt(8));
			values.put(MyDatabase.SMS[9], c.getString(9));
			mContext.getContentResolver().insert(mSmsUri, values);
			values.clear();
			result[1]++;
			c.moveToNext();
		}
		c.close();
		this.closeDb(sqlDb);
		result[2] =(int)(System.currentTimeMillis() - start);
		return result;
	}

	int[] backupCalllog(){
        int[] result = { 0, 0, 0};
        Cursor c, cur;
        SQLiteDatabase sqlDb = this.getWritableDatabase();
        SQLiteDatabase sqlDbDatabase = this.getReadableDatabase();
        long start = System.currentTimeMillis();
        // get all date in sqlDb, save them to ArrayList
        ArrayList<Long> al = new ArrayList<Long>();
        cur = sqlDbDatabase.query(Table[2], new String[] { "date" }, null,
                null, null, null, null);
        if (cur.getCount() > 0){
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                al.add(cur.getLong(0));
                cur.moveToNext();
            }
        }
        cur.close();
        
        c = mContext.getContentResolver().query(
                mCallsUri,
                MyDatabase.CALLS,null,null,
                "date DESC");
        c.moveToFirst();
        result[0] = c.getCount();
        while (!c.isAfterLast()) {
            // whether backup already?
            if(al.indexOf(c.getLong(2))!=-1){
                c.moveToNext();
                continue;
            }
            String phoneNumber =c.getString(1),name="";
            Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Cursor c_n=mContext.getContentResolver().query(uri, new String[]{PhoneLookup.DISPLAY_NAME},null,null,null);
			if (c_n != null && c_n.moveToFirst()) {
				name = c_n.getString(0);
			}
            String sql = "INSERT INTO " + Table[2] + " ("
                    + MyDatabase.toString(MyDatabase.CALLS) + " )  VALUES ( "
                    + " ' " + c.getString(1) + "' ," + // number
                    c.getLong(2) + "," +               // date
                    c.getInt(3) + "," +                // duration
                    c.getInt(4) + " ," +               // type
                    "'" + name + "');";      // name
            if(!sqlDb.isOpen())
                sqlDb = this.getWritableDatabase();
            sqlDb.execSQL(sql);
            result[1]++;
            c.moveToNext();
        }
        c.close();
        this.closeDb(sqlDb);
        result[2] =(int)(System.currentTimeMillis() - start);
	    return result;
	}
	
	int[] resumeCalllog(){
        int[] result = { 0, 0, 0 };
        long start = System.currentTimeMillis();
        Cursor c, cur;
        SQLiteDatabase sqlDb = getReadableDatabase();
        cur = mContext.getContentResolver().query(mCallsUri,
                new String[] { "date" }, null, null, null);
        ArrayList<Long> al = new ArrayList<Long>();
        if (cur.getCount() > 0){
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                al.add(cur.getLong(0));
                cur.moveToNext();
            }
        }
        cur.close();
        
        ContentValues values = new ContentValues(5);
        
        c = sqlDb.query(Table[2], null, null, null, null, null, null);
        result[0] = c.getCount();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            if(al.indexOf(c.getLong(2))!=-1){
                c.moveToNext();
                continue;
            }
            String name = c.getString(5);
            values.put(MyDatabase.CALLS[1], c.getString(1));
            values.put(MyDatabase.CALLS[2], c.getLong(2));
            values.put(MyDatabase.CALLS[3], c.getInt(3));
            values.put(MyDatabase.CALLS[4], c.getInt(4));
            values.put(MyDatabase.CALLS[5], ("null".equals(name)||TextUtils.isEmpty(name))?"":name);
            mContext.getContentResolver().insert(mCallsUri, values);
            values.clear();
            result[1]++;
            c.moveToNext();
        }
        c.close();
        closeDb(sqlDb);
        result[2] =(int)(System.currentTimeMillis() - start);
        return result;
	}
	
    static void logCursor(Cursor c) {
        String colName[] = c.getColumnNames();
        int columnLen = c.getColumnCount();
        c.moveToFirst();
        Log.i("dfdun", " log start ]");
        while (!c.isAfterLast()) {
            int i =0;
            String row="[ ", value;
            for(;i<columnLen;i++){
                value=c.getString(c.getColumnIndex(colName[i]));
                if(value!=null)
                    row+= " ("+colName[i]+":"+value+"),";
            }
            Log.i("dfdun", " " + row+" ]");
            c.moveToNext();
        }
        c.moveToFirst();
    }
	
	static class MyDatabase {
		static String SMS[] = new String[] { "_id", // auto ++
				"thread_id", 	// int 1
				"address", 		// string , text 2
				"person", 		// int 3
				"date", 		// int 4 , also long
				"protocol",		// int 5
				"read", 		// int 6
				"status", 		// int 7
				"type", 		// int 8
				"body", 		// string, text 9
		};
		static String SMS_1[] = new String[]{
			"address", "date"
		};

		static String CON[]=new String[]{
		    "_id",            // 0 auto ++ , 
		    "name",           // 1
		    "numbers",        // 2 all PhoneNumbers,eg: 10086,10010
		    "numbersType",    // 3
		    "emails",         // 4
		    "emailsType",     // 5
		};
		
		static String CALLS[]=new String[]{
		    "_id",
		    "number",     //1
		    "date",       //2
		    "duration",   //3
		    "type",       //4
		    "name",       //5
		};
		
		
		static String toString(String str[]){
			String s = "";
			int i = 0, len = str.length;
			for (i = 1; i < len - 1; i++)
				s += str[i] + ",";
			s += str[len - 1];
			return s;
		}
		
		static String Pro_con_query[]=new String[]{
		    ContactsContract.Contacts._ID,                // 0
		    ContactsContract.Contacts.DISPLAY_NAME,       // 1
		    ContactsContract.Contacts.HAS_PHONE_NUMBER,   // 2
		    ContactsContract.Contacts.LOOKUP_KEY,
		    //ContactsContract.CommonDataKinds.Phone.,
		};
	}

}
