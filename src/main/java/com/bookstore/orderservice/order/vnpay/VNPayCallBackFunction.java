package com.bookstore.orderservice.order.vnpay;

import com.bookstore.orderservice.order.Order;
import com.bookstore.orderservice.order.OrderService;

import javax.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface VNPayCallBackFunction {
    Order handle(OrderService orderService, HttpServletRequest request);
}
