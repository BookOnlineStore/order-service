package com.bookstore.orderservice.order.web;

import com.bookstore.orderservice.order.domain.VNPayService;
import com.bookstore.orderservice.order.web.dto.VNPayDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping(value = "/payment", produces = "application/json")
public class VnpayController {

    private static final Logger log = LoggerFactory.getLogger(VnpayController.class);
    private final VNPayService vnPayService;
    private final ObjectMapper objectMapper;

    public VnpayController(VNPayService vnPayService, ObjectMapper objectMapper) {
        this.vnPayService = vnPayService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/vnpay")
    public ResponseEntity<?> pay(HttpServletRequest req, @RequestParam(value = "price") Long price) throws JsonProcessingException {
        log.info("VNPayController attempt generate to paymentUrl with (price={}, req: {})", price, req);
        String paymentUrl = vnPayService.generatePaymentUrl(req, price);
        log.info("VNPayController result of processing to generate to paymentUrl is (result={}, paymentUrl={})",
                paymentUrl != null ? "true" : "false", paymentUrl);
        return ResponseEntity.ok(
                objectMapper.writeValueAsString(Map.of("paymentUrl", paymentUrl))
        );
    }

    @GetMapping("/vnpay-return")
    public VNPayDto payCallbackHandler(HttpServletRequest request) {
        log.info("VNPayController attempt to handle payment callback");
        String status = request.getParameter("vnp_ResponseCode");
        if (status.equals("00")) {
            return new VNPayDto(0, "Success", "");
        } else {
            return new VNPayDto(400, "Bad request", null);
        }
    }

}
