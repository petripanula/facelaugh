package com.babylaugh;


import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.BaseAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.GridView;
import android.widget.Toast;

/**
 * Created by ppanula on 15.9.2015.
 */
public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private int pics;
    private int picture_size;

    public ImageAdapter(Context c, int pictures, int pic_size) {
        mContext = c;
        pics = pictures;
        picture_size = pic_size;
    }


    //Number of elemens show in gridview...
    public int getCount() {

        return pics;
        //return Pictures.MEMORY_IDS.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        String newString;
        int in;

        if(BabyMain.ENABLE_LOGS) Log.d(BabyMain.TAG, "public View getView. position: " + position);

        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(picture_size, picture_size));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);

            //MUN...  baby_smile
            //newString = imageView.getResources().getResourceEntryName(Pictures.MEMORY_IDS[position]);
            newString = imageView.getResources().getResourceEntryName(MemoryGameActivity.NewArray[position]);
            newString = newString.replace("baby_smile", "");
            //in = Integer.valueOf(newString);

            in = 0;

            try {
                in = Integer.valueOf(newString);
            } catch(NumberFormatException nfe) {
                System.out.println("Could not parse " + newString);
                Toast.makeText(mContext, "ImageAdapter:getView Could not parse " + newString, Toast.LENGTH_SHORT).show();
            }

            imageView.setId(in);
        } else {
            imageView = (ImageView) convertView;
        }

        //Log.d(BabyMain.TAG, "getResources().getResourceEntryName: " + imageView.getResources().getResourceEntryName(Pictures.MEMORY_IDS[position]));

        imageView.setImageResource(MemoryGameActivity.NewArray[position]);
        int color = Color.parseColor("#FFFFFF");
        imageView.setColorFilter(color);


        return imageView;
    }


    /*
    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7,
            R.drawable.sample_0, R.drawable.sample_1,
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7,
            R.drawable.sample_0, R.drawable.sample_1,
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7
    };
    */
}