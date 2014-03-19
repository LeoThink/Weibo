package com.leo.wb;

import java.text.SimpleDateFormat;

import com.leo.wb.utils.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 有三种获取Token方式应用签名，sso，通过code获得
 * 本实例通过应用签名获得AccessToken
 * @author MinKing
 *
 */
public class WBAuthActivity extends Activity {
	private static final String TAG="WBAuthActivity";
	/**
     * WeiboSDKDemo 程序的 APP_SECRET。
     * 请注意：请务必妥善保管好自己的 APP_SECRET，不要直接暴露在程序中，此处仅作为一个DEMO来演示。
     */
    private static final String WEIBO_DEMO_APP_SECRET = "4e47e691a516afad0fc490e05ff70ee5";
    
    /** 通过 code 获取 Token 的 URL */
    private static final String OAUTH2_ACCESS_TOKEN_URL = "https://open.weibo.cn/oauth2/access_token";
    
	/** UI组件 */
	private TextView testAccessToken;
	private Button getCode;
	
	/** 获取Token成功或失败的消息 */
	private static final int MSG_FETCH_TOKEN_SUCCESS = 1;
    private static final int MSG_FETCH_TOKEN_FAILED  = 2;
	
    /** 微博web授权接口类，提供登陆等功能 */
    private WeiboAuth mWeiboAuth;
    /** 获取到的code */
    private String mCode;
    /** 获取到的Token */
    private Oauth2AccessToken mAccessToken;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auth);
		
		//初始化控件
		getCode=(Button)findViewById(R.id.get_token_btn);
		testAccessToken=(TextView)findViewById(R.id.access_token);
		
		//初始化微博对象
		mWeiboAuth=new WeiboAuth(this,Constants.APP_KEY,Constants.REDIRECT_URL,Constants.SCOPE);
		
		//通过应用签名获取AccessToken
		getCode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mWeiboAuth.anthorize(new AuthListener());
				
			}
		});
		//从sharedpreference中取出上次保存的Token
		mAccessToken=AccessTokenKeeper.readAccessToken(this);
		if(mAccessToken.isSessionValid()){
			updateTokenView();
		}
	}
	
	
	/**
     * 微博认证授权回调类。
     */
	class AuthListener implements WeiboAuthListener{
		@Override
		public void onComplete(Bundle values) {
			//从Bundle中解析Token
			mAccessToken=Oauth2AccessToken.parseAccessToken(values);
			if(mAccessToken.isSessionValid()){
				//show Token
				updateTokenView();
				
				//保存Token到sharedPreferences
				AccessTokenKeeper.writeAccessToken(WBAuthActivity.this, mAccessToken);
				
				Toast.makeText(WBAuthActivity.this, "成功获取Token", Toast.LENGTH_LONG);
			}else{
				// 当您注册的应用程序签名不正确时，就会收到 Code，请确保签名正确
                String code = values.getString("code");
                String message = "应用程序签名不正确";
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                Toast.makeText(WBAuthActivity.this, message, Toast.LENGTH_LONG).show();
			}
		}
		
		@Override
		public void onCancel() {
			Toast.makeText(WBAuthActivity.this, "toast_auth_canceld", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onWeiboException(WeiboException arg0) {
			Toast.makeText(WBAuthActivity.this, "Exception:"+arg0, Toast.LENGTH_LONG).show();
		}
		
	}
	
	/**
	 * 更新页面Token信息
	 */
	private void updateTokenView(){
		String date=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new java.util.Date(mAccessToken.getExpiresTime()));
		testAccessToken.setText("Token:"+mAccessToken.getToken()+"\n Expire:"+date);
		testAccessToken.setText(String.format("Token：%1$s \n有效期：%2$s", mAccessToken.getToken(),date));
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	

}
