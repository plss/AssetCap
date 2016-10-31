package mibh.mis.facount.Service;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import mibh.mis.facount.Data.PreferencesManager;

/**
 * Created by ponlakiss on 10/16/2015.
 */
public class Version {

    private static final int MAX_APP_FILE_SIZE = 40 * 1024;
    //private Context context;
    private Activity act;
    private ArrayList<HashMap<String, String>> Result;
    private String newVersion, urlDownload, appName, appVer;
    private ProgressDialog progressBar;
    private static final int progress_bar_type = 0;
    private int downloadedSize = 0, downloadtotalSize = 0;
    private boolean isCheckingUpdate = false;
    private PreferencesManager pm;

    public Version(final Activity act) {
        this.act = act;
        pm = PreferencesManager.getInstance();
        new checkVersion().execute();
    }

    public String GetVersionName() {
        String app_ver = "0.00";

        try {
            //Get version
            app_ver = act.getPackageManager().getPackageInfo(act.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {  /*No operation*/ }

        return app_ver;
    }

    private String GetApplicationName() {
        //Get application info
        final PackageManager pm = act.getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(act.getPackageName(), 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }

        final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");

        return applicationName;
    }

    private void DeleteFile(String path, String file) {
         /*Main directory (SD card directory) */
        File sdDir = Environment.getExternalStorageDirectory();
        String sdImageMainDirectory = (sdDir.toString() + path);
        try {
            File fXmlFile = new File(sdImageMainDirectory + file);
            fXmlFile.delete();
        } catch (Exception e) {
        }
    }

    void convertVersion(String result) throws Exception {
        JSONArray data = new JSONArray(result);
        JSONObject c = data.getJSONObject(0);
        newVersion = c.getString("CURRENT_VERSION");
        urlDownload = c.getString("URL");
    }

    public void DoUpdateProcess() {
        try {
            Log.d("Version", "===============DoUpdateProcess");
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(act);
            builderSingle.setMessage("ทำการติดตั้งจากเวอร์ชัน " + Double.parseDouble(appVer) + "ไปยัง " + Double.parseDouble(newVersion));
            builderSingle.setCancelable(false);
            builderSingle.setPositiveButton("ตกลง",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new DownloadFileFromURL().execute();
                            dialog.dismiss();
                        }
                    });
            if (Double.parseDouble(appVer) != Double.parseDouble(newVersion) && newVersion != null) {
                builderSingle.show();
            } else {
                /*Intent mainIntent = new Intent(act, Login.class);
                act.startActivity(mainIntent);
                act.finish();*/
            }
        } catch (Exception e) {
            Log.d("TEST VERION", e.toString());
            /*Intent mainIntent = new Intent(act, Login.class);
            act.startActivity(mainIntent);
            act.finish();*/
        }
    }

    class checkVersion extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            String str = new CallService().getActiveVersion();
            Log.d("TESTVERSION", str);
            return str;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                if (s.equalsIgnoreCase("error") || s.equalsIgnoreCase("")) {
                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(act);
                    builderSingle.setMessage("ไม่สามารถตนวจสอบเวอชันได้");
                    builderSingle.setPositiveButton("ตกลง",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    act.finish();
                                }
                            });
                } else {
                    appName = GetApplicationName();
                    appVer = GetVersionName();
                    pm.setValue(pm.VERSION, appVer);
                    convertVersion(s);
                    DoUpdateProcess();
                }

            } catch (Exception e) {
                e.printStackTrace();
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(act);
                builderSingle.setMessage("ไม่สามารถตรวจสอบเวอชันได้");
                builderSingle.setPositiveButton("ตกลง",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                act.finish();
                            }
                        });
            }

        }
    }

    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar = new ProgressDialog(act);
            progressBar.setMessage(Html.fromHtml("<h2>กำลังดาวโหลดไฟล์</h2><br><p>ชื่อ: " + appName + "<br>เวอร์ชันปัจจุบัน: " + appVer + "<br>เวอร์ชันไหม่: " + newVersion + "</p>"));
            progressBar.setIndeterminate(false);
            progressBar.setMax(100);
            progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressBar.setCancelable(true);
            progressBar.show();
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            Log.d("Version", "===============ApplicationDownloadUpdate");
            try {
             /*set the download URL, a url that points to a file on the internet */
                URL url = new URL(urlDownload);

             /*create the new http connection */
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

             /*set up some things on the connection */
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(false);

            /* and connect! */
                urlConnection.connect();

             /*set the path where we want to save the file */
                String PATH = Environment.getExternalStorageDirectory() + "/download/";

             /*Delete older file */
                DeleteFile(PATH, appName);

             /*Create new file */
                File file = new File(PATH);

        	/* Make directory to storage file */
                file.mkdirs();

        	 /*Create output file */
                File outputFile = new File(file, appName);

             /*this will be used to write the downloaded data into the file we created */
                FileOutputStream fileOutput = new FileOutputStream(outputFile);

             /*this will be used in reading the data from the internet */
                InputStream inputStream = urlConnection.getInputStream();

             /*this is the total size of the file */
                downloadtotalSize = urlConnection.getContentLength();
                Log.d("Version", "File size = " + downloadtotalSize);

             /*create a buffer... */
                byte[] buffer = new byte[MAX_APP_FILE_SIZE];

             /*used to store a temporary size of the buffer*/
                int bufferLength = 0;

             /*now, read through the input buffer and write the contents to the file */
                while ((bufferLength = inputStream.read(buffer)) > 0) {
                 /*add the data in the buffer to the file in the file output stream (the file on the sd card ) */
                    fileOutput.write(buffer, 0, bufferLength);

            	/* add up the size so we know how much is downloaded */
                    downloadedSize += bufferLength;

            	 /*Update display */
                 /*Update contract display */
                    if (progressBar != null) {
                        int Progress = (downloadedSize * 100) / downloadtotalSize;
                        publishProgress("" + (int) Progress);
                    }
                }

                Log.d("Version", "Download bp 9");
             /*close the output stream when done */
                fileOutput.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d("Version", "Download error: " + e.toString());

    		/* Exit application */
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Version", "Download error: " + e.toString());

    		/* Exit application */
                System.exit(0);
            }
            return null;
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            progressBar.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task
         * Dismiss the progress dialog
         * *
         */
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            progressBar.dismiss();
            pm.setValue(pm.VERSION, newVersion);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/download/" + appName)), "application/vnd.android.package-archive");
            act.startActivity(intent);

    		/* Exit application */
            /*setResult(Global.RESULT_CLOSE_ALL);*/
            /*finish();*/

        	 /*Exit application */
            System.exit(0);
        }

    }

}