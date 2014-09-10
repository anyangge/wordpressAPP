package cn.nextapp.app.blog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
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
import cn.nextapp.app.widget.PullToRefreshListView;
import cn.nextapp.app.widget.ScrollLayout;

/**
 * 显示所有的评论列表
 * @author liux
 * @date 2011-12-19 下午9:45:55
 */
public class ReviewListUI{

    private final static int DEFAULT_PAGE_SIZE = 10;//分页大小
    
	private Button						rl_btn_back;
	//private ListView					rl_listview;
	private PullToRefreshListView		rl_listview;
    private Handler 					rlHandler;
    private SimpleAdapter 				rlAdapter;
    private List<Map<String, Object>> 	rlDatalist = new ArrayList<Map<String, Object>>();
    private ProgressBar					rl_listview_foot_img_loading;
    private TextView 					rl_listview_foot_more;
    private int							LastCommentId;//滑动到最后一条数据的id
    private Comment						comDetail = new Comment();//评论
    private boolean 					isEnd = false;//第一次加载数据是否到底
    private View						footerView;//listview的底部刷新视图
    private Activity activity;
    private ScrollLayout scrolllayout;
	
	public static ReviewListUI onCreate(final Activity activity,ScrollLayout scrolllayout) {
		final ReviewListUI ui=new ReviewListUI();
		ui.activity = activity;
		ui.scrolllayout = scrolllayout;
		
		ui.rl_btn_back = (Button)activity.findViewById(R.id.review_list_button_back);
		ui.rl_btn_back.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View arg0) {
				ui.scrolllayout.setToScreen(0);
			}
		});
		
		//初始化listview
		try {
			ui.InitListViewReview();
		} catch (ApiException e) {
			e.makeToast(activity);
		}

		return ui;
	}

	/**
	 * 初始化listview
	 * @throws ApiException
	 */
	private void InitListViewReview() throws ApiException{
        rl_listview = (PullToRefreshListView) activity.findViewById(R.id.review_listview);

        rlDatalist = getListViewData();
        
        rlAdapter = new SimpleAdapter(
        		activity,rlDatalist,
        	  R.layout.review_listview,  
              new String[]{"blogtitle","user","date","content"},
              new int[]{R.id.review_listview_blogtitle,R.id.review_listview_user,R.id.review_listview_date,R.id.review_listview_content}
        );  
        
        //添加head 或 foot项 要在setAdapter前
        footerView = activity.getLayoutInflater().inflate(R.layout.review_listview_foot, null);
        if(rlDatalist.size() > 5 && rl_listview.getFooterViewsCount() == 0){
        	rl_listview.addFooterView(footerView);
        }
        
        rl_listview.setAdapter(rlAdapter);
        
        rl_listview.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
        		//判断点击是否为头部
        		if(rl_listview.getHeaderViewsCount() > 0 && arg2 == 0)return;
        		//判断点击是否为底部
        		if(rl_listview.getFooterViewsCount() > 0 && arg2 == (arg0.getCount()-1))return;
        		
				Map<String,Object> item = rlDatalist.get(arg2-1);
				Comment com  = (Comment)item.get("Entity");
				//跳转到文章详情
        		UIHelper.showBlogDetail(arg0.getContext(), com.getPost());
        	}
		});
        rl_listview.setOnScrollListener(new OnScrollListener() {		
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				rl_listview.onScrollStateChanged(view, scrollState);
				//判断是否有底部刷新栏
				if(rl_listview.getFooterViewsCount() > 0)
				{
					String str1 = activity.getResources().getString(R.string.main_listview_foot_more);
					String str2 = rl_listview_foot_more.getText().toString();
					if(rl_listview_foot_img_loading.getVisibility()!=ProgressBar.GONE || !str1.equals(str2))
						return;
					//判断滚动到底部
					if (view.getLastVisiblePosition()==(view.getCount()-1) && 
							(scrollState == OnScrollListener.SCROLL_STATE_IDLE || scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)){
						//获取当前滚动最后一项的id
						Map<String,Object> item = rlDatalist.get(rlDatalist.size()-1);
		        		Comment com = (Comment)item.get("Entity");
		        		LastCommentId = com.getId();
						//加载下一页数据
						getListViewScrollData();
	                }
				}
			}			
			public void onScroll(AbsListView view, int firstVisiableItem, int visibleItemCount, int totalItemCount) { 
				rl_listview.onScroll(view, firstVisiableItem, visibleItemCount, totalItemCount);
			}
		});
        rl_listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
        	public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
        		//获取选中的item的参数
				if(arg2 < 0 || arg2 >= rlDatalist.size())
					return false;
				
				final int index = arg2-1;
				Map<String,Object> item = rlDatalist.get(index);
				comDetail  = (Comment)item.get("Entity");
				
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setIcon(android.R.drawable.btn_star);
				builder.setTitle(activity.getString(R.string.blog_review_operate));				
				builder.setItems(R.array.blog_review_optItems,new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface arg0, int arg1) {
						switch (arg1) {
							case 0:	
								deleteItem(index);
								break;
						}
						
					}
				});
				builder.create().show();
        		return true;
        	}
		});
        rl_listview.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {
            	refreshListView();
            }
        });
        
		rl_listview_foot_img_loading = (ProgressBar) activity.findViewById(R.id.review_listview_foot_img_loading);
		rl_listview_foot_more = (TextView)activity.findViewById(R.id.review_listview_foot_more);
        
		if(isEnd && rl_listview.getFooterViewsCount()>0){
			rl_listview_foot_more.setText(activity.getString(R.string.main_listview_foot_end));
		}
		
	}
	
	//执行刷新的操作
	public void refreshListView(){
		rlHandler = new Handler(){
			public void handleMessage(Message msg) {
				if(msg.what < 0){
					((ApiException)msg.obj).makeToast(activity);
					return ;
				}
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> datalist = (List<Map<String, Object>>)msg.obj;
				if(msg.what > 0)
				{
					rlDatalist.clear();//清空之前的数据
					rlDatalist.addAll(datalist);

		            if(rlDatalist.size() < 5)
		            {
		            	rl_listview.removeFooterView(footerView);
		            }
		            else
		            {
		            	if(rl_listview.getFooterViewsCount() == 0)
		            		rl_listview.addFooterView(footerView);
		            	
		            	if(rl_listview_foot_more == null)
		            		rl_listview_foot_more = (TextView)activity.findViewById(R.id.review_listview_foot_more);
		            	if(rl_listview_foot_img_loading ==null)
		            		rl_listview_foot_img_loading = (ProgressBar) activity.findViewById(R.id.review_listview_foot_img_loading);
		            	if(isEnd)
		            		rl_listview_foot_more.setText(R.string.main_listview_foot_end);
	            		else
	            			rl_listview_foot_more.setText(R.string.main_listview_foot_more);
		            }
		            
		            rlAdapter.notifyDataSetChanged();
		            //加载数据完成,设置当前显示为第一条 #有头部为1 #没有则0
		            rl_listview.setSelection(1);
		            
				}
				else if(msg.what == 0)
				{
					rl_listview.removeFooterView(footerView);
					Toast.makeText(activity, activity.getString(R.string.no_review), Toast.LENGTH_SHORT).show();
				}
				
				if(rl_listview.getFooterViewsCount()>0 && rl_listview_foot_img_loading!=null)
					rl_listview_foot_img_loading.setVisibility(ProgressBar.GONE);

				rl_listview.onRefreshComplete(activity.getString(R.string.newest_update) + new Date().toLocaleString());
			}
		};
        new Thread(){
        	public void run() {
        		Message msg =new Message();
				try{ 
					//查询
					List<Map<String, Object>> datalist = getListViewData();	
					msg.what = datalist.size() ;//成功获取几条数据
					msg.obj = datalist;
				}catch (ApiException e){
			    	e.printStackTrace();
	            	msg.what = -1;
	            	msg.obj = e;
			    }
				rlHandler.sendMessage(msg);
			}
        }.start();
	}
	
	//获取listview滚动数据
	private void getListViewScrollData(){		
		rl_listview_foot_img_loading.setVisibility(ProgressBar.VISIBLE);
		rlHandler = new Handler(){
			public void handleMessage(Message msg) {
				if(msg.what == -1){
					((ApiException)msg.obj).makeToast(activity);
					return ;
				}
				CommentList commentlist = (CommentList)msg.obj;
				int commentCount = commentlist.getCommentCount();
				if(msg.what > 0 )
				{
					//判断是否读取到网络数据最后一条
					if(commentCount > rlDatalist.size())
					{
			            for (Comment comment : commentlist.getComments()) {
			            	Map<String, Object> map = new HashMap<String, Object>();
			            	map.put("blogtitle",comment.getTitle());
			    			map.put("user", comment.getName());
			    			map.put("date", comment.getPubDate());
			    			map.put("content", comment.getBody());
			    			map.put("Entity", comment);//把实体本身做参数
			    			rlDatalist.add(map);
						}
			            rlAdapter.notifyDataSetChanged();  
			            if(rlDatalist.size() >= commentCount)
			            	rl_listview_foot_more.setText(R.string.main_listview_foot_end);
		            	else
		            		rl_listview_foot_more.setText(R.string.main_listview_foot_more);
					}            
					else
					{
						rl_listview_foot_more.setText(R.string.main_listview_foot_end);
		            }
				}
				else if(commentlist != null && commentCount == rlDatalist.size())
				{
					rl_listview_foot_more.setText(R.string.main_listview_foot_end);
				}

				rl_listview_foot_img_loading.setVisibility(ProgressBar.GONE);
			}
		};
        new Thread(){
        	public void run() {
        		Message msg =new Message();
				try{ 
					//查询
					String url = ((NextAppContext)activity.getApplication()).getUrls().getCommentList();					
					CommentList commentList = NextAppClient.comments(url, 0, LastCommentId,DEFAULT_PAGE_SIZE,NextAppClient.DESC);					
					msg.what = commentList.getComments().size() ;//成功获取几条数据
					msg.obj = commentList;
				}catch (ApiException e){
			    	e.printStackTrace();
	            	msg.what = -1;
	            	msg.obj = e;
			    }
				rlHandler.sendMessage(msg);
			}
        }.start();
	}
	
	//获取listview初始化数据
	private List<Map<String, Object>> getListViewData() throws ApiException
	{
		String url = ((NextAppContext)activity.getApplication()).getUrls().getCommentList();					
		CommentList commentlist = NextAppClient.comments(url, 0, 0,DEFAULT_PAGE_SIZE,NextAppClient.DESC);
		List<Map<String, Object>> datalist = new ArrayList<Map<String, Object>>();
        for (Comment comment : commentlist.getComments()) {
        	Map<String, Object> map = new HashMap<String, Object>();
        	map.put("blogtitle",comment.getTitle());
			map.put("user", comment.getName());
			map.put("date", comment.getPubDate());
			map.put("content", comment.getBody());
			map.put("Entity", comment);//把实体本身做参数
			datalist.add(map);
		}
        if(commentlist.getCommentCount() == commentlist.getComments().size())
			isEnd = true;
		return datalist;
	}
	
	private void deleteItem(final int index){
		int size = rlDatalist.size();
		if( size == 0 ){
			return;
		}
		final Handler handler = new Handler()
		{
			@Override
			public void handleMessage(Message msg) {
				if(msg.what < 0){
					((ApiException)msg.obj).makeToast(activity);
					return ;
				}
				Result res = (Result)msg.obj;
				if(res.getErrorCode()==1){
					//删除listview当前项
					rlDatalist.remove(index);
					rlAdapter.notifyDataSetChanged();
					Toast.makeText(activity, activity.getString(R.string.blog_review_del_success), Toast.LENGTH_SHORT).show();
				}
				else
					Toast.makeText(activity, activity.getString(R.string.blog_review_del_fail)+res.getErrorMessage(), Toast.LENGTH_SHORT).show();				
			}
		};
		new Thread(){
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String url = ((NextAppContext)activity.getApplication()).getUrls().getCommentDelete();
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
