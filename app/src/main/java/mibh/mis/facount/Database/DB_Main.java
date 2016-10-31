package mibh.mis.facount.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by ponlakiss on 10/13/2015.
 */
public class DB_Main extends SQLiteOpenHelper {

    private static DB_Main sInstance;

    private final String TAG = "DB_Main";

    /* Database name */
    static final String dbName = "DB_ASSSET";
    static final String Tb_Img = "IMG_ASSET";
    static final int versionnew = 1;
    static int versionold;

    /* field name for IMAGE_TBL */
    /*//static final String Im_WorkHid = "Work_id";
    static final String Im_Group_Type = "Group_Type";
    static final String Im_Type_img = "Type_img";
    static final String Im_Filename = "Filename";
    //static final String Im_Doc_item = "Doc_item";
    static final String Im_Lat_img = "Lat";
    static final String Im_Lng_img = "Lng";
    static final String Im_Date_img = "Date_img";
    static final String Im_Status_Update = "Stat_Upd";
    static final String Im_Comment = "Comment_img";*/

    static final String ASSET_KEY = "ASSET_KEY";
    static final String ASSET_TXT = "ASSET_TXT";
    static final String LAT = "LAT";
    static final String LNG = "LNG";
    static final String LOCATIONNAME = "LOCATIONNAME";
    static final String FILENAME = "FILENAME";
    static final String EMP_INPUT = "EMP_INPUT";
    static final String ATYPE_ID = "ATYPE_ID";
    static final String ATYPE_NAME = "ATYPE_NAME";
    static final String COMMENT_IMG = "COMMENT_IMG";
    static final String COM_ID = "COM_ID";
    static final String DATE_IMG = "DATE_IMG";
    static final String UP_STATUS = "UP_STATUS";

    public static synchronized DB_Main getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DB_Main(context.getApplicationContext());
        }
        return sInstance;
    }

    public DB_Main(Context context) {
        super(context, dbName, null, versionnew);
        SQLiteDatabase db = this.getWritableDatabase();

        if (versionnew != db.getVersion()) {
            onUpgrade(db, versionnew, db.getVersion());
        }

    }

    public void finalize() throws Throwable {
        SQLiteDatabase db = this.getReadableDatabase();
        if (null != db) {
            db.close();
        }
        super.finalize();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.setLocale(Locale.getDefault());
        db.setLockingEnabled(true);

        db.execSQL("CREATE TABLE " + Tb_Img + " (" + ASSET_KEY + " TEXT ,"
                + ASSET_TXT + " TEXT ,"
                + LAT + " TEXT ,"
                + LNG + " TEXT ,"
                + LOCATIONNAME + " TEXT ,"
                + FILENAME + " TEXT ,"
                + EMP_INPUT + " TEXT ,"
                + ATYPE_ID + " TEXT ,"
                + ATYPE_NAME + " TEXT ,"
                + COMMENT_IMG + " TEXT ,"
                + COM_ID + " TEXT ,"
                + DATE_IMG + " TEXT ,"
                + UP_STATUS + " TEXT )");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade " + String.valueOf(oldVersion) + "," + String.valueOf(newVersion));
    }

    public void EmptyAllTable() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DELETE FROM " + Tb_Img);
    }

    String GetCurrentDateTime() {
        /* Add image capture time */
        Calendar c = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = date.format(c.getTime());
        return formattedDate;
    }

    String GetKeyReqTemp(String Id_ck) {
        /* Add image capture time */
        Calendar c = Calendar.getInstance();
        //SimpleDateFormat date = new SimpleDateFormat("yyMM");
        SimpleDateFormat date = new SimpleDateFormat("yyyy");

        int year = Integer.parseInt(date.format(c.getTime()).toString());

        if (year < 2500) {
            year = year + 543;
        }
        SimpleDateFormat date1 = new SimpleDateFormat("MM");
        String Key_str = String.valueOf(year) + date1.format(c.getTime()).toString();


        String formattedDate = "BT" + Key_str.substring(2, 6) + Id_ck;

        return formattedDate;
    }


    String GetKeyContract_req() {
        /* Add image capture time */
        Calendar c = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("yyMM");
        String formattedDate = date.format(c.getTime());
        return formattedDate;
    }

    String GetString(Cursor vCursor, String vindex, String vDefault) {
        int c = vCursor.getColumnIndex(vindex);

        if (c >= 0) {
            return vCursor.getString(c);
        } else {
            return vDefault;
        }
    }

    float GetFloat(Cursor vCursor, String vindex, float vDefault) {
        int c = vCursor.getColumnIndex(vindex);

        if (c >= 0) {
            return vCursor.getFloat(c);
        } else {
            return vDefault;
        }
    }

    int GetInteger(Cursor vCursor, String vindex, int vDefault) {
        int c = vCursor.getColumnIndex(vindex);

        if (c >= 0) {
            return vCursor.getInt(c);
        } else {
            return vDefault;
        }
    }

    String InsertImage(String asset_key,
                       String asset_txt,
                       String lat,
                       String lng,
                       String locationname,
                       String filename,
                       String emp_input,
                       String atype_id,
                       String atype_name,
                       String comment_img,
                       String com_id,
                       String up_status) {
        String Date = GetCurrentDateTime();
        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(ASSET_KEY, asset_key);
        cv.put(ASSET_TXT, asset_txt);
        cv.put(LAT, lat);
        cv.put(LNG, lng);
        cv.put(LOCATIONNAME, locationname);
        cv.put(FILENAME, filename);
        cv.put(EMP_INPUT, emp_input);
        cv.put(ATYPE_ID, atype_id);
        cv.put(ATYPE_NAME, atype_name);
        cv.put(COMMENT_IMG, comment_img);
        cv.put(COM_ID, com_id);
        cv.put(DATE_IMG, Date);
        cv.put(UP_STATUS, up_status);

        db.insert(Tb_Img, null, cv);
        /*db.close();*/

        return Date;
    }

    Cursor GetAllImg() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cur = db.rawQuery("Select * from " + Tb_Img + " ", null);
        return cur;
    }

    Cursor GetImgByType(String TYPE) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cur = db.rawQuery("Select * from " + Tb_Img + " WHERE " + ATYPE_ID + " ='" + TYPE + "'", null);
        return cur;
    }

    Cursor GetImgInactive() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cur = db.rawQuery("Select * from " + Tb_Img + " WHERE " + UP_STATUS + " = 'INACTIVE'", null);
        return cur;
    }

    long UpdateStatusImg(String filename) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues cv = new ContentValues();

        if (filename != null) {
            cv.put(FILENAME, filename);
            cv.put(UP_STATUS, "ACTIVE");
        }
        String where = FILENAME + "=?";
        String[] whereArgs = new String[]{filename};
        long l;
        l = db.update(Tb_Img, cv, where, whereArgs);
        return l;
  /*db.close();*/
    }

}
