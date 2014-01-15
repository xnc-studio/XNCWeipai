package xnc.weipai;


import xnc.widget.SlideHolder;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class MainActivity extends Activity {
	private SlideHolder mSlideHolder;
	private String httpServerWeipai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        mSlideHolder = (SlideHolder) findViewById(R.id.slideHolder);
		
		
		View toggleView = findViewById(R.id.menu_toggle);
		toggleView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mSlideHolder.toggle();
			}
		});
		systemAsyncInit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    
    //获取负载均衡地址
    private void systemAsyncInit(){
    	Ion.with(getBaseContext(), "http://w.weipai.cn/config")
    	.asJsonObject()
    	.setCallback(new FutureCallback<JsonObject>() {
    	   @Override
    	    public void onCompleted(Exception e, JsonObject result) {
    	        // do stuff with the result or error
    		   httpServerWeipai=result.get("http_server").getAsString();
    		   
    	    }
    	});
    	
    }
    
}
