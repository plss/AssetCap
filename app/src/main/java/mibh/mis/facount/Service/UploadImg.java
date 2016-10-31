package mibh.mis.facount.Service;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kobjects.base64.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import mibh.mis.facount.Data.PreferencesManager;
import mibh.mis.facount.Manager.RealmManager;
import mibh.mis.facount.Realm.ImgStore;

/**
 * Created by ponlakiss on 10/16/2015.
 */
public class UploadImg extends IntentService {

    String ASSET_KEY = "";
    String ASSET_TXT = "";
    String LAT = "";
    String LNG = "";
    String LOCATIONNAME = "";
    String FILENAME = "";
    String EMP_INPUT = "";
    String ATYPE_ID = "";
    String ATYPE_NAME = "";
    String COMMENT_IMG = "";
    String COM_ID = "";
    String DATE_IMG = "";
    String UP_STATUS = "";
    List<ImgStore> cursor;
    PreferencesManager pm;

    public UploadImg() {
        super("ScheduledService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PreferencesManager.initializeInstance(this);
        pm = PreferencesManager.getInstance();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        cursor = RealmManager.getInstance().getAllImgInactive();
        for (int i = 0; i < cursor.size(); i++) {
            Log.d("SaveImg", cursor.get(i).getAssetKey() + " " + cursor.get(i).getFilename());
            FILENAME = cursor.get(i).getFilename();
            ASSET_KEY = cursor.get(i).getAssetKey();
            ASSET_TXT = cursor.get(i).getAssetTxt();
            LAT = cursor.get(i).getLat();
            LNG = cursor.get(i).getLng();
            LOCATIONNAME = cursor.get(i).getLocationname();
            EMP_INPUT = cursor.get(i).getEmpInput();
            ATYPE_ID = cursor.get(i).getAssetTypeId();
            ATYPE_NAME = cursor.get(i).getAssetTypeName();
            COMMENT_IMG = cursor.get(i).getCommentImg();
            COM_ID = cursor.get(i).getCompanyId();
            DATE_IMG = cursor.get(i).getDateImg();
            UP_STATUS = cursor.get(i).getUploadStatus();
            String resultSave = savePhoto(FILENAME);
            if (!resultSave.equalsIgnoreCase("error") && !resultSave.equalsIgnoreCase("false")) {
                //imgDb.UpdateStatusImg(FILENAME);
                RealmManager.getInstance().updateUploadStatus(FILENAME);
            }
        }
    }

    private String savePhoto(String fileName) {
        String Result = null;
        File imagesFolder = new File(Environment.getExternalStorageDirectory(), "ASSET");
        File output = new File(imagesFolder, fileName);
        if (output.exists()) {
            try {
                JSONArray array = new JSONArray();
                JSONObject dataIm_reg;

                JSONObject Img_file;
                JSONArray arrayIm_reg = new JSONArray();//Table Image

                Bitmap bitmap;
                ByteArrayOutputStream stream;
                byte[] byteArray;
                String strBase64;

                // ข้อมูล Bitmap
                Img_file = new JSONObject();
                /*bitmap = BitmapFactory.decodeFile(output.getAbsolutePath());
                stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 30, stream);*/
                ////////////////////////////////
                FileInputStream fis = new FileInputStream(output);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] b = new byte[1024];

                int bytesRead;
                while ((bytesRead = fis.read(b)) != -1) {
                    bos.write(b, 0, bytesRead);
                }
                ////////////////////////////////
                byteArray = bos.toByteArray();
                strBase64 = Base64.encode(byteArray);
                Img_file.put("file_name", fileName);
                Img_file.put("img_file", strBase64);

                array.put(Img_file);

                //ข้อมูลรูป
                dataIm_reg = new JSONObject();
                dataIm_reg.put("Req_id", ASSET_KEY);
                dataIm_reg.put("Type_img", "17");
                dataIm_reg.put("File_name", fileName);
                dataIm_reg.put("Lat", LAT);
                dataIm_reg.put("Lng", LNG);
                dataIm_reg.put("date_image", DATE_IMG);
                dataIm_reg.put("Status", "Active");
                arrayIm_reg.put(dataIm_reg);

                if (COMMENT_IMG.length() >= 296) {
                    COMMENT_IMG = COMMENT_IMG.substring(0, 295) + "..";
                }

                String result = new CallService().saveStateAsset(ASSET_KEY, ASSET_TXT, String.format("%.5f", Double.parseDouble(LAT)), String.format("%.5f", Double.parseDouble(LNG)), LOCATIONNAME, FILENAME, EMP_INPUT, ATYPE_ID, ATYPE_NAME, COMMENT_IMG, COM_ID);
                Result = new CallService().savePic(array.toString(), arrayIm_reg.toString());
                /*bitmap.recycle();
                bitmap = null;*/
                System.gc();
                Log.d("Save pic", Result);
                return Result;
            } catch (Exception e) {
                Log.d("Error save photo", e.toString());
                return "error";
            }
        } else {
            return "error";
        }
    }

}
