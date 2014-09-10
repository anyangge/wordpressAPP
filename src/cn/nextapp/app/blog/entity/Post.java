package cn.nextapp.app.blog.entity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;
import cn.nextapp.app.blog.api.ApiException;
import cn.nextapp.app.blog.common.StringUtils;
/**
 * 实体类：文章详情类
 * @author liux
 */
public class Post extends Entity {
	
	private final static SimpleDateFormat in_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final static SimpleDateFormat out_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	private String title;
	private String url;
	private String img;
	private String outline;
	private int commentCount;
	private String author;
	private String catalog;
	private Date pubDate;
	private String body;
	private String tags;
	private List<File> imageFiles;
	private List<Post> relativePosts = new ArrayList<Post>();
	private int previous;
	private int next;
	
	public int getPrevious() {
		return previous;
	}
	public void setPrevious(int previous) {
		this.previous = previous;
	}
	public int getNext() {
		return next;
	}
	public void setNext(int next) {
		this.next = next;
	}
	public List<File> getImageFiles() {
		return imageFiles;
	}
	public void setImageFiles(List<File> imageFiles) {
		this.imageFiles = imageFiles;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}
	public String getOutline() {
		return outline.trim() + "...";
	}
	public void setOutline(String outline) {
		this.outline = outline;
	}
	public String getDispCommentCount() {
		return (commentCount>=1000)?"99+":String.valueOf(commentCount);
	}
	public int getCommentCount() {
		return commentCount;
	}
	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getCatalog() {
		return catalog;
	}
	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}
	public String getPubDate() {
		return (pubDate!=null)?out_sdf.format(pubDate):"Unknown";
	}
	public void setPubDate(String pubDate) {
		try {
			this.pubDate = in_sdf.parse(pubDate);
		} catch (ParseException e) {}
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	public List<Post> getRelativePosts() {
		return relativePosts;
	}
	public void setRelativePosts(List<Post> relativePosts) {
		this.relativePosts = relativePosts;
	}

	public static Post parse(InputStream inputStream) throws IOException, ApiException {
        Post post = new Post();
        Post relPost = null;
        //获得XmlPullParser解析器
        XmlPullParser xmlParser = Xml.newPullParser();
        try {        	
            xmlParser.setInput(inputStream, UTF8);
            //获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
            int evtType=xmlParser.getEventType();
			//一直循环，直到文档结束    
			while(evtType!=XmlPullParser.END_DOCUMENT){ 
	    		String tag = xmlParser.getName(); 
	    		int depth = xmlParser.getDepth();
			    switch(evtType){ 
			    
			    	case XmlPullParser.START_TAG:
			    		
			            if(tag.equalsIgnoreCase("id"))
			            {			      
			            	if(depth == 3)
			            		post.id = StringUtils.toInt(xmlParser.nextText(),0);
			            	else if(relPost != null)
					            relPost.id = StringUtils.toInt(xmlParser.nextText(),0);
			            }
			            else if(tag.equalsIgnoreCase("title"))
			            {			            	
			            	if(depth == 3)
			            		post.setTitle(xmlParser.nextText());
			            	else if(relPost != null)
				            	relPost.setTitle(xmlParser.nextText());
			            }
			            else if(tag.equalsIgnoreCase("body"))
			            {			            	
			            	post.setBody(xmlParser.nextText());
			            }
			            else if(tag.equalsIgnoreCase("url"))
			            {			            	
			            	post.setUrl(xmlParser.nextText());
			            }
			            else if(tag.equalsIgnoreCase("img"))
			            {			            	
			            	post.setImg(xmlParser.nextText());
			            }
			            else if(tag.equalsIgnoreCase("outline"))
			            {			            	
			            	post.setOutline(xmlParser.nextText());			            	
			            }
			            else if(tag.equalsIgnoreCase("commentCount"))
			            {			            	
			            	post.setCommentCount(StringUtils.toInt(xmlParser.nextText(),0));			            	
			            }
			            else if(tag.equalsIgnoreCase("author"))
			            {			            	
			            	if(depth == 3)
			            		post.setAuthor(xmlParser.nextText());
			            	else if(relPost != null)
				            	relPost.setAuthor(xmlParser.nextText());			            	
			            }
			            else if(tag.equalsIgnoreCase("catalog"))
			            {			            	
			            	post.setCatalog(xmlParser.nextText());			            	
			            }
			            else if(tag.equalsIgnoreCase("pubDate"))
			            {			            	
			            	if(depth == 3)
			            		post.setPubDate(xmlParser.nextText());
			            	else if(relPost != null)
				            	relPost.setPubDate(xmlParser.nextText());			            	
			            }
			            else if(tag.equalsIgnoreCase("tags"))
			            {			            	
			            	post.setPubDate(xmlParser.nextText());			            	
			            }
			            else if(tag.equalsIgnoreCase("relativePost"))
			            {			            	
			            	relPost = new Post();			            	
			            }
			            else if(tag.equalsIgnoreCase("previous"))
			            {
			            	post.setPrevious(StringUtils.toInt(xmlParser.nextText(),0));
			            }
			            else if(tag.equalsIgnoreCase("next"))
			            {
			            	post.setNext(StringUtils.toInt(xmlParser.nextText(),0));
			            }
			    		break;

			    	case XmlPullParser.END_TAG:
					   	//如果遇到标签结束，则把对象添加进集合中
			    		if(tag.equalsIgnoreCase("relativePost") && relPost != null){
			    			post.getRelativePosts().add(relPost);
			    			relPost = null;
			    		}			    		
				       	break; 
			    }
			    //如果xml没有结束，则导航到下一个节点
			    evtType=xmlParser.next();
			}		

        } catch (XmlPullParserException e) {
			throw ApiException.xml(e);
        } finally {
        	inputStream.close();	
        }
        
        return post; 
        
	}
}
