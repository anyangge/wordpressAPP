package cn.nextapp.app.blog.api;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.params.CookieSpecPNames;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import cn.nextapp.app.blog.entity.Catalog;
import cn.nextapp.app.blog.entity.Comment;
import cn.nextapp.app.blog.entity.CommentList;
import cn.nextapp.app.blog.entity.Post;
import cn.nextapp.app.blog.entity.PostList;
import cn.nextapp.app.blog.entity.Result;
import cn.nextapp.app.blog.entity.URLs;

/**
 * NextApp 客户端接口包
 * @author Winter Lau
 */
public class NextAppClient {

	public static final String DESC = "descend";
	public static final String ASC = "ascend";
	
	private final static int TIMEOUT_CONNECTION = 20000;
	private final static int TIMEOUT_SOCKET = 20000;
	private final static int TIMEOUT_TOTAL = 60000;
	
	private static DefaultHttpClient client;

	public static void init(String version, int code) {
		
		BasicHttpParams params = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.  
	    HttpConnectionParams.setConnectionTimeout(params, TIMEOUT_CONNECTION);
	    // Set the default socket timeout (SO_TIMEOUT) 
	    // in milliseconds which is the timeout for waiting for data.  
	    HttpConnectionParams.setSoTimeout(params, TIMEOUT_SOCKET);  

        ConnManagerParams.setMaxTotalConnections(params, 5);
        ConnManagerParams.setTimeout(params, TIMEOUT_TOTAL);
        
	    client = new DefaultHttpClient(params);
	    
		CookieStore cookieStore = new BasicCookieStore();
		//Bind custom cookie store to the local context
		client.setCookieStore(cookieStore);
		CookieSpecFactory csf = new CookieSpecFactory() {			
	        public CookieSpec newInstance(HttpParams params) {
	            return new BrowserCompatSpec() {
	                @Override
	                public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException 
	                {
	                    // Oh, I am easy
	                    // allow all cookies
	                    //log.debug("custom validate");
	                }
	            };
	        }
		};
		client.getCookieSpecs().register("nextapp", csf);
		client.getParams().setParameter(ClientPNames.COOKIE_POLICY, "nextapp");
		client.getParams().setParameter(CookieSpecPNames.SINGLE_COOKIE_HEADER, true);
		
		StringBuilder ua = new StringBuilder("NextApp");
		ua.append(version);
		ua.append('(');
		ua.append(code);
		ua.append(")/");
		ua.append(android.os.Build.MODEL); 
		ua.append("/Android ");
		ua.append(android.os.Build.VERSION.RELEASE);
		ua.append(" (http://www.nextapp.cn/)");
		HttpProtocolParams.setUserAgent(client.getParams(), ua.toString());
	}

	/**
	 * 列出API中所有URL地址
	 * @return
	 * @throws ApiException 
	 * @throws  
	 */
	public static URLs urls(String api_entry_url) throws ApiException {
		try{
			return URLs.parse(http_get(api_entry_url), api_entry_url);    
		}catch(Exception e){
			throw ApiException.network(e);
		}
	}
	
	/**
	 * 登录，由 DefaultHttpClient 自动处理cookie
	 * @param url
	 * @param username
	 * @param pwd
	 * @return
	 * @throws ApiException
	 */
	public static Result login(String url, String username, String pwd) throws ApiException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("username", username);
		params.put("pwd", pwd);
		params.put("keep_login", 1);
				
