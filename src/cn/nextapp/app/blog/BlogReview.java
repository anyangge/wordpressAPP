package cn.nextapp.app.blog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import cn.nextapp.app.blog.api.ApiException;
import cn.nextapp.app.blog.api.NextAppClient;
import cn.nextapp.app.blog.common.UIHelper;
import cn.nextapp.app.blog.entity.Comment;
import cn.nextapp.app.blog.entity.CommentList;
import cn.nextapp.app.blog.entity.Result;

/**
 * 显示文章的评论列表
 * @author liux
 * @date 2011-12-19 下午9:45:06
 */
public class BlogReview extends Activity{

	private final int				LISTVIEW_LOAD = 1;
    private final static int DEFAULT_PAGE_SIZE = 10;//分页大小
	
	private TextView 				br_textview_blogTitle;
	private ListView 				br_listview_review;
    private Button					br_button_back;
    private Button					br_button_review;
    private Handler 				brHandler;
    private SimpleAdapter 			brAdapter;
    private List<Map<String, Object>> brDatalist = new ArrayList<Map<String, Object>>();
    private ProgressBar				br_listview_foot_img_loading;
    private TextView 				br_listview_foot_more;
    private int						LastCommentId;//滑动到最后一条数据的id
    private int						post_id;
    private Comment					comDetail = new Comment();//评论
    private View					footerView;//listview的底部刷新视图
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blog_review);
		
		Intent it = getIntent();
		post_id = it.getIntExtra("id", 0);
		
		br_button_back = (Button) findViewById(R.id.blog_review_button_back);
		br_button_back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		
		br_button_review = (Button) findViewById(R.id.blog_review_button_review);
		br_button_review.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				UIHelper.showReviewPublish(v.getContext(), post_id);
			}
		});
				
		br_textview_blogTitle = (TextView)findViewById(R.id.blog_review_textview_blogTitle);
		br_textview_blogTitle.setText(it.getStringExtra("blogtitle"));
		
		//初始化listview
		InitListViewBlogReview();
	}
	
	//初始化listview
	private void InitListViewBlogReview(){
		br_listview_review = (ListView) findViewById(R.id.blog_review_listview);

        brAdapter = new SimpleAdapter(
        	  this,brDatalist,
        	  R.layout.blog_review_listview,  
              new String[]{"index","user","date","content"},
              new int[]{R.id.blog_review_listview_index,R.id.blog_review_listview_user,R.id.blog_review_listview_date,R.id.blog_review_listview_content}
        );  
        //添加head 或 foot项 要在setAdapter前
        footerView = getLayoutInflater().inflate(R.layout.blog_review_listview_foot, null);
        if(brDatalist.size() > 5 && br_listview_review.getFooterViewsCount() == 0)
        {
        	br_listview_review.addFooterView(footerView);
        }
        
        br_listview_review.setAdapter(brAdapter);
        
        br_listview_review.setOnScrollListener(new OnScrollListener() {		
			public void onScrollStateChanged(AbsListView arg0, int arg1) {
				//判断是否有底部刷新栏
				if(br_listview_review.getFooterViewsCount() > 0)
				{
					String str1 = getResources().getString(R.string.main_listview_foot_more);
					String str2 = br_listview_foot_more.getText().toString();
					if(br_listview_foot_img_loading.getVisibility()!=ProgressBar.GONE || !str1.equals(str2))
						return;
					//判断滚动到底部 
					if (arg0.getLastVisiblePosition()==(arg0.getCount()-1) && 
							(arg1 == OnScrollListener.SCROLL_STATE_IDLE || arg1 == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)){
						//获取当前滚动最后一项的id
						Map<String,Object> item = brDatalist.get(brDatalist.size()-1);
		        		Comment com = (Comment)item.get("Entity");
		        		LastCommentId = com.getId();
						//加载下一页数据
						BlogReview.this.getListViewScrollData();
	                }
				}
			}			
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {

			}
			
		});
        br_listview_review.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
        	public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
        		//获取选中的item的参数
				if(arg2 < 0 || arg2 >= brDatalist.size())
					return false;
				
				final int index = arg2;
				Map<String,Object> item = brDatalist.get(index);
				comDetail  = (Comment)item.get("Entity");
        		
				AlertDialog.Builder builder = new AlertDialog.Builder(BlogReview.this);
				builder.setIcon(android.R.drawable.btn_star);
				builder.setTitle(getString(R.string.blog_review_operate));
				builder.setItems(R.array.blog_review_optItems,new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface arg0, int arg1) {
						switch (arg1) {
							case 0:	
								BlogReview.this.deleteItem(index);
								break;
						}
					}
				});
				builder.create().show();
        		return true;
        	}
		});
        
		br_listview_foot_img_loading = (ProgressBar)findViewById(R.id.blog_review_listview_foot_img_loading);
		br_listview_foot_more = (TextView)findViewById(R.id.blog_review_listview_foot_more);
        
		//获取初始数据
		this.getListViewData(LISTVIEW_LOAD);
	}
	//获取listview滚动数据
	private void getListViewScrollData(){		
		br_listview_foot_img_loading.setVisibility(ProgressBar.VISIBLE);
		brHandler = new Handler(){
			public void handleMessage(Message msg) {          
				if(msg.what < 0){
					((ApiException)msg.obj).makeToast(BlogReview.this);
					return ;
				}
				CommentList commentlist = (CommentList)msg.obj;
				if(msg.what > 0 )
				{
					//判断是否读取到网络数据最后一条
					if(commentlist.getCommentCount() > brDatalist.size())
					{
						int i = brDatalist.size()+1;
			            for (Comment comment : commentlist.getComments()) {
			            	Map<String, Object> map = new HashMap<String, Object>();
			    			map.put("img", R.drawable.icon);
			    			map.put("index", i+getString(R.string.blog_review_floor));
			    			map.put("user", comment.getName());
			    			map.put("date", comment.getPubDate());
			    			map.put("content", comment.getBody());
			    			map.put("Entity", comment);//把实体本身做参数
		        			brDatalist.add(map);
		        			i++;
						}
			            brAdapter.notifyDataSetChanged();  
			            if(brDatalist.size() >= commentlist.getCommentCount())
			            	br_listview_foot_more.setText(R.string.main_listview_foot_end);
		            	else
		            		br_listview_foot_more.setText(R.string.main_listview_foot_more);
					}            
					else
					{
		            	br_listview_foot_more.setText(R.string.main_listview_foot_end);
		            }
				}
				else if(commentlist != null && commentlist.getCommentCount() == brDatalist.size())
				{
					br_listview_foot_more.setText(R.string.main_listview_foot_end);
				}

				br_listview_foot_img_loading.setVisibility(ProgressBar.GONE);
			}
		};
        new Thread(){
        	public void run() {
        		Message msg =new Message();
				try{ 
					//查询
					String url = ((NextAppContext)getApplication()).getUrls().getCommentList();
					CommentList commentlist = NextAppClient.comments(url, post_id,LastCommentId,DEFAULT_PAGE_SIZE, NextAppClient.ASC);					
					msg.what = commentlist.getComments().size() ;//成功获取几条数据
					msg.obj = commentlist;
				}catch (ApiException e){
			    	e.printStackTrace();
			    	msg.what = -1;
			    	msg.obj = e;
			    }
				brHandler.sendMessage(msg);
			}
        }.start();
	}
	//获取listview初始化数据
	private void getListViewData(final int status) {	
		brHandler = new Handler(){
			public void handleMessage(Message msg) {
				if(msg.what < 0){
					((ApiException)msg.obj).makeToast(BlogReview.this);
					return ;
				}
				CommentList commentList = (CommentList)msg.obj;
				if(msg.what > 0)
				{
					brDatalist.clear();//清空之前的数据
					int i = 1;
		            for (Comment comment : commentList.getComments()) {
		            	Map<String, Object> map = new HashMap<String, Object>();
		    			map.put("img", R.drawable.icon);
		    			map.put("index", i+getString(R.string.blog_review_floor));
		    			map.put("user", comment.getName());
		    			map.put("date", comment.getPubDate());
		    			map.put("content", comment.getBody());
		    			map.put("Entity", comment);//把实体本身做参数
	        			brDatalist.add(map);
	        			i++;
					}
		            brAdapter.notifyDataSetChanged();
		            br_listview_review.setSelection(0);//加载数据完成,设置当前显示为第一条
		            
		            if(brDatalist.size() < 5)
		            {
		            	br_listview_review.removeFooterView(footerView);
		            }
		            else
		            {
		            	if(br_listview_review.getFooterViewsCount() == 0)
		            		br_listview_review.addFooterView(footerView);

		            	if(br_listview_foot_more == null)
		            		br_listview_foot_more = (TextView)findViewById(R.id.blog_review_listview_foot_more);
		            	if(br_listview_foot_img_loading ==null )
		            		br_listview_foot_img_loading = (ProgressBar)findViewById(R.id.blog_review_listview_foot_img_loading);
		            	
		            	if(brDatalist.size() >= commentList.getCommentCount())
		            		br_listview_foot_more.setText(R.string.main_listview_foot_end);
	            		else
	            			br_listview_foot_more.setText(R.string.main_listview_foot_more);
		            }
	            	
				}
				else if(commentList != null && commentList.getCommentCount() == 0)
				{
					br_listview_review.removeFooterView(footerView);
					Toast.makeText(BlogReview.this, getString(R.string.no_review), Toast.LENGTH_SHORT).show();
				}

			}
		};
		new Thread(){
        	public void run() {
        		Message msg =new Message();
				try{ 
					//查询
					String url = ((NextAppContext)getApplication()).getUrls().getCommentList();
					CommentList commentList = NextAppClient.comments(url, post_id,0,DEFAULT_PAGE_SIZE, NextAppClient.ASC);										
					msg.what = commentList.getComments().size() ;//成功获取几条数据
					msg.obj = commentList;
				}catch (ApiException e){					
			    	e.printStackTrace();
			    	msg.what = -1;
			    	msg.obj = e;
			    }
				brHandler.sendMessage(msg);
			}
        }.start();
	}

	private void deleteItem(final int index){
		int size = brDatalist.size();
		if( size == 0 ){
			return;
		}
		final Handler handler = new Handler()
		{
			@Override
			public void handleMessage(Message msg) {
				if(msg.what == 1){
					Result res = (Result)msg.obj;
					if(res.getErrorCode()==1) {
						//删除listview当前项
						brDatalist.remove(index);
						brAdapter.notifyDataSetChanged();
						Toast.makeText(BlogReview.this, getString(R.string.blog_review_del_success), Toast.LENGTH_SHORT).show();
					}
					else {
						Toast.makeText(BlogReview.this, getString(R.string.blog_review_del_fail)+res.getErrorMessage(), Toast.LENGTH_SHORT).show();
					}
				}
				else {
					((ApiException)msg.obj).makeToast(BlogReview.this);
				}
			}
		};
		new Thread(){
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String url = ((NextAppContext)getApplication()).getUrls().getCommentDelete();
					Result res = NextAppClient.deleteComment(url, comDetail.getId());
					//是否删除成功
					msg.what = 1;
					msg.obj = res;
	            } catch (ApiException e) {
	            	msg.what = -1;
	            	msg.obj = e;
	            }
                handler.sendMessage(msg);
			}
		}.start();
	}
}
