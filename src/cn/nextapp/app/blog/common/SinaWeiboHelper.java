package cn.nextapp.app.blog.common;

import java.io.File;
import java.net.URLEncoder;

import cn.nextapp.app.blog.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import weibo4android.Status;
import weibo4android.Weibo;
import weibo4android.WeiboException;
import weibo4android.http.AccessToken;
import weibo4android.http.OAuthVerifier;
import weibo4android.http.RequestToken;
/**
 * 新浪微博帮助类
 * @author liux
 */
public class SinaWeiboHelper {

	public static final int OAUTH_ERROR = 0;
	public static final int OAUTH_RequestToken_ACCESS = 1;
	public static final int OAUTH_RequestToken_ERROR = 2;
	public static final int OAUTH_AccessToken_ACCESS = 3;
	public static final int OAUTH_AccessToken_ERROR = 4;
	public static final int Weibo_Message_CHECKED = 5;
	public static final int Weibo_Message_NULL = 6;
	public static final int Weibo_Message_LONG = 7;
	public static final int Weibo_Share_Success = 8;
	public static final int Weibo_Share_Error = 9;
	public static final int Weibo_Share_Repeat = 10;
	
	private static Weibo 			weibo = null;
	private static RequestToken 	requestToken = null;
	private static AccessToken 		accessToken = null;
	private static String 			accessTokenKey = null;
	private static String 			accessTokenSecret = null;
	private static String 			shareImage = null;
	private static String			shareMessage = null;
	private static boolean			isSaveAccessInfo = false;
	private static Context			context = null;
	public static ProgressDialog	progressDialog = null;
	
	public static void setShareMessage(String shareMsg){
		shareMessage = shareMsg;
	}
	
	public static void setShareImage(String shareImg){
		shareImage = shareImg;
	}
	
	public static void setContext(Context cont){
		context = cont;
	}
	
	public static void setIsSaveAccessInfo(boolean isSave){
		isSaveAccessInfo = isSave;
	}
	
	public static void setAccessTokenKey(String accessKey){
		accessTokenKey = accessKey;
	}
	
	public static void setAccessTokenSecret(String accessSecret){
		accessTokenSecret = accessSecret;
	}
	
