# MobilePayment
新版移动支付：微信支付&amp;支付宝支付demo
## 解决问题
- 部分机型微信回调返回短暂黑屏闪烁【三星s8,微信支付回调，点击取消或者直接返回】
- DialogFragment弹框导致内存泄漏问题
## 解决方案
- 黑屏闪烁，在WXPayEntryActivity回调界面在中增加透明主题  
```
    <!--解决微信支付回调部分机型黑屏闪烁的问题-->
    <style name="wxPayTheme" parent="Theme.AppCompat.Light.NoActionBar">
        <item name="android:windowBackground">@android:color/transparent</item>
        <item name="android:windowIsTranslucent">true</item>
        <item name="android:windowNoTitle">true</item>
    </style>
    
    <!--回调页面WXPayEntryActivity关闭finish的时候增加-->
     overridePendingTransition(0, 0);
    
    <!--回调页面WXPayEntryActivity配置-->
     <activity
            android:name=".wxapi.WXPayEntryActivity"
            android:configChanges="orientation|screenSize"
            android:exported="true"
            android:launchMode="singleTop"
            android:theme="@style/wxPayTheme"
            android:windowSoftInputMode="adjustPan|stateAlwaysHidden" />
```
- DialogFragment弹框导致内存泄漏问题，采取Activity替代DialogFragment  
```
底部弹框动画方案

  <!--底部弹框支付Activity主题样式-->
    <style name="PayTranslucentTheme" parent="Theme.AppCompat.Light.NoActionBar">
        <item name="android:windowBackground">@android:color/transparent</item>
        <item name="android:windowAnimationStyle">@null</item>
        <item name="android:windowIsTranslucent">true</item>
        <item name="android:windowNoTitle">true</item> <!-- 无标题 -->
        <item name="android:windowContentOverlay">@null</item>
        <item name="android:backgroundDimEnabled">true</item><!-- 半透明 -->
    </style>
    
<!-- R.anim.push_bottom_in 进入动画-->
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android">
    <translate
        android:duration="300"
        android:fromYDelta="100%p"
        android:toYDelta="0" />
</set>

<!-- R.anim.push_bottom_out 淡出动画-->
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android">
    <translate
        android:duration="300"
        android:fromYDelta="0"
        android:toYDelta="100%p" />
</set>

<!-- R.anim.push_bottom_silent 原点-->
<?xml version="1.0" encoding="utf-8"?>
<translate xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="300"
    android:fromYDelta="0"
    android:toYDelta="0" />
    
<!-- 底部弹框-->
startActivity(intent);
overridePendingTransition(R.anim.push_bottom_in,R.anim.push_bottom_silent);

<!-- 关闭底部弹框-->
finish();
overridePendingTransition(R.anim.push_bottom_silent,R.anim.push_bottom_out);

<!-- 实现透明状态栏-->
setContentView(R.layout.activity_dialog_pay_custom);
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    getWindow().setStatusBarColor(Color.TRANSPARENT);
    }
```
## 截图
![](https://github.com/NewHuLe/MobilePayment/blob/master/screenshots/device-2019-08-27-194019.png)
