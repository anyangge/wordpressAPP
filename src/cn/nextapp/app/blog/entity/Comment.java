package cn.nextapp.app.blog.entity;

import java.text.ParseException;
import java.util.Date;

/**
 * 实体类：评论详情类
 * @author liux
 *
 */
public class Comment extends Entity {

	private int post;
	private String name;
	private String title;
	private String email;
	private String url;
	private String body;
	private Date pubDate;
	
	public int getPost() {
		return post;
	}
	public void setPost(int post) {
		this.post = post;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getPubDate() {
		return (pubDate!=null)?SDF_OUT.format(pubDate):"Unknown";
	}
	public void setPubDate(String s_pubDate) {
		try {
			this.pubDate = SDF_IN.parse(s_pubDate);
		} catch (ParseException e) {
		}
	}
	
}