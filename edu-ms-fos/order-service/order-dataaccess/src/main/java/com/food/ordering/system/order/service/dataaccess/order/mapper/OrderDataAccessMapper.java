package com.food.ordering.system.order.service.dataaccess.order.mapper;

import com.food.ordering.system.domain.valueobject.*;
import com.food.ordering.system.order.service.dataaccess.order.entity.OrderAddressEntity;
import com.food.ordering.system.order.service.dataaccess.order.entity.OrderEntity;
import com.food.ordering.system.order.service.dataaccess.order.entity.OrderItemEntity;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.valueobject.OrderItemId;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class OrderDataAccessMapper {
    public OrderEntity orderToOrderEntity(Order order) {
        OrderEntity entity = OrderEntity.builder()
            .id(order.getId().getValue())
            .customerId(order.getCustomerId().getValue())
            .restaurantId(order.getRestaurantId().getValue())
            .trackingId(order.getTrackingId().getValue())
            .address(deliveryAddressToAddressEntity(order.getDeliveryAddress()))
            .price(order.getPrice().getAmount())
            .items(orderItemsToOrderItemEntities(order.getItems()))
            .orderStatus(order.getOrderStatus())
            .failureMessages(
                Optional.ofNullable(order.getFailureMessages())
                    .map(messages -> String.join(Order.FAILURE_MESSAGE_DELIMITER, messages)
                    ).orElse("")
            ).build();

        entity.getAddress()
            .setOrder(entity);
        entity.getItems()
            .forEach(itemEntity -> itemEntity.setOrder(entity));

        return entity;
    }

    public Order orderEntityToOrder(OrderEntity orderEntity) {
        return Order.builder()
            .orderId(new OrderId(orderEntity.getId()))
            .customerId(new CustomerId(orderEntity.getCustomerId()))
            .restaurantId(new RestaurantId(orderEntity.getRestaurantId()))
            .deliveryAddress(addressEntityToDeliveryAddress(orderEntity.getAddress()))
            .price(new Money(orderEntity.getPrice()))
            .items(orderItemEntitiesToOrderItems(orderEntity.getItems()))
            .trackingId(new TrackingId(orderEntity.getTrackingId()))
            .orderStatus(orderEntity.getOrderStatus())
            .failureMessages(Optional.ofNullable(orderEntity.getFailureMessages())
                .filter(messages -> !messages.isEmpty())
                .map(messages -> messages.split(Order.FAILURE_MESSAGE_DELIMITER))
                .map(Arrays::asList)
                .map(ArrayList::new)
                .orElseGet(ArrayList::new)
            ).build();
    }

    private StreetAddress addressEntityToDeliveryAddress(OrderAddressEntity orderAddressEntity) {
        return new StreetAddress(
            orderAddressEntity.getId(),
            orderAddressEntity.getStreet(),
            orderAddressEntity.getPostalCode(),
            orderAddressEntity.getCity()
        );
    }

    private OrderAddressEntity deliveryAddressToAddressEntity(StreetAddress streetAddress) {
        return OrderAddressEntity.builder()
            .id(streetAddress.getId())
            .street(streetAddress.getStreet())
            .postalCode(streetAddress.getPostalCode())
            .city(streetAddress.getCity())
            .build();
    }

    private List<OrderItemEntity> orderItemsToOrderItemEntities(List<OrderItem> items) {
        return items.stream()
            .map(item -> OrderItemEntity.builder()
                .id(item.getId().getValue())
                .productId(item.getProduct().getId().getValue())
                .price(item.getPrice().getAmount())
                .quantity(item.getQuantity())
                .subTotal(item.getSubTotal().getAmount())
                .build()
            ).toList();
    }

    private List<OrderItem> orderItemEntitiesToOrderItems(List<OrderItemEntity> orderItemEntities) {
        return orderItemEntities.stream()
            .map(item -> OrderItem.builder()
                .orderItemId(new OrderItemId(item.getId()))
                .product(new Product(new ProductId(item.getProductId())))
                .price(new Money(item.getPrice()))
                .quantity(item.getQuantity())
                .subTotal(new Money(item.getSubTotal()))
                .build()
            ).toList();
    }
}
