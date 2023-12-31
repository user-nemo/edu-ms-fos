package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.valueobject.*;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.OrderAddress;
import com.food.ordering.system.order.service.domain.dto.create.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.input.service.OrderApplicationService;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = OrderTestConfiguration.class)
public class OrderApplicationServiceTest {
    @Autowired
    private OrderApplicationService orderApplicationService;
    @Autowired
    private OrderDataMapper orderDataMapper;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;

    private CreateOrderCommand createOrderCommand;
    private CreateOrderCommand createOrderCommandWrongPrice;
    private CreateOrderCommand createOrderCommandWrongProductPrice;

    private final UUID CUSTOMER_ID = UUID.randomUUID();
    private final UUID RESTAURANT_ID = UUID.randomUUID();
    private final UUID PRODUCT_ID = UUID.randomUUID();
    private final UUID ORDER_ID = UUID.randomUUID();
    private final BigDecimal PRICE = new BigDecimal("200.00");

    @BeforeAll
    public void init() {
        createOrderCommand = CreateOrderCommand.builder()
            .customerId(CUSTOMER_ID)
            .restaurantId(RESTAURANT_ID)
            .address(OrderAddress.builder()
                .street("Street 1")
                .postalCode("1000AB")
                .city("Paris")
                .build()
            ).price(PRICE)
            .items(List.of(
                OrderItem.builder()
                    .productId(PRODUCT_ID)
                    .quantity(1)
                    .price(new BigDecimal("50.00"))
                    .subTotal(new BigDecimal("50.00"))
                    .build(),
                OrderItem.builder()
                    .productId(PRODUCT_ID)
                    .quantity(3)
                    .price(new BigDecimal("50.00"))
                    .subTotal(new BigDecimal("150.00"))
                    .build()
            )).build();

        createOrderCommandWrongPrice = CreateOrderCommand.builder()
            .customerId(CUSTOMER_ID)
            .restaurantId(RESTAURANT_ID)
            .address(OrderAddress.builder()
                .street("Street 1")
                .postalCode("1000AB")
                .city("Paris")
                .build()
            ).price(new BigDecimal("250.00"))
            .items(List.of(
                OrderItem.builder()
                    .productId(PRODUCT_ID)
                    .quantity(1)
                    .price(new BigDecimal("50.00"))
                    .subTotal(new BigDecimal("50.00"))
                    .build(),
                OrderItem.builder()
                    .productId(PRODUCT_ID)
                    .quantity(3)
                    .price(new BigDecimal("50.00"))
                    .subTotal(new BigDecimal("150.00"))
                    .build()
            )).build();

        createOrderCommandWrongProductPrice = CreateOrderCommand.builder()
            .customerId(CUSTOMER_ID)
            .restaurantId(RESTAURANT_ID)
            .address(OrderAddress.builder()
                .street("Street 1")
                .postalCode("1000AB")
                .city("Paris")
                .build()
            ).price(new BigDecimal("210.00"))
            .items(List.of(
                OrderItem.builder()
                    .productId(PRODUCT_ID)
                    .quantity(1)
                    .price(new BigDecimal("60.00"))
                    .subTotal(new BigDecimal("60.00"))
                    .build(),
                OrderItem.builder()
                    .productId(PRODUCT_ID)
                    .quantity(3)
                    .price(new BigDecimal("50.00"))
                    .subTotal(new BigDecimal("150.00"))
                    .build()
            )).build();

        Customer customer = new Customer();
        customer.setId(new CustomerId(CUSTOMER_ID));

        Restaurant restaurantResponse = Restaurant.builder()
            .restaurantId(new RestaurantId(RESTAURANT_ID))
            .products(List.of(
                new Product(new ProductId(PRODUCT_ID), "product-1", new Money(new BigDecimal("50.00"))),
                new Product(new ProductId(PRODUCT_ID), "product-2", new Money(new BigDecimal("50.00")))
            )).active(true)
            .build();

        Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
        order.setId(new OrderId(ORDER_ID));

        Mockito.when(customerRepository.findCustomer(CUSTOMER_ID))
            .thenReturn(Optional.of(customer));
        Mockito.when(restaurantRepository.findRestaurantInformation(orderDataMapper.createOrderCommandToRestaurant(createOrderCommand)))
            .thenReturn(Optional.of(restaurantResponse));
        Mockito.when(orderRepository.save(Mockito.any(Order.class)))
            .thenReturn(order);
    }

    @Test
    public void testCreateOrder() {
        var createOrderResponse = orderApplicationService.createOrder(createOrderCommand);

        Assertions.assertEquals(createOrderResponse.getOrderStatus(), OrderStatus.PENDING);
        Assertions.assertEquals(createOrderResponse.getMessage().toLowerCase(), "order created successfully");
        Assertions.assertNotNull(createOrderResponse.getOrderTrackingId());
    }

    @Test
    public void testCreateOrderWithWrongTotalPrice() {
        var orderDomainException = Assertions.assertThrows(
            OrderDomainException.class,
            () -> orderApplicationService.createOrder(createOrderCommandWrongPrice)
        );
        Assertions.assertEquals(orderDomainException.getMessage(), "Total price is not equal to order items price!");
    }

    @Test
    public void testCreateOrderWithWrongProductPrice() {
        var orderDomainException = Assertions.assertThrows(
            OrderDomainException.class,
            () -> orderApplicationService.createOrder(createOrderCommandWrongProductPrice)
        );
        Assertions.assertEquals(orderDomainException.getMessage(), "Order item price is not valid for product!");
    }

    @Test
    public void testCreateOrderWithPassiveRestaurant() {
        var restaurantResponse = Restaurant.builder()
            .restaurantId(new RestaurantId(RESTAURANT_ID))
            .products(List.of(
                new Product(new ProductId(PRODUCT_ID), "product-1", new Money(new BigDecimal("50.00"))),
                new Product(new ProductId(PRODUCT_ID), "product-2", new Money(new BigDecimal("50.00")))
            )).active(false)
            .build();

        Mockito.when(restaurantRepository.findRestaurantInformation(orderDataMapper.createOrderCommandToRestaurant(createOrderCommand)))
            .thenReturn(Optional.of(restaurantResponse));

        var orderDomainException = Assertions.assertThrows(
            OrderDomainException.class,
            () -> orderApplicationService.createOrder(createOrderCommand)
        );

        Assertions.assertEquals(orderDomainException.getMessage(), "Restaurant is currently not active!");
    }
}
