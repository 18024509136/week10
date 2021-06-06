package com.geek;

import com.geek.dto.Order;
import com.geek.stub.OrderService;
import org.springframework.stereotype.Service;

/**
 * 客户端测试类
 */
@Service
public class TestService {

    @RpcReference
    private OrderService orderService;

    public void test() {
        Order order = new Order();
        order.setTotalAmount(2);
        order.setTotalPrice(20000);
        String orderNo = orderService.createOrder(order);
        System.out.println("新建订单的订单号：" + orderNo);

        Order orderFind = orderService.getByOrderNo("111111");
        System.out.println("查询订单：" + orderFind);
    }
}
