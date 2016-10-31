package mibh.mis.facount.Data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ponlakiss on 10/09/2015.
 */

public class AccessData {

    private static ArrayList<AssetType> assetTypes = new ArrayList<>();
    private static ArrayList<Asset> assets = new ArrayList<>();
    private static ArrayList<Company> companies = new ArrayList<>();

    public static ArrayList<AssetType> getAssetTypes() {
        return assetTypes;
    }

    public static void setAssetTypes(String Json) {
        try {
            assetTypes.clear();
            JSONArray data = new JSONArray(Json);
            for (int i = 0; i < data.length(); ++i) {
                JSONObject c = data.getJSONObject(i);
                AssetType assetType = new AssetType();
                assetType.setASSET_NAME(c.getString("ASSET_NAME") == null ? "" : c.getString("ASSET_NAME"));
                assetType.setASSET_TYPE(c.getString("ASSET_TYPE") == null ? "" : c.getString("ASSET_TYPE"));
                assetType.setSTATUS(c.getString("STATUS") == null ? "" : c.getString("STATUS"));
                assetTypes.add(assetType);
            }
        } catch (Exception e) {
            Log.d("Error Json AssetType", e.toString());
        }
    }

    public static ArrayList<Asset> getAssets() {
        return assets;
    }

    public static void setAssets(String Json) {
        try {
            assets.clear();
            JSONArray data = new JSONArray(Json);
            for (int i = 0; i < data.length(); ++i) {
                JSONObject c = data.getJSONObject(i);
                Asset asset = new Asset();
                asset.setAssetId(c.getString("ASS_REF_ID") == null ? "" : c.getString("ASS_REF_ID"));
                asset.setAssetName(c.getString("ASS_REF_NAME") == null ? "" : c.getString("ASS_REF_NAME"));
                asset.setAssetDetail(c.getString("ASS_DETAIL") == null ? "" : c.getString("ASS_DETAIL"));
                asset.setComId(c.getString("COM_ID") == null ? "" : c.getString("COM_ID"));
                asset.setOwnerName(c.getString("OWNER_NAME") == null ? "" : c.getString("OWNER_NAME"));
                asset.setDateBuy(c.getString("DATE_BUY") == null ? "" : c.getString("DATE_BUY"));
                asset.setStatus(c.getString("STATUS") == null ? "" : c.getString("STATUS"));
                asset.setDateServer(c.getString("SERVER_DATE") == null ? "" : c.getString("SERVER_DATE"));
                asset.setAssetTypeId(c.getString("ASSET_TYPE") == null ? "" : c.getString("ASSET_TYPE"));
                asset.setAssetTypeName(c.getString("ASSET_NAME") == null ? "" : c.getString("ASSET_NAME"));
                asset.setTypeAst(c.getString("TypeAst") == null ? "" : c.getString("TypeAst"));
                assets.add(asset);
            }
        } catch (Exception e) {
            Log.d("Error Json Asset", e.toString());
        }
    }

    public static ArrayList<Company> getCompanies() {
        return companies;
    }

    public static void setCompanies(String Json) {
        try {
            companies.clear();
            JSONArray data = new JSONArray(Json);
            for (int i = 0; i < data.length(); ++i) {
                JSONObject c = data.getJSONObject(i);
                Company company = new Company();
                company.setCOMPANY_ID(c.getString("COMPANY_ID") == null ? "" : c.getString("COMPANY_ID"));
                company.setNAME_1(c.getString("NAME_1") == null ? "" : c.getString("NAME_1"));
                company.setNAME_2(c.getString("NAME_2") == null ? "" : c.getString("NAME_2"));
                companies.add(company);
            }
        } catch (Exception e) {
            Log.d("Error Json Company", e.toString());
        }
    }

}
