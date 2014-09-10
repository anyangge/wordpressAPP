package cn.nextapp.app.blog;

import cn.nextapp.app.blog.api.ApiException;
import cn.nextapp.app.blog.api.NextAppClient;
import cn.nextapp.app.blog.common.StringUtils;
import cn.nextapp.app.blog.common.UIHelper;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Toast;

/**
 * 启动界面
 * @author Winter Lau
 */
public class Start extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		final View view = View.inflate(this, R.layout.start, null);
		setContentView(view);		
		
		//渐变展示启动屏
		AlphaAnimation aa = new AlphaAnimation(0.3f,1.0f);
		aa.setDuration(2000);
		view.startAnimation(aa);
		aa.setAnimationListener(new AnimationListener()
		{
			@Override
			public void onAnimationEnd(Animation arg0) {}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationStart(Animation animation) {}
			
		});

		final Handler mHandler = new Handler(){
			public void handleMessage(Message msg) {
				if(msg.what == 1)
					UIHelper.showHome(Start.this);				
				else if(msg.what == 0){
					Toast.makeText(Start.this, R.string.network_not_connected, Toast.LENGTH_LONG).show();
				}
				else if(msg.obj != null){
					ApiException ae = (ApiException)msg.obj;
					ae.makeToast(Start.this);
				}
			}
		};
		
		NextAppClient.init(getString(R.string.app_version_name), StringUtils.toInt(getString(R.string.app_version_code),1));
		
		new Thread(){
        	public void run() {
				Message msg = new Message();
				NextAppContext appContext = (NextAppContext)getApplication();
				final boolean networkOk = appContext.isNetworkConnected();
				try{
					if(appContext.catalogs() != null) {
						msg.what = 1;
						if(!networkOk)
							try {
								sleep(1500);
							} catch (InterruptedException e) {}
					}
					else
						msg.what = 0;
				}catch (ApiException e){
					msg.what = -1;
					msg.obj = e;
			    }
				mHandler.sendMessage(msg);
			}
        }.start();
		
	}
	
}
