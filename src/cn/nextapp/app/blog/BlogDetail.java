package cn.nextapp.app.blog;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import cn.nextapp.app.blog.api.ApiException;
import cn.nextapp.app.blog.common.AppManager;
import cn.nextapp.app.blog.common.PageFlipHelper;
import cn.nextapp.app.blog.common.UIHelper;
import cn.nextapp.app.blog.entity.Post;

/**
 * 显示文章
 * @author liux
 * @date 2011-12-19 下午9:34:55
 */
public class BlogDetail extends Activity{
	
    private Button				b_button_back;
    private Button				b_button_review;
    private ImageView			b_image_share;
    private	TextView			b_textview_showreview;
	private TextView 			b_textview_title;
	private TextView			b_textview_user;
	private TextView			b_textview_date;
	private TextView			b_textview_relate;
	private LinearLayout		b_scrollview_linearlayout;
	private WebView				b_webview_content;
    private Handler 			bHandler;
    private Post				blogDetail;
    private View 				blogView;//文章视图
    private boolean				fetching = false;
    private ProgressDialog 		progressBar;
    private ScrollView			b_scrollview;
    private PageFlipHelper		pageFlipper;//翻页助手
    private int					prevPostId;//上一篇文章id
    private int					nextPostId;//下一篇文章id
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//载入当前activity
		AppManager.getAppManager().addActivity(this);

