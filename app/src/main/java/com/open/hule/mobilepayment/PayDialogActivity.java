package com.open.hule.mobilepayment;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.alipay.sdk.app.PayTask;
import com.open.hule.mobilepayment.entity.WXPayEntryEntity;
import com.open.hule.mobilepayment.utlis.PayResult;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.Map;

/**
 * @author hule
 * @date 2019/8/23 16:15
 * description: 支付弹框
 */
public class PayDialogActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AppCompatActivity";

    public static String APP_ID = "wxd930ea5d5a258f4f";

    /**
     * 支付宝结果
     */
    private static final int SDK_PAY_FLAG = 1;

    /**
     * 支付成功
     */
    private static final String CODE_9000 = "9000";

    /**
     * 支付宝回调
     */
    private PayHandler payHandler;

    /**
     * 微信支付
     */
    private IWXAPI api;

    /**
     * 钱包支付
     */
    private static final String WALLET_PAY = "1";
    /**
     * 支付宝支付
     */
    private static final String ALI_PAY = "2";
    /**
     * 微信支付
     */
    private static final String WECHAT_PAY = "3";

    /**
     * 扣款金额
     */
    private String price;

    /**
     * 余额，钱包可用余额
     */
    private String balance;

    /**
     * 默认当前的支付方式
     */
    private String currentPay = WECHAT_PAY;

    private RadioButton rbWalletPay;
    private LinearLayout llWallet;
    private RadioButton rbWechatPay;
    private RadioButton rbAliPay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_pay_custom);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        payHandler = new PayHandler(this);
        api = WXAPIFactory.createWXAPI(this, APP_ID);
        initView();
    }

    /**
     * 初始化view
     */
    private void initView() {
        TextView tvPayMoney = findViewById(R.id.tvPayMoney);
        TextView tvWalletAvailable = findViewById(R.id.tvWalletAvailable);
        rbWalletPay = findViewById(R.id.rbWallet);
        llWallet = findViewById(R.id.llWallet);
        rbWalletPay = findViewById(R.id.rbWallet);
        rbWechatPay = findViewById(R.id.rbWechatPay);
        rbAliPay = findViewById(R.id.rbAliPay);

        llWallet.setOnClickListener(this);
        findViewById(R.id.llAliPay).setOnClickListener(this);
        findViewById(R.id.rgPay).setOnClickListener(this);
        findViewById(R.id.llWechatPay).setOnClickListener(this);
        findViewById(R.id.btnGoPay).setOnClickListener(this);
        findViewById(R.id.ivClose).setOnClickListener(this);

        price = getIntent().getExtras().getString("price", "0");
        balance = getIntent().getExtras().getString("balance", "0");
        BigDecimal listenPriceBdl = new BigDecimal(price != null ? price : "0");
        BigDecimal balanceBdl = new BigDecimal(balance != null ? balance : "0");
        // 如果钱包余额 大于 支付金额，默认选中钱包支付
        if (listenPriceBdl.compareTo(balanceBdl) > 0) {
            currentPay = WECHAT_PAY;
            llWallet.setEnabled(false);
        } else {
            currentPay = WALLET_PAY;
            llWallet.setEnabled(true);
        }
        updateChecked();
        String payReally = "实付金额" + price + "元";
        SpannableString spannableString = new SpannableString(payReally);
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#999999")), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new AbsoluteSizeSpan(14, true), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE4E4E")), 4, payReally.indexOf("元"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new AbsoluteSizeSpan(24, true), 4, payReally.indexOf("元"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#EE4E4E")), payReally.indexOf("元"), payReally.indexOf("元"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new AbsoluteSizeSpan(15, true), payReally.indexOf("元"), payReally.indexOf("元"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPayMoney.setText(spannableString);
        tvWalletAvailable.setText(String.format(getResources().getString(R.string.pay_available), balance));
    }

    /**
     * 更改支付状态
     */
    private void updateChecked() {
        switch (currentPay) {
            case WALLET_PAY:
                rbWalletPay.setChecked(true);
                rbAliPay.setChecked(false);
                rbWechatPay.setChecked(false);
                break;
            case WECHAT_PAY:
                rbWalletPay.setChecked(false);
                rbAliPay.setChecked(false);
                rbWechatPay.setChecked(true);
                break;
            case ALI_PAY:
                rbWalletPay.setChecked(false);
                rbAliPay.setChecked(true);
                rbWechatPay.setChecked(false);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivClose:
                finishAnimation();
                break;
            case R.id.llWallet:
                // 錢包
                currentPay = WALLET_PAY;
                updateChecked();
                break;
            case R.id.llWechatPay:
                // 微信支付
                currentPay = WECHAT_PAY;
                updateChecked();
                break;
            case R.id.llAliPay:
                // 支付寶支付
                currentPay = ALI_PAY;
                updateChecked();
                break;
            case R.id.btnGoPay:
                // TODO 立即支付,调用服务器接口，产生预支付订单，然后根据支付方式调用相关的支付
                // 以下只是模拟支付逻辑
                // 服务器获取的预支付订单信息
                String payInfo = "";
                if (ALI_PAY == currentPay) {
                    // 支付宝支付
                    aliPay(payInfo);
                } else if (WECHAT_PAY == currentPay) {
                    // 微信支付
                    wechatPay(payInfo);
                } else if (WALLET_PAY == currentPay) {
                    // 钱包支付
                    showToast(this,"钱包支付成功");
                    finishAnimation();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 支付宝回调
     */
    static class PayHandler extends Handler {

        private final WeakReference<PayDialogActivity> payDialogActivityWrf;

        private PayHandler(PayDialogActivity payDialogActivityWrf) {
            this.payDialogActivityWrf = new WeakReference<>(payDialogActivityWrf);
        }

        @Override
        public void handleMessage(Message msg) {
            if (SDK_PAY_FLAG == msg.what) {
                @SuppressWarnings("unchecked")
                PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                //对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                String resultStatus = payResult.getResultStatus();
                // 判断resultStatus 为9000则代表支付成功
                if (TextUtils.equals(resultStatus, CODE_9000)) {
                    // TODO 该笔订单是否真实支付成功，需要依赖[服务端的异步通知]。
                    // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                    if (payDialogActivityWrf.get() != null) {
                        payDialogActivityWrf.get().showAlert(payDialogActivityWrf.get(), "支付成功！");
                    }
                } else {
                    // TODO 该笔订单是否真实支付成功，需要依赖[服务端的异步通知]。
                    if (payDialogActivityWrf.get() != null) {
                        payDialogActivityWrf.get().showAlert(payDialogActivityWrf.get(), "支付失败！");
                    }
                }
            }
        }
    }


    /**
     * 支付宝支付
     *
     * @param payInfo 预支付信息
     */
    private void aliPay(final String payInfo) {
        // 1.去服务器拿支付订单
        final Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                PayTask aliPay = new PayTask(PayDialogActivity.this);
                Map<String, String> result = aliPay.payV2(payInfo, true);
                Log.d(TAG, result.toString());
                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                payHandler.sendMessage(msg);
            }
        };

        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();

    }

    /**
     * 微信支付
     *
     * @param payInfo 预支付信息
     */
    private void wechatPay(final String payInfo) {
        try {
            JSONObject json = new JSONObject(payInfo);
            if (!json.has("retcode")) {
                PayReq req = new PayReq();
                // 测试用appId
                req.appId = APP_ID;
//                req.appId = json.getString("appid");
                req.partnerId = json.getString("partnerid");
                req.prepayId = json.getString("prepayid");
                req.nonceStr = json.getString("noncestr");
                req.timeStamp = json.getString("timestamp");
                req.packageValue = json.getString("package");
                req.sign = json.getString("sign");
                req.extData = "app data";
                Toast.makeText(PayDialogActivity.this, "正常调起支付", Toast.LENGTH_SHORT).show();
                // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
                api.sendReq(req);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            showToast(this, "异常：" + e.getMessage());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void payment(WXPayEntryEntity wxPayEntryEntity) {
        //TODO 当客户端收到微信支付成功的回调后，继续向后台服务器进行验证为最终的结果
        // .......此处省略调用服务器接口，查询结果逻辑......

        // 此处以下代码只是作为测试使用，这里需要去服务器异步查询结果
        switch (wxPayEntryEntity.getPayStatus()) {
            case BaseResp.ErrCode.ERR_OK:
                showToast(this, "支付成功");
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                showToast(this, "微信授权失败");
                break;
            case BaseResp.ErrCode.ERR_COMM:
                showToast(this, "订单支付失败！");
                break;
            case BaseResp.ErrCode.ERR_SENT_FAILED:
                showToast(this, "微信发送失败");
                break;
            case BaseResp.ErrCode.ERR_UNSUPPORT:
                showToast(this, "微信不支持");
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                showToast(this, "用户点击取消并返回");
                break;
            default:
                showToast(this, "订单支付失败！");
                break;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_UP == event.getAction()) {
            finishAnimation();
            return true;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 关闭动画
     */
    private void finishAnimation() {
        finish();
        overridePendingTransition(R.anim.push_bottom_silent, R.anim.push_bottom_out);
    }

    /**
     * 提示框
     *
     * @param activity activity
     * @param info     提示信息
     */
    private void showAlert(Activity activity, String info) {
        showAlert(activity, info, null);
    }

    /**
     * @param activity  activity
     * @param info      info 提示信息
     * @param onDismiss 监听
     */
    private void showAlert(Activity activity, String info, DialogInterface.OnDismissListener onDismiss) {
        new AlertDialog.Builder(activity)
                .setMessage(info)
                .setPositiveButton("确定", null)
                .setOnDismissListener(onDismiss)
                .show();
    }

    /**
     * 提示吐司框
     *
     * @param activity activity
     * @param msg      提示信息
     */
    private void showToast(Activity activity, String msg) {
        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }
}
