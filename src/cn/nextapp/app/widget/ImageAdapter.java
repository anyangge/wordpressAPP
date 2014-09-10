package cn.nextapp.app.widget;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter
{
	// 定义Context
	private Context		mContext;
	// 定义整型数组 即图片源
	private Integer[]	mImageIds;
	
	private Bitmap[]	mBitmaps;
	
	private List<Bitmap>	mBitmaplist;

	public ImageAdapter(Context c,Integer[] imageIds)
	{
		mContext = c;
		mImageIds = imageIds;
	}
	
	public ImageAdapter(Context c,Bitmap[] imageBitmaps)
	{
		mContext = c;
		mBitmaps = imageBitmaps;
	}
	
	public ImageAdapter(Context c,List<Bitmap> bitmaplist)
	{
		mContext = c;
		mBitmaplist = bitmaplist;
	}

	// 获取图片的个数
	public int getCount()
	{
		int count = 0;
		if(mImageIds!=null && mImageIds.length>0)
			count = mImageIds.length;
		else if(mBitmaps!=null && mBitmaps.length>0)
			count = mBitmaps.length;
		else if(mBitmaplist!=null && mBitmaplist.size()>0)
			count = mBitmaplist.size();
		return count;
	}

	// 获取图片在库中的位置
	public Object getItem(int position)
	{
		return position;
	}


	// 获取图片ID
	public long getItemId(int position)
	{
		return position;
	}


	public View getView(int position, View convertView, ViewGroup parent)
	{
		ImageView imageView;
		if (convertView == null)
		{
			// 给ImageView设置资源
			imageView = new ImageView(mContext);
			// 设置布局 图片120×120显示
			//imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
			// 设置显示比例类型
			imageView.setScaleType(ImageView.ScaleType.FIT_XY);
		}
		else
		{
			imageView = (ImageView) convertView;
		}

		if(mImageIds != null && mImageIds.length > 0)
			imageView.setImageResource(mImageIds[position]);
		else if(mBitmaps != null && mBitmaps.length > 0)
			imageView.setImageBitmap(mBitmaps[position]);
		else if(mBitmaplist != null && mBitmaplist.size() > 0)
			imageView.setImageBitmap(mBitmaplist.get(position));
			
		return imageView;
	}

}

