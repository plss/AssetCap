package mibh.mis.facount.Service;


import android.util.Log;

import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import LibClass.SOAPWebserviceProperty;

public class CallService {

    private static final String NAMESPACE = "http://tempuri.org/";
    private static final String URLHRMS = "http://www.mibholding.com/DABT.asmx";
    private static final String URLLogin = "http://www.mibholding.com/fuel.asmx";
    private static final String URL = "http://www.mibholding.com/TMSMOBILE.asmx";
    private static String SOAP_ACTION = "http://tempuri.org/";
    private static String METHOD_NAME = "";
    private static SOAPWebserviceProperty soap_property = null;

    public String checkLogin(String ID_CARD, String EMP_ID) {
        try {
            METHOD_NAME = "CheckLogInFuel";
            soap_property = new SOAPWebserviceProperty();
            soap_property.urlWebservice = URLLogin;
            soap_property.namespaceWebservice = NAMESPACE;
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            request.addProperty("Id_Card", ID_CARD);
            request.addProperty("EMP_ID", EMP_ID);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

            HttpTransportSE androidHttpTransport = new HttpTransportSE(URLLogin);
            androidHttpTransport.call(SOAP_ACTION + METHOD_NAME, envelope);
            SoapPrimitive result = (SoapPrimitive) envelope.getResponse();
            return result.toString();
        } catch (Exception e) {
            Log.d("Error Login", e.toString());
            return "error";
        }
    }

    public String saveRegister(String ID_CARD, String EMP_ID, String FNAME, String LNAME, String TEL) {
        try {
            METHOD_NAME = "SaveRegisterFuel";
            soap_property = new SOAPWebserviceProperty();
            soap_property.urlWebservice = URLLogin;
            soap_property.namespaceWebservice = NAMESPACE;
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            JSONObject polydata = new JSONObject();
            polydata.put("ID_CARD", ID_CARD);
            polydata.put("EMP_ID", EMP_ID);
            polydata.put("FNAME", FNAME);
            polydata.put("LNAME", LNAME);
            polydata.put("TEL", TEL);
            request.addProperty("JsonOb_UserData", polydata.toString());
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

            HttpTransportSE androidHttpTransport = new HttpTransportSE(URLLogin);
            androidHttpTransport.call(SOAP_ACTION + METHOD_NAME, envelope);
            SoapPrimitive result = (SoapPrimitive) envelope.getResponse();
            return result.toString();
        } catch (Exception e) {
            Log.d("Error Register", e.toString());
            return "error";
        }
    }

    public String getHrmsData(String empid) {
        try {
            METHOD_NAME = "Get_HRMS_DATA";
            soap_property = new SOAPWebserviceProperty();
            soap_property.urlWebservice = URLHRMS;
            soap_property.namespaceWebservice = NAMESPACE;
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            request.addProperty("EMP_ID", empid);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

            HttpTransportSE androidHttpTransport = new HttpTransportSE(URLHRMS);
            androidHttpTransport.call(SOAP_ACTION + METHOD_NAME, envelope);
            SoapPrimitive result = (SoapPrimitive) envelope.getResponse();
            return result.toString();
        } catch (Exception e) {
            Log.d("Error ForResult", e.toString());
            return "error";
        }
    }

    public String getAssetType(String empId) {
        try {
            METHOD_NAME = "Get_AssetType";
            soap_property = new SOAPWebserviceProperty();
            soap_property.urlWebservice = URL;
            soap_property.namespaceWebservice = NAMESPACE;
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty("JsonEMP_ID", empId);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            androidHttpTransport.call(SOAP_ACTION + METHOD_NAME, envelope);
            SoapPrimitive result = (SoapPrimitive) envelope.getResponse();
            //Log.d("Get_AssetType", result.toString());
            return result.toString();
        } catch (Exception e) {
            Log.d("Error Get_AssetType", e.toString());
            return "error";
        }
    }

    public String getCompanyList(String empId) {
        try {
            METHOD_NAME = "Get_CompanyList";
            soap_property = new SOAPWebserviceProperty();
            soap_property.urlWebservice = URL;
            soap_property.namespaceWebservice = NAMESPACE;
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            request.addProperty("JsonEMP_ID", empId);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            Log.d("TEST", "getCompanyList: " + empId + " " + request.toString());
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            androidHttpTransport.call(SOAP_ACTION + METHOD_NAME, envelope);
            SoapPrimitive result = (SoapPrimitive) envelope.getResponse();
            //Log.d("Get_CompanyList", result.toString());
            return result.toString();
        } catch (Exception e) {
            Log.d("Error Get_CompanyList", e.toString());
            return "error";
        }
    }

