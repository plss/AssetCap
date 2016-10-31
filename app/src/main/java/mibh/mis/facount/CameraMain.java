package mibh.mis.facount;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import mibh.mis.facount.Data.AccessData;
import mibh.mis.facount.Data.Asset;
import mibh.mis.facount.Data.AssetType;
import mibh.mis.facount.Data.Company;
import mibh.mis.facount.Data.PreferencesManager;
import mibh.mis.facount.Manager.RealmManager;
import mibh.mis.facount.Realm.HashtagData;
import mibh.mis.facount.Service.CallService;
import mibh.mis.facount.Utils.Utils;

/**
 * Created by ponlakiss on 03/17/2016.
 */
public class CameraMain extends AppCompatActivity {
    // Native camera.
    private Camera mCamera;

    // View to display the camera output.
    private CameraPreview mPreview;

    private int currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK,
            frontCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT,
            backCameraId = Camera.CameraInfo.CAMERA_FACING_BACK,
            rotateAngle,
            countCapture;

    private String flashOn = Camera.Parameters.FLASH_MODE_ON,
            flashOff = Camera.Parameters.FLASH_MODE_OFF,
            currentFlash = flashOff;

    private static final int FOCUS_AREA_SIZE = 300;

    private OrientationEventListener cOrientationEventListener;

    private Boolean cameraState = true;

    private Bitmap bitmapHolder, bitmap;

    private AlertDialog dialogList2;
    private AutoCompleteTextView input;
    private TextView tvCamMode;
    private Button btnSelectCompanies, btnSelectType;

    private ArrayList<AssetType> assetTypes = new ArrayList<>();
    private ArrayList<Company> companies = new ArrayList<>();

    PreferencesManager pm;

