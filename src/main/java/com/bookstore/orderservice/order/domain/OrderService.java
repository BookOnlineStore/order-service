package com.bookstore.orderservice.order.domain;

import com.bookstore.orderservice.book.BookClient;
import com.bookstore.orderservice.book.BookNotFoundException;
import com.bookstore.orderservice.book.InsufficientStockException;
import com.bookstore.orderservice.order.event.OrderAcceptedMessage;
import com.bookstore.orderservice.order.event.OrderDispatchedMessage;
import com.bookstore.orderservice.order.web.dto.LineItemRequest;
import com.bookstore.orderservice.order.web.dto.UserInformation;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final LineItemRepository lineItemRepository;
    private final BookClient bookClient;
    private final StreamBridge streamBridge;

    public OrderService(OrderRepository orderRepository,
                        LineItemRepository lineItemRepository,
                        BookClient bookClient,
                        StreamBridge streamBridge) {
        this.orderRepository = orderRepository;
        this.lineItemRepository = lineItemRepository;
        this.bookClient = bookClient;
        this.streamBridge = streamBridge;
    }

    public Flux<Order> getAllOrder(String username) {
        return orderRepository.findAllByCreatedBy(username);
    }

    @SneakyThrows
    @Transactional
    public Mono<Order> submitOrder(List<LineItemRequest> lineItems, UserInformation userInformation) {
        AtomicReference<Double> totalPrice = new AtomicReference<>(0.0);
        var order = new Order();
        order.setStatus(OrderStatus.ACCEPTED);
        order.setFullName(userInformation.getFullName());
        order.setEmail(userInformation.getEmail());
        order.setPhoneNumber(userInformation.getPhoneNumber());
        order.setCity(userInformation.getCity());
        order.setZipCode(userInformation.getZipCode());
        order.setAddress(userInformation.getAddress());

        order.setAddress(userInformation.getAddress());
        return orderRepository.save(order)
                .flatMap(orderSaved ->
                        Flux.fromIterable(lineItems)
                                .flatMap(lineItemRequest -> {
                                    String isbn = lineItemRequest.getIsbn();
                                    Integer quantity = lineItemRequest.getQuantity();
                                    return bookClient.getBookByIsbn(isbn)
                                            .switchIfEmpty(Mono.error(new BookNotFoundException("Book with isbn " + isbn + " not found")))
                                            .filter(bookDto -> bookDto.inventory() >= quantity)
                                            .switchIfEmpty(Mono.error(new InsufficientStockException("Book with isbn " + isbn + " has insufficient stock")))
                                            .flatMap(bookDto -> {
                                                LineItem lineItem = new LineItem();
                                                lineItem.setQuantity(quantity);
                                                lineItem.setIsbn(bookDto.isbn());
                                                lineItem.setTitle(bookDto.title());
                                                lineItem.setAuthor(bookDto.author());
                                                lineItem.setPublisher(bookDto.publisher());
                                                lineItem.setSupplier(bookDto.supplier());
                                                lineItem.setPrice(bookDto.price());
                                                lineItem.setPhotos(bookDto.photos());
                                                lineItem.setOrderId(orderSaved.getId());
                                                return lineItemRepository.save(lineItem);
                                            });
                                }).doOnNext(lineItem -> {
                                    totalPrice.accumulateAndGet(lineItem.getPrice() * lineItem.getQuantity(), Double::sum);
                                }).then(Mono.defer(() -> orderRepository.findById(orderSaved.getId())))
                                .flatMap(updatedOrder -> {
                                    updatedOrder.setTotalPrice(totalPrice.get());
                                    return orderRepository.save(updatedOrder);
                                })
                )
                .doOnNext(this::publishOrderAcceptedEvent);
    }


    private void publishOrderAcceptedEvent(Order order) {
        if (order.getStatus().equals(OrderStatus.REJECTED)) {
            return;
        }
        Flux<LineItem> lineItemsFlux = lineItemRepository.findAllByOrderId(order.getId());
        Mono<List<LineItem>> lineItemsMono = lineItemsFlux.collectList();
        lineItemsMono.subscribe(lineItems -> {
            var userInformation = UserInformation.builder()
                    .fullName(order.getFullName())
                    .email(order.getEmail())
                    .phoneNumber(order.getPhoneNumber())
                    .city(order.getCity())
                    .zipCode(order.getZipCode())
                    .address(order.getAddress()).build();
            OrderAcceptedMessage orderAcceptedMessage = new OrderAcceptedMessage(order.getId(), lineItems, userInformation);
            var result = streamBridge.send("acceptOrder-out-0", orderAcceptedMessage);
            log.info("Result of sending data for order with id {}: {}", order.getId(), result);
        });
    }

    public Flux<Order> consumeOrderDispatchedEvent(Flux<OrderDispatchedMessage> flux) {
        return flux
                .flatMap(message -> orderRepository.findById(message.orderId()));
//                        .map(this::buildDispatchedOrder)
//                        .flatMap(orderRepository::save));
    }

//    private Order buildDispatchedOrder(Order existingOrder) {
//        return new Order(
//                existingOrder.id(),
//                existingOrder.isbn(),
//                existingOrder.bookTitle(),
//                existingOrder.price(),
//                existingOrder.quantity(),
//                OrderStatus.DISPATCHED,
//                existingOrder.createdDate(),
//                existingOrder.createdBy(),
//                existingOrder.lastModifiedDate(),
//                existingOrder.lastModifiedBy(),
//                existingOrder.version()
//        );
//    }


}
