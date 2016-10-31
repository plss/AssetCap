package mibh.mis.facount.Utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.provider.Settings;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import mibh.mis.facount.Manager.Contextor;

/**
 * Created by ponlakiss on 06/25/2016.
 */
public class Utils {

    private static Utils instance;

    public static Utils getInstance() {
        if (instance == null)
            instance = new Utils();
        return instance;
    }

    private Context mContext;

    private Utils() {
        mContext = Contextor.getInstance().getContext();
    }


    public String getDeviceId() {
        return Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public String getVersionName() {
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            return pInfo.versionName;
        } catch (Exception e) {
            return "0.0";
        }
    }

    public String getDateDiffFormat(String yyMMdd) {
        Calendar c = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("yyMMdd", Locale.US);
        Date startDate;
        String strResult = "";
        try {
            startDate = df.parse(yyMMdd);
            long diff = c.getTimeInMillis() - startDate.getTime();
            long diffYear = diff / ((long) 60 * 60 * 1000 * 24 * 365);
            diff = (diff - (diffYear * ((long) 60 * 60 * 1000 * 24 * 365)));
            long diffMonth = diff / (long) (60 * 60 * 1000 * 24 * 30.41666666);
            diff = diff - (diffMonth * (long) (60 * 60 * 1000 * 24 * 30.41666666));
            long diffDay = diff / (60 * 60 * 1000 * 24);
            if (diffYear > 0) {
                strResult += diffYear + " y ";
            }
            if (diffMonth > 0) {
                strResult += diffMonth + " m.";
            } else if (diffMonth <= 0) {
                strResult += "1 m.";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return strResult;
    }

    public String getDateFormat(String yyMMdd) {
        DateFormat df = new SimpleDateFormat("yyMMdd");
        String formattedDate = "";
        try {
            Date dateDefault = df.parse(yyMMdd);
            SimpleDateFormat date = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            formattedDate = date.format(dateDefault.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedDate;
    }

    public String getCurrentDateTime() {
        /* Add image capture time */
        Calendar c = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = date.format(c.getTime());
        return formattedDate;
    }

}
