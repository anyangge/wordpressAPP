/**
 * 
 */
package cn.nextapp.app.blog.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import cn.nextapp.app.blog.api.ApiException;

import android.util.Xml;

/**
 * 接口URL定义
 * @author Winter Lau
 */
public class URLs implements Serializable {

	private String api_entry_url;
	
	private String catalogs;
	private String postList;
	private String postDetail;
	private String postPublish;
	private String postDelete;
	private String commentList;
	private String commentPublish;
	private String commentDelete;
	private String loginValidate;
	
	private URLs(){}
	public static URLs parse(InputStream in, String api_entry_url) throws IOException, ApiException {
		URLs url = null;
		// 获得XmlPullParser解析器
		XmlPullParser xmlParser = Xml.newPullParser();
		try {
			xmlParser.setInput(in, Entity.UTF8);
			// 获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
			int evtType = xmlParser.getEventType();
			// 一直循环，直到文档结束
			while (evtType != XmlPullParser.END_DOCUMENT) {
				String tag = xmlParser.getName();
				switch (evtType) {

				case XmlPullParser.START_TAG:
					// 如果是标签开始，则说明需要实例化对象了
					if (tag.equalsIgnoreCase("urls")) {
						url = new URLs();
						url.api_entry_url = api_entry_url;
					}
					if(url != null){
			            if(tag.equalsIgnoreCase("catalog-list"))
			            	url.catalogs = xmlParser.nextText();
			            else if(tag.equalsIgnoreCase("post-list"))
			            	url.postList = xmlParser.nextText();
			            else if(tag.equalsIgnoreCase("post-detail"))
			            	url.postDetail = xmlParser.nextText();
			            else if(tag.equalsIgnoreCase("post-pub"))
			            	url.postPublish = xmlParser.nextText();
			            else if(tag.equalsIgnoreCase("post-delete"))
			            	url.postDelete = xmlParser.nextText();
			            else if(tag.equalsIgnoreCase("comment-list"))
			            	url.commentList = xmlParser.nextText();
			            else if(tag.equalsIgnoreCase("comment-pub"))
			            	url.commentPublish = xmlParser.nextText();
			            else if(tag.equalsIgnoreCase("comment-delete"))
			            	url.commentDelete = xmlParser.nextText();
			            else if(tag.equalsIgnoreCase("login-validate"))
			            	url.loginValidate = xmlParser.nextText();
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				// 如果xml没有结束，则导航到下一个节点
				evtType = xmlParser.next();
			}
		} catch (XmlPullParserException e) {
			throw ApiException.xml(e);
		} finally {
			in.close();
		}
		return url;
	}

	/**
	 * 对URL进行格式处理
	 * @param path
	 * @return
	 */
	private String formatURL(String path) {
		if(path.startsWith("http://")||path.startsWith("https://"))
			return path;
		StringBuilder newUrl = new StringBuilder();
		try {
			URL url = new URL(api_entry_url);
			newUrl.append(url.getProtocol());
			newUrl.append("://");
			newUrl.append(url.getHost());
			int port = url.getPort();
			if(port != 80 && port>0){
				newUrl.append(':');
				newUrl.append(port);
			}
			if(path.charAt(0) != '/')
				newUrl.append('/');
			newUrl.append(path);			
		} catch (MalformedURLException e) {
		}
		return newUrl.toString();
	}
	
	public String getCatalogs() { return formatURL(catalogs); }
	public String getPostList() { return formatURL(postList); }
	public String getPostDetail() { return formatURL(postDetail); }
	public String getPostPublish() { return formatURL(postPublish); }
	public String getPostDelete() { return formatURL(postDelete); }
	public String getCommentList() { return formatURL(commentList); }
	public String getCommentPublish() { return formatURL(commentPublish); }
	public String getCommentDelete() { return formatURL(commentDelete); }
	public String getLoginValidate() { return formatURL(loginValidate); }
}
