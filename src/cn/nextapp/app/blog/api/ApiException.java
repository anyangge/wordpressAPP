/**
 * 
 */
package cn.nextapp.app.blog.api;

import java.net.SocketException;

import android.content.Context;
import android.widget.Toast;
import cn.nextapp.app.blog.R;

/**
 * 接口异常
 * @author Winter Lau
 */
public class ApiException extends Exception {

	public final static byte TYPE_NETWORK 	= 0x01;
	public final static byte TYPE_SOCKET	= 0x02;
	public final static byte TYPE_HTTP 		= 0x03;
	public final static byte TYPE_XML	 	= 0x04;
	private byte type;
	private int code;
	
	/**
	 * 提示友好的错误信息
	 * @param ctx
	 */
	public void makeToast(Context ctx){
		switch(this.getType()){
		case TYPE_HTTP:
			String err = ctx.getString(R.string.http_status_code_error, this.getCode());
			Toast.makeText(ctx, err, Toast.LENGTH_LONG).show();
			break;
		case TYPE_NETWORK:
			Toast.makeText(ctx, R.string.network_not_connected, Toast.LENGTH_LONG).show();
			break;
		case TYPE_XML:
			Toast.makeText(ctx, R.string.xml_parser_failed, Toast.LENGTH_LONG).show();
			break;
		}
	}
	
	public static ApiException http(int code) {
		return new ApiException(TYPE_HTTP, code, null);
	}

	public static ApiException xml(Exception e) {
		return new ApiException(TYPE_XML, 0, e);
	}
	
	public static ApiException network(Exception e) {
		if(e instanceof SocketException){
			return new ApiException(TYPE_SOCKET, 0 ,e);
		}
		return new ApiException(TYPE_NETWORK, 0, e);
	}
	
	private ApiException(byte type, int code, Exception excp) {
		super(excp);
		this.type = type;
		this.code = code;
	}
	public int getCode(){
		return this.code;
	}
	public int getType(){
		return this.type;
	}
	
}
