package mibh.mis.facount;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import mibh.mis.facount.Data.AccessData;
import mibh.mis.facount.Data.AssetType;
import mibh.mis.facount.Data.Company;
import mibh.mis.facount.Data.PreferencesManager;
import mibh.mis.facount.Manager.RealmManager;
import mibh.mis.facount.Realm.TbServerUpdate;
import mibh.mis.facount.Service.CallService;
import mibh.mis.facount.Utils.Utils;

public class LoginActivity extends AppCompatActivity {

    private UserLoginTask mAuthTask = null;

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private View focusView = null;
    private LinearLayout registerBtn;
    private RelativeLayout layoutLogin;
    private EditText emp_id, id_card, fname, lname, tel;
    private EditText ID, PASSWORD;
    private TextView version;
    private Button btnSearchEmp, btnLogin;
    private FrameLayout loginForm;
    private ProgressBar loginProgress;
    private AlertDialog dialogSelect;
    private String TYPE = "", TYPENAME = "", COMPANY = "", COMPANYNAME = "";
    private ArrayList<AssetType> assetTypes = new ArrayList<>();
    private ArrayList<Company> companies = new ArrayList<>();
    private Dialog dialogRegister;
    private PreferencesManager pm;
    private SweetAlertDialog pDialog;
    boolean doubleBackToExitPressedOnce = false;
    private Button btnSelectCompanies, btnSelectType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pm = PreferencesManager.getInstance();

        if (isPackageInstalled()) dialogFyi();

        //new Version(LoginActivity.this);

