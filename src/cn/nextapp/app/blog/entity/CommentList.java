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
 * 评论列表
 * @author liux
 */
public class CommentList {
	
	private int commentCount;
	private List<Comment> Comments = new ArrayList<Comment>();

	public static CommentList parse(InputStream inputStream) throws IOException, ApiException {
		CommentList commentList = new CommentList();
		Comment com = null;
        //获得XmlPullParser解析器
        XmlPullParser xmlParser = Xml.newPullParser();
        try {         	
            xmlParser.setInput(inputStream, "utf-8");
            //获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
            int evtType=xmlParser.getEventType();
			//一直循环，直到文档结束    
			while(evtType!=XmlPullParser.END_DOCUMENT){ 
	    		String tag = xmlParser.getName(); 
			    switch(evtType){ 
			    
			    	case XmlPullParser.START_TAG:
			    		
			    		if(com!=null)
			    		{
				            if(tag.equalsIgnoreCase("id"))
				            {
				            	com.id = StringUtils.toInt(xmlParser.nextText(),0);
				            }
				            else if(tag.equalsIgnoreCase("post"))
				            {
				            	com.setPost(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("name"))
				            {
				            	com.setName(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("title"))
				            {
				            	com.setTitle(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("email"))
				            {
				            	com.setEmail(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("url"))
				            {
				            	com.setUrl(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("body"))
				            {
				            	com.setBody(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("pubDate"))
				            {
				            	com.setPubDate(xmlParser.nextText());
				            }
			    		}
			    		else if (tag.equalsIgnoreCase("comment")) 
			    		{ 
				        	com = new Comment(); 
			    		}
			    		else if(tag.equalsIgnoreCase("commentCount")) 
			    		{
			    			commentList.commentCount = StringUtils.toInt(xmlParser.nextText(),0);
			    		}
			    		 
			    		break;

			    	case XmlPullParser.END_TAG:
					   	//如果遇到标签结束，则把对象添加进集合中
				       	if (tag.equalsIgnoreCase("comment") && com != null) { 
				       		commentList.getComments().add(com); 
				       		com = null; 
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
        
        return commentList; 
        
	}
	
	public int getCommentCount() {
		return commentCount;
	}
	public List<Comment> getComments() {
		return Comments;
	}
}
