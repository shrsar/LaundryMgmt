package com.laundrymgmt.modern.controller;

import com.laundrymgmt.modern.dto.OrderDtos;
import com.laundrymgmt.modern.service.OrderService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderDtos.OrderResponse> listOrders(
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return orderService.listOrders(authorizationHeader);
    }

    @PostMapping
    public OrderDtos.OrderResponse createOrder(
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
        @RequestBody OrderDtos.OrderRequest request
    ) {
        return orderService.createOrder(authorizationHeader, request);
    }

    @PostMapping("/{orderCode}/receive")
    public OrderDtos.OrderResponse markReceived(
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
        @PathVariable String orderCode
    ) {
        return orderService.markReceived(authorizationHeader, orderCode);
    }
}
