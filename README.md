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
```
- DialogFragment弹框导致内存泄漏问题，采取Activity替代DialogFragment  
```
 <!--底部弹框动画方案-->
 <!--进入动画方案-->
  startActivity(intent);
  overridePendingTransition(R.anim.push_bottom_in, R.anim.push_bottom_silent);
 <!--淡出动画方案-->
  finish();
  overridePendingTransition(R.anim.push_bottom_silent, R.anim.push_bottom_out);
 <!--透明状态栏-->  
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
 <!--弹框界面支付样式-->
    <style name="PayTranslucentTheme" parent="Theme.AppCompat.Light.NoActionBar">
        <item name="android:windowBackground">@android:color/transparent</item>
        <item name="android:windowAnimationStyle">@null</item>
        <item name="android:windowIsTranslucent">true</item>
        <item name="android:windowNoTitle">true</item> <!-- 无标题 -->
        <item name="android:windowContentOverlay">@null</item>
        <item name="android:backgroundDimEnabled">true</item><!-- 半透明 -->
    </style>
```
