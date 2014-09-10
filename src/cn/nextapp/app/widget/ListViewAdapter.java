package cn.nextapp.app.widget;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.nextapp.app.blog.BlogReview;
import cn.nextapp.app.blog.R;
import cn.nextapp.app.blog.common.UIHelper;
import cn.nextapp.app.blog.entity.Post;
/**
 * 自定义Adapter类
 * @author liux
 */
public class ListViewAdapter extends BaseAdapter {
	private Context 					context;//运行上下文
	private List<Map<String, Object>> 	listItems;//数据集合
	private LayoutInflater 				listContainer;//视图容器
	private int 						itemViewResource;//自定义项视图源
	private boolean[] 					hasChecked;//记录复选框选中状态
	private class ListItemView{				//自定义控件集合  
			public ImageView image;  
	        public TextView title;  
		    public TextView user;
		    public TextView date;  
		    public TextView content;
		    public TextView count;
		    public CheckBox check;
		    public Button detail;  	    
	 }  

	/**
	 * 实例化Adapter 参数：from to 没用到
	 * @param context
	 * @param data
	 * @param resource
	 * @param from
	 * @param to
	 */
	public ListViewAdapter(Context context, List<Map<String, Object>> data,int resource,String[] from,int[] to) {
		this.context = context;			
		this.listContainer = LayoutInflater.from(context);	//创建视图容器并设置上下文
		this.itemViewResource = resource;
		this.listItems = data;
		this.hasChecked = new boolean[getCount()];
	}
	
	public int getCount() {
		// TODO Auto-generated method stub
		return listItems.size();
	}

	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * 记录勾选了哪个复选框
	 * @param checkedID 选中的复选框序号
	 */
	private void checkedChange(int checkedID) {
		hasChecked[checkedID] = !hasChecked[checkedID];
	}
	
	/**
	 * 判断复选框是否选择
	 * @param checkedID 复选框序号
	 * @return 返回是否选中状态
	 */
	public boolean hasChecked(int checkedID) {
		return hasChecked[checkedID];
	}	
	   
	/**
	 * ListView Item设置
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		//Log.d("method", "getView");
		
		//自定义视图
		ListItemView  listItemView = null;
		if (convertView == null) {
			//获取list_item布局文件的视图
			convertView = listContainer.inflate(this.itemViewResource, null);
			
			listItemView = new ListItemView();
			//获取控件对象
			listItemView.image = (ImageView)convertView.findViewById(R.id.blog_listview_img);
			listItemView.title = (TextView)convertView.findViewById(R.id.blog_listview_title);
			listItemView.content = (TextView)convertView.findViewById(R.id.blog_listview_content);
			listItemView.count= (TextView)convertView.findViewById(R.id.blog_listview_reviewcount);
			listItemView.user= (TextView)convertView.findViewById(R.id.blog_listview_user);
			listItemView.date= (TextView)convertView.findViewById(R.id.blog_listview_date);
			
			//设置控件集到convertView
			convertView.setTag(listItemView);
		}else {
			listItemView = (ListItemView)convertView.getTag();
		}
		
		//Log.d("title", (String) listItems.get(position).get("title"));
		
		//设置文字和图片
		listItemView.image.setBackgroundResource((Integer) listItems.get(position).get("img"));
		listItemView.title.setText((String) listItems.get(position).get("title"));
		listItemView.title.setTag(listItems.get(position).get("Entity"));//设置隐藏参数(实体类)
		listItemView.user.setText((String) listItems.get(position).get("user"));
		listItemView.date.setText((String) listItems.get(position).get("date"));
		listItemView.count.setText(String.valueOf(listItems.get(position).get("reviewcount")));
		listItemView.count.setTag(listItems.get(position).get("Entity"));//设置隐藏参数(实体类)
		listItemView.content.setText(Html.fromHtml((String) listItems.get(position).get("content")));
		//注册点击事件
		listItemView.count.setOnClickListener(this.countClickListener);
		
		return convertView;
	}
	
	private View.OnClickListener countClickListener = new View.OnClickListener(){
		@Override
		public void onClick(View v) {
			Intent it = new Intent(ListViewAdapter.this.context,BlogReview.class);
			Post post = (Post) v.getTag();			
			if(post.getCommentCount() > 0)
				UIHelper.showBlogReview(ListViewAdapter.this.context, post.getId());
			else
				Toast.makeText(ListViewAdapter.this.context, R.string.no_review, Toast.LENGTH_SHORT).show();
		}
	};
}