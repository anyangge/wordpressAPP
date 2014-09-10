package cn.nextapp.app.blog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import cn.nextapp.app.blog.api.ApiException;
import cn.nextapp.app.blog.api.NextAppClient;
import cn.nextapp.app.blog.common.StringUtils;
import cn.nextapp.app.blog.entity.Result;

/**
 * login dialog
 * @author liux
 * @date 2011-12-17 下午11:52:33
 */
public class Login extends Activity {
	
	public static final String LOGIN = "LOGIN";
	public static final String PUBLISH = "PUBLISH";
	
	private static AlertDialog 			alertdialog_login;
    public static ProgressDialog 		pdialog;
    public static EditText 				et_usename;
    public static EditText 				et_password;
    private static Handler 				mHandler;
    private static Activity 			activity;
	
	//显示登陆框
    public static void ShowLoginDialog(Activity activity,String opt)
    {
        Login.activity = activity;
    	Login.mHandler = getLoginHandler(opt);
    	InitLoginDialog(activity);
    }
    
    //初始化登录框
	public static void InitLoginDialog(Activity activity)
	{
		//得到自定义对话框
		View DialogView = View.inflate(activity,R.layout.login_dialog, null);
		
        //创建对话框
       alertdialog_login = new AlertDialog.Builder(activity)
        .setIcon(android.R.drawable.ic_dialog_alert) 
        .setTitle(activity.getString(R.string.login))
        .setView(DialogView)//设置自定义对话框的样式
        .setPositiveButton(activity.getString(R.string.sure), //设置"确定"按钮
	        new DialogInterface.OnClickListener() //设置事件监听
	        {    	
	            public void onClick(DialogInterface dialog, int whichButton) {
	            	//输入完成后，点击“确定”开始登陆
	            	Login.UserLogin();	
	            }
	        })
        .setNegativeButton(activity.getString(R.string.cancle), //设置“取消”按钮
	        new DialogInterface.OnClickListener() 
	        {
	            public void onClick(DialogInterface dialog, int whichButton)
	            {
	            	//点击"取消"按钮
	            	alertdialog_login.cancel();
	            }
	        })
        .create();//创建

       	alertdialog_login.show();//显示
       
		et_usename = (EditText)alertdialog_login.findViewById(R.id.login_dialog_edittext_username);
		et_password = (EditText)alertdialog_login.findViewById(R.id.login_dialog_edittext_password);
		
		NextAppContext nac = ((NextAppContext)activity.getApplication());
		NextAppContext.LoginUser user = nac.getLoginInfo();
		if(!StringUtils.isEmpty(user.username))
			et_usename.setText(user.username);
		if(!StringUtils.isEmpty(user.pwd))
			et_password.setText(user.pwd);
	}
	
	/**
	 * 开始执行登录过程
	 */
	private static void UserLogin()
	{
		final String username= et_usename.getText().toString().trim();
		final String pwd= et_password.getText().toString().trim();
    	pdialog = ProgressDialog.show(activity, activity.getString(R.string.waiting), activity.getString(R.string.logining),true);
		new Thread(){
			public void run() {
				Message msg =new Message();
				try{ 
					//登入查询 
					NextAppContext nac = (NextAppContext)activity.getApplication();
					String url = nac.getUrls().getLoginValidate();
					Result result = NextAppClient.login(url, username, pwd);
					if(result.OK()){
						nac.saveLoginInfo(username, pwd);
						msg.what = 1 ;//成功
					}
					else{
						nac.cleanLoginInfo();
						msg.what = 0;//失败
						msg.obj = result.getErrorMessage();
					}
				}catch (ApiException e){
			    	e.printStackTrace();
			    	msg.what = -1;
			    	msg.obj = e;
			    }
				mHandler.sendMessage(msg);
			}
		}.start();
	}
	
	private static Handler getLoginHandler(final String opt){
		Handler handler = new Handler(){
			public void handleMessage(Message msg) {
				if(msg.what == 1)
			    	Toast.makeText(activity, activity.getString(R.string.login_success), Toast.LENGTH_SHORT).show();
				else if(msg.what == 0)
					Toast.makeText(activity, activity.getString(R.string.login_fail) + msg.obj, Toast.LENGTH_SHORT).show();
				else if(msg.obj != null)
					((ApiException)msg.obj).makeToast(activity);				
				
		    	//登录结束，取消pdialog对话框
		    	pdialog.dismiss();
		    	if(msg.what == 1 && PUBLISH.equals(opt)){
		    		Intent it = new Intent(activity, BlogPublish.class);
		    		activity.startActivity(it);
		    	}
			}
		};
		return handler;
	}
}