		initBlogView();
		//初始化当前博文详情
		InitBlogDetail();
	}
	
	private void initBlogView() {
		
		setContentView(R.layout.blog_detail);
		pageFlipper = PageFlipHelper.getPageFlipper(this);
		pageFlipper.setLeftOnClickListener(prevClickListener);//设置点击上一篇
		pageFlipper.setRightOnClickListener(nextClickListener);//设置点击下一篇
		b_scrollview = (ScrollView)findViewById(R.id.blog_detail_scrollview);
		b_scrollview.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_MOVE:
				case MotionEvent.ACTION_DOWN:
					pageFlipper.showFloatView();			
					break;
				case MotionEvent.ACTION_UP:
					pageFlipper.hideFloatView();				
					break;
				}
				return b_scrollview.onTouchEvent(event);
			}
		});
		
		blogView = findViewById(R.id.blog_detail_scrollview_linearlayout);
		blogView.setVisibility(View.INVISIBLE);
		
		b_textview_relate = (TextView)findViewById(R.id.blog_detail_textview_relate);
		b_scrollview_linearlayout = (LinearLayout)findViewById(R.id.blog_detail_scrollview_linearlayout);
				
		b_button_back = (Button) findViewById(R.id.blog_detail_button_back);
		b_button_back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				UIHelper.showHome(BlogDetail.this);
			}
		});
		
		//发布评论
		b_button_review = (Button) findViewById(R.id.blog_detail_button_review);
		b_button_review.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent it = new Intent(v.getContext(),ReviewPublish.class);
				it.putExtra("id", blogDetail.getId());
				startActivity(it);
			}
		});
		
		b_image_share = (ImageView) findViewById(R.id.foot_default_image_share);
		b_image_share.setOnClickListener(shareClickListener);
		
		b_textview_title = (TextView) findViewById(R.id.blog_detail_title);
		b_textview_user = (TextView) findViewById(R.id.blog_detail_user);
		b_textview_date = (TextView) findViewById(R.id.blog_detail_date);
		b_webview_content = (WebView)findViewById(R.id.blog_detail_webview);
		b_webview_content.getSettings().setJavaScriptEnabled(false);
		b_webview_content.getSettings().setSupportZoom(true);
		b_webview_content.getSettings().setBuiltInZoomControls(true);
		b_webview_content.getSettings().setDefaultFontSize(14);
	}
	
	//获取相关的博文
	private void getRelativityBlogs(List<Post> list){
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		b_scrollview_linearlayout.removeAllViews();//先清空
		for (Post relPost : list) {
			View view = getLayoutInflater().inflate(R.layout.blog_relative, null);
			TextView tv = (TextView)view.findViewById(R.id.blog_detail_relative_blog_title);
			tv.setText(relPost.getTitle());
			tv.setTag(relPost);
			tv.setOnClickListener(new View.OnClickListener() {				
				public void onClick(View v) {
					Post rel = (Post)v.getTag();
					Intent it = new Intent(BlogDetail.this,BlogDetail.class);
					it.putExtra("id", rel.getId());
					it.putExtra("title", rel.getTitle());
					startActivity(it);
				}
			});
			b_scrollview_linearlayout.addView(view, params);
		}
	}
	//初始化当前博文详情
	private void InitBlogDetail()
	{
		int blog_id = getIntent().getIntExtra("id", 0);
		InitBlogDetail(blog_id);
	}
	private void InitBlogDetail(final int blog_id)
	{	
		this.progressBar = ProgressDialog.show(this, null, getString(R.string.loading));
		this.fetching = true;
		
		bHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg) {
				progressBar.dismiss();
				if(msg.what == 1)
				{
					final Post post = (Post)msg.obj;
					//设置上、下一篇文章id
					prevPostId = post.getPrevious();
					nextPostId = post.getNext();
					//设置上、下一篇的按钮是否显示
					if(prevPostId>0)
						pageFlipper.setLeftViewHide(false);
					else
						pageFlipper.setLeftViewHide(true);
					
					if(nextPostId>0)
						pageFlipper.setRightViewHide(false);
					else
						pageFlipper.setRightViewHide(true);
					
					b_textview_title.setText(post.getTitle());
					b_textview_user.setText(post.getAuthor());
					b_textview_date.setText(post.getPubDate());

					b_textview_showreview = (TextView) findViewById(R.id.foot_default_image_review);
					int commentCount = post.getCommentCount();
					b_textview_showreview.setText(String.valueOf(commentCount));
					b_textview_showreview.setOnClickListener(showReviewClickListener);	
					
					String body = post.getBody();
					body = body.replaceAll("(<img[^>]*?)\\s+width\\s*=\\s*\\S+","$1");
					body = body.replaceAll("(<img[^>]*?)\\s+height\\s*=\\s*\\S+","$1");
					if(!body.trim().startsWith("<style>")){
						String html = "<style>p {color:#333;} a {color:#3E62A6;} img {max-width:310px;} img.alignleft {float:left;max-width:120px;margin:0 10px 5px 0;border:1px solid #ccc;background:#fff;padding:2px;}</style>";
						body = html + body;
					}

					b_webview_content.loadDataWithBaseURL(null, body, "text/html", "utf-8",null);
					b_webview_content.setOnTouchListener(new View.OnTouchListener() {
						public boolean onTouch(View v, MotionEvent event) {
							switch (event.getAction()) {
							case MotionEvent.ACTION_DOWN:
								pageFlipper.showFloatView();			
								break;
							case MotionEvent.ACTION_UP:
								pageFlipper.hideFloatView();				
								break;
							}
							return b_webview_content.onTouchEvent(event);
						}
					});

					//获取相关的博文
					if(post.getRelativePosts().size() > 0)
						getRelativityBlogs(post.getRelativePosts());					
					else
						b_textview_relate.setVisibility(View.GONE);					

					blogView.setVisibility(View.VISIBLE);
					

					ImageView b_image_web = (ImageView) findViewById(R.id.foot_default_image_web);
					b_image_web.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							Intent intent = new Intent(Intent.ACTION_VIEW);  
							intent.setData(Uri.parse(post.getUrl()));  
							startActivity(intent);
						}
					});
					
				}
				else if(msg.what == 0){
					Toast.makeText(BlogDetail.this, R.string.msg_blog_is_null, Toast.LENGTH_SHORT).show();
				}
				else if(msg.obj != null){
					((ApiException)msg.obj).makeToast(BlogDetail.this);
				}

				BlogDetail.this.fetching = false;
			}
		};
		new Thread(){
			@Override
			public void run() {
                Message msg = new Message();
				try {
					blogDetail = ((NextAppContext)getApplication()).post(blog_id);
	                msg.what = (blogDetail!=null&&blogDetail.getId()>0) ? 1 : 0;
	                msg.obj = blogDetail;
	            } catch (ApiException e) {
	                e.printStackTrace();
	            	msg.what = -1;
	            	msg.obj = e;
	            }
                bHandler.sendMessage(msg);
			}
		}.start();
	}
	
	//上一篇
	private View.OnClickListener prevClickListener = new View.OnClickListener() {
		public void onClick(View v) {	
			if(prevPostId>0){
				initBlogView();
				InitBlogDetail(prevPostId);
			}
		}
	};
	//下一篇
	private View.OnClickListener nextClickListener = new View.OnClickListener() {
		public void onClick(View v) {		
			if(nextPostId>0){
				initBlogView();
				InitBlogDetail(nextPostId);
			}
		}
	};
	
	private View.OnClickListener shareClickListener = new View.OnClickListener() {
		public void onClick(View v) {		
			//分享到
			UIHelper.showShareDialog(BlogDetail.this, blogDetail.getTitle(), blogDetail.getUrl());
		}
	};
	
	private View.OnClickListener showReviewClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			if(blogDetail.getCommentCount() > 0)
				UIHelper.showBlogReview(BlogDetail.this, blogDetail.getId());
			else
				Toast.makeText(BlogDetail.this, R.string.no_review, Toast.LENGTH_SHORT).show();
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(this.fetching){
				this.progressBar.dismiss();
				this.fetching = false;
			}
			finish();
			return true;
		}
		return false;
	}


	/**
	 * 创建menu
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		//设置menu界面为res/menu/menu.xml
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
	
	/**
	 * 菜单被显示之前的事件
	 */
	public boolean onPrepareOptionsMenu(Menu menu) {
		//判断显示登录或登出
		UIHelper.showMenuLoginOrLogout(this, menu);
		return true;
	}

	/**
	 * 处理menu的事件
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		//菜单项选择处理
		UIHelper.menuItemSelectedAction(this,item);
		return true;
	}
	
	/**
	 * activity销毁事件
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		pageFlipper.destroy();
	}
	
}
