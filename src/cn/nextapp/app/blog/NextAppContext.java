package cn.nextapp.app.blog;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import cn.nextapp.app.blog.api.ApiException;
import cn.nextapp.app.blog.api.NextAppClient;
import cn.nextapp.app.blog.common.AppConfigHelper;
import cn.nextapp.app.blog.common.CyptoUtils;
import cn.nextapp.app.blog.entity.Catalog;
import cn.nextapp.app.blog.entity.Post;
import cn.nextapp.app.blog.entity.PostList;
import cn.nextapp.app.blog.entity.URLs;

/**
 * NextApp 的应用扩展信息
 * @author Winter Lau
 * @date 2011-12-17 下午10:17:51
 */
public class NextAppContext extends Application {
	
	private boolean login = false;	//登录状态
	private Hashtable<String, Object> memCacheRegion = new Hashtable<String, Object>();

	/**
	 * 检测网络是否可用
	 * @param context
	 * @return
	 */
	public boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}

	public boolean isLogin() {
		return login;
	}

	public void setLogin(boolean login) {
		this.login = login;
	}

	private URLs urls = null; //全局接口URL定义
	public URLs getUrls() throws ApiException {
		if(urls == null) {
			try{
				urls = NextAppClient.urls(this.getString(R.string.app_api_url));
				if(urls != null)
					saveObject(urls, "urls.obj");
			}catch(ApiException e){
				urls = (URLs)readObject("urls.obj");
				if(urls == null)
					throw e;
			}
		}
		return urls;
	}

	public void setUrls(URLs urls) {
		this.urls = urls;
	}
	
	List<Catalog> catalogs = null;	
	@SuppressWarnings("unchecked")
	public List<Catalog> catalogs() throws ApiException {
		if(catalogs == null){
			String url = getUrls().getCatalogs();
			try{
				catalogs = NextAppClient.catalogs(url);
				if(catalogs != null)
					saveObject((Serializable)catalogs, "catalogs.obj");
			}catch(ApiException e){
				catalogs = (List<Catalog>)readObject("catalogs.obj");
				if(catalogs == null)
					throw e;
			}
		}
		return catalogs;
	}
	
	/**
	 * 文章列表
	 * @param catalog
	 * @param fromPost
	 * @param fetchCount
	 * @return
	 * @throws ApiException
	 */
	public PostList posts(int catalog, int fromPost) throws ApiException {
		PostList posts = null;
		String key = "posts_"+catalog+"_"+fromPost;
		if(fromPost > 0) {//非最新列表，则优先从缓存读取
			posts = (PostList)readObject(key);
			if(posts == null){
				posts = NextAppClient.posts(getUrls().getPostList(), catalog, fromPost, 10);
				if(posts != null)
					saveObject(posts, key);
			}
		}
		else {//最新列表优先从网络读取
			try{
				posts = NextAppClient.posts(getUrls().getPostList(), catalog, fromPost, 10);
				if(posts != null)
					saveObject(posts, key);
			}catch(ApiException e){
				posts = (PostList)readObject(key);
				if(posts == null)
					throw e;
			}
		}
		return posts;
	}
	
	/**
	 * 读取某篇文章内容
	 * @param post_id
	 * @return
	 * @throws ApiException
	 */
	public Post post(int post_id) throws ApiException {
		//return NextAppClient.post(getUrls().getPostDetail(), post_id);
		
		Post post = null;
		try{
			post = NextAppClient.post(getUrls().getPostDetail(), post_id);
			if(post != null)
				saveObject(post, "post_"+post_id);
		}catch(ApiException e){
			post = (Post)readObject("post_"+post_id);
			if(post == null)
				throw e;
		}
		return post;
		
	}
	
	/**
	 * 保存登录信息
	 * @param username
	 * @param pwd
	 */
	public void saveLoginInfo(final String username, final String pwd) {
		setLogin(true);
		setProperties(new Properties(){{
			setProperty("username", username);
			setProperty("pwd", CyptoUtils.encode("NextAppC",pwd));
		}});
	}
	
	/**
	 * 清除登录信息
	 */
	public void cleanLoginInfo() {
		setLogin(false);
		removeProperty("username","pwd");
	}
	
	/**
	 * 获取登录信息
	 * @return
	 */
	public LoginUser getLoginInfo() {
		LoginUser lu = new LoginUser();
		lu.username = getProperty("username");
		lu.pwd = CyptoUtils.decode("NextAppC",getProperty("pwd"));
		return lu;
	}
	
	/**
	 * 将对象保存到内存缓存中
	 * @param key
	 * @param value
	 */
	public void setMemCache(String key, Object value) {
		memCacheRegion.put(key, value);
	}
	
	/**
	 * 从内存缓存中获取对象
	 * @param key
	 * @return
	 */
	public Object getMemCache(String key){
		return memCacheRegion.get(key);
	}
	
	/**
	 * 保存磁盘缓存
	 * @param key
	 * @param value
	 * @throws IOException
	 */
	public void setDiskCache(String key, String value) throws IOException {
		FileOutputStream fos = null;
		try{
			fos = openFileOutput("cache_"+key+".data", Context.MODE_PRIVATE);
			fos.write(value.getBytes());
			fos.flush();
		}finally{
			try {
				fos.close();
			} catch (Exception e) {}
		}
	}
	
	/**
	 * 获取磁盘缓存数据
	 * @param key
	 * @return
	 * @throws IOException
	 */
	public String getDiskCache(String key) throws IOException {
		FileInputStream fis = null;
		try{
			fis = openFileInput("cache_"+key+".data");
			byte[] datas = new byte[fis.available()];
			fis.read(datas);
			return new String(datas);
		}finally{
			try {
				fis.close();
			} catch (Exception e) {}
		}
	}
	
	/**
	 * 保存对象
	 * @param ser
	 * @param file
	 * @throws IOException
	 */
	public boolean saveObject(Serializable ser, String file) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try{
			fos = openFileOutput(file, MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(ser);
			oos.flush();
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}finally{
			try {
				oos.close();
			} catch (Exception e) {}
			try {
				fos.close();
			} catch (Exception e) {}
		}
	}
	
	/**
	 * 读取对象
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public Serializable readObject(String file){
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try{
			fis = openFileInput(file);
			ois = new ObjectInputStream(fis);
			return (Serializable)ois.readObject();
		}catch(FileNotFoundException e){
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				ois.close();
			} catch (Exception e) {}
			try {
				fis.close();
			} catch (Exception e) {}
		}
		return null;
	}

	public void setProperties(Properties ps){
		AppConfigHelper.getAppConfig(this).set(ps);
	}

	public Properties getProperties(){
		return AppConfigHelper.getAppConfig(this).get();
	}
	
	public void setProperty(String key,String value){
		AppConfigHelper.getAppConfig(this).set(key, value);
	}
	
	public String getProperty(String key){
		return AppConfigHelper.getAppConfig(this).get(key);
	}
	public void removeProperty(String...key){
		AppConfigHelper.getAppConfig(this).remove(key);
	}
	
	public static class LoginUser {
		public String username;
		public String pwd;
	}
	
}
