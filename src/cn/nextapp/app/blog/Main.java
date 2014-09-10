package cn.nextapp.app.blog;

import java.util.ArrayList;
import java.util.Date;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import cn.nextapp.app.blog.api.ApiException;
import cn.nextapp.app.blog.common.AppManager;
import cn.nextapp.app.blog.common.StringUtils;
import cn.nextapp.app.blog.common.UIHelper;
import cn.nextapp.app.blog.entity.Catalog;
import cn.nextapp.app.blog.entity.Post;
import cn.nextapp.app.blog.entity.PostList;
import cn.nextapp.app.widget.ListViewAdapter;
import cn.nextapp.app.widget.PullToRefreshListView;
import cn.nextapp.app.widget.ScrollLayout;

/**
 * 主界面
 * @author liux
 * @date 2011-12-18 上午9:55:37
 */
public class Main extends Activity {

	private final int				LISTVIEW_REFRESH = 2;
	
	private TextView				m_textview_category;
	private ImageView				m_imageview_category_down;
    private ProgressBar				m_listview_foot_img_loading;
    private TextView 				m_listview_foot_more;
    private ListViewAdapter			mAdapter;//自定义adapter
    private PullToRefreshListView 	m_listview;
    private Handler 				mHandler;//listview的处理handler
    private static List<Map<String, Object>> mDatalist = new ArrayList<Map<String, Object>>();
    private static int				CatalogId;//当前选择的分类id
    private static String			CatalogName;//当前选择的分类name
    private static int				LastBlogId;//滑动到最后一条数据的id
    private	static List<Catalog> 	cataloglist = new ArrayList<Catalog>();//文章分类集合
    private boolean 				isEnd = false;//第一次加载数据是否到底
    private View					footerView;//listview的底部刷新视图

    private ScrollLayout m_scrolllayout;//屏幕切换控件
    private ReviewListUI reviewlistUI;//评论列表

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//载入当前activity
		AppManager.getAppManager().addActivity(this);
		
		//初始化listview
		try {
			InitListViewBlog();
		} catch (ApiException e) {			
			e.printStackTrace(System.err);
			e.makeToast(this);
		}
		//初始化文章分类
		InitCatalogs();
		
