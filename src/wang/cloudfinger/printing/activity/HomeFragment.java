package wang.cloudfinger.printing.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import wang.cloudfinger.printing.R;
import wang.cloudfinger.printing.adapter.BannerPageAdapter;
import wang.cloudfinger.printing.entity.Advertisement;
import wang.cloudfinger.printing.entity.News;
import wang.cloudfinger.printing.listener.OnBannerPageChangeListener;

public class HomeFragment extends Fragment {
	
	public static String IMAGE_CACHE_PATH = "imageLoader/cache";
	
	public final static int NEWS_TYPE_REGISTER = 0;
	public final static int NEWS_TYPE_ADVERTISING = 1;
	public final static int NEWS_TYPE_PRINTING = 2;
	public final static int NEWS_TYPE_BID = 3;
	public final static int NEWS_TYPE_HOBBY = 4;
	
	private View homeView;
	private ViewPager viewPager;
	private List<ImageView> imageViews;
	
	private List<View> dots;
	private List<View> dotList;
	
	private int currentItem = 0;
	
	private View dot0;
	private View dot1;
	private View dot2;
	private View dot3;
	private View dot4;
	
	private ScheduledExecutorService scheduledExecutorService;
	
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	
	private List<Advertisement> adList;
	private List<News> newsList;
	private ListView listViewNews;
	
	private PagerAdapter bannerPageAdapter;
	private OnPageChangeListener onBannerPageChangeListener;
	
	private Handler handler = new Handler(){
		public void handleMessage(Message msg){
			viewPager.setCurrentItem(currentItem);
		}
	};
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		homeView = inflater.inflate(R.layout.tab_home, container, false);
		
		initImageLoader();
		
