package mibh.mis.facount.Manager;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import mibh.mis.facount.Realm.HashtagData;
import mibh.mis.facount.Realm.ImgStore;
import mibh.mis.facount.Realm.TbServerUpdate;
import mibh.mis.facount.Utils.Utils;

public class RealmManager {

    private static RealmManager instance;

    public static RealmManager getInstance() {
        if (instance == null)
            instance = new RealmManager();
        return instance;
    }

    private Context mContext;

    private RealmManager() {
        mContext = Contextor.getInstance().getContext();
    }

    public void insertImage(String assetKey,
                            String assetTxt,
                            String lat,
                            String lng,
                            String locationname,
                            String filename,
                            String empInput,
                            String assetTypeId,
                            String assetTypeName,
                            String commentImg,
                            String companyId) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        ImgStore imgStore = new ImgStore();
        imgStore.setAssetKey(assetKey);
        imgStore.setAssetTxt(assetTxt);
        imgStore.setLat(lat);
        imgStore.setLng(lng);
        imgStore.setLocationname(locationname);
        imgStore.setFilename(filename);
        imgStore.setEmpInput(empInput);
        imgStore.setAssetTypeId(assetTypeId);
        imgStore.setAssetTypeName(assetTypeName);
        imgStore.setCommentImg(commentImg);
        imgStore.setCompanyId(companyId);
        imgStore.setDateImg(Utils.getInstance().getCurrentDateTime());
        imgStore.setUploadStatus("INACTIVE");
        realm.copyToRealmOrUpdate(imgStore);
        realm.commitTransaction();
        realm.close();
    }

    public String genImgName(String assetType, String AssetId) {

        Calendar c = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        String formattedDate = date.format(c.getTime());
        return "A" + assetType + "_" + formattedDate + "_" + AssetId + ".jpg";

    }

    public List<ImgStore> getImageByField(String fieldName, String fieldValue) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<ImgStore> result = realm.where(ImgStore.class)
                .equalTo(fieldName, fieldValue)
                .findAll();
        List<ImgStore> listResult = realm.copyFromRealm(result);
        realm.close();
        return listResult;
    }

    public List<ImgStore> getAllImg() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<ImgStore> result = realm.where(ImgStore.class)
                .findAll();
        List<ImgStore> listResult = realm.copyFromRealm(result);
        realm.close();
        return listResult;
    }

    public List<ImgStore> getImgByAssetType(String assetType) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<ImgStore> result = realm.where(ImgStore.class)
                .equalTo("assetTypeId", assetType)
                .findAll();
        List<ImgStore> listResult = realm.copyFromRealm(result);
        realm.close();
        return listResult;
    }

    public List<ImgStore> getAllImgInactive() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<ImgStore> result = realm.where(ImgStore.class)
                .equalTo("uploadStatus", "INACTIVE")
                .findAll();
        List<ImgStore> listResult = realm.copyFromRealm(result);
        realm.close();
        return listResult;
    }

    public void clearAllTbImage() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(ImgStore.class);
        realm.commitTransaction();
    }

    public void updateUploadStatus(String filename) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        /*RealmResults<ImgStore> qry = realm.where(ImgStore.class).equalTo("filename", filename).findAll();
        if (qry.size() > 0) {*/
        ImgStore result = realm.where(ImgStore.class)
                .equalTo("filename", filename)
                .findFirst();
        result.setUploadStatus("ACTIVE");
        realm.commitTransaction();
        realm.close();
        //}

    }

    public void manageHashtagValue(String jsonResult) {
        try {
            String lastDateServer = jsonResult.substring(0, 12);
            String strHashtag = jsonResult.substring(12);
            JSONArray data = new JSONArray(strHashtag);
            for (int i = 0; i < data.length(); i++) {
                JSONObject c = data.getJSONObject(i);
                upsertHashtag(c.getString("LIST_ID"),
                        c.getString("LIST_NAME"),
                        c.getString("GROUP_ID"),
                        c.getString("TYPE_ID"),
                        c.getString("SERVERDATE"),
                        c.getString("STATUS"));
            }
            upsertTbServerUpdate(lastDateServer);
        } catch (StringIndexOutOfBoundsException e) {
            Log.e("ConvertHashtagString", e.toString());
        } catch (JSONException e) {
            Log.e("ConvertHashtagJson", e.toString());
        }
    }

    public void upsertTbServerUpdate(String lastUpdate) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        TbServerUpdate tbServerUpdate = new TbServerUpdate();
        tbServerUpdate.setTbName("TbHashtag");
        tbServerUpdate.setServerDate(lastUpdate);
        realm.copyToRealmOrUpdate(tbServerUpdate);
        realm.commitTransaction();
        realm.close();
        /*Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        TbServerUpdate tbServerUpdate = realm.createObject(TbServerUpdate.class);
        tbServerUpdate.setTbName(name);
        tbServerUpdate.setServerDate(lastUpdate);
        realm.commitTransaction();
        realm.close();*/
    }

    public void upsertHashtag(String listId, String listName, String gropId, String typeId, String serverDate, String status) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        HashtagData hashtagData = new HashtagData();
        hashtagData.setListId(listId);
        hashtagData.setListName(listName);
        hashtagData.setGroupId(gropId);
        hashtagData.setTypeId(typeId);
        hashtagData.setServerDate(serverDate);
        hashtagData.setStatus(status);
        realm.copyToRealmOrUpdate(hashtagData);
        realm.commitTransaction();
        realm.close();
    }

    public List<TbServerUpdate> getLastDateTbServerUpdate() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<TbServerUpdate> result = realm.where(TbServerUpdate.class)
                .equalTo("tbName", "TbHashtag")
                .findAll();
        List<TbServerUpdate> listResult = realm.copyFromRealm(result);
        realm.close();
        return listResult;
    }

    public List<HashtagData> getHasgtag() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<HashtagData> result = realm.where(HashtagData.class)
                .equalTo("status", "Active")
                .findAllSorted("listName", Sort.ASCENDING);
        List<HashtagData> listResult = realm.copyFromRealm(result);
        realm.close();
        return listResult;
    }

}
