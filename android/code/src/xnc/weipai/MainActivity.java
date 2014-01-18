package xnc.weipai;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;

import xnc.widget.SlideHolder;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;

import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

public class MainActivity extends Activity {
	public final static String INTENT_DATA_VIDEO_ID="xnc.weipai.data.video_id";
	private SlideHolder mSlideHolder;
	private APIWeipai apiWeipai;
	private AppInitTask appInitTask;
	private LinkedList<String> mListItems;
	private PullToRefreshGridView mPullRefreshGridView;
	private GridView mGridView;
	private ArrayAdapter<String> mAdapter;
	private String[] mStrings = { "Abbaye de Belloc", "Abbaye du Mont des Cats", "Abertam", "Abondance", "Ackawi",
			"Acorn", "Adelost", "Affidelice au Chablis", "Afuega'l Pitu", "Airag", "Airedale", "Aisy Cendre",
			"Allgauer Emmentaler" };
	private Context context;
	private ArrayList<HashMap<String, Object>> videoItem;
	private SimpleAdapter videoListAdapter;
	private ArrayList<WeipaiVideo> videos=new ArrayList<WeipaiVideo>();
	private ImageLoader imageLoader;
	private VideoAdapter videoAdapter;
	private DisplayImageOptions videoDisplayOptions;
	private ArrayList<SidebarMenuItem> sidebarMenuItems = new ArrayList<SidebarMenuItem>();
	private ArrayList<RelativeLayout> sidebarViews=new ArrayList<RelativeLayout>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	context=MainActivity.this;
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        
        
        setContentView(R.layout.activity_main);
        initSidebar();
        mSlideHolder = (SlideHolder) findViewById(R.id.slideHolder);
		
		
		View toggleView = findViewById(R.id.menu_toggle);
		toggleView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mSlideHolder.toggle();
			}
		});
		
		
		mPullRefreshGridView = (PullToRefreshGridView) findViewById(R.id.pull_refresh_grid);
		mGridView = mPullRefreshGridView.getRefreshableView();

		// Set a listener to be invoked when the list should be refreshed.
		mPullRefreshGridView.setOnRefreshListener(new OnRefreshListener2<GridView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
				Toast.makeText(MainActivity.this, "Pull Down!", Toast.LENGTH_SHORT).show();
				new GetDataTask().execute();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
				Toast.makeText(MainActivity.this, "Pull Up!", Toast.LENGTH_SHORT).show();
				new GetDataTask().execute();
			}

		});

		

		TextView tv = new TextView(this);
		tv.setGravity(Gravity.CENTER);
		tv.setText("Empty View, Pull Down/Up to Add Items");
		mPullRefreshGridView.setEmptyView(tv);
		
		
		// 初始化图片接收模块
		videoAdapter=new VideoAdapter();
		mGridView.setAdapter(videoAdapter);
		mGridView.setOnTouchListener(videoMenuTouchLister);
