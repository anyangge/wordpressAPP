package cn.nextapp.app.blog;

import java.util.Properties;

import cn.nextapp.app.blog.api.ApiException;
import cn.nextapp.app.blog.api.NextAppClient;
import cn.nextapp.app.blog.common.StringUtils;
import cn.nextapp.app.blog.common.UIHelper;
import cn.nextapp.app.blog.entity.Comment;
import cn.nextapp.app.blog.entity.Result;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 发表评论
 * @author Winter Lau
 */
public class ReviewPublish extends Activity{

	private Button				pr_btn_back;	//返回按钮
	private Button				pr_btn_submit;	//发表评论按钮
	private EditText			pr_edit_user;	//名称输入框
	private EditText			pr_edit_email;	//电子邮件输入框
	private EditText			pr_edit_url;	//网站输入框
	private EditText			pr_edit_comment;//评论输入框
	private int					BlogId;			//评论文章的id
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.review_publish);
		
		pr_edit_user = (EditText)findViewById(R.id.post_review_user);
		pr_edit_email = (EditText)findViewById(R.id.post_review_email);
		pr_edit_url = (EditText)findViewById(R.id.post_review_url);
		
		NextAppContext app = ((NextAppContext)getApplication());
		Properties props = app.getProperties();
		pr_edit_user.setText(props.getProperty("reply_name"));
		pr_edit_email.setText(props.getProperty("reply_email"));
		pr_edit_url.setText(props.getProperty("reply_url"));
		
		pr_edit_comment = (EditText)findViewById(R.id.post_review_comment);
		
		pr_btn_back = (Button)findViewById(R.id.review_publish_button_back);
		pr_btn_back.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View arg0) {
				finish();
			}
		});
		
		pr_btn_submit = (Button)findViewById(R.id.review_publish_button_publish);
		pr_btn_submit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				reviewPublish();
			}
		});
		
		Intent it = getIntent();
		BlogId = it.getIntExtra("id", 0);
	}
	
	private void reviewPublish()
	{
		//检查输入是否满足要求
		final String name = pr_edit_user.getText().toString().trim();
		if(StringUtils.isEmpty(name)){
			Toast.makeText(ReviewPublish.this, R.string.msg_name_empty, Toast.LENGTH_SHORT).show();
			return ;
		}
		final String email = pr_edit_email.getText().toString().trim();
		if(StringUtils.isEmpty(email) || !StringUtils.isEmail(email)){
			Toast.makeText(ReviewPublish.this, R.string.msg_email_illegal, Toast.LENGTH_SHORT).show();
			return ;
		}
		final String url = pr_edit_url.getText().toString();
		String text = pr_edit_comment.getText().toString().trim();
		if(StringUtils.isEmpty(text)){
			Toast.makeText(ReviewPublish.this, R.string.msg_review_empty, Toast.LENGTH_SHORT).show();
			return ;
		}
		
		NextAppContext app = ((NextAppContext)getApplication());
		app.setProperties(new Properties(){{
			setProperty("reply_name", name);
			setProperty("reply_email", email);
			setProperty("reply_url", url);
		}});
		
		final Handler handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if(msg.what > 0){
					Result res = (Result)msg.obj;
					if(res.OK()){
						String err_msg = res.getErrorMessage();
						if(StringUtils.isEmpty(err_msg))
							err_msg = getString(R.string.review_publish_success);
						else
							err_msg = getString(R.string.review_publish_successInfo)+err_msg;
						Toast.makeText(ReviewPublish.this, err_msg, Toast.LENGTH_LONG).show();
						//跳转到文章详情
		        		UIHelper.showBlogDetail(ReviewPublish.this, BlogId);		        		
						finish();
					}
					else
						Toast.makeText(ReviewPublish.this, getString(R.string.review_publish_failInfo)+res.getErrorMessage(), Toast.LENGTH_LONG).show();					
				}
				else
					((ApiException)msg.obj).makeToast(ReviewPublish.this);
			}
		};

		final Comment com = new Comment();
		com.setPost(BlogId);
		com.setName(name);
		com.setBody(text);
		com.setEmail(email);
		com.setUrl(url);
		
		new Thread(){
        	public void run() {
				Message msg =new Message();
				try{ 
					String url = ((NextAppContext)getApplication()).getUrls().getCommentPublish();
					Result result = NextAppClient.reply(url, com);	
					msg.what = 1;
					msg.obj = result;
				}catch (ApiException e){
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
			    }		
				//发送消息
				handler.sendMessage(msg);
			}
        }.start();
	}
	
}
