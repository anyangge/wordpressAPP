package cn.nextapp.app.blog.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import cn.nextapp.app.blog.*;
import cn.nextapp.app.widget.ScrollLayout;

/**
 * 封装UI之间的切换入口
 * @author liux
 *
 */
public class UIHelper {

	/**
	 * 显示文章
	 * @param context
	 * @param blogId
	 */
	public static void showBlogDetail(Context context, int blogId)
	{
		Intent intent = new Intent(context, BlogDetail.class);
		intent.putExtra("id", blogId);
		context.startActivity(intent);
	}
	
	/**
	 * 显示文章评论
	 * @param context
	 * @param blogId
	 */
	public static void showBlogReview(Context context, int blogId)
	{
		Intent intent = new Intent(context, BlogReview.class);
		intent.putExtra("id", blogId);
		context.startActivity(intent);
	}

	/**
	 * 显示评论发表页面
	 * @param context
	 * @param blogId
	 */
	public static void showReviewPublish(Context context, int blogId)
	{
		Intent intent = new Intent(context, ReviewPublish.class);
		intent.putExtra("id", blogId);
		context.startActivity(intent);
	}
	
	/**
	 * 分享到'新浪微博'或'腾讯微博'的对话框
	 * @param context 当前Activity
	 * @param title	分享的标题
	 * @param url 分享的链接
	 */
	public static void showShareDialog(final Activity context,final String title,final String url)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(android.R.drawable.btn_star);
		builder.setTitle(context.getString(R.string.share));
		builder.setItems(R.array.app_share_items,new DialogInterface.OnClickListener(){
			AppConfigHelper cfgHelper = AppConfigHelper.getAppConfig(context);
			public void onClick(DialogInterface arg0, int arg1) {
				switch (arg1) {
					case 0:
						//判断之前是否登陆过
				        if(cfgHelper.getAccessInfo() != null)
				        {   
				        	SinaWeiboHelper.progressDialog = new ProgressDialog(context); 
				        	SinaWeiboHelper.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); 
				        	SinaWeiboHelper.progressDialog.setMessage(context.getString(R.string.sharing));
				        	SinaWeiboHelper.progressDialog.show();
				        	new Thread()
				        	{
				        		public void run() {
						    		String blogTitle = title;
						    		String blogUrl = url;
						    		String shareMessage = blogTitle + " " +blogUrl;
						    		if(SinaWeiboHelper.isWeiboNull())
						    		{
						    			SinaWeiboHelper.initWeibo(context.getString(R.string.app_sina_consumer_key), context.getString(R.string.app_sina_consumer_secret));
						    		}
						        	SinaWeiboHelper.setAccessTokenKey(cfgHelper.getAccessInfo().getAccessToken());
						        	SinaWeiboHelper.setAccessTokenSecret(cfgHelper.getAccessInfo().getAccessSecret());
						        	SinaWeiboHelper.shareMessage(shareMessage,context);
				        		};
				        	}.start();
				        }
				        else
				        {
				        	Intent intent = new Intent();
							intent.putExtra("blog_title", title);
							intent.putExtra("blog_url", url);
							intent.setClass(context, LoginSina.class);
							context.startActivity(intent);
				        }
						break;
					case 1:
//						Intent it = new Intent();
//						it.setClass(context, LoginTencent.class);
//						context.startActivity(it);
						QQWeiboHelper.shareToQQ(context, title, url);
						break;
				}				
			}
		});
		builder.create().show();
	}
	
	/**
	 * 显示首页
	 * @param activity
	 */
	public static void showHome(Activity activity)
	{
		Intent intent = new Intent(activity,Main.class);
		activity.startActivity(intent);
		activity.finish();
	}
	
	/**
	 * 菜单显示登录或登出
	 * @param activity
	 * @param menu
	 */
	public static void showMenuLoginOrLogout(Activity activity,Menu menu)
	{
		if(((NextAppContext)activity.getApplication()).isLogin()){
			menu.findItem(R.id.main_menu_login).setTitle(R.string.main_menu_logout);
			menu.findItem(R.id.main_menu_login).setIcon(R.drawable.menu_logout_icon);
		}
		else{
			menu.findItem(R.id.main_menu_login).setTitle(R.string.main_menu_login);
			menu.findItem(R.id.main_menu_login).setIcon(R.drawable.menu_login_icon);
		}
	}
	
	/**
	 * 菜单项选择处理
	 * @param activity
	 * @param item
	 */
	public static void menuItemSelectedAction(Activity activity,MenuItem item)
	{
		//得到当前选中的MenuItem的ID,
		int item_id = item.getItemId();
		NextAppContext app = ((NextAppContext)activity.getApplication());
		switch (item_id)
		{
//			//屏幕左右切换之前版本
//			case R.id.main_menu_bloglist:
//				Intent intent = new Intent(activity, Main.class);
//				intent.putExtra("REFRESH", true);
//				activity.startActivity(intent);
//				break;
//			case R.id.main_menu_reviewlist:
//				Intent intent2 = new Intent(activity, ReviewList.class);
//				intent2.putExtra("REFRESH", true);
//				activity.startActivity(intent2);
//				break;
			case R.id.main_menu_bloglist:
				Intent intent = new Intent(activity, Main.class);
				intent.putExtra("REFRESH", "BLOGLIST");
				activity.startActivity(intent);
				break;
			case R.id.main_menu_reviewlist:
				Intent intent2 = new Intent(activity, Main.class);
				intent2.putExtra("REFRESH", "REVIEWLIST");
				activity.startActivity(intent2);
				break;
				
			case R.id.main_menu_publish:
				//判断是否登录
				if(app.isLogin())
					activity.startActivity(new Intent(activity,BlogPublish.class));				
				else
					//加载登陆框 --登录成功跳到发布界面
					Login.ShowLoginDialog(activity,Login.PUBLISH);				
				break;
			case R.id.main_menu_login:
				//判断是否登录
				if(app.isLogin())
				{
					item.setTitle(R.string.main_menu_login);
					item.setIcon(R.drawable.menu_login_icon);
					app.cleanLoginInfo();
					Toast.makeText(activity, R.string.logout_success, Toast.LENGTH_SHORT).show();
				}
				else
				{
					Login.ShowLoginDialog(activity,Login.LOGIN);
				}
				break;
			case R.id.main_menu_about:
				String ver = activity.getString(R.string.app_version_name);
				String title = activity.getString(R.string.app_about_title) + " " + ver; 
				new AlertDialog.Builder(activity)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(title)
				.setView(View.inflate(activity, R.layout.about, null))
				.create().show();
				break;
			case R.id.main_menu_exit:
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setIcon(android.R.drawable.ic_dialog_info);
				builder.setTitle(R.string.surelogout);
				builder.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						//退出
						AppManager.getAppManager().AppExit();
					}
				});
				builder.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.create().show();
				break;
		}
	}
}
