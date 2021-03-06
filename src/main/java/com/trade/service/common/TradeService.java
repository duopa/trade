package com.trade.service.common;

import com.trade.vo.AssetVo;
import com.trade.vo.DailyVo;
import com.trade.vo.OrderVo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author georgy
 * @Date 2020-01-14 下午 2:38
 * @DESC TODO
 */
public interface TradeService {

    void open(DailyVo daily, OrderVo orderVo);

    void close(DailyVo daily, OrderVo orderVo);

    OrderVo getOrderVo(String tsCode);

    BigDecimal getTotalCapital();

    void calTotalCapital(BigDecimal bp);

    BigDecimal getRiskParameter();

    List<OrderVo> getTradeOrders();

    Boolean isHoldPosition(OrderVo orderVo);

    Boolean allowOpen(DailyVo daily, OrderVo orderVo);

    Boolean allowClose(DailyVo daily, OrderVo orderVo);

    String selectOpenStrategy(String strategy);

    String selectCloseStrategy(String strategy);
}
