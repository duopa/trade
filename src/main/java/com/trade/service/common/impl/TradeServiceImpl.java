package com.trade.service.common.impl;

import com.alibaba.fastjson.JSON;
import com.trade.capital.CapitalManager;
import com.trade.config.TradeConstantConfig;
import com.trade.service.common.LogFormatService;
import com.trade.service.common.TradeService;
import com.trade.vo.DailyVo;
import com.trade.vo.OrderVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author georgy
 * @Date 2020-01-14 下午 2:40
 * @DESC 交易服务
 */
@Service
public class TradeServiceImpl implements TradeService {

    @Autowired
    private TradeConstantConfig tradeConstantConfig;
    @Autowired
    private LogFormatService logFormatService;

    Logger logger = LoggerFactory.getLogger(getClass());
    Logger tradeLogger = LoggerFactory.getLogger("trade");
    Logger todayTradeLogger = LoggerFactory.getLogger("todayTrade");

    @Override
    public synchronized void open(DailyVo daily, OrderVo orderVo, boolean isUsedCapitail) {
        // 判断现在的可用资金是否满足 订单金额
        if(isUsedCapitail && CapitalManager.assetVo.getUsableCapital().compareTo(orderVo.getPrice().multiply(orderVo.getVolume())) < 0){
            tradeLogger.error("可用金额不足，可用金额:{}, 订单金额:{}", CapitalManager.assetVo.getUsableCapital(), orderVo.getPrice().multiply(orderVo.getVolume()));
            return;
        }

        // 开仓 - 保存订单
        CapitalManager.tradeOrders.add(orderVo);
        // 冻结金额
        if(isUsedCapitail){
            this.doFrozenCapital(orderVo.getPrice().multiply(orderVo.getVolume()));
        }

        // 记录交易日志
        logFormatService.logOpen(tradeLogger, daily, orderVo);
        if(daily.getTrade_date().equals(tradeConstantConfig.getToday())){ // 记录今天的交易日志
            logFormatService.logOpen(todayTradeLogger, daily, orderVo);
        }
    }

    @Override
    public synchronized void close(DailyVo daily, OrderVo orderVo, boolean isUsedCapitail) {
        // 获取收盘价
        BigDecimal closePrice = new BigDecimal(daily.getClose());

        // 移除仓位
        CapitalManager.tradeOrders.remove(orderVo);

        // 核算资金
        if(isUsedCapitail){
            BigDecimal bp = BigDecimal.ZERO;
            // 计算交易损益(BP)
            if(orderVo.getDirection() == 1){ // 多头头寸平仓计算 BP
                bp = closePrice.subtract(orderVo.getPrice()).multiply(orderVo.getVolume());

            }else if(orderVo.getDirection() == 0){ // 空头头寸平仓计算 BP
                bp = orderVo.getPrice().subtract(closePrice).multiply(orderVo.getVolume());

            }else{
                throw new RuntimeException("数据错误: 交易订单没有方向");
            }

            this.calTotalCapital(bp); // 核算总资金
            this.doFrozenCapital(orderVo.getPrice().multiply(orderVo.getVolume()).negate()); // 释放锁定资金
        }

        // 记录交易日志
        logFormatService.logClose(tradeLogger, daily, orderVo);
        if(daily.getTrade_date().equals(tradeConstantConfig.getToday())){ // 记录今天的交易日志
            logFormatService.logClose(todayTradeLogger, daily, orderVo);
        }

    }

    /**
     * 获取交易池中的订单
     * @param tsCode
     * @return
     */
    @Override
    public synchronized OrderVo getOrderVo(String tsCode){
        if(!CollectionUtils.isEmpty(CapitalManager.tradeOrders)){
            for (OrderVo orderVo : CapitalManager.tradeOrders) {
                if(orderVo.getTsCode().equals(tsCode)){
                    return orderVo;
                }
            }
        }
        return null;
    }

    /**
     * 获取总资金
     * @return
     */
    @Override
    public BigDecimal getTotalCapital(){
        return CapitalManager.assetVo.getTotalCapital();
    }

    /**
     * 核算总资金
     * @return
     */
    @Override
    public synchronized void calTotalCapital(BigDecimal bp){
        CapitalManager.assetVo.setTotalCapital(CapitalManager.assetVo.getTotalCapital().add(bp));
    }

    /**
     * 冻结金额操作 - 正数冻结，负数释放
     */
    public synchronized void doFrozenCapital(BigDecimal capital){
        logger.info("冻结金额:{}" , capital.doubleValue());
        CapitalManager.assetVo.setFrozenCapital(CapitalManager.assetVo.getFrozenCapital().add(capital));

    }



    /**
     * 获取风险系数
     * @return
     */
    @Override
    public BigDecimal getRiskParameter(){
        return CapitalManager.assetVo.getRiskParameter();
    }

    /**
     * 获取交易池
     * @return
     */
    @Override
    public List<OrderVo> getTradeOrders(){
        return CapitalManager.tradeOrders;
    }

    /**
     * 判断是否持仓
     * @param orderVo
     * @return
     */
    @Override
    public Boolean isHoldPosition(OrderVo orderVo) {
        return orderVo != null;
    }

}
