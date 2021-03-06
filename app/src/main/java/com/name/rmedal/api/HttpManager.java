package com.name.rmedal.api;


import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.name.rmedal.api.cookie.CookieJarImpl;
import com.name.rmedal.api.cookie.store.DBCookieStore;
import com.name.rmedal.tools.AppTools;
import com.veni.tools.VnUtils;
import com.veni.tools.baserx.BasicParamsInterceptor;
import com.veni.tools.baserx.DownloadInterceptor;
import com.veni.tools.baserx.DownloadListener;
import com.veni.tools.baserx.MyHttpLoggingInterceptor;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 作者：kkan on 2017/11/30
 * 当前类注释:
 * 使用Retrofit 封装 的网络请求
 * 用例:
 * HttpManager.getOkHttpUrlService() .compose(RxSchedulers.<HttpRespose<Bean>io_main())
 * .subscribe(new RxSubscriber<Bean>(mContext, "加载框信息,不传不显示") {
 * public void _onNext(Bean data) {
 * //处理返回数据,根据需要返回给页面
 * }
 * public void _onError(int code, String message) {
 * //处理异常数据
 * mView.onError(code, message);
 * }
 * });
 */
public class HttpManager {
    //读超时长，单位：毫秒
    private static final int READ_TIME_OUT = 17676;
    //连接时长，单位：毫秒
    private static final int CONNECT_TIME_OUT = 17676;
    private static final int DEFAULT_TIMEOUT = 15;
    private static final String TAG = HttpManager.class.getSimpleName();

    /*服务器跟地址*/
    private static final String BASE_URL = "https://www.baidu.com/";
    private volatile static HttpManager INSTANCE;

    private HttpUrlService httpUrlService;
    private HttpUrlService httpDownloadUrlService;


    /* 获取单例*/
    public static HttpManager getInstance() {
        if (INSTANCE == null) {
            synchronized (HttpManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new HttpManager(BASE_URL);
                }
            }
        }
        return INSTANCE;
    }

    /*--------------公共参数,只添加请求头--------------*/
    private String TOKEN = "";

    public HttpManager setToken(String token) {
        String oldtoken  = AppTools.getToken();
        if (!token.equals(oldtoken)) {
            INSTANCE = null;
        }
        AppTools.saveToken(VnUtils.getContext(), token);
        getInstance().TOKEN = token;
        return getInstance();
    }

    //构造方法私有
    private HttpManager(String BaseUrl) {
        //缓存
        File cacheFile = new File(VnUtils.getContext().getCacheDir(), "cache");
        Cache cache = new Cache(cacheFile, 1024 * 1024 * 100); //100Mb

        /*
        * 请求的拦截处理 添加公共参数
        * 请求头和请求参数都增加
        * */
        if (TextUtils.isEmpty(TOKEN)) {
            TOKEN = AppTools.getToken();
        }
        BasicParamsInterceptor.Builder builder = new BasicParamsInterceptor.Builder();
        if (!TextUtils.isEmpty(TOKEN)) {
            builder.addHeaderParams("token", TOKEN);//添加公共参数
        }
//        builder.addHeaderParams("login_type", "2");//
        BasicParamsInterceptor basicParamsInterceptor = builder.build();

        //开启Log
        MyHttpLoggingInterceptor logInterceptor = new MyHttpLoggingInterceptor();
        logInterceptor.setLevel(MyHttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(READ_TIME_OUT, TimeUnit.MILLISECONDS)
                .connectTimeout(CONNECT_TIME_OUT, TimeUnit.MILLISECONDS)
                .addInterceptor(basicParamsInterceptor)
                .addInterceptor(logInterceptor)
                .cookieJar(new CookieJarImpl(new DBCookieStore(VnUtils.getContext())))  //使用数据库保持cookie，如果cookie不过期，则一直有效
                //.cookieJar(new CookieJarImpl(new SPCookieStore(this)))            //使用sp保持cookie，如果cookie不过期，则一直有效
                //.cookieJar(new CookieJarImpl(new MemoryCookieStore()))           //使用内存保持cookie，app退出后，cookie消失
                .cache(cache)
                .build();

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").serializeNulls().create();
        Retrofit retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BaseUrl)
                .build();
        httpUrlService = retrofit.create(HttpUrlService.class);
    }

    /**
     * HttpUrlService
     */
    public HttpUrlService getOkHttpUrlService() {
        return getInstance().httpUrlService;
    }

    /**
     * HttpUrlService
     */
    public HttpUrlService getDownloadUrlService(String BaseUrl, DownloadListener downloadListener) {

        DownloadInterceptor mInterceptor = new DownloadInterceptor(downloadListener);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(mInterceptor)
                .build();

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").serializeNulls().create();
        Retrofit retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BaseUrl)
                .build();
        httpDownloadUrlService = retrofit.create(HttpUrlService.class);
        return getInstance().httpDownloadUrlService;
    }

}