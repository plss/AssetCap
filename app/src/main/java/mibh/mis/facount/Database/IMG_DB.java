package mibh.mis.facount.Database;

import android.content.Context;
import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by ponlakiss on 10/14/2015.
 */
public class IMG_DB {

    private DB_Main db_main;

    public IMG_DB(Context context) {
        db_main = new DB_Main(context);
        db_main.getWritableDatabase();
    }

    public static class Image_Asset {

        public String ASSET_KEY = "";
        public String ASSET_TXT = "";
        public String LAT = "";
        public String LNG = "";
        public String LOCATIONNAME = "";
        public String FILENAME = "";
        public String EMP_INPUT = "";
        public String ATYPE_ID = "";
        public String ATYPE_NAME = "";
        public String COMMENT_IMG = "";
        public String COM_ID = "";
        public String DATE_IMG = "";
        public String UP_STATUS = "";

        Image_Asset() {
        }
    }

    public void SaveImg(String asset_key,
                        String asset_txt,
                        String lat,
                        String lng,
                        String locationname,
                        String filename,
                        String emp_input,
                        String atype_id,
                        String atype_name,
                        String comment_img,
                        String com_id) {
        db_main.InsertImage(asset_key, asset_txt, lat, lng, locationname, filename, emp_input, atype_id, atype_name, comment_img, com_id, "INACTIVE");
    }

    public String Gen_imgName(String ASSETTYPE, String ASSETID) {

        Calendar c = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        String formattedDate = date.format(c.getTime());
        return "A" + ASSETTYPE + "_" + formattedDate + "_" + ASSETID + ".jpg";

    }

    public ArrayList<Image_Asset> Img_GetAllImg() {
        ArrayList<Image_Asset> Images = new ArrayList<>();

        Cursor c = db_main.GetAllImg();
        if (c.getCount() > 0) {
            if (c.moveToFirst()) {
                do {
                    Image_Asset m = new Image_Asset();

                    m.ASSET_KEY = db_main.GetString(c, DB_Main.ASSET_KEY, "");
                    m.ASSET_TXT = db_main.GetString(c, DB_Main.ASSET_TXT, "");
                    m.LAT = db_main.GetString(c, DB_Main.LAT, "");
                    m.LNG = db_main.GetString(c, DB_Main.LNG, "");
                    m.LOCATIONNAME = db_main.GetString(c, DB_Main.LOCATIONNAME, "");
                    m.FILENAME = db_main.GetString(c, DB_Main.FILENAME, "");
                    m.EMP_INPUT = db_main.GetString(c, DB_Main.EMP_INPUT, "");
                    m.ATYPE_ID = db_main.GetString(c, DB_Main.ATYPE_ID, "");
                    m.ATYPE_NAME = db_main.GetString(c, DB_Main.ATYPE_NAME, "");
                    m.COMMENT_IMG = db_main.GetString(c, DB_Main.COMMENT_IMG, "");
                    m.COM_ID = db_main.GetString(c, DB_Main.COM_ID, "");
                    m.DATE_IMG = db_main.GetString(c, DB_Main.DATE_IMG, "");
                    m.UP_STATUS = db_main.GetString(c, DB_Main.UP_STATUS, "");

                    Images.add(m);

                } while (c.moveToNext());
            }
        }
        c.close();
        return Images;
    }

    public ArrayList<Image_Asset> GetImgByType(String TYPE) {
        ArrayList<Image_Asset> Images = new ArrayList<>();

        Cursor c = db_main.GetImgByType(TYPE);
        if (c.getCount() > 0) {
            if (c.moveToFirst()) {
                do {
                    Image_Asset m = new Image_Asset();

                    m.ASSET_KEY = db_main.GetString(c, DB_Main.ASSET_KEY, "");
                    m.ASSET_TXT = db_main.GetString(c, DB_Main.ASSET_TXT, "");
                    m.LAT = db_main.GetString(c, DB_Main.LAT, "");
                    m.LNG = db_main.GetString(c, DB_Main.LNG, "");
                    m.LOCATIONNAME = db_main.GetString(c, DB_Main.LOCATIONNAME, "");
                    m.FILENAME = db_main.GetString(c, DB_Main.FILENAME, "");
                    m.EMP_INPUT = db_main.GetString(c, DB_Main.EMP_INPUT, "");
                    m.ATYPE_ID = db_main.GetString(c, DB_Main.ATYPE_ID, "");
                    m.ATYPE_NAME = db_main.GetString(c, DB_Main.ATYPE_NAME, "");
                    m.COMMENT_IMG = db_main.GetString(c, DB_Main.COMMENT_IMG, "");
                    m.COM_ID = db_main.GetString(c, DB_Main.COM_ID, "");
                    m.DATE_IMG = db_main.GetString(c, DB_Main.DATE_IMG, "");
                    m.UP_STATUS = db_main.GetString(c, DB_Main.UP_STATUS, "");

                    Images.add(m);

                } while (c.moveToNext());
            }
        }
        c.close();
        return Images;
    }

    public ArrayList<Image_Asset> Img_GetImgInactive() {
        ArrayList<Image_Asset> Images = new ArrayList<>();

        Cursor c = db_main.GetImgInactive();
        if (c.getCount() > 0) {
            if (c.moveToFirst()) {
                do {
                    Image_Asset m = new Image_Asset();

                    m.ASSET_KEY = db_main.GetString(c, DB_Main.ASSET_KEY, "");
                    m.ASSET_TXT = db_main.GetString(c, DB_Main.ASSET_TXT, "");
                    m.LAT = db_main.GetString(c, DB_Main.LAT, "");
                    m.LNG = db_main.GetString(c, DB_Main.LNG, "");
                    m.LOCATIONNAME = db_main.GetString(c, DB_Main.LOCATIONNAME, "");
                    m.FILENAME = db_main.GetString(c, DB_Main.FILENAME, "");
                    m.EMP_INPUT = db_main.GetString(c, DB_Main.EMP_INPUT, "");
                    m.ATYPE_ID = db_main.GetString(c, DB_Main.ATYPE_ID, "");
                    m.ATYPE_NAME = db_main.GetString(c, DB_Main.ATYPE_NAME, "");
                    m.COMMENT_IMG = db_main.GetString(c, DB_Main.COMMENT_IMG, "");
                    m.COM_ID = db_main.GetString(c, DB_Main.COM_ID, "");
                    m.DATE_IMG = db_main.GetString(c, DB_Main.DATE_IMG, "");
                    m.UP_STATUS = db_main.GetString(c, DB_Main.UP_STATUS, "");

                    Images.add(m);

                } while (c.moveToNext());
            }
        }
        c.close();
        return Images;
    }

    public void UpdateStatusImg(String vFilename) {
         /* Create new img data */
        db_main.UpdateStatusImg(vFilename);
    }

    public void close() {
        db_main.close();
    }

    public void EmptyAllTable() {
        db_main.EmptyAllTable();
    }
}
