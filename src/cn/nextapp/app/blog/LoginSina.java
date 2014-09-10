package cn.nextapp.app.blog;

import cn.nextapp.app.blog.common.SinaWeiboHelper;
import cn.nextapp.app.blog.common.StringUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 登录新浪微博并发送博客
 * @author liux
 * @date 2011-12-19 下午9:45:33
 */
public class LoginSina extends Activity{
	
	private Button				ls_btn_sina_login;
	private EditText			ls_edit_sina_userName;
	private EditText			ls_edit_sina_password;
	private CheckBox			ls_checkbox_savepwd;
	private ProgressDialog		ls_pdialog;
	private String 				shareMessage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_sina_weibo);
		
		Intent intent = getIntent();
		String blogTitle = intent.getStringExtra("blog_title");
		String blogUrl = intent.getStringExtra("blog_url");
		shareMessage = blogTitle + " " +blogUrl;
		
		ls_edit_sina_userName = (EditText)findViewById(R.id.login_sina_edittext_username);
		ls_edit_sina_password = (EditText)findViewById(R.id.login_sina_edittext_password);
		ls_checkbox_savepwd = (CheckBox)findViewById(R.id.login_sina_checkbox_savepwd);
		
		ls_btn_sina_login = (Button)findViewById(R.id.login_sina_button_login);		
		ls_btn_sina_login.setOnClickListener(loginClickListener);
	}
	
	private View.OnClickListener loginClickListener = new View.OnClickListener(){
	   public void onClick(View v) 
	   {			   
		   //设置第三方应用key & secret
		   final String consumer_key = getString(R.string.app_sina_consumer_key);
		   final String consumer_secret = getString(R.string.app_sina_consumer_secret);
		   //获取用户name & pwd
		   final String username = ls_edit_sina_userName.getText().toString().trim();    
		   final String password = ls_edit_sina_password.getText().toString().trim();
		   
		   if(StringUtils.isEmpty(username)){
			   Toast.makeText( LoginSina.this, getString(R.string.sinalogin_check_account), Toast.LENGTH_SHORT ).show();
			   return;
		   }
		   if(StringUtils.isEmpty(password)){
			   Toast.makeText( LoginSina.this, getString(R.string.sinalogin_check_pass), Toast.LENGTH_SHORT ).show();
			   return;
		   }
		   
		   //设置分享的内容 & 当前Context
		   SinaWeiboHelper.setShareMessage(shareMessage);
		   SinaWeiboHelper.setContext(LoginSina.this);
		   
		   ls_pdialog = new ProgressDialog(LoginSina.this); 
		   ls_pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); 
		   ls_pdialog.setMessage(getString(R.string.sinalogin_check_server));
		   ls_pdialog.show(); 
		   
		   if( !((NextAppContext)getApplicationContext()).isNetworkConnected() )
		   {
				ls_pdialog.dismiss();
				Toast.makeText( LoginSina.this, getString(R.string.network_not_connected), Toast.LENGTH_SHORT ).show();
				return;
		   }

		   new Thread(){
			   public void run() 
			   {
				   Message msg = new Message();
				   //是否保存登录信息
				   SinaWeiboHelper.setIsSaveAccessInfo(ls_checkbox_savepwd.isChecked());
				   try {
					   
					   msg.what = SinaWeiboHelper.oauthVerify(consumer_key,consumer_secret) ? SinaWeiboHelper.OAUTH_RequestToken_ACCESS : SinaWeiboHelper.OAUTH_RequestToken_ERROR;
					   
					   if(msg.what == SinaWeiboHelper.OAUTH_RequestToken_ACCESS)
						   msg.what = SinaWeiboHelper.oauthUserVerify(LoginSina.this, username,password) ? SinaWeiboHelper.OAUTH_AccessToken_ACCESS : SinaWeiboHelper.OAUTH_AccessToken_ERROR;
					   
					   if(msg.what == SinaWeiboHelper.OAUTH_AccessToken_ACCESS)
						   msg.what = SinaWeiboHelper.messageChecked();
					   
					   if(msg.what == SinaWeiboHelper.Weibo_Message_CHECKED)
						   msg.what = SinaWeiboHelper.shareMessage();						   
					   
				   } catch (Exception e) {
					   msg.what = SinaWeiboHelper.OAUTH_ERROR;
				   }finally{
					   ls_pdialog.dismiss();
					   if(msg.what == SinaWeiboHelper.Weibo_Share_Success)
						   finish();
					   SinaWeiboHelper.handler.sendMessage(msg);
				   }
			   };
		   }.start();
	   }
	};
}