        layoutLogin = (RelativeLayout) findViewById(R.id.layoutLogin);
        loginForm = (FrameLayout) findViewById(R.id.loginForm);
        loginProgress = (ProgressBar) findViewById(R.id.loginProgress);
        registerBtn = (LinearLayout) findViewById(R.id.btnRegister);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        ID = (EditText) findViewById(R.id.empid_lgn);
        PASSWORD = (EditText) findViewById(R.id.idcard_lgn);
        version = (TextView) findViewById(R.id.txtVersion);
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);

        ID.setText(pm.getValue(pm.EMP_ID));
        PASSWORD.setText(pm.getValue(pm.ID_CARD));
        version.setText("V-" + Utils.getInstance().getVersionName());

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogRegister();
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateLogin();
            }
        });
        layoutLogin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                return true;
            }
        });

    }

    private void setDialogCompany() {
        ArrayList<String> arrCompany = new ArrayList<>();
        arrCompany.clear();
        ArrayList<Company> companies = AccessData.getCompanies();
        this.companies.addAll(companies);
        for (Company company : companies) {
            arrCompany.add(company.getNAME_1());
        }
        dialogList("เลือกบริษัท", arrCompany, "COMPANY");
    }

    private void setDialogType() {
        ArrayList<String> arrType = new ArrayList<>();
        arrType.clear();
        ArrayList<AssetType> assetTypes = AccessData.getAssetTypes();
        this.assetTypes.addAll(assetTypes);
        for (AssetType assetType : assetTypes) {
            arrType.add(assetType.getASSET_NAME());
        }
        dialogList("เลือกประเภท", arrType, "TYPE");
    }

    private void dialogRegister() {
        dialogRegister = new Dialog(LoginActivity.this);
        dialogRegister.requestWindowFeature(dialogRegister.getWindow().FEATURE_NO_TITLE);
        dialogRegister.setContentView(R.layout.dialog_register);
        dialogRegister.setCancelable(true);
        emp_id = (EditText) dialogRegister.findViewById(R.id.emp_id);
        id_card = (EditText) dialogRegister.findViewById(R.id.id_card);
        fname = (EditText) dialogRegister.findViewById(R.id.fname);
        lname = (EditText) dialogRegister.findViewById(R.id.lname);
        tel = (EditText) dialogRegister.findViewById(R.id.tel);
        Button btnRegister = (Button) dialogRegister.findViewById(R.id.btnRegister);
        btnSearchEmp = (Button) dialogRegister.findViewById(R.id.btnSearchEmp);
        btnSearchEmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SearchEmp(emp_id.getText().toString()).execute();
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new UserRegister(emp_id.getText().toString(), id_card.getText().toString(), fname.getText().toString(), lname.getText().toString(), tel.getText().toString()).execute();
                validateRegister();
            }
        });
        dialogRegister.show();
    }

    private void dialogList(String title, final ArrayList<String> arrType, final String type) {

        AlertDialog.Builder dialogList = new AlertDialog.Builder(this);
        dialogList.setTitle(title);
        View dialoglist_view = this.getLayoutInflater().inflate(R.layout.dialog_list2, null);
        final ListView listView = (ListView) dialoglist_view.findViewById(R.id.dialoglist_ls);
        final EditText filterTxt = (EditText) dialoglist_view.findViewById(R.id.filterText);
        final DialogAdapter adapter = new DialogAdapter(this, R.layout.dialog_list_item, arrType);
        filterTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adapter.getFilter().filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (type.equalsIgnoreCase("COMPANY")) {
                    //selectCompanyBtn.setText(arrType.get(i));
                    setValueCompany(arrType.get(i));
                    btnSelectCompanies.setText(COMPANYNAME);
                    dialogSelect.dismiss();
                    //setDialogType();
                } else if (type.equalsIgnoreCase("TYPE")) {
                    //selectTypeBtn.setText(arrType.get(i));
                    //TYPE = arrType.get(i);
                    setValueType(arrType.get(i));
                    btnSelectType.setText(TYPENAME);
                    dialogSelect.dismiss();
                }
            }
        });
        dialogList.setView(dialoglist_view);
        dialogList.setNegativeButton("ยกเลิก",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dialogSelect = dialogList.create();
        dialogSelect.show();
    }

    private class DialogAdapter extends ArrayAdapter<String> {

        private ArrayList<String> originalList;
        private ArrayList<String> countryList;
        private CountryFilter filter;

        public DialogAdapter(Context context, int textViewResourceId, ArrayList<String> countryList) {
            super(context, textViewResourceId, countryList);
            this.countryList = new ArrayList<>();
            this.countryList.addAll(countryList);
            this.originalList = new ArrayList<>();
            this.originalList.addAll(countryList);
        }

        @Override
        public Filter getFilter() {
            if (filter == null) {
                filter = new CountryFilter();
            }
            return filter;
        }


        private class ViewHolder {
            TextView name;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            if (convertView == null) {

                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.dialog_list_item, null);

                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.dialoglist_txt);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String country = countryList.get(position);
            holder.name.setText(country);
            return convertView;

        }

        private class CountryFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                constraint = constraint.toString().toLowerCase();
                FilterResults result = new FilterResults();
                if (constraint != null && constraint.toString().length() > 0) {
                    ArrayList<String> filteredItems = new ArrayList<>();

                    for (int i = 0, l = originalList.size(); i < l; i++) {
                        String country = originalList.get(i);
                        if (country.toLowerCase().contains(constraint))
                            filteredItems.add(country);
                    }
                    result.count = filteredItems.size();
                    result.values = filteredItems;
                } else {
                    synchronized (this) {
                        result.values = originalList;
                        result.count = originalList.size();
                    }
                }
                return result;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {

                countryList = (ArrayList<String>) results.values;
                notifyDataSetChanged();
                clear();
                for (int i = 0, l = countryList.size(); i < l; i++)
                    add(countryList.get(i));
                notifyDataSetInvalidated();
            }
        }

    }

    private void validateLogin() {
        ID.setError(null);
        PASSWORD.setError(null);

        String emp = ID.getText().toString().trim();
        String idcard = PASSWORD.getText().toString().trim();

        boolean cancel = false;

        if (TextUtils.isEmpty(emp)) {
            ID.setError("กรุณาระบุรหัสพนักงาน");
            focusView = ID;
            cancel = true;
        } else if (TextUtils.isEmpty(idcard)) {
            PASSWORD.setError("กรุณาระบุรหัสบัตรประชาชน");
            focusView = PASSWORD;
            cancel = true;
        } else if (idcard.length() != 13) {
            PASSWORD.setError("กรุณาระบุรหัสบัตรประชาชนให้ถูกต้อง");
            focusView = PASSWORD;
            cancel = true;
        } /*else if (COMPANY.trim().equalsIgnoreCase("")) {
            selectCompanyBtn.setError("กรุณาเลือกบริษัท");
            focusView = selectCompanyBtn;
            cancel = true;
        } else if (TYPE.trim().equalsIgnoreCase("")) {
            selectTypeBtn.setError("กรุณาเลือกชนิด");
            focusView = selectTypeBtn;
            cancel = true;
        }*/

        if (cancel) {
            focusView.requestFocus();
        } else {
            new UserLoginTask(emp, idcard).execute();
            /*Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);*/
        }
    }

    private void validateRegister() {

        emp_id.setError(null);
        id_card.setError(null);
        fname.setError(null);
        lname.setError(null);
        tel.setError(null);

        String Empid = emp_id.getText().toString().trim();
        String Idcard = id_card.getText().toString().trim();
        String Fname = fname.getText().toString().trim();
        String Lname = lname.getText().toString().trim();
        String Tel = tel.getText().toString().trim();

        boolean cancel = false;

        if (TextUtils.isEmpty(Empid)) {
            emp_id.setError("กรุณาระบุรหัสพนักงาน");
            focusView = emp_id;
            cancel = true;
        } else if (TextUtils.isEmpty(Idcard)) {
            id_card.setError("กรุณาระบุรหัสประจำตัวประชาชน");
            focusView = id_card;
            cancel = true;
        } else if (Idcard.length() != 13) {
            id_card.setError("กรุณาระบุเลขบัตรประชาชนให้ถูกต้อง");
            focusView = id_card;
            cancel = true;
        } else if (TextUtils.isEmpty(Fname)) {
            fname.setError("กรุณาระบุชื่อพนักงาน");
            focusView = fname;
            cancel = true;
        } else if (TextUtils.isEmpty(Lname)) {
            lname.setError("กรุณาระบุนามกุล");
            focusView = lname;
            cancel = true;
        } /*else if (TextUtils.isEmpty(Tel)) {
            tel.setError("กรุณาระบุเบอร์โทรศัพท์");
            focusView = tel;
            cancel = true;
        } else if (Tel.length() != 10) {
            tel.setError("กรุณาระบุเบอร์โทรศัพท์ให้ถูกต้อง");
            focusView = tel;
            cancel = true;
        }*/

        if (cancel) {
            focusView.requestFocus();
        } else {
            new UserRegister(Empid, Idcard, Fname, Lname, Tel).execute();
        }
    }

    private class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmp;
        private final String mPassword;

        UserLoginTask(String emp, String password) {
            mEmp = emp;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String result = new CallService().checkLogin(mPassword, mEmp);
            if (!result.equalsIgnoreCase("error") && !result.equalsIgnoreCase("False")) {
                setLoginData(result);
                AccessData.setCompanies(new CallService().getCompanyList(mEmp));
                AccessData.setAssetTypes(new CallService().getAssetType(mEmp));
                List<TbServerUpdate> tbServerUpdate = RealmManager.getInstance().getLastDateTbServerUpdate();
                String jsonHashtag = new CallService().getHashtag(tbServerUpdate.size() <= 0 ? "000000000000" : tbServerUpdate.get(0).getServerDate());
                Log.d("test", "doInBackground: " + jsonHashtag);
                RealmManager.getInstance().manageHashtagValue(jsonHashtag);
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            mAuthTask = null;

            if (success) {
                //setDialogCompany();
                final Dialog dialog = new Dialog(LoginActivity.this);
                dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_compay_type);
                dialog.setCancelable(true);
                btnSelectCompanies = (Button) dialog.findViewById(R.id.btnSelectCompanies);
                btnSelectType = (Button) dialog.findViewById(R.id.btnSelectType);
                Button btnConfirmSelectDialog = (Button) dialog.findViewById(R.id.btnConfirmDialogSelect);
                btnSelectCompanies.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setDialogCompany();
                    }
                });
                btnSelectType.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setDialogType();
                    }
                });
                btnConfirmSelectDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        new GetAssetList().execute();
                    }
                });
                dialog.show();
            } else {
                showDialog("เข้าสู่ระบบผิดพลาด กรุณาลองไหม่");
            }
            pDialog.dismiss();
            //loginForm.setVisibility(View.VISIBLE);
            //loginProgress.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Loading");
            pDialog.setCancelable(false);
            pDialog.show();
            //loginForm.setVisibility(View.INVISIBLE);
            //loginProgress.setVisibility(View.VISIBLE);
        }
    }

    private class UserRegister extends AsyncTask<Void, Void, Boolean> {

        String EMP_ID, ID_CARD, FName, LName, TEL;

        public UserRegister(String emp_id, String id_card, String fname, String lname, String tel) {
            this.EMP_ID = emp_id;
            this.ID_CARD = id_card;
            this.FName = fname;
            this.LName = lname;
            this.TEL = tel;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                dialogRegister.dismiss();
            } else {
                dialogRegister.dismiss();
                showDialog("ไม่สามารถสมัครได้ กรุณาลองไหม่");
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            String result = new CallService().saveRegister(ID_CARD, EMP_ID, FName, LName, TEL);
            if (!result.equalsIgnoreCase("error") && !result.equalsIgnoreCase("false")) {
                return true;
            } else {
                return false;
            }
        }
    }

    private class SearchEmp extends AsyncTask<String, Void, String> {

        String EMPID = "";

        public SearchEmp(String EMPID) {
            this.EMPID = EMPID;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btnSearchEmp.setVisibility(View.INVISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            return new CallService().getHrmsData(EMPID);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            setRegisterData(s);
            btnSearchEmp.setVisibility(View.VISIBLE);
        }
    }

    private class GetCompany extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            //AccessData.setCompanies(new CallService().getCompanyList());
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }
    }

    private class GetAssetType extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            //AccessData.setAssetTypes(new CallService().getAssetType());
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            pDialog.dismiss();
            setDialogType();
        }
    }

    private class GetAssetList extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            String strAssetList = new CallService().getAssetList(TYPE, COMPANY);
            AccessData.setAssets(strAssetList);
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            pDialog.dismiss();
            startCameraActivity();
        }
    }

    private void setRegisterData(String result) {
        try {
            JSONArray Arr = new JSONArray(result);
            JSONObject c = Arr.getJSONObject(0);
            emp_id.setText(c.getString("ID_Emp").equals("null") ? "" : c.getString("ID_Emp"));
            id_card.setText(c.getString("IdentityID").equals("null") ? "" : c.getString("IdentityID"));
            fname.setText(c.getString("FNameT").equals("null") ? "" : c.getString("FNameT"));
            lname.setText(c.getString("LNameT").equals("null") ? "" : c.getString("LNameT"));
            tel.setText(c.getString("Mobile").equals("null") ? "" : c.getString("Mobile"));
            //}
        } catch (Exception e) {
            Log.d("error convertresult", e.toString());
            emp_id.setText("");
            id_card.setText("");
            fname.setText("");
            lname.setText("");
            tel.setText("");
        }

    }

    private void setLoginData(String result) {
        try {
            JSONArray Arr = new JSONArray(result);
            JSONObject c = Arr.getJSONObject(0);
            pm.setValue(pm.ID_CARD, c.getString("ID_CARD").equals("null") ? "" : c.getString("ID_CARD"));
            pm.setValue(pm.EMP_ID, c.getString("EMP_ID").equals("null") ? "" : c.getString("EMP_ID"));
            pm.setValue(pm.FNAME, c.getString("FNAME").equals("null") ? "" : c.getString("FNAME"));
            pm.setValue(pm.LNAME, c.getString("LNAME").equals("null") ? "" : c.getString("LNAME"));
            pm.setValue(pm.TEL, c.getString("TEL").equals("null") ? "" : c.getString("TEL"));
            //}
        } catch (Exception e) {
            Log.d("error convertresult", e.toString());
        }

    }

    private void setValueCompany(String comName) {
        for (Company company : companies) {
            if (company.getNAME_1().equalsIgnoreCase(comName)) {
                COMPANY = company.getCOMPANY_ID();
                COMPANYNAME = company.getNAME_1();
                //Toast.makeText(LoginActivity.this, COMPANY + " " + company.getCOMPANY_ID() + " " + comName, Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void setValueType(String typeName) {
        for (AssetType assetType : assetTypes) {
            if (assetType.getASSET_NAME().equals(typeName)) {
                TYPE = assetType.getASSET_TYPE();
                TYPENAME = assetType.getASSET_NAME();
                //Toast.makeText(LoginActivity.this, COMPANY + " " + assetType.getASSET_TYPE() + " " + typeName, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDialog(String message) {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("ขออภัย")
                .setContentText(message)
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                    }
                })
                .show();
    }

    private void startCameraActivity() {
        Intent intent = new Intent(LoginActivity.this, CameraMain.class);
        intent.putExtra("TYPE", TYPE);
        intent.putExtra("COMPANY", COMPANY);
        intent.putExtra("COMPANYNAME", COMPANYNAME);
        intent.putExtra("TYPENAME", TYPENAME);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void dialogFyi() {
        final Dialog dialog = new Dialog(LoginActivity.this);
        dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_fyi);
        dialog.setCancelable(false);
        Button btnDialogFyi = (Button) dialog.findViewById(R.id.btnDialogFyi);
        btnDialogFyi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                uninstallOldApp();
            }
        });
        dialog.show();
    }

    private boolean isPackageInstalled() {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo("mibh.mis.assetcap", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void uninstallOldApp() {
        Uri packageURI = Uri.parse("package:" + "mibh.mis.assetcap");
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        startActivity(uninstallIntent);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        View parentLayout = findViewById(R.id.layoutLogin);
        Snackbar.make(parentLayout, "กด Back อีกครั้งเพื่อออก", Snackbar.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 3000);

    }

}