	public static Handler			handler	= new Handler(){ 
		public void handleMessage(Message msg) { 
			if(progressDialog != null)
				progressDialog.dismiss();
		    switch (msg.what) 
		    { 
		    	case OAUTH_ERROR: 
		    		Toast.makeText(context, context.getString(R.string.OAUTH_ERROR), Toast.LENGTH_SHORT).show();
		    		break; 
		    	case OAUTH_RequestToken_ACCESS:
		    		Toast.makeText(context, context.getString(R.string.OAUTH_RequestToken_ACCESS), Toast.LENGTH_SHORT).show();
		    		break;
		    	case OAUTH_RequestToken_ERROR: 
		    		Toast.makeText(context, context.getString(R.string.OAUTH_RequestToken_ERROR), Toast.LENGTH_SHORT).show();
		    		break; 
		    	case OAUTH_AccessToken_ACCESS:
		    		Toast.makeText(context, context.getString(R.string.OAUTH_AccessToken_ACCESS), Toast.LENGTH_SHORT).show();
		    		break;
		    	case OAUTH_AccessToken_ERROR: 
		    		Toast.makeText(context, context.getString(R.string.OAUTH_AccessToken_ERROR), Toast.LENGTH_SHORT).show();
		    		break;
		    	case Weibo_Message_NULL:
		    		Toast.makeText(context, context.getString(R.string.Weibo_Message_NULL), Toast.LENGTH_SHORT).show();
		    		break;
		    	case Weibo_Message_LONG: 
					Toast.makeText(context, context.getString(R.string.Weibo_Message_LONG), Toast.LENGTH_SHORT ).show();
		    		break;
		    	case Weibo_Share_Success:
		    		Toast.makeText(context, context.getString(R.string.Weibo_Share_Success), Toast.LENGTH_SHORT).show();
		    		break;
		    	case Weibo_Share_Error:
					Toast.makeText(context, context.getString(R.string.Weibo_Share_Error), Toast.LENGTH_SHORT).show();
					break;
		    	case Weibo_Share_Repeat:
		    		Toast.makeText(context, context.getString(R.string.Weibo_Share_Repeat), Toast.LENGTH_SHORT).show();
		    		break;
		    }
		};
	};
	/**
	 * 判断weibo是否为null
	 * @return
	 */
	public static boolean isWeiboNull()
	{
		if(weibo == null)
			return true;
		else 
			return false;
	}
	/**
	 * 初始化weibo
	 * @param consumerKey
	 * @param consumerSecret
	 */
	public static void initWeibo(String consumerKey,String consumerSecret)
	{
		Weibo.CONSUMER_KEY = consumerKey;
		Weibo.CONSUMER_SECRET = consumerSecret;
		System.setProperty("weibo4j.oauth.consumerKey", Weibo.CONSUMER_KEY);
    	System.setProperty("weibo4j.oauth.consumerSecret", Weibo.CONSUMER_SECRET);
    	weibo = new Weibo();
	}
	/**
	 * 第三方应用验证
	 * @return
	 */
	public static boolean oauthVerify(String consumerKey,String consumerSecret)
	{
		boolean verifyCode = false;
		if(isWeiboNull())
		{
			initWeibo(consumerKey,consumerSecret);
		}
		try 
		{
			if(requestToken == null)
			{
				//根据app key第三方应用向新浪微博获取授权requestToken
				requestToken = weibo.getOAuthRequestToken();
			}
			
			System.out.println("========Request token key: "+ requestToken.getToken());
			System.out.println("========Request token secret: "+ requestToken.getTokenSecret()); 	
			
			if(requestToken != null)
			{
				verifyCode = true;
			}
		} 
		catch (WeiboException e) 
		{
			e.printStackTrace();
		}
		return verifyCode;
	}
	/**
	 * 微博用户验证
	 * @param username
	 * @param password
	 * @return
	 */
	public static boolean oauthUserVerify(Activity activity, String username,String password)
	{
		boolean verifyCode = false;
		try 
		{
			if(requestToken != null && accessToken == null)
			{
				//验证微博用户是否存在
				OAuthVerifier oauthVerifier = weibo.getOAuthVerifier(username,password); // get verifier
				String verifier = oauthVerifier.getVerifier();
				
				//验证微博用户是否通过第三方应用授权登录
	            accessToken = requestToken.getAccessToken(verifier); // get access token
			}
			
            System.out.println("========Access Token key: "+accessToken.getToken());
    		System.out.println("========Access Token Secret: "+accessToken.getTokenSecret());            
            
    		if(accessToken != null)
    		{        		
                accessTokenKey = accessToken.getToken();
                accessTokenSecret = accessToken.getTokenSecret();
                
                //得到AccessToken的key和Secret,可以使用这两个参数进行授权登录了
                if(isSaveAccessInfo)
                	AppConfigHelper.getAppConfig(activity).setAccessInfo(accessTokenKey, accessTokenSecret);
                
				verifyCode = true;
    		}
		} 
		catch (WeiboException e) 
		{
			e.printStackTrace();
		}
		return verifyCode;
	}
	/**
	 * 数据合法性判断
	 * @return
	 */
	public static int messageChecked()
	{
		int ret = Weibo_Message_CHECKED;
		if( StringUtils.isEmpty(shareMessage) )
		{
			ret = Weibo_Message_NULL;
		}
		else if( shareMessage.length() > 140 )
		{
			ret = Weibo_Message_LONG;
		}
		return ret;
	}
	/**
	 * 微博分享
	 * @author liux
	 * @return statusCode
	 */
    public static int shareMessage()
    {	
		int what = Weibo_Share_Error;	
        try 
        {   
        	weibo.setToken( accessTokenKey, accessTokenSecret );
        	if(shareMessage.getBytes().length != shareMessage.length())
        	{
        		shareMessage = URLEncoder.encode(shareMessage, "UTF-8");
        	}
        	System.out.println("ShareMessage===="+shareMessage+"====");
        	Status status = null;
        	if( StringUtils.isEmpty(shareImage) )
        	{
        		status = weibo.updateStatus(shareMessage);
        	}
        	else
        	{
        		File file = new File(shareImage);
        		status = weibo.uploadStatus(shareMessage, file);
        	}
			
			if(status!=null)
			{
				what = Weibo_Share_Success;
			}
		} 
        catch(WeiboException e)
        {
        	if(e.getStatusCode() == 400 && e.getMessage().contains("40025") && e.getMessage().contains("repeated"))
        		what = Weibo_Share_Repeat;
        	e.printStackTrace();
        }
        catch (Exception e) 
        {
        	e.printStackTrace();
		}
        return what;
	}
    /**
     * 微博分享   
     * 直接设置分享内容shareMessage & 当前context
     * Toast会提示分享成功或失败
     * @param shareMsg
     * @param cont
     */
    public static void shareMessage(String shareMsg,Context cont)
    {
    	shareMessage = shareMsg;
    	context = cont;
    	Message msg = new Message();
    	msg.what = shareMessage();
    	handler.sendMessage(msg);
    }
}
