package cn.nextapp.app.blog.entity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;
import cn.nextapp.app.blog.api.ApiException;
import cn.nextapp.app.blog.common.StringUtils;

/**
 * 文章列表
 * @author liux
 */
public class PostList extends Entity {
	
	private int catalog;
	private int postCount;
	private List<Post> posts = new ArrayList<Post>();
	
	public static PostList parse(InputStream stream) throws IOException, ApiException {
		PostList postlist=new PostList();
        Post post=null; 
        //获得XmlPullParser解析器
        XmlPullParser xmlParser = Xml.newPullParser();
        try {  
            xmlParser.setInput(stream, UTF8);
            //获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
            int evtType=xmlParser.getEventType();
			//一直循环，直到文档结束    
			while(evtType!=XmlPullParser.END_DOCUMENT){ 
	    		String tag = xmlParser.getName(); 
			    switch(evtType){ 
			    
			    	case XmlPullParser.START_TAG:
			    		
			    		if(tag.equalsIgnoreCase("catalog")) 
			    		{
			    			postlist.catalog = StringUtils.toInt(xmlParser.nextText(),0);
			    		}
			    		else if(tag.equalsIgnoreCase("postCount")) 
			    		{
			    			postlist.postCount = StringUtils.toInt(xmlParser.nextText(),0);
			    		}
			    		else if (tag.equalsIgnoreCase("post")) 
			    		{ 
				        	post = new Post(); 
			    		}
			    		else if(post!=null)
			    		{
				            if(tag.equalsIgnoreCase("id")){
				            	post.id = StringUtils.toInt(xmlParser.nextText(),0);
				            }
				            else if(tag.equalsIgnoreCase("title")){
				            	post.setTitle(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("url")){
				            	post.setUrl(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("img")){
				            	post.setImg(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("outline")){
				            	post.setOutline(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("commentCount")){
				            	post.setCommentCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("author")){
				            	post.setAuthor(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("catalog")){
				            	post.setCatalog(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("pubDate")){
				            	post.setPubDate(xmlParser.nextText());
				            }
			    		}
			    		break;

			    	case XmlPullParser.END_TAG:
					   	//如果遇到标签结束，则把对象添加进集合中
				       	if (tag.equalsIgnoreCase("post") && post != null) { 
				    	   postlist.getPosts().add(post); 
				           post = null; 
				       	}
				       	break; 
			    }
			    //如果xml没有结束，则导航到下一个节点
			    evtType=xmlParser.next();
			}		

		} catch (XmlPullParserException e) {
			throw ApiException.xml(e);
        } finally {
			stream.close();	
        }
        
        return postlist; 
        
	}
	
	public int getCatalog() {
		return catalog;
	}
	public int getPostCount() {
		return postCount;
	}
	public List<Post> getPosts() {
		return posts;
	}
}
