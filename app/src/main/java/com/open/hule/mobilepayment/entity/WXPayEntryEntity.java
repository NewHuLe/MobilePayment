package com.open.hule.mobilepayment.entity;

/**
 * @author hule
 * @date 2019/8/23 15:34
 * description: 微信支付结果回调
 */
public class WXPayEntryEntity {
    /**
     * 微信回调的状态
     */
    private int payStatus;

    public WXPayEntryEntity(int errCode) {

        this.payStatus = errCode;
    }

    public int getPayStatus() {
        return payStatus;
    }
}
