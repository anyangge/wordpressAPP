package cn.nextapp.app.blog.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;
import cn.nextapp.app.blog.api.ApiException;
import cn.nextapp.app.blog.common.StringUtils;

/**
 * 实体类：文章分类
 * @author liux
 */
public class Catalog extends Entity {

	public final static String NODE_CATALOGS = "catalogs";
	public final static String NODE_CATALOG = "catalog";
	public final static String NODE_ID = "id";
	public final static String NODE_NAME = "name";
	public final static String NODE_DESC = "description";
	public final static String NODE_PC = "postCount";
	
	private String name;
	private String description;
	private int postCount;
	
	public Catalog(){}
	
	public static Catalog newInstance(int id,String name,String description){
		Catalog catalog = new Catalog();
		catalog.id = id;
		catalog.name = name;
		catalog.description = description;
		return catalog;
	}
	
	/**
	 * 解析分类列表
	 * @param stream
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException 
	 * @throws ApiException 
	 */
	public static List<Catalog> parse(InputStream stream) throws IOException, ApiException {
        List<Catalog> catalogs=new ArrayList<Catalog>();
        Catalog catalog=null;   
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
			    		//如果是catalog标签开始，则说明需要实例化对象了
			    		if (tag.equalsIgnoreCase(NODE_CATALOG)) { 
				        	catalog = new Catalog(); 
				        	//取出catalog标签中的一些属性值
				        	catalog.id = StringUtils.toInt(xmlParser.getAttributeValue(null, NODE_ID),0);
				        	catalog.name = xmlParser.getAttributeValue(null, NODE_NAME);
				        	catalog.description = xmlParser.getAttributeValue(null, NODE_DESC);
				        	catalog.postCount = StringUtils.toInt(xmlParser.getAttributeValue(null, NODE_PC), 0);
			    		}
			    		break;
				        
			    	case XmlPullParser.END_TAG:
					   	//如果遇到catalog标签结束，则把catalog对象添加进集合中
				       	if (tag.equalsIgnoreCase(NODE_CATALOG) && catalog != null) { 
				       		catalogs.add(catalog);
				       	}
				       	break; 
			    }
			    //如果xml没有结束，则导航到下一个catalog节点
			    evtType=xmlParser.next();
			}
		} catch (XmlPullParserException e) {
			throw ApiException.xml(e);
        }finally {
        	stream.close();
        }
        
        return catalogs; 
        
	}	
	
	//使用XML格式将分类列表保存到XML
    public static void save(List<Catalog> list, OutputStream stream) throws IOException{   
        XmlSerializer serializer = Xml.newSerializer();   
        serializer.setOutput(stream, UTF8);   
        serializer.startDocument(UTF8, true);   
        serializer.startTag(null, NODE_ROOT);
        serializer.startTag(null, NODE_CATALOGS);   
        for(Catalog catalog : list){   
            serializer.startTag(null, NODE_CATALOG);   
            serializer.attribute(null, NODE_ID, String.valueOf(catalog.getId()));  
            serializer.attribute(null, NODE_NAME, String.valueOf(catalog.getName()));  
            serializer.attribute(null, NODE_DESC, String.valueOf(catalog.getDescription()));  
            serializer.endTag(null, NODE_CATALOG);   
        }   
        serializer.endTag(null, NODE_CATALOGS);   
        serializer.endTag(null, NODE_ROOT);   
        serializer.endDocument();   
        stream.flush();
    }
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public int getPostCount() {
		return postCount;
	}

	@Override
	public String toString() {
		return this.name;
	}
}