		try{
			return http_post(url, params, null);		
		}catch(Exception e){
			if(e instanceof ApiException)
				throw (ApiException)e;
			throw ApiException.network(e);
		}
	}
	
	/**
	 * 列出所有分类
	 * @param url
	 * @return
	 * @throws ApiException
	 */
	public static List<Catalog> catalogs(String url) throws ApiException {
		try{
			return Catalog.parse(http_get(url));
		}catch(Exception e){
			if(e instanceof ApiException)
				throw (ApiException)e;
			throw ApiException.network(e);
		}
	}
	
	/**
	 * 获取文章列表
	 * @param url
	 * @param catalog
	 * @param fromPost
	 * @param fetchCount
	 * @return
	 * @throws ApiException
	 */
	public static PostList posts(String url, final int catalog, final int fromPost, final int fetchCount) throws ApiException {
		String newUrl = _MakeURL(url, new HashMap<String, Object>(){{
			put("catalog", catalog);
			put("fromPost", fromPost);
			put("fetchCount", fetchCount);
		}});

		try{
			return PostList.parse(http_get(newUrl));		
		}catch(Exception e){
			if(e instanceof ApiException)
				throw (ApiException)e;
			throw ApiException.network(e);
		}
	}
	
	/**
	 * 获取某篇文章的详情
	 * @param url
	 * @param post_id
	 * @return
	 * @throws ApiException
	 */
	public static Post post(String url, final int post_id) throws ApiException {
		String newUrl = _MakeURL(url, new HashMap<String, Object>(){{
			put("post", post_id);
		}});
		try{
			return Post.parse(http_get(newUrl));			
		}catch(Exception e){
			if(e instanceof ApiException)
				throw (ApiException)e;
			throw ApiException.network(e);
		}
	}
	
	/**
	 * 发表文章
	 * @param url
	 * @param post
	 * @return
	 * @throws ApiException
	 */
	public static Result publish(String url, Post post) throws ApiException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("catalog", post.getCatalog());
		params.put("title", post.getTitle());
		params.put("body", post.getBody());
		params.put("tag", post.getTags());
				
		Map<String, File> files = new HashMap<String, File>();
		int i = 0;
		if(post.getImageFiles() != null)
		for(File file : post.getImageFiles()){
			files.put("img"+i, file);
			i++;
		}
		
		try{
			return http_post(url, params, files);		
		}catch(Exception e){
			if(e instanceof ApiException)
				throw (ApiException)e;
			throw ApiException.network(e);
		}
	}

	/**
	 * 删除文章
	 * @param url
	 * @param post
	 * @return
	 * @throws ApiException
	 */
	public static Result deletePost(String url, final int post) throws ApiException {
		String newUrl = _MakeURL(url, new HashMap<String, Object>(){{
			put("post", post);
		}});
		try{
			return Result.parse(http_get(newUrl));			
		}catch(Exception e){
			if(e instanceof ApiException)
				throw (ApiException)e;
			throw ApiException.network(e);
		}
	}
	
	/**
	 * 获取评论列表
	 * @param url
	 * @param post
	 * @param fromComment
	 * @param fetchCount
	 * @param sortMethod : descend 降序, ascend 升序
	 * @return
	 * @throws ApiException
	 */
	public static CommentList comments(String url, int post, int fromComment, int fetchCount, String sortMethod) throws ApiException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("post", post);
		params.put("fromComment", fromComment);
		params.put("fetchCount", fetchCount);
		params.put("sortMethod", sortMethod);
		
		try{
			return CommentList.parse(http_get(_MakeURL(url, params)));
		}catch(Exception e){
			if(e instanceof ApiException)
				throw (ApiException)e;
			throw ApiException.network(e);
		}
	}
	
	/**
	 * 发表评论
	 * @param url
	 * @param cmt
	 * @return
	 * @throws ApiException
	 */
	public static Result reply(String url, Comment cmt) throws ApiException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("post", 	cmt.getPost());
		params.put("name", 	cmt.getName());
		params.put("email", cmt.getEmail());
		params.put("url", 	cmt.getUrl());
		params.put("body", 	cmt.getBody());
		
		try{
			return http_post(url, params, null);		
		}catch(Exception e){
			if(e instanceof ApiException)
				throw (ApiException)e;
			throw ApiException.network(e);
		}
	}
	
	/**
	 * 删除评论
	 * @param url
	 * @param comment
	 * @return
	 * @throws ApiException
	 */
	public static Result deleteComment(String url, final int comment) throws ApiException {
		String newUrl = _MakeURL(url, new HashMap<String, Object>(){{
			put("comment", comment);
		}});
		try{
			return Result.parse(http_get(newUrl));			
		}catch(Exception e){
			if(e instanceof ApiException)
				throw (ApiException)e;
			throw ApiException.network(e);
		}
	}
	
	/**
	 * URL拼凑
	 * @param p_url
	 * @param params
	 * @return
	 */
	private static String _MakeURL(String p_url, Map<String, Object> params) {
		StringBuilder url = new StringBuilder(p_url);
		if(url.indexOf("?")<0)
			url.append('?');
		try {
			for(String name : params.keySet()){
				url.append('&');
				url.append(name);
				url.append('=');
				url.append(URLEncoder.encode(String.valueOf(params.get(name)), HTTP.UTF_8));
			}
		} catch (UnsupportedEncodingException e) {}
		return url.toString();
	}
	
	/**
	 * 请求URL
	 * @param url
	 * @param params
	 * @param files
	 * @throws ApiException 
	 */
	private static InputStream http_get(String url) throws ApiException, IOException {	
		HttpGet get = new HttpGet(url);
        HttpResponse response = client.execute(get);
        int code = response.getStatusLine().getStatusCode();
        if(code != 200)
        	throw ApiException.http(code);
        HttpEntity entity = response.getEntity();
        return entity.getContent();
        /*
        String body = EntityUtils.toString(entity);
        return new ByteArrayInputStream(body.getBytes());
        */
	}
	
	/**
	 * 请求URL
	 * @param url
	 * @param params
	 * @param files
	 * @throws ApiException 
	 * @throws IOException 
	 * @throws  
	 */
	private static Result http_post(String url, Map<String, Object> params, Map<String,File> files) throws ApiException, IOException {
        HttpPost post = new HttpPost(url);
        MultipartEntity entities = new MultipartEntity();
        if(params != null)
        for(String name : params.keySet()){
        	entities.addPart(name, _NewBody(String.valueOf(params.get(name))));
        }
        if(files != null)
        for(String file : files.keySet()){
        	entities.addPart(file, new FileBody(files.get(file)));
        }
        
        post.setEntity(entities);
        HttpResponse response = client.execute(post);
        int code = response.getStatusLine().getStatusCode();
        if(code != 200)
        	throw ApiException.http(code);
        HttpEntity entity = response.getEntity();
        return Result.parse(entity.getContent());  
	}
	
	private static StringBody _NewBody(String value) {
		try {
			return new StringBody(value, Charset.forName(HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}	
	
}
