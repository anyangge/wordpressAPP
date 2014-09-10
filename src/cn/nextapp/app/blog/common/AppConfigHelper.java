package cn.nextapp.app.blog.common;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import cn.nextapp.app.blog.entity.AccessInfo;
import android.content.Context;

public class AppConfigHelper{
	
	private final static String g_conf = "config";
	private Context activity;
	private AccessInfo	accessInfo = null;
	
	public static AppConfigHelper getAppConfig(Context activity)
	{
		AppConfigHelper ach = new AppConfigHelper();
		ach.activity = activity;
		return ach;
	}
	
	private AppConfigHelper(){}
	
	public String cookie() {
		return get("cookie");
	}
	
	public void setLoginUser(String user)
	{
		set("loginUser", user);
	}
	public String getLoginUser()
	{
		return get("loginUser");
	}
	public void removeLoginUser()
	{
		remove("loginUser");
	}
	public void setAccessToken(String accessToken)
	{
		set("accessToken", accessToken);
	}
	public String getAccessToken()
	{
		return get("accessToken");
	}
	public void setAccessSecret(String accessSecret)
	{
		set("accessSecret", accessSecret);
	}
	public String getAccessSecret()
	{
		return get("accessSecret");
	}
	public void setAccessInfo(String accessToken,String accessSecret)
	{
		if(accessInfo == null)
			accessInfo = new AccessInfo();
		accessInfo.setAccessToken(accessToken);
		accessInfo.setAccessSecret(accessSecret);
	}
	public AccessInfo getAccessInfo()
	{
		if(accessInfo == null && !StringUtils.isEmpty(getAccessToken()) && !StringUtils.isEmpty(getAccessSecret()))
			setAccessInfo(getAccessToken(), getAccessSecret());
//		if(accessInfo == null)
//			accessInfo = InfoHelper.getAccessInfo(activity.getApplicationContext());
		return accessInfo;
	}
	
	public Properties get() {
		FileInputStream fis = null;
		Properties props = new Properties();
		try{
			fis = activity.openFileInput(g_conf);
			props.load(fis);
		}catch(Exception e){
		}finally{
			try {
				fis.close();
			} catch (Exception e) {}
		}
		return props;
	}
	
	private void setProps(Properties p) {
		FileOutputStream fos = null;
		try{
			fos = activity.openFileOutput(g_conf, Context.MODE_PRIVATE);
			p.store(fos, null);
			fos.flush();
		}catch(Exception e){	
			e.printStackTrace();
		}finally{
			try {
				fos.close();
			} catch (Exception e) {}
		}
	}

	public void set(Properties ps)
	{
		Properties props = get();
		props.putAll(ps);
		setProps(props);
	}
	
	public void set(String key,String value)
	{
		Properties props = get();
		props.setProperty(key, value);
		setProps(props);
	}
	
	public String get(String key)
	{
		Properties props = get();
		return (props!=null)?props.getProperty(key):null;
	}
	public void remove(String...key)
	{
		Properties props = get();
		for(String k : key)
			props.remove(k);
		setProps(props);
	}
}
