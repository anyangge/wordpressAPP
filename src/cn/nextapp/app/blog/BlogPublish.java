package cn.nextapp.app.blog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.commonsware.cwac.richedit.RichEditText;
import com.commonsware.cwac.richedit.RichEditor;
import com.commonsware.cwac.richedit.Effect;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;
import cn.nextapp.app.blog.api.ApiException;
import cn.nextapp.app.blog.api.NextAppClient;
import cn.nextapp.app.blog.common.FileUtils;
import cn.nextapp.app.blog.common.ImageUtils;
import cn.nextapp.app.blog.common.MediaUtils;
import cn.nextapp.app.blog.common.StringUtils;
import cn.nextapp.app.blog.common.UIHelper;
import cn.nextapp.app.blog.entity.Catalog;
import cn.nextapp.app.blog.entity.Post;
import cn.nextapp.app.blog.entity.Result;
import cn.nextapp.app.widget.ImageAdapter;

/**
 * 发表文章
 * @author liux
 * @date 2011-12-19 下午9:44:53
 */
public class BlogPublish extends Activity implements OnCheckedChangeListener, RichEditText.OnSelectionChangedListener {
	
	private EditText			pb_edit_title;
	private RichEditor			pb_edit_content;
	private EditText			pb_edit_tag;
	private LinearLayout		pb_layout_category;
	private GridView 			pb_gridview;
	private ImageAdapter 		pb_imgAdapter;
	
	private ProgressDialog pdialog;

	private HashMap<CompoundButton, Effect<Boolean>> baseEffects=
	      new HashMap<CompoundButton, Effect<Boolean>>();
	  
	private HashMap<String,Boolean> selectCategorys = new HashMap<String,Boolean>();	
	private String strCatalogs;
	private String theLarge;
	private List<File> imageFiles = new ArrayList<File>();
	private List<Bitmap> imageBitmaps =new ArrayList<Bitmap>();
	private List<Catalog> cataloglist;

	@Override
	protected void onStop() {
		//保存当前正在编辑的数据
		final String title = pb_edit_title.getText().toString();
		final String content = _GetContent();
		final String tags = pb_edit_tag.getText().toString();
		((NextAppContext)getApplication()).setProperties(new Properties(){{
			if(!StringUtils.isEmpty(title))
				setProperty("blog_title", title);
			if(!StringUtils.isEmpty(content))
				setProperty("blog_content", content);
			if(!StringUtils.isEmpty(tags))
				setProperty("blog_tags", tags);
		}});
		super.onStop();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blog_publish);
		
		pb_edit_title = (EditText)findViewById(R.id.post_blog_title);
		pb_edit_content = (RichEditor)findViewById(R.id.post_blog_content);

		
		pb_edit_content.getEditText().setOnSelectionChangedListener(this);
	
	    for (CompoundButton btn : baseEffects.keySet()) {
	      btn.setOnCheckedChangeListener(this);
	    }
		
		pb_edit_tag = (EditText)findViewById(R.id.post_blog_tag);
		pb_layout_category = (LinearLayout)findViewById(R.id.post_blog_linearlayout_category);
		
		//还原上次编辑的信息
		Properties props = ((NextAppContext)getApplication()).getProperties();
		if(!StringUtils.isEmpty(props.getProperty("blog_title")))
			pb_edit_title.setText(props.getProperty("blog_title"));
		if(!StringUtils.isEmpty(props.getProperty("blog_content")))
			pb_edit_content.getEditText().setText(Html.fromHtml(props.getProperty("blog_content")));
		if(!StringUtils.isEmpty(props.getProperty("blog_tags")))
			pb_edit_tag.setText(props.getProperty("blog_tags"));
		
		View.OnClickListener insertImgTrigger = new View.OnClickListener() {
			public void onClick(View arg0) {
				CharSequence[] items = {
					BlogPublish.this.getString(R.string.img_from_album),
					BlogPublish.this.getString(R.string.img_from_camera)
				};
				imageChooseItem(items);
			}
		};
		
		findViewById(R.id.post_blog_img_insert).setOnClickListener(insertImgTrigger);
		findViewById(R.id.post_blog_img_insert_2).setOnClickListener(insertImgTrigger);
		
		findViewById(R.id.blog_publish_button_back).setOnClickListener(new View.OnClickListener() {			
			public void onClick(View arg0) {
				finish();
			}
		});
		
