package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper {
    void insert(Orders orders);

    //TODO 这里处理得到该订单号
    Orders getByNumber(String orderNumber);
    //TODO 更新对应订单的状态
    void update(Orders orders);
}
