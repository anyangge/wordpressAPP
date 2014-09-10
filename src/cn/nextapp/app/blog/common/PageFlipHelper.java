package cn.nextapp.app.blog.common;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import cn.nextapp.app.blog.R;
/**
 * 悬浮按钮实现文章翻篇
 * @author liux
 * @date 2012-2-4 下午2:48:52
 */
public class PageFlipHelper {
	private WindowManager wm=null;
	private WindowManager.LayoutParams wmParams=null;
	
	private ImageView leftbtn=null;
	private ImageView rightbtn=null;
	
	// ImageView的alpha值   
	private int mAlpha = 0;
	private boolean isHide;

	private Context activity;
	
	public static PageFlipHelper getPageFlipper(Context activity)
	{
		PageFlipHelper pf = new PageFlipHelper();
		pf.activity = activity;
		pf.init();
		return pf;
	}
	
	private void init(){
    	//获取WindowManager
    	wm=(WindowManager)(activity.getApplicationContext()).getSystemService("window");
        //设置LayoutParams(全局变量）相关参数
    	wmParams = new WindowManager.LayoutParams();
    	
        wmParams.type=LayoutParams.TYPE_PHONE;   //设置window type
        wmParams.format=PixelFormat.RGBA_8888;   //设置图片格式，效果为背景透明
        //设置Window flag
         wmParams.flags=LayoutParams.FLAG_NOT_TOUCH_MODAL 
                               | LayoutParams.FLAG_NOT_FOCUSABLE;

        //以屏幕左上角为原点，设置x、y初始值
        wmParams.x=0;
        wmParams.y=0;
        //设置悬浮窗口长宽数据
        wmParams.width=60;
        wmParams.height=60;
    	
        //创建悬浮按钮
        createLeftFloatView();
        createRightFloatView();
	}
	/**
	 * 创建左边悬浮按钮
	 */
    private void createLeftFloatView(){
    	leftbtn=new ImageView(activity);
    	leftbtn.setImageResource(R.drawable.prev);
    	leftbtn.setAlpha(0);
    	
    	//调整悬浮窗口
        wmParams.gravity=Gravity.LEFT|Gravity.CENTER_VERTICAL;
        //显示myFloatView图像
        wm.addView(leftbtn, wmParams);
    }
    /**
	 * 创建右边悬浮按钮
	 */
    private void createRightFloatView(){
    	rightbtn=new ImageView(activity);
    	rightbtn.setImageResource(R.drawable.next);
    	rightbtn.setAlpha(0);
    	
    	//调整悬浮窗口
        wmParams.gravity=Gravity.RIGHT|Gravity.CENTER_VERTICAL;
        //显示myFloatView图像
        wm.addView(rightbtn, wmParams);
    }
    /**
     * 设置左边按钮点击事件
     * 上一篇
     * @param listener
     */
    public void setLeftOnClickListener(View.OnClickListener listener){
    	leftbtn.setOnClickListener(listener);
    }
    /**
     * 设置左边按钮点击事件
     * 下一篇
     * @param listener
     */
    public void setRightOnClickListener(View.OnClickListener listener){
    	rightbtn.setOnClickListener(listener);
    }
    /**
     * 设置左边按钮显示或隐藏
     * @param boolean v
     */
    public void setLeftViewHide(boolean v){
    	if(v)
    		leftbtn.setVisibility(View.GONE);
    	else
    		leftbtn.setVisibility(View.VISIBLE);
    }
    /**
     * 设置右边按钮显示或隐藏
     * @param boolean v
     */
    public void setRightViewHide(boolean v){
    	if(v)
    		rightbtn.setVisibility(View.GONE);
    	else
    		rightbtn.setVisibility(View.VISIBLE);
    }
    /**
     * 图片渐变显示处理
     */
    private Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg) {
			if(msg.what==1 && mAlpha<255){   
				//System.out.println("---"+mAlpha);					
				mAlpha += 50;
				if(mAlpha>255)
					mAlpha=255;
		        leftbtn.setAlpha(mAlpha);
		        leftbtn.invalidate();
		        rightbtn.setAlpha(mAlpha);
		        rightbtn.invalidate();
				if(!isHide && mAlpha<255)
					mHandler.sendEmptyMessageDelayed(1, 100);
			}else if(msg.what==0 && mAlpha>0){
				//System.out.println("---"+mAlpha);
				mAlpha -= 10;
				if(mAlpha<0)
					mAlpha=0;
		        leftbtn.setAlpha(mAlpha);
		        leftbtn.invalidate();
		        rightbtn.setAlpha(mAlpha);
		        rightbtn.invalidate();
		        if(isHide && mAlpha>0)
		        	mHandler.sendEmptyMessageDelayed(0, 100);
			}			
		}
	};
	
    public void showFloatView(){
    	isHide = false;
    	mHandler.sendEmptyMessage(1);
    }
    
    public void hideFloatView(){
		new Thread(){
			public void run() {
				try {
	                Thread.sleep(1500);
	                isHide = true;
	                mHandler.sendEmptyMessage(0);
	            } catch (Exception e) {
	                ;
	            }
			}
		}.start();
    }
    
    public void destroy(){
    	//在程序退出(Activity销毁）时销毁悬浮窗口
    	wm.removeView(leftbtn);
    	wm.removeView(rightbtn);
    }
}
