package com.bookstore.orderservice.order;

import com.bookstore.orderservice.book.BookClient;
import com.bookstore.orderservice.book.BookDto;
import com.bookstore.orderservice.book.BookNotFoundException;
import com.bookstore.orderservice.book.InsufficientStockException;
import com.bookstore.orderservice.order.event.BooksPlacedMessage;
import com.bookstore.orderservice.order.event.OrderAcceptedMessage;
import com.bookstore.orderservice.order.event.OrderDispatchedMessage;
import com.bookstore.orderservice.order.dto.LineItemRequest;
import com.bookstore.orderservice.order.dto.UserInformation;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final LineItemRepository lineItemRepository;
    private final OrderRepository orderRepository;
    private final BookClient bookClient;
    private final StreamBridge streamBridge;

    public OrderService(OrderRepository orderRepository,
                        LineItemRepository lineItemRepository,
                        BookClient bookClient,
                        StreamBridge streamBridge) {
        this.lineItemRepository = lineItemRepository;
        this.orderRepository = orderRepository;
        this.bookClient = bookClient;
        this.streamBridge = streamBridge;
    }


    @Transactional
    public Order submitOrder(List<LineItemRequest> lineItems, UserInformation userInformation) {
        var order = new Order();
        order.setStatus(OrderStatus.WAITING_FOR_PAYMENT);
        order.setUserInformation(userInformation);

        // process line item
        long totalPrice = 0L;
        for (LineItemRequest lineItemRequest : lineItems) {
            LineItem lineItem = processLineItem(lineItemRequest, order);
            totalPrice += lineItem.getBookDto().price() * lineItem.getQuantity();
        }
        order.setTotalPrice(totalPrice);
        orderRepository.save(order);
        // end process line item
        return order;
    }

    public Order buildAcceptedOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    if (order.getStatus() != OrderStatus.WAITING_FOR_PAYMENT) {
                        throw new ConsistencyDataException("Order's status is not waiting for payment");
                    }
                    order.setStatus(OrderStatus.ACCEPTED);
                    orderRepository.save(order);
                    // reduce inventory
                    reduceInventory(orderId, lineItemRepository.findAllByOrderId(orderId));
                    // end reduce inventory
                    return order;
                })
                .map(order -> {
                    publishOrderAcceptedEvent(order);
                    return order;
                })
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }


    public Order consumeOrderDispatchedEvent(OrderDispatchedMessage orderDispatchedMessage) {
        return orderRepository.findById(orderDispatchedMessage.orderId()).map(order -> {
            order.setStatus(OrderStatus.DISPATCHED);
            orderRepository.save(order);
            return order;
        }).orElseThrow(() -> new OrderNotFoundException(orderDispatchedMessage.orderId()));
    }


    private LineItem processLineItem(LineItemRequest lineItemRequest, Order order)
            throws BookNotFoundException, InsufficientStockException {
        var isbn = lineItemRequest.getIsbn();
        var quantity = lineItemRequest.getQuantity();
        try {
            ResponseEntity<BookDto> bookDtoResponse = bookClient.getBookByIsbn(isbn);
            BookDto bookDto = bookDtoResponse.getBody();
            if (bookDto.inventory() < quantity) {
                throw new InsufficientStockException("Book with isbn " + isbn + " has insufficient stock");
            }

            LineItem lineItem = new LineItem();
            lineItem.setQuantity(quantity);
            lineItem.setOrderId(order.getId());
            lineItem.setBookDto(bookDto);
            order.getLineItems().add(lineItem);

            return lineItem;
        } catch (HttpClientErrorException.NotFound ex) {
            log.warn("Book with isbn {} not found", isbn);
            throw new BookNotFoundException("Book with isbn " + isbn + " not found");
        } catch (HttpServerErrorException ex) {
            log.error("Server error when calling book service", ex);
            throw new RuntimeException("Book service has errors when calling book service", ex);
        }
    }

    private void reduceInventory(UUID orderId, List<LineItem> lineItems) {
        var mapLineItems = new HashMap<String, Integer>();
        for (LineItem lineItem : lineItems) {
            mapLineItems.put(lineItem.getBookDto().isbn(), lineItem.getQuantity());
        }
        BooksPlacedMessage booksPlacedMessage = new BooksPlacedMessage(orderId, mapLineItems);
        var result = streamBridge.send("reduceInventory-out-0", booksPlacedMessage);
        log.info("Result of sending data for list books placed with orderId {}: {}", orderId, result);
    }

    private void publishOrderAcceptedEvent(Order order) {
        if (order.getStatus().equals(OrderStatus.ACCEPTED)) {
            List<LineItem> lineItems = lineItemRepository.findAllByOrderId(order.getId());
            OrderAcceptedMessage orderAcceptedMessage = new OrderAcceptedMessage(order.getId(), lineItems, order.getUserInformation());
            var result = streamBridge.send("acceptOrder-out-0", orderAcceptedMessage);
            log.info("Result of sending data for order with id {}: {}", order.getId(), result);
        }
    }



}
