package cn.nextapp.app.blog.common;

import java.util.Stack;

import android.app.Activity;
import android.app.ActivityManager;

/**
 * 自定义应用程序管理类
 * @author liux
 */
public class AppManager extends Activity{
	private static Stack<Activity> activityStack;
	private static AppManager instance;
	private  AppManager(){
	}
	public static AppManager getAppManager(){
		if(instance==null){
			instance=new AppManager();
		}
		return instance;
	}
	public void finishActivity(){
		Activity activity=activityStack.lastElement();
		if(activity!=null){
			activity.finish();
			activity=null;
		}
	}
	public void finishActivity(Activity activity){
		if(activity!=null){
			activity.finish();
			activityStack.remove(activity);
			activity=null;
		}
	}
	public void finishActivity(Class cls){
		for (Activity activity : activityStack) {
			if(activity.getClass().equals(cls) ){
				finishActivity(activity);
			}
		}
	}
	public Activity currentActivity(){
		Activity activity=activityStack.lastElement();
		return activity;
	}
	public void addActivity(Activity activity){
		if(activityStack==null){
			activityStack=new Stack<Activity>();
		}
		activityStack.add(activity);
	}
	
	public void finishAllActivityExceptOne(Class cls){
		while(true){
			Activity activity=currentActivity();
			if(activity==null){
				break;
			}
			if(activity.getClass().equals(cls) ){
				break;
			}
			finishActivity(activity);
		}
	}
	
	public void AppExit() {
		try {
			for (int i = 0, size = activityStack.size(); i < size; i++){
	            if (null != activityStack.get(i)){
	            	activityStack.get(i).finish();
	            }
		    }
			ActivityManager activityMgr= (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
			activityMgr.restartPackage(getPackageName()); 
		} catch (Exception e) {	
			System.exit(0);
		}
	}
}