		m_scrolllayout = (ScrollLayout)findViewById(R.id.main_scrolllayout);
		//实例化评论列表
		reviewlistUI = ReviewListUI.onCreate(this,m_scrolllayout);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
//		if(intent.getBooleanExtra("REFRESH", false)){
//			this.refreshListView();
//		}
		//获取菜单选项跳转并刷新
		if("BLOGLIST".equals(intent.getStringExtra("REFRESH"))){
			m_scrolllayout.setToScreen(0);
			this.refreshListView();
		}else if("REVIEWLIST".equals(intent.getStringExtra("REFRESH"))){
			m_scrolllayout.setToScreen(1);
			reviewlistUI.refreshListView();
		}
	}

	//初始化分类
	private void InitCatalogs()
	{
		m_textview_category = (TextView)findViewById(R.id.main_textview_category);
		m_imageview_category_down = (ImageView)findViewById(R.id.main_imageview_category_down);
		
		if(!StringUtils.isEmpty(CatalogName))
			m_textview_category.setText(CatalogName);
		
		//添加事件监听
		m_textview_category.setOnClickListener(catalogClickListener);
		m_imageview_category_down.setOnClickListener(catalogClickListener);
		
	}
	//点击分类事件监听器
	private View.OnClickListener catalogClickListener = new View.OnClickListener() {			
		public void onClick(View v) {
			AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
			builder.setIcon(android.R.drawable.ic_dialog_info);
			builder.setTitle(getString(R.string.main_spinner_category));
			ListAdapter catalogsAdapter = new ArrayAdapter<String>(Main.this, R.layout.catalogs_dialog, buildCatalogs());
			builder.setAdapter(catalogsAdapter, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface arg0, int arg1) {
					Catalog catalog = cataloglist.get(arg1);
					//如果选择的分类是当前分类，不执行刷新操作
					if(CatalogId == catalog.getId())
						return;
					m_textview_category.setText(catalog.getName());
					CatalogId = catalog.getId();
					CatalogName = catalog.getName();
					//选择分类后刷新
					refreshListView();					
				}
			});
			/*
			builder.setItems(buildCatalogs() ,new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface arg0, int arg1) {
					Catalog catalog = cataloglist.get(arg1);
					//如果选择的分类是当前分类，不执行刷新操作
					if(CatalogId == catalog.getId())
						return;
					m_textview_category.setText(catalog.getName());
					CatalogId = catalog.getId();
					CatalogName = catalog.getName();
					//选择分类后刷新
					refreshListView();					
				}
			});
			*/
			builder.create().show();			
		}
	};
	
	private String[] buildCatalogs(){
		try{
			if(cataloglist.size() == 0){
				cataloglist.add(Catalog.newInstance(0, getString(R.string.main_textview_category), null));						
				cataloglist.addAll(((NextAppContext)getApplication()).catalogs());
			}
			String[] catalogs = new String[cataloglist.size()];
			for(int i=0,j=cataloglist.size();i<j;i++){
				Catalog cat = cataloglist.get(i);
				if(cat.getId() == 0 || cat.getPostCount() <= 0)
					catalogs[i] = cat.getName();
				else
					catalogs[i] = cat.getName() + ':' + cat.getPostCount();
			}
			return catalogs;
		}catch(ApiException e){
			e.printStackTrace();
		}
		return null;
	}
	
	//初始化listview
	private void InitListViewBlog() throws ApiException{

		m_listview = (PullToRefreshListView) findViewById(R.id.main_listview);
		
		if(mDatalist.size() == 0)
			mDatalist = getListViewData();
		
		mAdapter = new ListViewAdapter(
		    	  this,mDatalist,
		    	  R.layout.main_listview,  
		          new String[]{"img","title","user","date","content","reviewcount"},
		          new int[]{R.id.blog_listview_img,R.id.blog_listview_title,R.id.blog_listview_user,R.id.blog_listview_date,R.id.blog_listview_content,R.id.blog_listview_reviewcount}
		);
		
        //添加head 或 foot项 要在setAdapter前
		footerView = getLayoutInflater().inflate(R.layout.main_listview_foot, null);
		
		if(mDatalist.size() > 5 && m_listview.getFooterViewsCount() == 0)
        {
			m_listview.addFooterView(footerView);
        }
        
        m_listview.setAdapter(mAdapter);
        
        m_listview.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
        		//判断点击是否为头部
        		if(m_listview.getHeaderViewsCount() > 0 && arg2 == 0)return;
        		//判断点击是否为底部
        		if(m_listview.getFooterViewsCount() > 0 && arg2 == (arg0.getCount()-1))return;
        		
        		TextView txtTitle = (TextView)arg1.findViewById(R.id.blog_listview_title);
        		Post post = (Post)txtTitle.getTag();
        		//跳转到文章详情
        		UIHelper.showBlogDetail(arg0.getContext(), post.getId());
        	}
		});
        m_listview.setOnScrollListener(new OnScrollListener() {		
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				m_listview.onScrollStateChanged(view, scrollState);
				//判断是否有底部刷新栏
				if(m_listview.getFooterViewsCount() > 0)
				{
					String str1 = getResources().getString(R.string.main_listview_foot_more);
					String str2 = m_listview_foot_more.getText().toString();
					if(m_listview_foot_img_loading.getVisibility()!=ProgressBar.GONE || !str1.equals(str2))
						return;
					//判断滚动到底部
					if(view.getLastVisiblePosition()==(view.getCount()-1) && 
							(scrollState == OnScrollListener.SCROLL_STATE_IDLE || scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)){
						//获取当前滚动最后一项的id
						Map<String,Object> item = mDatalist.get(mDatalist.size()-1);
		        		Post post = (Post)item.get("Entity");
		        		LastBlogId = post.getId();
						//加载下一页数据
						Main.this.getListViewScrollData();
	                }
				}
			}			
			public void onScroll(AbsListView view, int firstVisiableItem, int visibleItemCount, int totalItemCount) {
				m_listview.onScroll(view, firstVisiableItem, visibleItemCount, totalItemCount);
			}
			
		});
        m_listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> view, View arg1,int arg2, long arg3) {
				//获取选中的item的参数
				TextView txtTitle = (TextView)arg1.findViewById(R.id.blog_listview_title);
				Post post = (Post)txtTitle.getTag();
				//分享到
				UIHelper.showShareDialog(Main.this, post.getTitle(), post.getUrl());
				return true;
			}
		});
        m_listview.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {
            	refreshListView();
            }
        });
        
		m_listview_foot_img_loading = (ProgressBar) findViewById(R.id.main_listview_foot_img_loading);
		m_listview_foot_more = (TextView)findViewById(R.id.main_listview_foot_more);
        
		if(isEnd && m_listview.getFooterViewsCount()>0){
			m_listview_foot_more.setText(getString(R.string.main_listview_foot_end));
		}
			
	}
	//执行刷新的操作
	private void refreshListView(){
		this.getListViewData(LISTVIEW_REFRESH);
	}
	
	//获取listview滚动数据
	private void getListViewScrollData(){		
		m_listview_foot_img_loading.setVisibility(ProgressBar.VISIBLE);
		mHandler = new Handler(){
			public void handleMessage(Message msg) {
				if(msg.what < 0)
					((ApiException)msg.obj).makeToast(Main.this);
				else{
					PostList postlist = (PostList)msg.obj;
					if(msg.what > 0 )
					{
						//判断是否读取到网络数据最后一条
						if(postlist.getPostCount() > mDatalist.size())
						{
				            for (Post post : postlist.getPosts()) {
				            	Map<String, Object> map = new HashMap<String, Object>();
				    			map.put("img", R.drawable.icon);
				    			map.put("title", post.getTitle());
				    			map.put("user", post.getAuthor());
				    			map.put("date", post.getPubDate());
				    			map.put("content", post.getOutline());
				    			map.put("reviewcount", post.getCommentCount());
				    			map.put("Entity", post);//把实体本身做参数
			        			mDatalist.add(map); 
				            }
				            mAdapter.notifyDataSetChanged();  
				            //m_listview.setSelection(mDatalist.size()-1);//设置下一条数据置顶
				            if(mDatalist.size() >= postlist.getPostCount())
			            		m_listview_foot_more.setText(R.string.main_listview_foot_end);
			            	else
			            		m_listview_foot_more.setText(R.string.main_listview_foot_more);
						}
			            else
				        {
			            	m_listview_foot_more.setText(R.string.main_listview_foot_end);
			            }
					}
					else if(postlist != null && postlist.getPostCount() == mDatalist.size())
					{
						m_listview_foot_more.setText(R.string.main_listview_foot_end);
					}
				}
				m_listview_foot_img_loading.setVisibility(ProgressBar.GONE);
			}
		};
		
		startFetchPostsThread(CatalogId, LastBlogId);
		
	}
	
	/**
	 * 获取listview初始化数据
	 * @return
	 * @throws ApiException
	 */
	private List<Map<String, Object>> getListViewData() throws ApiException
	{
		PostList postlist = ((NextAppContext)getApplication()).posts(0, 0);
		List<Map<String, Object>> datalist = new ArrayList<Map<String, Object>>();
		for (Post post : postlist.getPosts()) {
        	Map<String, Object> map = new HashMap<String, Object>();
			map.put("img", R.drawable.icon);
			map.put("title", post.getTitle());
			map.put("user", post.getAuthor());
			map.put("date", post.getPubDate());
			map.put("content", post.getOutline());
			map.put("reviewcount", post.getCommentCount());
			map.put("Entity", post);//把实体本身做参数
			datalist.add(map);
		}
		if(postlist.getPostCount() == postlist.getPosts().size())
			isEnd = true;
		return datalist;
	}
	private void getListViewData(final int status) {	
		mHandler = new Handler(){
			public void handleMessage(Message msg) {
				if(msg.what < 0){
					((ApiException)msg.obj).makeToast(Main.this);
					if(m_listview.getFooterViewsCount()>0 && m_listview_foot_img_loading!=null)
						m_listview_foot_img_loading.setVisibility(ProgressBar.GONE);
					m_listview.onRefreshComplete();
					return ;
				}
				PostList postlist = (PostList)msg.obj;
				if(msg.what > 0)
				{
					mDatalist.clear();//清空之前的数据
		            for (Post post : postlist.getPosts()) {
		            	Map<String, Object> map = new HashMap<String, Object>();
		    			map.put("img", R.drawable.icon);
		    			map.put("title", post.getTitle());
		    			map.put("user", post.getAuthor());
		    			map.put("date", post.getPubDate());
		    			map.put("content", post.getOutline());
		    			map.put("reviewcount", post.getCommentCount());
		    			map.put("Entity", post);//把实体本身做参数
	        			mDatalist.add(map);
					}
		            
		            if(mDatalist.size() < 5)
		            {
		            	m_listview.removeFooterView(footerView);
		            }
		            else
		            {
		            	if(m_listview.getFooterViewsCount() == 0)
		            		m_listview.addFooterView(footerView);
		            	
		            	if(m_listview_foot_img_loading ==null)
		            		m_listview_foot_img_loading = (ProgressBar) findViewById(R.id.main_listview_foot_img_loading);
		            	if(m_listview_foot_more ==null)
		            		m_listview_foot_more = (TextView)findViewById(R.id.main_listview_foot_more);
		            	if(mDatalist.size() >= postlist.getPostCount())
		            		m_listview_foot_more.setText(R.string.main_listview_foot_end);
	            		else
	            			m_listview_foot_more.setText(R.string.main_listview_foot_more);
		            }
		            
		            mAdapter.notifyDataSetChanged();
		            //加载数据完成,设置当前显示为第一条 #有头部为1 #没有则0
		            m_listview.setSelection(1);
		            
				}
				else if(postlist != null && postlist.getPostCount() == 0)
				{
					m_listview.removeFooterView(footerView);
					Toast.makeText(Main.this, getString(R.string.no_blog), Toast.LENGTH_SHORT).show();
				}
				
				if(m_listview.getFooterViewsCount()>0 && m_listview_foot_img_loading!=null)
					m_listview_foot_img_loading.setVisibility(ProgressBar.GONE);
				
				if(status == LISTVIEW_REFRESH)
				{
					m_listview.onRefreshComplete(getString(R.string.newest_update) + new Date().toLocaleString());
				}				
			}
		};
		
		startFetchPostsThread(CatalogId, 0);
	}
	
	/**
	 * 启动线程去抓取文章列表
	 * @param catalog
	 * @param fromPost
	 */
	private void startFetchPostsThread(final int catalog, final int fromPost) {
		new Thread(){
        	public void run() {
				Message msg =new Message();
				try{ 
					//查询
					PostList postlist = ((NextAppContext)getApplication()).posts(catalog, fromPost);
					msg.what = postlist.getPosts().size() ;//成功获取几条数据
					msg.obj = postlist;
				}catch (ApiException e){
			    	e.printStackTrace();
			    	msg.what = -1;
			    	msg.obj = e;
			    }
				mHandler.sendMessage(msg);
			}
        }.start();
	}
	
	/**
	 * 创建menu
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		//设置menu界面为res/menu/menu.xml
		inflater.inflate(R.menu.menu, menu);
		menu.getItem(0).setTitle(R.string.main_menu_refresh);
		return true;
	}
	
	/**
	 * 菜单被显示之前的事件
	 */
	public boolean onPrepareOptionsMenu(Menu menu) {
		//判断显示登录或登出
		UIHelper.showMenuLoginOrLogout(this, menu);
		
		//判断当前屏幕显示‘刷新’
		if(m_scrolllayout.getCurScreen()==0){
			menu.getItem(0).setTitle(R.string.main_menu_refresh);
			menu.getItem(1).setTitle(R.string.main_menu_reviewlist);
		}else{
			menu.getItem(0).setTitle(R.string.main_menu_bloglist);
			menu.getItem(1).setTitle(R.string.main_menu_refresh);
		}
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
	
}