//		mGridView.setOnClickListener(videoMenuClickLister);
	    
	    
	    DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisc(true).build();
	    ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).defaultDisplayImageOptions(defaultOptions).build();
	    imageLoader=ImageLoader.getInstance();
	    imageLoader.init(config);
	    
	    videoDisplayOptions =  new DisplayImageOptions.Builder()
	        .showImageOnLoading(R.drawable.default_video)
	        .showImageForEmptyUri(R.drawable.default_video) // resource or drawable
	        .showImageOnFail(R.drawable.default_video) // resource or drawable
	        .considerExifParams(false) // default
	        .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
	        .displayer(new SimpleBitmapDisplayer()) // default
	        .build();
		
	    
		apiWeipai=new APIWeipai();
		apiWeipai.setContext(context);
		
		appInitTask=new AppInitTask();
		appInitTask.execute();
    }
    
    public class VideoAdapter extends BaseAdapter {
        @Override
        public int getCount() {
                return videos.size();
        }

        @Override
        public Object getItem(int position) {
                return null;
        }

        @Override
        public long getItemId(int position) {
                return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                final ViewHolder holder;
                View view = convertView;
                if (view == null) {
                        view = getLayoutInflater().inflate(R.layout.item_video, parent, false);
                        holder = new ViewHolder();
                        assert view != null;
                         
                        view.setTag(holder);
                } else {
                        holder = (ViewHolder) view.getTag();
                }
                holder.imageView = (ImageView) view.findViewById(R.id.video_item_image);
                holder.textDesc = (TextView) view.findViewById(R.id.video_item_desc);
                WeipaiVideo video=videos.get(position);
                
                imageLoader.displayImage(video.imagePreview, holder.imageView,videoDisplayOptions);
//                view.setOnTouchListener(videoMenuTouchLister);
//                view.set
//                view.set
                holder.textDesc.setText(video.desc);  
                return view;
	        }
	
	        class ViewHolder {
	                ImageView imageView;
	                TextView textDesc;
	        }	
	}
    
    private class AppInitTask extends AsyncTask<Void, Void, JsonObject> {
    	
		@Override
		protected JsonObject doInBackground(Void... params) {	
			apiWeipai.initWeipaiApi();
        	return apiWeipai.getHotVideos();
		}
		
		@Override
		protected void onPostExecute(JsonObject result) {
			videoObj2Videos(result);
			videoAdapter.notifyDataSetChanged();
			super.onPostExecute(result);
		}
		
    }
    private class GetDataTask extends AsyncTask<Void, Void, JsonObject> {

		@Override
		protected JsonObject doInBackground(Void... params) {
			return apiWeipai.getHotVideos();
		}

		@Override
		protected void onPostExecute(JsonObject result) {
			videoObj2Videos(result);
			videoAdapter.notifyDataSetChanged();
			mPullRefreshGridView.onRefreshComplete();
			super.onPostExecute(result);
		}
	}
    
    private void videoObj2Videos(JsonObject result){
    	JsonArray array =  result.get("video_list").getAsJsonArray();
		for (int i = 0; i < array.size(); i++) {
			JsonObject obj=array.get(i).getAsJsonObject();
			String videoDesc = obj.get("video_desc").getAsString();
			String videoUrl = obj.get("video_screenshots").getAsString();
			String id = obj.get("video_id").getAsString();
			
			WeipaiVideo video=new WeipaiVideo();
			video.desc=(videoDesc.isEmpty()?"啥都没说":videoDesc);
			video.imagePreview=videoUrl;
			video.id=id;
			videos.add(video);
		}
    	
    }
    private View.OnTouchListener sidebarMenuTouchLister = new View.OnTouchListener(){

    	public boolean onTouch(View v, MotionEvent event) {
//			MotionEvent.
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				
				v.setBackgroundColor(getResources().getColor(R.color.red));
			}
			else if(event.getAction()== MotionEvent.ACTION_UP){
				for (int i = 0; i < sidebarViews.size(); i++) {
		    		sidebarViews.get(i).setBackgroundColor(Color.TRANSPARENT);
		    		
				}
				v.setBackgroundColor(getResources().getColor(R.color.red));
				int idView = v.getId();
				Intent intent;
				switch (idView) {
					default:
					case R.id.menu_guangchang:
						intent = new Intent(context, VideoActivity.class);
						break;
					}
				startActivity(intent);
			}
			return true;
		}
    };
    
    private View.OnTouchListener videoMenuTouchLister = new View.OnTouchListener(){
    	private int posDown=0;
    	private int posUp=0;

    	public boolean onTouch(View v, MotionEvent event) {
    		
			int position = mGridView.pointToPosition((int)event.getX(),(int)event.getY());
			if(event.getAction()==MotionEvent.ACTION_DOWN){
				posDown=(int)event.getY();
			}
			else if(event.getAction()== MotionEvent.ACTION_UP){
				posUp=(int)event.getY();
				if(posDown==posUp){
					v.setBackgroundColor(getResources().getColor(R.color.red));
					WeipaiVideo video = videos.get(position);
					if(video!=null){
						Intent intent = new Intent(context, VideoActivity.class);
						intent.putExtra(INTENT_DATA_VIDEO_ID, video.id);
						startActivity(intent);
					}	
					return true;
				}
				
				
			}
			return false;
			
		}
    };
    
    private View.OnClickListener videoMenuClickLister = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}
	};
    
    private void initSidebar(){
    	final RelativeLayout menuGuangchang = (RelativeLayout)findViewById(R.id.menu_guangchang);
    	final RelativeLayout menuWeekVideos = (RelativeLayout)findViewById(R.id.menu_week_videos);
    	sidebarViews.add(menuGuangchang);
    	sidebarViews.add(menuWeekVideos);
    	for (int i = 0; i < sidebarViews.size(); i++) {
    		sidebarViews.get(i).setOnTouchListener(sidebarMenuTouchLister);
		}
    	
    	
//    	InputStream stream;
//		try {
//			stream = getAssets().open("json/sidebar_menu.json");
//			int size = stream.available();
//	        byte[] buffer = new byte[size];
//	        stream.read(buffer);
//	        stream.close();
//	        String tContents = new String(buffer);
//	        JsonParser jsonParser = new JsonParser();
//	        JsonObject json = (JsonObject)jsonParser.parse(tContents);
//	        JsonArray array = json.getAsJsonArray();
//	        for (int i = 0; i < array.size(); i++) {
//				JsonObject obj = array.get(i).getAsJsonObject();
//				SidebarMenuItem menu = new SidebarMenuItem();
//				menu.name=obj.get("name").getAsString();
//				menu.icon=obj.get("icon").getAsString();
//				sidebarMenuItems.add(menu);
//			}
//	        
//	        LinearLayout sidebarMenu = (LinearLayout)findViewById(R.id.sidebar_menu);
//	    	
//	        for (int i = 0; i < sidebarMenuItems.size(); i++) {
//	        	SidebarMenuItem item =sidebarMenuItems.get(i);
//	        	
////	        	RelativeLayout menu = new RelativeLayout(context);
////	        	menu.getLayoutParams().height= LayoutParams.WRAP_CONTENT;
////	        	menu.getLayoutParams().width= LayoutParams.WRAP_CONTENT;
////	        	menu.setPadding(50, 8, 70, 8);
////	        	
////	        	
////	        	ImageView imageView = new ImageView(context);
////	        	imageView.getLayoutParams().height= 30;
////	        	imageView.getLayoutParams().width= 30;
////	        	imageView.setImageDrawable(getResources().getDrawable(getResources().getIdentifier(item.icon,"drawable", getPackageName())));
////	        	
////	        	menu.addView(imageView);
////	        	
////	        	TextView textView = new TextView(context);
////	        	textView.setText(item.name);
////	        	imageView.getLayoutParams()
//	        	
//	        	
//	        	
//	        	
//	        	
////	        	sidebarMenu.addView(menu);
//			}
//	        
//	        
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

        
       

    	
    }
    
    
   

}
