package com.open.hule.mobilepayment.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.open.hule.mobilepayment.PayDialogActivity;
import com.open.hule.mobilepayment.entity.WXPayEntryEntity;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.greenrobot.eventbus.EventBus;

/**
 * @author hule
 * @date 2019/7/29 15:58
 * description: 微信支付回调
 */
public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        api = WXAPIFactory.createWXAPI(this, PayDialogActivity.APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {

    }

    @Override
    public void onResp(BaseResp resp) {
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            //通知我们发起支付调用的页面
            EventBus.getDefault().post(new WXPayEntryEntity(resp.errCode));
        }
        // 清除动画，有助于防止黑屏闪烁
        overridePendingTransition(0, 0);
        finish();
    }
}