    public String getAssetList(String TYPEID, String COMID) {
        try {
            METHOD_NAME = "Get_AssetList";
            soap_property = new SOAPWebserviceProperty();
            soap_property.urlWebservice = URL;
            soap_property.namespaceWebservice = NAMESPACE;
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            request.addProperty("Type_id", TYPEID);
            request.addProperty("Com_ID", COMID);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            androidHttpTransport.call(SOAP_ACTION + METHOD_NAME, envelope);
            SoapPrimitive result = (SoapPrimitive) envelope.getResponse();
            Log.d("Get_AssetList", result.toString());
            return result.toString();
        } catch (Exception e) {
            Log.d("Error Get_AssetList", e.toString());
            return "error";
        }
    }

    public String saveStateAsset(String asset_key,
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
        try {
            METHOD_NAME = "Save_AssetState";
            soap_property = new SOAPWebserviceProperty();
            soap_property.urlWebservice = URL;
            soap_property.namespaceWebservice = NAMESPACE;
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            JSONObject polydata = new JSONObject();
            polydata.put("RUN_ID", "New");
            polydata.put("TCK_ID", asset_key);
            polydata.put("TCK_LICEN", asset_txt);
            polydata.put("LAT", lat);
            polydata.put("LNG", lng);
            polydata.put("LOCATIONNAME", locationname);
            polydata.put("PICTUREPATH", filename);
            polydata.put("EMP_INPUT", emp_input);
            polydata.put("TYPE_ASSET", atype_id);
            polydata.put("TYPE_NAME", atype_name);
            polydata.put("COMMENT_PHOTO", comment_img);
            polydata.put("COM_ID", com_id);
            polydata.put("IMG_TYPE", "");
            polydata.put("IMG_TYPENAME", "");
            polydata.put("STATUS", "");
            request.addProperty("json_Asset", polydata.toString());
            Log.d("TEST SENT", request.toString());
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            androidHttpTransport.call(SOAP_ACTION + METHOD_NAME, envelope);
            SoapPrimitive result = (SoapPrimitive) envelope.getResponse();
            return result.toString();
        } catch (Exception e) {
            Log.d("Error StateAsset", e.toString());
            return "error";
        }
    }

    public String savePic(String json_photo, String json_Img_ct) {
        try {
            String URL_SAVEPIC = "http://www.mibholding.com/dabt.asmx";
            METHOD_NAME = "SavePhoto_json";
            soap_property = new SOAPWebserviceProperty();
            soap_property.urlWebservice = URL_SAVEPIC;
            soap_property.namespaceWebservice = NAMESPACE;
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty("json_photo", json_photo);
            request.addProperty("json_Img_ct", json_Img_ct);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL_SAVEPIC);
            androidHttpTransport.call(SOAP_ACTION + METHOD_NAME, envelope);
            SoapPrimitive result = (SoapPrimitive) envelope.getResponse();
            String resultData = result.toString();
            Log.d("SavePhoto", resultData);
            return resultData;
        } catch (Exception e) {
            Log.d("Error SavePhoto", e.toString());
            return "error";
        }
    }

    public String getActiveVersion() {
        try {
            String URL = "http://www.mibholding.com/InterfaceTmsView.svc";
            METHOD_NAME = "GetActiveVersion";
            soap_property = new SOAPWebserviceProperty();
            soap_property.urlWebservice = URL;
            soap_property.namespaceWebservice = NAMESPACE;
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty("AppId", "M006");
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            androidHttpTransport.call("http://tempuri.org/IInterfaceTmsView/" + METHOD_NAME, envelope);
            SoapPrimitive result = (SoapPrimitive) envelope.getResponse();
            String resultData = result.toString();
            return resultData;
        } catch (Exception e) {
            Log.d("Error version", e.toString());
        }
        return "error";
    }

    public String getHashtag(String date) {
        try {
            METHOD_NAME = "Get_data_hashtagFixAsset";
            soap_property = new SOAPWebserviceProperty();
            soap_property.urlWebservice = URL;
            soap_property.namespaceWebservice = NAMESPACE;
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty("yyMMddHHmmss", date);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            androidHttpTransport.call(SOAP_ACTION + METHOD_NAME, envelope);
            SoapPrimitive result = (SoapPrimitive) envelope.getResponse();
            String resultData = result.toString();
            return resultData;
        } catch (Exception e) {
            Log.d("Error getHashtag", e.toString());
            return "error";
        }
    }

}