		findViewById(R.id.blog_publish_button_publish).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				publish();
			}
		});
		
		//初始化分类选择
		InitCatalogList();
		//初始化GridView
	    InitGridView();
	}
	
	//初始化分类选择
	private void InitCatalogList()
	{
		final Handler handler = new Handler()
		{
			public void handleMessage(Message msg) {
				LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,1);
				for(Catalog catalog:cataloglist){
					View view = getLayoutInflater().inflate(R.layout.blog_publish_catalog_list, null);
					CheckBox chkbox = (CheckBox)view.findViewById(R.id.post_blog_catalog_chkbox);
					chkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							String catalogId = buttonView.getTag().toString();
							selectCategorys.put(catalogId, isChecked);							
						}
					});
					chkbox.setText(catalog.getName());
					chkbox.setTag(catalog.getId());
					pb_layout_category.addView(view);
					View vline = new View(BlogPublish.this);
					vline.setLayoutParams(params);
					vline.setBackgroundColor(getResources().getColor(R.color.gray));
					pb_layout_category.addView(vline);
				}
			}
		};
		new Thread(){
			public void run() {
				try {
					cataloglist = ((NextAppContext)getApplication()).catalogs();
	                handler.sendEmptyMessage(1);
	            } catch (ApiException e) {
	            	e.printStackTrace();
	            }
			}
		}.start();
	}
	
	/**
	 * 返回文章编辑器的内容
	 * @return
	 */
	private String _GetContent() {
		return Html.toHtml(pb_edit_content.getEditText().getText());
	}

	//发布文章
	private void publish()
	{
		//检查输入是否满足要求
		final String title = pb_edit_title.getText().toString().trim();
		if(StringUtils.isEmpty(title)){
			Toast.makeText(this, R.string.msg_blog_title_empty, Toast.LENGTH_SHORT).show();
			return ;
		}
		
		final String content = _GetContent();
		if(StringUtils.isEmpty(content)){
			Toast.makeText(this, R.string.msg_blog_content_empty, Toast.LENGTH_SHORT).show();
			return ;
		}
		
		final String tags = pb_edit_tag.getText().toString().trim();
		
		final Handler handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				pdialog.dismiss();
				if(msg.what == 1){
					Result res = (Result)msg.obj;
					if(res.getErrorCode() > 0){
						((NextAppContext)getApplication()).removeProperty("blog_title","blog_content","blog_tags");
						UIHelper.showBlogDetail(BlogPublish.this, res.getErrorCode());
						Toast.makeText(BlogPublish.this, getString(R.string.blog_publish_success), Toast.LENGTH_SHORT).show();
						finish();
					}
					else
						Toast.makeText(BlogPublish.this, getString(R.string.blog_publish_fail)+res.getErrorMessage(), Toast.LENGTH_SHORT).show();					
				}
				else
					((ApiException)msg.obj).makeToast(BlogPublish.this);
			}
		};

		//选中的分类
		strCatalogs = "";
		for(Map.Entry<String, Boolean> entry :selectCategorys.entrySet()){
			if(entry.getValue()){
				if(!StringUtils.isEmpty(strCatalogs))
					strCatalogs += ",";
				strCatalogs += entry.getKey();
			}
		}
		final Post post = new Post();
		post.setCatalog(strCatalogs);
		post.setTitle(title);
		post.setBody(content);
		post.setTags(tags);			
		post.setImageFiles(imageFiles);		

    	pdialog = ProgressDialog.show(this, getString(R.string.blog_publish_waiting), getString(R.string.blog_publish_publishing),true);
		new Thread(){
        	public void run() {
				Message msg =new Message();
				try{ 
					String url = ((NextAppContext)getApplication()).getUrls().getPostPublish();
					Result result = NextAppClient.publish(url, post);			
					//发送消息
					msg.what = 1;
					msg.obj = result;
					
				}catch (ApiException e){
			    	e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
			    }
				handler.sendMessage(msg);
			}
        }.start();
      
	}
	/**
	 * 操作选择
	 * @param items
	 */
	public void imageChooseItem(CharSequence[] items )
	{
		AlertDialog imageDialog = new AlertDialog.Builder(this).setTitle(R.string.ui_insert_image).setIcon(android.R.drawable.btn_star).setItems(items,
			new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int item)
				{
					//手机选图
					if( item == 0 )
					{
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
						intent.setType("image/*"); 
						startActivityForResult(intent,ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD); 
					}
					//拍照
					else if( item == 1 )
					{	  
						Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");	
						theLarge = ImageUtils.getLatestImage(BlogPublish.this);
						//System.out.println("theLarge=="+theLarge);
						startActivityForResult(intent, ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA);
					}   
				}}).create();
		
		 imageDialog.show();
	}
	
	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{ 
		Bitmap bitmap = null;
        if ( requestCode == ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD ) 
        { 
        	if (resultCode != RESULT_OK) 
    		{   
    	        return;   
    	    }
        	
        	if(data == null)    return;
        	
        	Uri thisUri = data.getData();
        	String thePath = ImageUtils.getAbsolutePathFromNoStandardUri(thisUri);
        	
        	//如果是标准Uri
        	if( StringUtils.isEmpty(thePath) )
        	{
        		theLarge = ImageUtils.getAbsoluteImagePath(this,thisUri);
        	}
        	else
        	{
        		theLarge = thePath;
        	}
        	
        	String attFormat = FileUtils.getFileFormat(theLarge);
        	if( !"photo".equals(MediaUtils.getContentType(attFormat)) )
        	{
        		Toast.makeText(this, getString(R.string.blog_publish_choose_image), Toast.LENGTH_SHORT).show();
        		return;
        	}
        	String imgName = FileUtils.getFileName(theLarge);
    		
        	bitmap = ImageUtils.loadImgThumbnail(this,imgName, MediaStore.Images.Thumbnails.MICRO_KIND );
        }
        //拍摄图片
        else if(requestCode == ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA )
        {	
        	if (resultCode != RESULT_OK) 
    		{   
    	        return;   
    	    }
        	
        	super.onActivityResult(requestCode, resultCode, data);
        	
			theLarge = ImageUtils.getLatestImage(BlogPublish.this);
			//System.out.println("theLarge=="+theLarge);
        	
        	String imgName = FileUtils.getFileName(theLarge);

        	bitmap = ImageUtils.loadImgThumbnail(this,imgName, MediaStore.Images.Thumbnails.MICRO_KIND );      	
        }
        
		if(bitmap!=null)
		{
			imageFiles.add(new File(theLarge));//添加选择的图片
			imageBitmaps.add(bitmap);
			
			if(imageBitmaps.size() > 0)
			{ 
				if(pb_gridview.getVisibility() == GridView.GONE)
					pb_gridview.setVisibility(GridView.VISIBLE);
		    	
		    	//当前行数
		    	int nowLine = ((imageBitmaps.size()%4) == 0 ) ? (imageBitmaps.size()/4) : (imageBitmaps.size()/4+1);
		    	// 取控件GridView当前的布局参数
		    	LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) pb_gridview.getLayoutParams(); 
		    	// 设置控件的高度
		    	linearParams.height = 110 * nowLine;
		    	pb_gridview.setLayoutParams(linearParams);
		    	//更新
		    	pb_imgAdapter.notifyDataSetChanged();
			}
		}
    }
    
    //初始化GridView
    private void InitGridView()
    {
    	//图片集GridView对象
    	pb_gridview = (GridView) findViewById(R.id.post_blog_gridview);
    	pb_gridview.setVisibility(GridView.GONE);
    	pb_imgAdapter = new ImageAdapter(this,imageBitmaps);
    	pb_gridview.setAdapter(pb_imgAdapter);
		//事件监听
    	pb_gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
    		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,final int arg2, long arg3) {
    			new AlertDialog.Builder(arg0.getContext())
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(getString(R.string.blog_publish_delete_image))
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						imageFiles.remove(arg2);
						imageBitmaps.remove(arg2);
						pb_imgAdapter.notifyDataSetChanged();
						
						if(imageBitmaps.size()==0)
							pb_gridview.setVisibility(GridView.GONE);
						else{
							//当前行数
					    	int nowLine = ((imageBitmaps.size()%4) == 0 ) ? (imageBitmaps.size()/4) : (imageBitmaps.size()/4+1);
					    	// 取控件GridView当前的布局参数
					    	LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) pb_gridview.getLayoutParams(); 
					    	// 设置控件的高度
					    	linearParams.height = 110 * nowLine;
					    	pb_gridview.setLayoutParams(linearParams);
					    	//更新
					    	pb_imgAdapter.notifyDataSetChanged();
						}
						dialog.dismiss();
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.create().show();
    			return true;
    		}
		});
    }
    
    @Override
    public void onCheckedChanged(CompoundButton v, boolean checked) {
      Effect<Boolean> effect=baseEffects.get(v);
      
      if (effect!=null) {
    	  pb_edit_content.getEditText().applyEffect(effect, new Boolean(checked));
      }
    }

    @Override
    public void onSelectionChanged(int start, int end, List<Effect<?>> effects) {
      for (Entry<CompoundButton, Effect<Boolean>> entry : baseEffects.entrySet()) {
        if (effects.contains(entry.getValue())!=entry.getKey().isChecked()) {
          entry.getKey().toggle();
        }
      }
    }
}