		//获取图片加载实例
		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.top_banner_android)
				.showImageForEmptyUri(R.drawable.top_banner_android)
				.showImageOnFail(R.drawable.top_banner_android)
				.cacheInMemory(true).cacheOnDisc(true)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.imageScaleType(ImageScaleType.EXACTLY).build();
		
		initAdData();
		
		startAd();
		
		//==================云指信息=====================
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				this.getActivity(), android.R.layout.simple_list_item_1, initNews());
		
		listViewNews = (ListView) homeView.findViewById(R.id.listView_news);
		listViewNews.setAdapter(adapter);
		return homeView;
	}

	/**
	 * 模拟新闻
	 * @return
	 */
	private String[] initNews() {
		newsList = new ArrayList<News>();
		News news1 = new News();
		news1.setType(NEWS_TYPE_REGISTER);
		news1.setDescription("南阳豫南广告公司加入");
		newsList.add(news1);
		News news2 = new News();
		news2.setType(NEWS_TYPE_BID);
		news2.setDescription("邓州市李总1万张名片印刷");
		newsList.add(news2);
		News news3 = new News();
		news3.setType(NEWS_TYPE_ADVERTISING);
		news3.setDescription("新野公交车集团15路车体广告");
		newsList.add(news3);
		News news4 = new News();
		news4.setType(NEWS_TYPE_BID);
		news4.setDescription("南阳市卧龙区云指印务大厦楼顶霓虹灯制作");
		newsList.add(news4);
		News news5 = new News();
		news5.setType(NEWS_TYPE_ADVERTISING);
		news5.setDescription("南阳市李先生车辆后窗广告位招商 每月800元");
		newsList.add(news5);
		News news6 = new News();
		news6.setType(NEWS_TYPE_HOBBY);
		news6.setDescription("南阳市体校学生暑假工找工作 每天50元");
		newsList.add(news6);
		News news7 = new News();
		news7.setType(NEWS_TYPE_REGISTER);
		news7.setDescription("邓州市枫林广告设计公司加入");
		newsList.add(news7);
		News news8 = new News();
		news8.setType(NEWS_TYPE_PRINTING);
		news8.setDescription("蛋糕包装盒印刷制作500个");
		newsList.add(news8);
		News news9 = new News();
		news9.setType(NEWS_TYPE_ADVERTISING);
		news9.setDescription("我想在南阳火车站投放广告");
		newsList.add(news9);
		
		String[] allNews = new String[newsList.size()];
		for (int i = 0; i < newsList.size(); i++) {
			News news = newsList.get(i);
			allNews[i] = "【" + getNewsTypeName(news.getType()) + "】 " + news.getDescription();
		}
		return allNews;
	}

	private String getNewsTypeName(int type) {
		String typeName = null;
		switch (type) {
		case NEWS_TYPE_REGISTER:
			typeName = "云指印务";
			break;

		case NEWS_TYPE_ADVERTISING:
			typeName = "广告投放";
			break;

		case NEWS_TYPE_PRINTING:
			typeName = "印刷印务";
			break;

		case NEWS_TYPE_BID:
			typeName = "投标竞标";
			break;

		case NEWS_TYPE_HOBBY:
			typeName = "印刷周边";
			break;

		default:
			break;
		}
		return typeName;
	}

	private void initAdData() {
		adList = getBannerAd();
		imageViews = new ArrayList<ImageView>();
		dots = new ArrayList<View>();
		dotList = new ArrayList<View>();
		
		dot0 = homeView.findViewById(R.id.view_dot0);
		dot1 = homeView.findViewById(R.id.view_dot1);
		dot2 = homeView.findViewById(R.id.view_dot2);
		dot3 = homeView.findViewById(R.id.view_dot3);
		dot4 = homeView.findViewById(R.id.view_dot4);
		
		dots.add(dot0);
		dots.add(dot1);
		dots.add(dot2);
		dots.add(dot3);
		dots.add(dot4);
		
		viewPager = (ViewPager) homeView.findViewById(R.id.viewPager);
		
		bannerPageAdapter = new BannerPageAdapter(adList, imageViews);
		onBannerPageChangeListener = new OnBannerPageChangeListener(adList, homeView, dots, currentItem);
		viewPager.setAdapter(bannerPageAdapter);
		viewPager.addOnPageChangeListener(onBannerPageChangeListener);
		addDynamicView();
	}
	
	private void addDynamicView() {
		//动态添加图片和下面指示的圆点
		//初始化图片资源
		for (int i = 0; i < adList.size(); i++) {
			ImageView imageView = new ImageView(this.getActivity());
			//异步加载图片
			imageLoader.displayImage(adList.get(i).getImgUrl(), imageView, options);
			imageView.setScaleType(ScaleType.CENTER_CROP);
			imageViews.add(imageView);
			dots.get(i).setVisibility(View.VISIBLE);
			dotList.add(dots.get(i));
		}
	}

	private void startAd() {
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		//当fragment显示出来后，每隔两秒钟切换一次图片显示
		scheduledExecutorService.scheduleAtFixedRate(new ScrollTask(), 1, 2, TimeUnit.SECONDS);
	}

	private class ScrollTask implements Runnable {
		@Override
		public void run() {
			synchronized(viewPager) {
				currentItem = (currentItem + 1) % imageViews.size();
				handler.obtainMessage().sendToTarget();
			}
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		//当Activity不可见时停止切换
		scheduledExecutorService.shutdown();
	}
	
	private void initImageLoader() {
		File cacheDir = com.nostra13.universalimageloader.utils.StorageUtils
				.getOwnCacheDirectory(this.getActivity(), IMAGE_CACHE_PATH);
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true).cacheOnDisc(true).build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this.getActivity())
				.defaultDisplayImageOptions(defaultOptions)
				.memoryCache(new LruMemoryCache(12*1024*1024))
				.memoryCacheSize(12*1024*1024)
				.discCacheSize(32*1024*1024).discCacheFileCount(100)
				.discCache(new UnlimitedDiscCache(cacheDir))
				.threadPriority(Thread.NORM_PRIORITY -2)
				.tasksProcessingOrder(QueueProcessingType.LIFO).build();
		ImageLoader.getInstance().init(config);
	}
	
	/**
	 * 模拟数据
	 * @return
	 */
	public static List<Advertisement> getBannerAd(){
		List<Advertisement> adList = new ArrayList<Advertisement>();
		Advertisement advertisement1 = new Advertisement();
		advertisement1.setDate("12月21日");
		advertisement1.setTitle("云指印务上线了");
		advertisement1.setAuthor("By WaTer");
		advertisement1.setImgUrl("http://g.hiphotos.baidu.com/image/w%3D310/sign=bb99d6add2c8a786be2a4c0f5708c9c7/d50735fae6cd7b8900d74cd40c2442a7d9330e29.jpg");
		adList.add(advertisement1);

		Advertisement advertisement2 = new Advertisement();
		advertisement2.setDate("12月22日");
		advertisement2.setTitle("云指印务上线了");
		advertisement2.setAuthor("By WaTer");
		advertisement2.setImgUrl("http://g.hiphotos.baidu.com/image/w%3D310/sign=7cbcd7da78f40ad115e4c1e2672e1151/eaf81a4c510fd9f9a1edb58b262dd42a2934a45e.jpg");
		adList.add(advertisement2);

		Advertisement advertisement3 = new Advertisement();
		advertisement3.setDate("12月23日");
		advertisement3.setTitle("云指印务上线了");
		advertisement3.setAuthor("By WaTer");
		advertisement3.setImgUrl("http://e.hiphotos.baidu.com/image/w%3D310/sign=392ce7f779899e51788e3c1572a6d990/8718367adab44aed22a58aeeb11c8701a08bfbd4.jpg");
		adList.add(advertisement3);

		Advertisement advertisement4 = new Advertisement();
		advertisement4.setDate("12月24日");
		advertisement4.setTitle("云指印务上线了");
		advertisement4.setAuthor("By WaTer");
		advertisement4.setImgUrl("http://d.hiphotos.baidu.com/image/w%3D310/sign=54884c82b78f8c54e3d3c32e0a282dee/a686c9177f3e670932e4cf9338c79f3df9dc55f2.jpg");
		adList.add(advertisement4);

		Advertisement advertisement5 = new Advertisement();
		advertisement5.setDate("12月25日");
		advertisement5.setTitle("云指印务上线了");
		advertisement5.setAuthor("By WaTer");
		advertisement5.setImgUrl("http://e.hiphotos.baidu.com/image/w%3D310/sign=66270b4fe8c4b7453494b117fffd1e78/0bd162d9f2d3572c7dad11ba8913632762d0c30d.jpg");
		adList.add(advertisement5);
		return adList;
	}
}
