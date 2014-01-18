package xnc.weipai;

import java.util.concurrent.ExecutionException;

import android.content.Context;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;


public class APIWeipai {
	private Context context;
	private String httpServerWeipai="http://w1.weipai.cn";
	private int pageHotVideos=0;
	private int sizeHotVideos=12;
	private int nowCursor=0;
	
	//调用API
	private void callAPIAsync(String api,final APIWeipaiCallback callback){
		String apiUrl=String.format("%s%s", httpServerWeipai,api);
		Ion.with(context, apiUrl)
    	.setHeader("User-Agent","android-async-http/1.4.1 (weipaipro)")
    	.setHeader("Phone-Type","android_2013022_4.2.1")
    	.setHeader("os","android")
    	.setHeader("Channel","weipai")
    	.asJsonObject()
    	.setCallback(new FutureCallback<JsonObject>() {
    	   @Override
    	    public void onCompleted(Exception e, JsonObject result) {
    		   callback.run(e,result);
    	    }
    	});
	}
	private JsonObject callAPISync(String api){
		String apiUrl=String.format("%s%s", httpServerWeipai,api);
		try {
			return Ion.with(context, apiUrl)
			.setHeader("User-Agent","android-async-http/1.4.1 (weipaipro)")
			.setHeader("Phone-Type","android_2013022_4.2.1")
			.setHeader("os","android")
			.setHeader("Channel","weipai")
			.asJsonObject()
			.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void setContext(Context context){
		this.context=context;
	}
	
	
	
	public void initWeipaiApi(){
		
		JsonObject json = null;
		try {
			json = Ion.with(context, "http://w.weipai.cn/config").asJsonObject().get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		httpServerWeipai=json.get("http_server").getAsString();  
		
		
	}
	//最热视频
	public JsonObject getHotVideos(){
		JsonObject obj = callAPISync(String.format("/top_video?type=top_day&count=%s&relative=after&cursor=%s", sizeHotVideos,nowCursor));
		nowCursor+=sizeHotVideos;
		return obj;
	}

}

interface APIWeipaiCallback  
{  
    public void run(Exception e, JsonObject result);  
  
} 

