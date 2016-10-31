package mibh.mis.facount;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import mibh.mis.facount.Data.PreferencesManager;
import mibh.mis.facount.Database.IMG_DB;
import mibh.mis.facount.Manager.Contextor;
import mibh.mis.facount.Manager.RealmManager;
import mibh.mis.facount.Service.GetLocation;
import mibh.mis.facount.Service.UploadImg;

/**
 * Created by ponlakiss on 06/25/2016.
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Contextor.getInstance().init(getApplicationContext());
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(realmConfiguration);
        Fabric.with(this, new Crashlytics());
        new GetLocation(this);
        startService();
        PreferencesManager.initializeInstance(this);
        changeDatabase();
    }

    private void startService() {
        Log.i("SERVICE", "Service created...");
        Intent startServiceIntent = new Intent(this, UploadImg.class);
        PendingIntent startWebServicePendingIntent = PendingIntent.getService(this, 0, startServiceIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60 * 1000 * 30, startWebServicePendingIntent);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    private void changeDatabase() {
        if (PreferencesManager.getInstance().getBoolValue(PreferencesManager.CHANGEDB)) {
            //RealmManager.getInstance().clearAllTbImage();
            IMG_DB img_db = new IMG_DB(this);
            ArrayList<IMG_DB.Image_Asset> cursor = img_db.Img_GetAllImg();
            for (int i = 0; i < cursor.size(); i++) {
                Log.d("TEST", cursor.size() + " " + PreferencesManager.getInstance().getBoolValue(PreferencesManager.CHANGEDB) + " " + cursor.get(i).FILENAME);
                RealmManager.getInstance().insertImage(cursor.get(i).ASSET_KEY, cursor.get(i).ASSET_TXT, cursor.get(i).LAT, cursor.get(i).LNG, cursor.get(i).LOCATIONNAME, cursor.get(i).FILENAME, cursor.get(i).EMP_INPUT, cursor.get(i).ATYPE_ID, cursor.get(i).ATYPE_NAME, cursor.get(i).COMMENT_IMG, cursor.get(i).DATE_IMG);
            }
            PreferencesManager.getInstance().setBoolValue(PreferencesManager.CHANGEDB, false);
        }

    }

}
