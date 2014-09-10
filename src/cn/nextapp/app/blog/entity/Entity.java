/**
 * 
 */
package cn.nextapp.app.blog.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * 实体类的基类
 * @author Winter Lau
 */
public abstract class Entity implements Serializable {

	public final static SimpleDateFormat SDF_IN = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public final static SimpleDateFormat SDF_OUT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	public final static String UTF8 = "UTF-8";
	public final static String NODE_ROOT = "nextapp";
	
	protected int id;

	public int getId() {
		return id;
	}

}
