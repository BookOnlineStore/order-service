package com.bookstore.orderservice.order.domain;

import com.bookstore.orderservice.config.VNPayUtils;
import com.bookstore.orderservice.order.web.dto.VNPayDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class VNPayService {

    private static final Logger log = LoggerFactory.getLogger(VNPayService.class);
    private static String vnp_PayUrl;
    private static String vnp_ReturnUrl;
    private static String vnp_TmnCode ;
    private static String secretKey;
    private static String vnp_Version;
    private static String vnp_Command;
    private static String orderType;

    public VNPayService(@Value("${bookstore.vnPay.api-url}") String vnpPayUrl,
                        @Value("${bookstore.vnPay.return-url}") String vnpReturnUrl,
                        @Value("${bookstore.vnPay.tmn-code}") String vnpTmnCode,
                        @Value("${bookstore.vnPay.secret-key}") String secretKey,
                        @Value("${bookstore.vnPay.version}") String vnpVersion,
                        @Value("${bookstore.vnPay.command}") String vnpCommand,
                        @Value("${bookstore.vnPay.order-type}") String orderType) {
        vnp_PayUrl = vnpPayUrl;
        vnp_ReturnUrl = vnpReturnUrl;
        vnp_TmnCode = vnpTmnCode;
        this.secretKey = secretKey;
        vnp_Version = vnpVersion;
        vnp_Command = vnpCommand;
        this.orderType = orderType;
    }

    public String generatePaymentUrl(HttpServletRequest request, Long price) {
        long finalPrice = price * 100L;
        String bankCode = request.getParameter("bankCode");
        Map<String, String> vnpParamsMap = getVNPayPayload();
        vnpParamsMap.put("vnp_Amount", String.valueOf(finalPrice));
        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
        }
        vnpParamsMap.put("vnp_IpAddr", VNPayUtils.getIpAddress(request));
        //build query url
        String queryUrl = VNPayUtils.getPaymentURL(vnpParamsMap, true);
        String hashData = VNPayUtils.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VNPayUtils.hmacSHA512(secretKey, hashData);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        log.warn("vnp_PayUrl {}", vnp_PayUrl);
        String paymentUrl = vnp_PayUrl + "?" + queryUrl;
        return paymentUrl;
    }

    private static Map<String, String> getVNPayPayload() {
        var vnpParamsMap = new HashMap<String, String>();
        vnpParamsMap.put("vnp_Version", vnp_Version);
        vnpParamsMap.put("vnp_Command", vnp_Command);
        vnpParamsMap.put("vnp_TmnCode", vnp_TmnCode);
        vnpParamsMap.put("vnp_CurrCode", "VND");
        vnpParamsMap.put("vnp_TxnRef",  VNPayUtils.getRandomNumber(8));
        vnpParamsMap.put("vnp_OrderInfo", "Thanh toan don hang:" +  VNPayUtils.getRandomNumber(8));
        vnpParamsMap.put("vnp_OrderType", orderType);
        vnpParamsMap.put("vnp_Locale", "vn");
        vnpParamsMap.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnpParamsMap.putAll(getTime());
        return vnpParamsMap;
    }

    private static Map<String, String> getTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        // Lấy thời gian hiện tại (ZonedDateTime) tại múi giờ "Asia/Ho_Chi_Minh"
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        String vnpCreateDate = formatter.format(now);

        // Tính thời gian hết hạn sau 15 phút (ZonedDateTime)
        ZonedDateTime fifteenMinutesLater = now.plusMinutes(15);
        String vnpExpireDate = formatter.format(fifteenMinutesLater);

        Map<String, String> vnpParamsMap = new HashMap<>();
        vnpParamsMap.put("vnp_CreateDate", vnpCreateDate);
        vnpParamsMap.put("vnp_ExpireDate", vnpExpireDate);
        return vnpParamsMap;
    }
}

