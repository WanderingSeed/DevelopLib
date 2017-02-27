package com.morgan.app.account.data;

import com.morgan.app.account.model.TransferRecord;
import com.morgan.lib.util.PreferenceUtil;

/**
 * 用于存储一些全局的股票相关的数据
 * 
 * @author Morgan.ji
 * @version 1.0
 * @date 2016-06-16
 */
public class StockDataManager {

    private static StockDataManager mInstance;
    private static final String KEY_TRANSFER_IN = "key_transfer_in";
    private static final String KEY_TRANSFER_OUT = "key_transfer_out";
    private float mTotalTransferIn = 0.0f; // 总转入
    private float mTotalTransferOut = 0.0f; // 总转出

    private StockDataManager() {
        mTotalTransferIn = PreferenceUtil.getInstance().getFloat(KEY_TRANSFER_IN, 0);
        mTotalTransferOut = PreferenceUtil.getInstance().getFloat(KEY_TRANSFER_OUT, 0);
    }

    public static StockDataManager getInstance() {
        if (null == mInstance) {
            mInstance = new StockDataManager();
        }
        return mInstance;
    }

    /**
     * 获取所有转入资产
     * 
     * @return
     */
    public float getTotalTransferIn() {
        return mTotalTransferIn;
    }

    /**
     * 设置所有转入资产
     * 
     * @param totalTransferIn
     */
    public void setTotalTransferIn(float totalTransferIn) {
        this.mTotalTransferIn = totalTransferIn;
        PreferenceUtil.getInstance().putFloat(KEY_TRANSFER_IN, totalTransferIn);
    }

    /**
     * 获取所有转出资产
     * 
     * @return
     */
    public float getTotalTransferOut() {
        return mTotalTransferOut;
    }

    /**
     * 设置所有转出资产
     * 
     * @param totalTransferOut
     */
    public void setTotalTransferOut(float totalTransferOut) {
        this.mTotalTransferOut = totalTransferOut;
        PreferenceUtil.getInstance().putFloat(KEY_TRANSFER_OUT, totalTransferOut);
    }

    /**
     * 获取总投资，即转入减转出
     * 
     * @return
     */
    public float getTotalInvestment() {
        return mTotalTransferIn - mTotalTransferOut;
    }

    /**
     * 在添加转账记录的时候调用
     * 
     * @param record
     *            新的转账记录
     */
    public void onAddTransferRecord(TransferRecord record) {
        switch (record.getType()) {
        case IN:
            setTotalTransferIn(getTotalTransferIn() + record.getMoney());
            break;
        case OUT:
            setTotalTransferOut(getTotalTransferOut() + record.getMoney());
            break;
        default:
            break;
        }
    }
}
