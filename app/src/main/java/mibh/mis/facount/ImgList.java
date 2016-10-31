package mibh.mis.facount;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mibh.mis.facount.Manager.RealmManager;
import mibh.mis.facount.Realm.ImgStore;

/**
 * Created by ponlakiss on 10/15/2015.
 */
public class ImgList extends AppCompatActivity {

    private SparseBooleanArray mCheckStates;
    private Uri uri;
    private String TYPE;
    private List<ImgStore> Arr;
    private ArrayList<String> arrFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.grid_img);

        Bundle extras = getIntent().getExtras();

        if (extras.containsKey("TYPE")) {
            TYPE = extras.getString("TYPE", "");
        }

        List<ImgStore> ArrTemp = RealmManager.getInstance().getImgByAssetType(TYPE);
        Arr = new ArrayList<>();

        for (int i = ArrTemp.size() - 1; i >= 0; i--) {
            Arr.add(ArrTemp.get(i));
        }

        final GridView gridview = (GridView) findViewById(R.id.gridview);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabImg);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareLine();
            }
        });

        mCheckStates = new SparseBooleanArray(Arr.size());
        gridview.setAdapter(new ImageAdapter(this));
    }

    private void shareLine() {
        ArrayList<Uri> imageUris = new ArrayList<>();
        File imagesFolder = new File(Environment.getExternalStorageDirectory(), "ASSET");
        Uri newUri;

        for (int i = 0; i < Arr.size(); i++) {
            if (mCheckStates.get(i, false)) {
                File output = new File(imagesFolder, Arr.get(i).getFilename());
                if (output.exists()) {
                    System.gc();
                    newUri = Uri.fromFile(output);
                    imageUris.add(newUri);
                }
            }
        }
        try {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setPackage("jp.naver.line.android");
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
            shareIntent.setType("image/*");
            startActivity(Intent.createChooser(shareIntent, "Share images to.."));
        } catch (Exception e) {
        }
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;

        public ImageAdapter(Context c) {
            mContext = c;
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return Arr.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.grid_item, null);
                holder.imageview = (ImageView) convertView.findViewById(R.id.thumbImage);
                holder.checkbox = (CheckBox) convertView.findViewById(R.id.itemCheckBox);
                holder.itemtext = (TextView) convertView.findViewById(R.id.item_text);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.checkbox.setId(position);
            holder.imageview.setId(position);
            holder.checkbox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    CheckBox cb = (CheckBox) v;
                    int id = cb.getId();
                    if (mCheckStates.get(id, false)) {
                        cb.setChecked(false);
                        mCheckStates.put(id, false);
                    } else {
                        cb.setChecked(true);
                        mCheckStates.put(id, true);
                    }
                }
            });

            final File imagesFolder = new File(Environment.getExternalStorageDirectory(), "ASSET");
            File output = new File(imagesFolder, Arr.get(position).getFilename());
            uri = Uri.fromFile(output);

            holder.imageview.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    int id = v.getId();
                    File output = new File(imagesFolder, Arr.get(id).getFilename());
                    Uri uri2 = Uri.fromFile(output);
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri2, "image/*");
                    startActivity(intent);
                }
            });

            Picasso.with(mContext).load(uri).noFade().resize(240, 320).into(holder.imageview);
            //holder.imageview.setImageURI(uri);
            holder.checkbox.setChecked(mCheckStates.get(position, false));
            String str = Arr.get(position).getAssetKey() + "\n" + Arr.get(position).getAssetTxt();
            holder.itemtext.setText(str);
            holder.id = position;
            return convertView;
        }
    }

    class ViewHolder {
        ImageView imageview;
        CheckBox checkbox;
        TextView itemtext;
        int id;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
