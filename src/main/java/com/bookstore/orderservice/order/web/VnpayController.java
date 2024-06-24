package com.bookstore.orderservice.order.web;

import com.bookstore.orderservice.order.Order;
import com.bookstore.orderservice.order.OrderService;
import com.bookstore.orderservice.order.vnpay.VNPayService;
import com.bookstore.orderservice.order.vnpay.VNPayStatusCodeEnum;
import com.bookstore.orderservice.order.dto.PaymentRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/payment", produces = "application/json")
public class VnpayController {

    private static final Logger log = LoggerFactory.getLogger(VnpayController.class);
    private final VNPayService vnPayService;
    private final ObjectMapper objectMapper;
    private final OrderService orderService;

    public VnpayController(VNPayService vnPayService,
                           ObjectMapper objectMapper,
                           OrderService orderService) {
        this.vnPayService = vnPayService;
        this.objectMapper = objectMapper;
        this.orderService = orderService;
    }

    @PostMapping("/vnpay")
    public ResponseEntity<?> pay(HttpServletRequest req, @Valid @RequestBody PaymentRequest paymentRequest)
            throws JsonProcessingException {
        log.info("VNPayController attempt generate to paymentUrl with (orderId={}, req: {})", paymentRequest.orderId(), req);
        String paymentUrl = vnPayService.generatePaymentUrl(req, paymentRequest);
        log.info("VNPayController result of processing to generate to paymentUrl is (result={}, paymentUrl={})",
                paymentUrl != null ? "true" : "false", paymentUrl);
        return ResponseEntity.ok(
                objectMapper.writeValueAsString(Map.of("paymentUrl", paymentUrl))
        );
    }

    @GetMapping("/vnpay-return")
    public Order payCallbackHandler(HttpServletRequest request) {
        log.info("VNPayController attempt to handle payment callback with (request={})", request);
        String status = request.getParameter("vnp_ResponseCode");
        log.info("status is {}", status);
        return Optional.ofNullable(VNPayStatusCodeEnum.isMember(status))
                .map(vnPayStatusCodeEnum -> vnPayStatusCodeEnum.handleCallback(orderService, request))
                .orElseThrow(() -> new RuntimeException("Status code of VNPAY not matched!!!"));
    }

}