    String TYPE = "", TYPENAME = "", COMPANY = "", COMPANYNAME = "", ASSETKEY = "", ASSETTEXT = "", COMMENT = "", FILENAME = "", OWNER = "", LOCATIONNAME = "", DATEBUY = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_main2);

        pm = PreferencesManager.getInstance();

        Bundle extras = getIntent().getExtras();

        if (extras.containsKey("TYPE")) {
            TYPE = extras.getString("TYPE");
        }
        if (extras.containsKey("COMPANY")) {
            COMPANY = extras.getString("COMPANY");
        }
        if (extras.containsKey("COMPANYNAME")) {
            COMPANYNAME = extras.getString("COMPANYNAME");
        }
        if (extras.containsKey("TYPENAME")) {
            TYPENAME = extras.getString("TYPENAME");
        }

        boolean opened = safeCameraOpenInView();

        if (!opened) {
            Log.d("CameraGuide", "Error, Camera failed to open");
            //return view;
        }

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.camera_previewCT);
        ImageView btnTakePicture = (ImageView) findViewById(R.id.btnTakePictureCT);
        ImageView btnComment = (ImageView) findViewById(R.id.btnCommentCT);
        ImageView imgGallery = (ImageView) findViewById(R.id.imgGallery);
        ImageView imgSelectAsset = (ImageView) findViewById(R.id.imgSelectAsset);
        ImageView imgSelectSetting = (ImageView) findViewById(R.id.imageSelectSetting);

        final ImageView btnFlash = (ImageView) findViewById(R.id.btnFlashCT);
        ImageView btnSwitchCamera = (ImageView) findViewById(R.id.btnSwitchCameraCT);
        tvCamMode = (TextView) findViewById(R.id.tvCamMode);

        btnTakePicture.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (pm.getValueZ(pm.LATITUDE).equalsIgnoreCase("0") || pm.getValueZ(pm.LONGTITUDE).equalsIgnoreCase("0")) {
                            Toast.makeText(CameraMain.this, "กรณารอสัญญาน GPS สักครู่", Toast.LENGTH_SHORT).show();
                        } else if (ASSETKEY.equalsIgnoreCase("") || ASSETTEXT.equalsIgnoreCase("")) {
                            //Toast.makeText(CameraMain.this, "กรณาเลือกชนิด ASSET ก่อน", Toast.LENGTH_SHORT).show();
                            setDialogAsset();
                        } else {
                            if (cameraState) {
                                mCamera.takePicture(null, null, mPicture);
                                cameraState = false;
                                countCapture = 0;
                            }
                        }


                    }
                }
        );
        btnSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentCameraId = (currentCameraId == backCameraId ? frontCameraId : backCameraId);
                safeCameraOpenInView();
            }
        });
        btnFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentFlash.equalsIgnoreCase(flashOff) && hasFlash()) {
                    currentFlash = flashOn;
                    btnFlash.setBackground(getResources().getDrawable(R.drawable.pnflash));
                } else {
                    currentFlash = flashOff;
                    btnFlash.setBackground(getResources().getDrawable(R.drawable.csflash));
                }

                Camera.Parameters pm = mPreview.mCamera.getParameters();
                pm.setFlashMode(currentFlash);
                mPreview.mCamera.setParameters(pm);
            }
        });
        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDialogComment();
            }
        });

        frameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mPreview.focusOnTouch(motionEvent);
                }
                return true;
            }
        });

        imgSelectAsset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDialogAsset();
            }
        });

        imgSelectSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(CameraMain.this);
                dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_compay_type);
                dialog.setCancelable(true);
                btnSelectCompanies = (Button) dialog.findViewById(R.id.btnSelectCompanies);
                btnSelectType = (Button) dialog.findViewById(R.id.btnSelectType);
                Button btnConfirmSelectDialog = (Button) dialog.findViewById(R.id.btnConfirmDialogSelect);
                btnSelectCompanies.setText(COMPANYNAME);
                btnSelectType.setText(TYPENAME);
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
            }
        });

        imgGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CameraMain.this, ImgList.class);
                intent.putExtra("TYPE", TYPE);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            countCapture++;
            if (countCapture == 1) {
                bitmapHolder = BitmapFactory.decodeByteArray(data, 0, data.length);
                bitmapHolder = RotateBitmap(bitmapHolder, rotateAngle, (float) 1);
                currentCameraId = frontCameraId;
                safeCameraOpenInView();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCamera.takePicture(null, null, mPicture);
                    }
                }, 2000);
            } else {
                cameraState = true;
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                bitmap = RotateBitmap(bitmap, rotateAngle, (float) 1);
                bitmap = DrawBitmapborder(bitmapHolder);
                bitmapHolder.recycle();
                bitmapHolder = null;
                System.gc();
                final Dialog dialog = new Dialog(CameraMain.this);
                dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.camera_preview);
                dialog.setCancelable(false);
                final ImageView previewImg = (ImageView) dialog.findViewById(R.id.previewPic);
                Button yes = (Button) dialog.findViewById(R.id.yes);
                Button no = (Button) dialog.findViewById(R.id.no);
                previewImg.setImageBitmap(bitmap);
                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        FILENAME = RealmManager.getInstance().genImgName(TYPE, ASSETKEY);

                        String root = Environment.getExternalStorageDirectory().toString();
                        File myDir = new File(root + "/ASSET");
                        myDir.mkdirs();
                        File photo = new File(myDir, FILENAME);
                        if (photo.exists()) {
                            photo.delete();
                        }
                        try {
                            FileOutputStream fos = new FileOutputStream(photo);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 35, fos);
                            fos.flush();
                            fos.close();

                            ExifInterface exif = new ExifInterface(photo.getAbsolutePath());
                            String dateTaken = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                            double latitude = Double.parseDouble(String.format("%.5f", Double.parseDouble(pm.getValueZ(pm.LATITUDE))));
                            double longitude = Double.parseDouble(String.format("%.5f", Double.parseDouble(pm.getValueZ(pm.LONGTITUDE))));
                            int num1Lat = (int) Math.floor(latitude);
                            int num2Lat = (int) Math.floor((latitude - num1Lat) * 60);
                            double num3Lat = (latitude - ((double) num1Lat + ((double) num2Lat / 60))) * 3600000;

                            int num1Lon = (int) Math.floor(longitude);
                            int num2Lon = (int) Math.floor((longitude - num1Lon) * 60);
                            double num3Lon = (longitude - ((double) num1Lon + ((double) num2Lon / 60))) * 3600000;

                            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, num1Lat + "/1," + num2Lat + "/1," + num3Lat + "/1000");
                            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, num1Lon + "/1," + num2Lon + "/1," + num3Lon + "/1000");

                            if (latitude > 0) {
                                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
                            } else {
                                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
                            }

                            if (longitude > 0) {
                                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
                            } else {
                                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
                            }

                            exif.setAttribute(ExifInterface.TAG_DATETIME, dateTaken);
                            exif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, dateTaken);

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(System.currentTimeMillis());
                            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                            int minutes = calendar.get(Calendar.MINUTE);
                            int seconds = calendar.get(Calendar.SECOND);
                            String exifGPSTimestamp = hourOfDay + "/1," + minutes + "/1," + seconds + "/1";
                            exif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, exifGPSTimestamp);
                            exif.saveAttributes();

                        } catch (java.io.IOException e) {
                            Log.e("ErrorSavePic", "Exception in photoCallback", e);
                        }


                        if (pm.getValue(pm.LOCATIONNAME).length() > 200) {
                            LOCATIONNAME = pm.getValue(pm.LOCATIONNAME).substring(0, 198);
                        } else LOCATIONNAME = pm.getValue(pm.LOCATIONNAME);
                        if (COMMENT.length() >= 296) {
                            COMMENT = COMMENT.substring(0, 295) + "..";
                        }

                        RealmManager.getInstance().insertImage(ASSETKEY, ASSETTEXT, String.format("%.5f", Double.parseDouble(pm.getValueZ(PreferencesManager.LATITUDE))), String.format("%.5f", Double.parseDouble(pm.getValueZ(PreferencesManager.LONGTITUDE))), LOCATIONNAME, FILENAME, pm.getValue(PreferencesManager.EMP_ID), TYPE, TYPENAME, COMMENT, COMPANY);
                        //img_db.SaveImg(ASSETKEY, ASSETTEXT, String.format("%.5f", Double.parseDouble(pm.getValueZ(pm.LATITUDE))), String.format("%.5f", Double.parseDouble(pm.getValueZ(pm.LONGTITUDE))), LOCATIONNAME, FILENAME, pm.getValue(pm.EMP_ID), TYPE, TYPENAME, COMMENT, COMPANY);

                        new Loading().execute();
                        currentCameraId = backCameraId;
                        countCapture = 0;
                        bitmap.recycle();
                        dialog.dismiss();
                        System.gc();
                        safeCameraOpenInView();
                    }
                });
                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        currentCameraId = backCameraId;
                        countCapture = 0;
                        bitmap.recycle();
                        dialog.dismiss();
                        System.gc();
                        safeCameraOpenInView();
                    }
                });
                dialog.show();
            }
        }
    };

    class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.AutoFocusCallback {

        // SurfaceHolder
        private SurfaceHolder mHolder;

        // Our Camera.
        private Camera mCamera;

        // Parent Context.
        private Context mContext;

        // Camera Sizing (For rotation, orientation changes)
        private Camera.Size mPreviewSize;

        // List of supported preview sizes
        private List<Camera.Size> mSupportedPreviewSizes;

        // Flash modes supported by this camera
        private List<String> mSupportedFlashModes;

        // View holding this camera.
        //private View mCameraView;

        public CameraPreview(Context context, Camera camera) {
            super(context);

            // Capture the context
            //mCameraView = cameraView;
            mContext = context;
            setCamera(camera);

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setKeepScreenOn(true);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        /**
         * Begin the preview of the camera input.
         */
        public void startCameraPreview() {
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Extract supported preview and flash modes from the camera.
         *
         * @param camera
         */
        private void setCamera(Camera camera) {
            // Source: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
            mCamera = camera;
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            mSupportedFlashModes = mCamera.getParameters().getSupportedFlashModes();

            // Set the camera to Auto Flash mode.
            /*if (mSupportedFlashModes != null && mSupportedFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                mCamera.setParameters(parameters);
            }*/

            requestLayout();
        }

        /**
         * The Surface has been created, now tell the camera where to draw the preview.
         *
         * @param holder
         */
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Dispose of the camera preview.
         *
         * @param holder
         */
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mCamera != null) {
                mCamera.stopPreview();
            }
        }

        /**
         * React to surface changed events
         *
         * @param holder
         * @param format
         * @param w
         * @param h
         */
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            //Log.e("surfaceChanged", "surfaceChanged => w=" + w + ", h=" + h);
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null) {
                // preview surface does not exist
                return;
            }
            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
            }
            // set preview size and make any resize, rotate or reformatting changes here
            // start preview with new settings
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                Log.d("PreviewSize", mPreviewSize.width + " " + mPreviewSize.height);
                parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
                parameters.setFlashMode(currentCameraId == frontCameraId ? flashOff : currentFlash);
                List<Camera.Size> pictureSize = parameters.getSupportedPictureSizes();
                int maxWidth = 0, maxIndex = 0;
                for (int i = 0; i < pictureSize.size(); ++i) {
                    //Log.d("Size", pictureSize.get(i).width + " " + pictureSize.get(i).height + " " + (Math.round((pictureSize.get(i).width * 1.0 / pictureSize.get(i).height) * 10000)));
                    if ((Math.round((pictureSize.get(i).width * 1.0 / pictureSize.get(i).height) * 10000) == 13333)
                            && pictureSize.get(i).width < 2000
                            && pictureSize.get(i).width > maxWidth) {
                        maxWidth = pictureSize.get(i).width;
                        maxIndex = i;
                    }
                }
                parameters.setPictureSize(pictureSize.get(maxIndex).width, pictureSize.get(maxIndex).height);
                mCamera.setParameters(parameters);
                mCamera.setDisplayOrientation(90);
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e) {
                Log.d("surfaceChanged", "Error starting camera preview: " + e.getMessage());
            }
        }

        /**
         * Calculate the measurements of the layout
         *
         * @param widthMeasureSpec
         * @param heightMeasureSpec
         */
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = resolveSize((int) this.getSuggestedMinimumWidth(), (int) widthMeasureSpec);
            int height = resolveSize((int) this.getSuggestedMinimumHeight(), (int) heightMeasureSpec);
            this.setMeasuredDimension(width, height);
            if (this.mSupportedPreviewSizes != null) {
                this.mPreviewSize = this.getOptimalPreviewSize(this.mSupportedPreviewSizes, width, height);
            }
            float ratio = this.mPreviewSize.height >= this.mPreviewSize.width ? (float) this.mPreviewSize.height / (float) this.mPreviewSize.width : (float) this.mPreviewSize.width / (float) this.mPreviewSize.height;
            if (currentCameraId == backCameraId) {
                this.setMeasuredDimension(width, (int) ((float) width * ratio));
            } else if ((int) ((float) height / ratio) < width) {
                int plusWidth = width - (int) ((float) height / ratio);
                this.setMeasuredDimension(width, height + (int) ((float) plusWidth * ratio));
            } else {
                this.setMeasuredDimension((int) ((float) height / ratio), height);
            }

        }

        /**
         * Update the layout based on rotation and orientation changes.
         *
         * @param changed
         * @param left
         * @param top
         * @param right
         * @param bottom
         */
        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            // Source: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
            if (changed) {
                final int width = right - left;
                final int height = bottom - top;

                int previewWidth = width;
                int previewHeight = height;

                if (mPreviewSize != null) {
                    Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

                    switch (display.getRotation()) {
                        case Surface.ROTATION_0:
                            previewWidth = mPreviewSize.height;
                            previewHeight = mPreviewSize.width;
                            mCamera.setDisplayOrientation(90);
                            break;
                        case Surface.ROTATION_90:
                            previewWidth = mPreviewSize.width;
                            previewHeight = mPreviewSize.height;
                            break;
                        case Surface.ROTATION_180:
                            previewWidth = mPreviewSize.height;
                            previewHeight = mPreviewSize.width;
                            break;
                        case Surface.ROTATION_270:
                            previewWidth = mPreviewSize.width;
                            previewHeight = mPreviewSize.height;
                            mCamera.setDisplayOrientation(180);
                            break;
                    }
                }

                final int scaledChildHeight = previewHeight * width / previewWidth;
                //mCameraView.layout(0, height - scaledChildHeight, width, height);
            }
        }

        /**
         * @param sizes
         * @param w
         * @param h
         * @return
         */
        private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
            final double ASPECT_TOLERANCE = 0.1;
            double targetRatio = (double) h / w;

            if (sizes == null)
                return null;

            Camera.Size optimalSize = null;
            double minDiff = Double.MAX_VALUE;

            int targetHeight = h;

            for (Camera.Size size : sizes) {
                double ratio = (double) size.height / size.width;
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                    continue;

                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }

            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE;
                for (Camera.Size size : sizes) {
                    if (Math.abs(size.height - targetHeight) < minDiff) {
                        optimalSize = size;
                        minDiff = Math.abs(size.height - targetHeight);
                    }
                }
            }

            return optimalSize;
        }

        @Override
        public void onAutoFocus(boolean b, Camera camera) {

        }

        private void focusOnTouch(MotionEvent event) {
            try {
                if (this.mCamera != null) {
                    Camera.Parameters parameters = this.mCamera.getParameters();
                    if (parameters.getMaxNumMeteringAreas() > 0) {

                        Rect rect = calculateFocusArea(event.getX(), event.getY());

                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                        List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                        meteringAreas.add(new Camera.Area(rect, 800));
                        parameters.setFocusAreas(meteringAreas);

                        this.mCamera.setParameters(parameters);
                        this.mCamera.autoFocus(this);
                    } else {
                        this.mCamera.autoFocus(this);
                    }
                }
            } catch (Exception e) {
                Log.d("Error Touch Focus", e.toString());
            }

        }

        private Rect calculateFocusArea(float x, float y) {
            int left = clamp(Float.valueOf((x / mPreview.getWidth()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
            int top = clamp(Float.valueOf((y / mPreview.getHeight()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);

            return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
        }

        private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
            int result;
            if (Math.abs(touchCoordinateInCameraReper) + focusAreaSize / 2 > 1000) {
                if (touchCoordinateInCameraReper > 0) {
                    result = 1000 - focusAreaSize / 2;
                } else {
                    result = -1000 + focusAreaSize / 2;
                }
            } else {
                result = touchCoordinateInCameraReper - focusAreaSize / 2;
            }
            return result;
        }
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle, float rescale) {
        int width = source.getWidth();
        int height = source.getHeight();
        float scaleWidth = (float) .4;
        float scaleHeight = (float) .4;
        scaleWidth = rescale;
        scaleHeight = rescale;
        System.gc();
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        if (angle != 0) {
            matrix.postRotate(angle);
        }
        return Bitmap.createBitmap(source, 0, 0, width, height, matrix, false);
    }

    private boolean safeCameraOpenInView() {
        boolean qOpened = false;
        releaseCameraAndPreview();
        mCamera = getCameraInstance(currentCameraId);
        //mCameraView = view;
        qOpened = (mCamera != null);
        if (qOpened) {
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_previewCT);
            preview.setBackgroundColor(Color.BLACK);
            preview.addView(mPreview);
            mPreview.startCameraPreview();
        }
        return qOpened;
    }

    private void releaseCameraAndPreview() {

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (mPreview != null) {
            mPreview.mCamera = null;
            mPreview.surfaceDestroyed(mPreview.getHolder());
            mPreview.getHolder().removeCallback(mPreview);
            mPreview.destroyDrawingCache();
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_previewCT);
            preview.removeView(mPreview);
            mPreview.mCamera = null;
            mPreview = null;
        }
    }

    public Camera getCameraInstance(int param) {
        Camera c = null;
        try {
            c = Camera.open(param); // attempt to get a Camera instance
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    public boolean hasFlash() {
        if (mCamera == null) {
            return false;
        }

        Camera.Parameters parameters = mCamera.getParameters();

        if (parameters.getFlashMode() == null) {
            return false;
        }

        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if (supportedFlashModes == null || supportedFlashModes.isEmpty() || supportedFlashModes.size() == 1 && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF)) {
            return false;
        }

        return true;
    }

    private void setDialogComment() {
        AlertDialog.Builder alert = new AlertDialog.Builder(CameraMain.this);
        alert.setTitle("ใส่ข้อความ");
        View view = CameraMain.this.getLayoutInflater().inflate(R.layout.dialog_comment, null);
        input = (AutoCompleteTextView) view.findViewById(R.id.inputComment);
        input.setThreshold(1);
        input.setLines(3);
        input.setPaddingRelative(16, 0, 16, 0);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String filter = s.toString().toLowerCase();
                ArrayList<String> listItems = new ArrayList<>();
                int countItems = 0;
                for (String listItem : pm.getValue(pm.COMMENT).split("&")) {
                    if (listItem.toLowerCase().contains(filter)) {
                        if (countItems >= 3) {
                            break;
                        }
                        listItems.add(listItem);
                        countItems++;
                    }

                }
                ArrayAdapter<String> adapt = new ArrayAdapter<>(CameraMain.this, android.R.layout.simple_list_item_1, listItems);
                input.setAdapter(adapt);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        alert.setView(view);
        alert.setPositiveButton("ตกลง", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                COMMENT = input.getText().toString();
                if (pm.getValue(pm.COMMENT).equals("")) {
                    pm.setValue(pm.COMMENT, COMMENT);
                } else {
                    String str = COMMENT;
                    List<String> items = Arrays.asList(pm.getValue(pm.COMMENT).split("&"));
                    if (!items.contains(COMMENT) && !COMMENT.equals("")) {
                        for (int x = 0, i = 0; x < items.size(); ++x, i++) {
                            if (i >= 20) {
                                break;
                            }
                            str += "&";
                            str += items.get(x);
                        }
                        pm.setValue(pm.COMMENT, str);
                    }
                }
            }
        });
        alert.setNeutralButton("# รูปแบบข้อความ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        final AlertDialog alert2 = alert.create();
        alert2.show();
        Button btn = alert2.getButton(DialogInterface.BUTTON_NEUTRAL);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDialogHashtag();
            }
        });
    }

    private void setDialogHashtag() {
        AlertDialog.Builder dialogList = new AlertDialog.Builder(this);
        dialogList.setTitle("เลือกข้อความ");
        View dialoglist_view = this.getLayoutInflater().inflate(R.layout.dialog_list2, null);
        final ListView listView = (ListView) dialoglist_view.findViewById(R.id.dialoglist_ls);
        final EditText filterTxt = (EditText) dialoglist_view.findViewById(R.id.filterText);
        final List<HashtagData> cursor = RealmManager.getInstance().getHasgtag();
        ArrayList<String> arrHashtag = new ArrayList<>();
        for (int i = 0; i < cursor.size(); i++) {
            arrHashtag.add(cursor.get(i).getListName());
        }
        final CommentAdapter adapter = new CommentAdapter(this, R.layout.dialog_list_item, arrHashtag);
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
        dialogList.setView(dialoglist_view);
        dialogList.setNegativeButton("ยกเลิก",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.getFilter().filter("");
                        dialog.dismiss();
                    }
                });
        final AlertDialog dialogList2 = dialogList.create();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(CameraMain.this, i + " " + assets.get(i).getAss_ref_name() + " " + " " + assets.get(i).getAss_ref_Id(), Toast.LENGTH_SHORT).show();
                adapter.getFilter().filter("");
                input.setText(cursor.get(i).getListName());
                COMMENT = cursor.get(i).getListName();
                dialogList2.dismiss();
            }
        });

        dialogList2.show();
    }

    private void setDialogAsset() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.clear();
        ArrayList<Asset> assets = AccessData.getAssets();
        for (Asset asset : assets) {
            arrayAdapter.add(asset.getAssetName());
        }
        dialogAsset("เลือกชนิด", assets);
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

    private void dialogAsset(String title, final ArrayList<Asset> assets) {
        final AlertDialog.Builder dialogList = new AlertDialog.Builder(this);
        dialogList.setTitle(title);
        View dialoglist_view = this.getLayoutInflater().inflate(R.layout.dialog_list2, null);
        final ListView listView = (ListView) dialoglist_view.findViewById(R.id.dialoglist_ls);
        final EditText filterTxt = (EditText) dialoglist_view.findViewById(R.id.filterText);
        final AssetAdapter adapter = new AssetAdapter(this, R.layout.dialog_list_item, assets);
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
                //Toast.makeText(CameraMain.this, i + " " + assets.get(i).getAss_ref_name() + " " + " " + assets.get(i).getAss_ref_Id(), Toast.LENGTH_SHORT).show();
                tvCamMode.setText(assets.get(i).getAssetId() + "\n" + assets.get(i).getAssetName());
                ASSETKEY = assets.get(i).getAssetId();
                ASSETTEXT = assets.get(i).getAssetName();
                OWNER = "OWNER: " + assets.get(i).getOwnerName();
                DATEBUY = assets.get(i).getDateBuy();
                adapter.getFilter().filter("");
                dialogList2.dismiss();
            }
        });
        dialogList.setView(dialoglist_view);
        dialogList.setNegativeButton("ยกเลิก",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.getFilter().filter("");
                        dialog.dismiss();
                    }
                });
        dialogList2 = dialogList.create();
        dialogList2.show();
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
        dialogList.setView(dialoglist_view);
        dialogList.setNegativeButton("ยกเลิก",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog dialogSelect = dialogList.create();
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
        //dialogSelect = dialogList.create();
        dialogSelect.show();
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

    private Bitmap DrawBitmapborder(Bitmap src) {

        System.gc();
        Bitmap workingBitmap = Bitmap.createBitmap(src);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);  // Bitmap.createBitmap(workingBitmap);

        Canvas canvas = new Canvas(mutableBitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        int rw = mutableBitmap.getWidth();
        int rh = mutableBitmap.getHeight();
        if (rw > rh) {
            rw = mutableBitmap.getHeight();
            rh = mutableBitmap.getWidth();
        }
        int w = mutableBitmap.getWidth();
        int h = mutableBitmap.getHeight();


        workingBitmap.recycle();

        Calendar c = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        String formattedDate = date.format(c.getTime());
        SimpleDateFormat time = new SimpleDateFormat("HH:mm", Locale.US);
        String formattedTime = time.format(c.getTime());

        int globalOffsetR = (int) (w * 0.965);
        int globalOffsetL = (int) (w * 0.035);

        Paint paintPic = new Paint(Paint.FILTER_BITMAP_FLAG);
        paintPic.setAlpha(200);
        Rect destinationRect = new Rect();
        destinationRect.set(0, 0, (int) (w * 0.2), (int) (h * 0.2));
        destinationRect.offsetTo((int) (w * 0.02), ((int) (h * 0.965) - (int) (h * 0.2)));
        canvas.drawBitmap(bitmap, null, destinationRect, paintPic);
        bitmap.recycle();

        int clr = Color.parseColor("#FFFFFF");

        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setColor(clr);
        paint.setTextSize((int) (rh * 0.04));
        paint.setShadowLayer((float) 3, 2, 2, Color.BLACK);
        canvas.drawText(formattedDate, globalOffsetR, (int) (rh * 0.06), paint);

        paint.setTextSize((int) (rh * 0.04));
        canvas.drawText(formattedTime, globalOffsetR, (int) (rh * 0.10), paint);

        paint.setTextSize((int) (rh * 0.025));
        canvas.drawText(ASSETKEY, globalOffsetR, (int) (rh * 0.14), paint);

        canvas.drawText(ASSETTEXT, globalOffsetR, (int) (rh * 0.18), paint);

        //canvas.drawText(COMPANYNAME, globalOffsetR, (int) (rh * 0.22), paint);

        int x = globalOffsetR, y = (int) (rh * 0.215);
        int Begin = 0;
        for (int i = 0; i <= COMPANYNAME.length(); ++i) {
            if (i % 20 == 0 || i == COMPANYNAME.length()) {
                canvas.drawText(COMPANYNAME.substring(Begin, i), x, y, paint);
                if (i != COMPANYNAME.length()) {
                    y += -paint.ascent() + paint.descent();
                }
                Begin = i;
            }
        }

        y += (int) (rh * 0.01);
        Begin = 0;
        for (int i = 0; i <= OWNER.length(); ++i) {
            if (i % 20 == 0 || i == OWNER.length()) {
                canvas.drawText(OWNER.substring(Begin, i), x, y, paint);
                if (i != OWNER.length()) {
                    y += -paint.ascent() + paint.descent();
                }
                Begin = i;
            }
        }

        y += (int) (rh * 0.01);
        if (COMMENT != null && !COMMENT.equalsIgnoreCase("")) {
            paint.setTextSize((int) (rh * 0.03));
            if (COMMENT.contains("\n")) {
                y += -paint.ascent() + paint.descent();
                for (String line : COMMENT.split("\n")) {

                    /*canvas.drawText(line, x, y, paint);
                    y += -paint.ascent() + paint.descent();*/
                    int Start = 0;
                    for (int i = 1; i <= line.length() + 1; ++i) {
                        if (i % 21 == 0 || i == line.length() + 1) {
                            //Log.d("TEST Hash", line + " " + y + " " + (i - 1));
                            canvas.drawText(line.substring(Start, i - 1), x, y, paint);
                            y += -paint.ascent() + paint.descent();
                            Start = i - 1;
                        }
                    }
                }
            } else {
                int Start = 0, End = 0;
                for (int i = 0; i <= COMMENT.length(); ++i) {
                    if (i % 20 == 0 || i == COMMENT.length()) {
                        canvas.drawText(COMMENT.substring(Start, i), x, y, paint);
                        y += -paint.ascent() + paint.descent();
                        Start = i;
                    }
                }
            }
        }

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize((int) (rh * 0.03));
        canvas.drawText("Age : " + Utils.getInstance().getDateDiffFormat(DATEBUY), ((int) (w * 0.02)), (int) (h * 0.055), paint);
        paint.setTextSize((int) (rh * 0.03));
        canvas.drawText(Utils.getInstance().getDateFormat(DATEBUY), ((int) (w * 0.02)), (int) (h * 0.1), paint);

        paint.setTextSize((int) (h * 0.03));
        canvas.drawText(pm.getValue(pm.FNAME) + " " + pm.getValue(pm.LNAME), ((int) (w * 0.02) + (int) (w * 0.22)), (int) (h * 0.79), paint);

        paint.setTextSize((int) (h * 0.03));
        canvas.drawText(pm.getValue(pm.EMP_ID), ((int) (w * 0.02) + (int) (w * 0.22)), (int) (h * 0.83), paint);

        paint.setTextSize((int) (h * 0.03));
        canvas.drawText(pm.getValue(pm.TEL), ((int) (w * 0.02) + (int) (w * 0.22)), (int) (h * 0.87), paint);

        paint.setTextSize((int) (h * 0.025));
        canvas.drawText(String.format("GPS: %.5f,%.5f", Double.parseDouble(pm.getValueZ(pm.LATITUDE)), Double.parseDouble(pm.getValueZ(pm.LONGTITUDE))), ((int) (w * 0.02) + (int) (w * 0.22)), (int) (h * 0.925), paint);

        paint.setTextSize((int) (h * 0.025));
        canvas.drawText(pm.getValue(pm.LOCATIONNAME), ((int) (w * 0.02) + (int) (w * 0.22)), (int) (h * 0.96), paint);

        /* Paint edge */
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((int) (rh * 0.02));
        paint.setColor(Color.WHITE);
        paint.setShadowLayer((float) 5, 2, 2, 0xFF000000);
        canvas.drawRect(0, 0, w, h, paint);
        paint.setShadowLayer((float) 5, -2, -2, 0xFF000000);
        canvas.drawRect(0, 0, w, h, paint);

        System.gc();
        return mutableBitmap;
    }

    private class AssetAdapter extends ArrayAdapter<Asset> {

        private ArrayList<Asset> originalList;
        private ArrayList<Asset> countryList;
        private CountryFilter filter;

        public AssetAdapter(Context context, int textViewResourceId,
                            ArrayList<Asset> countryList) {
            super(context, textViewResourceId, countryList);
            this.countryList = new ArrayList<Asset>();
            this.countryList.addAll(countryList);
            this.originalList = new ArrayList<Asset>();
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
            //Log.v("ConvertView", String.valueOf(position));
            if (convertView == null) {

                LayoutInflater vi = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.dialog_list_item, null);

                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.dialoglist_txt);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Asset country = countryList.get(position);
            holder.name.setText(country.getAssetId() + "\n" + country.getAssetName());
            holder.name.setTextSize(24);
            return convertView;

        }

        private class CountryFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                constraint = constraint.toString().toLowerCase();
                FilterResults result = new FilterResults();
                if (constraint != null && constraint.toString().length() > 0) {
                    ArrayList<Asset> filteredItems = new ArrayList<Asset>();

                    for (int i = 0, l = originalList.size(); i < l; i++) {
                        Asset country = originalList.get(i);
                        if (country.getAssetId().toLowerCase().contains(constraint) || country.getAssetName().toLowerCase().contains(constraint))
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
            protected void publishResults(CharSequence constraint, FilterResults results) {

                countryList = (ArrayList<Asset>) results.values;
                notifyDataSetChanged();
                clear();
                for (int i = 0, l = countryList.size(); i < l; i++)
                    add(countryList.get(i));
                notifyDataSetInvalidated();
            }
        }
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

    private class CommentAdapter extends ArrayAdapter<String> {

        private ArrayList<String> originalList;
        private ArrayList<String> countryList;
        private CountryFilter filter;

        public CommentAdapter(Context context, int textViewResourceId, ArrayList<String> countryList) {
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
            Log.v("ConvertView", String.valueOf(position));
            if (convertView == null) {

                LayoutInflater vi = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.dialog_list_item, null);

                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.dialoglist_txt);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String country = countryList.get(position);
            holder.name.setText(country);
            holder.name.setTextSize(22);
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

    private class Loading extends AsyncTask<Bitmap, Void, Void> {

        @Override
        protected Void doInBackground(Bitmap... jpeg) {
            String result = new CallService().saveStateAsset(ASSETKEY, ASSETTEXT, String.format("%.5f", Double.parseDouble(pm.getValueZ(pm.LATITUDE))), String.format("%.5f", Double.parseDouble(pm.getValueZ(pm.LONGTITUDE))), LOCATIONNAME, FILENAME, pm.getValue(pm.EMP_ID), TYPE, TYPENAME, COMMENT, COMPANY);
            Log.d("Result savestate", result);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }

        }
    }

    private class GetAssetList extends AsyncTask<Void, Void, Boolean> {

        private SweetAlertDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new SweetAlertDialog(CameraMain.this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Loading");
            pDialog.setCancelable(false);
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
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (cOrientationEventListener == null) {
            cOrientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
                public void onOrientationChanged(int orientation) {
                    if (orientation == ORIENTATION_UNKNOWN) return;
                    Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
                    android.hardware.Camera.getCameraInfo(currentCameraId, info);
                    orientation = (orientation + 45) / 90 * 90;
                    int rotation = 0;
                    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        rotation = (info.orientation - orientation + 360) % 360;
                    } else {  // back-facing camera
                        rotation = (info.orientation + orientation) % 360;
                    }
                    rotateAngle = rotation;
                }

            };
        }
        if (cOrientationEventListener.canDetectOrientation()) {
            cOrientationEventListener.enable();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCameraAndPreview();

        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        if (bitmapHolder != null) {
            bitmapHolder.recycle();
            bitmapHolder = null;
        }
        System.gc();
        Intent intent = new Intent();
        intent.putExtra("fileName", FILENAME);
        setResult(RESULT_CANCELED, intent);
        this.finish();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
