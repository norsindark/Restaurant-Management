package com.restaurant_management.services.impls;

import com.restaurant_management.dtos.OrderDto;
import com.restaurant_management.dtos.OrderItemDto;
import com.restaurant_management.entites.*;
import com.restaurant_management.enums.UnitType;
import com.restaurant_management.exceptions.DataExitsException;
import com.restaurant_management.payloads.responses.ApiResponse;
import com.restaurant_management.payloads.responses.OrderResponse;
import com.restaurant_management.repositories.*;
import com.restaurant_management.services.interfaces.OrderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final DishRepository dishRepository;
    private final DishOptionSelectionRepository dishOptionSelectionRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final WarehouseRepository warehouseRepository;
    private final RecipeRepository recipeRepository;
    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final JavaMailSender javaMailSender;
    private final OrderItemOptionRepository orderItemOptionRepository;
    private final OfferRepository offerRepository;
    private final PagedResourcesAssembler<OrderResponse> pagedResourcesAssembler;


    @Override
    @Transactional
    public PagedModel<EntityModel<OrderResponse>> getAllOrders(int pageNo, int pageSize, String sortBy, String sortDir)
            throws DataExitsException {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<Order> orders = orderRepository.findAll(pageable);
        if (orders.isEmpty()) {
            throw new DataExitsException("No orders found");
        }
        List<OrderResponse> orderResponses = orders.stream()
                .map(order -> {
                    Address address = addressRepository.findById(order.getAddressId())
                            .orElseThrow(() -> new RuntimeException("Address not found"));
                    List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
                    return new OrderResponse(order, address, orderItems);
                })
                .collect(Collectors.toList());
        return pagedResourcesAssembler.toModel(new PageImpl<>(orderResponses, pageable, orders.getTotalElements()));
    }

    @Override
    @Transactional
    public ApiResponse addNewOrder(OrderDto request)
            throws DataExitsException, MessagingException, UnsupportedEncodingException {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new DataExitsException("User not found"));
        addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new DataExitsException("Address not found"));

        updateWarehouse(request);
        Order order = createNewOrder(request, user);
        order.setTotalPrice(totalOrderPrice(request));
        orderRepository.save(order);
        addOrderItem(order, request);

        if (request.getCouponId() != null && !request.getCouponId().isEmpty()) {
            setCouponUsage(request);
            order.setTotalPrice(applyCoupon(request));
            orderRepository.save(order);
        }

        sendEmailListOrderItems(
                user.getEmail(), request.getItems(),
                request.getCouponId(), order.getTotalPrice(),
                request.getPaymentMethod(), order.getStatus(),
                request.getShippingFee());

        return new ApiResponse("Order created successfully", HttpStatus.CREATED);
    }

    @Override
    public ApiResponse updateOrderStatus(String orderId, String status) throws DataExitsException, MessagingException, UnsupportedEncodingException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataExitsException("Order not found"));
        User user = order.getUser();
        order.setStatus(status.toUpperCase(Locale.ROOT));
        orderRepository.save(order);
        sendMailWhenUpdateOrderStatus(user.getEmail(), orderId, status);
        return new ApiResponse("Order updated successfully", HttpStatus.OK);
    }

    private Order createNewOrder(OrderDto request, User user) throws DataExitsException {
        Order order = Order.builder()
                .user(user)
                .addressId(request.getAddressId())
                .status("PENDING")
                .note(request.getNote())
                .paymentMethod(request.getPaymentMethod())
                .shippingFee(request.getShippingFee())
                .totalPrice(request.getTotalPrice())
                .build();
        orderRepository.save(order);
        return order;
    }

    private void addOrderItem(Order order, OrderDto request) throws DataExitsException {
        for (OrderItemDto orderItemDto : request.getItems()) {
            if (orderItemDto.getQuantity() <= 0) {
                throw new DataExitsException("Quantity must be greater than zero");
            }

            Dish dish = dishRepository.findById(orderItemDto.getDishId())
                    .orElseThrow(() -> new DataExitsException("Dish not found"));

            double totalAdditionalPrice = orderItemDto.getDishOptionSelectionIds().stream()
                    .map(dishOptionSelectionId -> dishOptionSelectionRepository.findById(dishOptionSelectionId)
                            .orElseThrow(() -> new RuntimeException("Dish option selection not found")))
                    .mapToDouble(DishOptionSelection::getAdditionalPrice)
                    .sum();

            double totalPrice = (dish.getPrice() * orderItemDto.getQuantity()) + totalAdditionalPrice;

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .dish(dish)
                    .quantity(orderItemDto.getQuantity())
                    .price(dish.getPrice())
                    .totalPrice(totalPrice)
                    .options(new ArrayList<>())
                    .build();

            orderItem = orderItemRepository.save(orderItem);

            for (String optionId : orderItemDto.getDishOptionSelectionIds()) {
                DishOptionSelection dishOptionSelection = dishOptionSelectionRepository.findById(optionId)
                        .orElseThrow(() -> new DataExitsException("Dish option selection not found"));
                OrderItemOption orderItemOption = OrderItemOption.builder()
                        .orderItem(orderItem)
                        .additionalPrice(dishOptionSelection.getAdditionalPrice())
                        .dishOptionSelection(dishOptionSelection)
                        .build();

                orderItemOptionRepository.save(orderItemOption);
                orderItem.getOptions().add(orderItemOption);
            }
        }
    }

    private double calculatePriceWithOffer(Dish dish, double basePrice) throws DataExitsException {
        List<Offer> offers = offerRepository.findAllByDish(dish);
        LocalDate today = LocalDate.now();

        Offer activeOffer = offers.stream()
                .filter(offer -> !offer.getStartDate().isAfter(today) && !offer.getEndDate().isBefore(today))
                .findFirst()
                .orElse(null);

        if (activeOffer != null) {
            double discountAmount = (basePrice * activeOffer.getDiscountPercentage()) / 100;
            return basePrice - discountAmount;
        }
        return basePrice;
    }

    private double totalOrderPrice(OrderDto request) throws DataExitsException {
        double total = 0;

        for (OrderItemDto orderItemDto : request.getItems()) {
            Dish dish = dishRepository.findById(orderItemDto.getDishId())
                    .orElseThrow(() -> new DataExitsException("Dish not found"));

            double basePrice = calculatePriceWithOffer(dish, dish.getOfferPrice() != null ? dish.getOfferPrice() : dish.getPrice());

            double totalAdditionalPrice = orderItemDto.getDishOptionSelectionIds().stream()
                    .map(dishOptionSelectionId -> dishOptionSelectionRepository.findById(dishOptionSelectionId)
                            .orElseThrow(() -> new RuntimeException("Dish option selection not found")))
                    .mapToDouble(DishOptionSelection::getAdditionalPrice)
                    .sum();

            total += (basePrice + totalAdditionalPrice) * orderItemDto.getQuantity();
        }
        return total + request.getShippingFee();
    }

    private double applyCoupon(OrderDto request) throws DataExitsException {
        Coupon coupon = couponRepository.findById(request.getCouponId())
                .orElseThrow(() -> new DataExitsException("Coupon not found"));

        double totalPrice = totalOrderPrice(request);

        if (coupon.getMinOrderValue() != null && totalPrice < coupon.getMinOrderValue()) {
            throw new IllegalStateException("Order value does not meet the minimum requirement for the coupon");
        }

        double discount = 0;
        if (coupon.getDiscountPercent() != null) {
            discount = totalPrice * (coupon.getDiscountPercent() / 100);
            if (coupon.getMaxDiscount() != null && discount > coupon.getMaxDiscount()) {
                discount = coupon.getMaxDiscount();
            }
        }
        return totalPrice - discount;
    }

    private void setCouponUsage(OrderDto request) throws DataExitsException {
        Coupon coupon = couponRepository.findById(request.getCouponId())
                .orElseThrow(() -> new DataExitsException("Coupon not found"));

        if (coupon.getQuantity() <= 0) {
            throw new IllegalStateException("Coupon has run out of stock");
        }

        if (LocalDate.now().isBefore(LocalDate.parse(coupon.getStartDate()))) {
            throw new IllegalStateException("Coupon is not valid yet");
        }

        if (LocalDate.now().isAfter(LocalDate.parse(coupon.getExpirationDate()))) {
            throw new IllegalStateException("Coupon has expired");
        }

        if (couponUsageRepository.existsByCouponIdAndUserId(coupon.getId(), request.getUserId())) {
            throw new IllegalStateException("Coupon has already been used by this user");
        }
        CouponUsage couponUsage = CouponUsage.builder()
                .couponId(request.getCouponId())
                .userId(request.getUserId())
                .build();

        coupon.setQuantity(coupon.getQuantity() - 1);
        couponRepository.save(coupon);
        couponUsageRepository.save(couponUsage);
    }

    @Transactional
    private void updateWarehouse(OrderDto request) {
        for (OrderItemDto orderItemDto : request.getItems()) {
            dishRepository.findById(orderItemDto.getDishId())
                    .ifPresentOrElse(dish -> {
                        List<Recipe> recipes = recipeRepository.findByDish(dish);

                        for (Recipe recipe : recipes) {
                            warehouseRepository.findById(recipe.getWarehouse().getId())
                                    .ifPresentOrElse(warehouse -> {
                                        UnitType recipeUnit = UnitType.fromString(recipe.getUnit());
                                        UnitType warehouseUnit = UnitType.fromString(warehouse.getUnit());

                                        double quantityUsed = UnitType.convert(
                                                recipe.getQuantityUsed() * orderItemDto.getQuantity(),
                                                recipeUnit, warehouseUnit
                                        );

                                        double newAvailableQuantity = warehouse.getAvailableQuantity() - quantityUsed;
                                        double newQuantityUsed = warehouse.getQuantityUsed() + quantityUsed;

                                        if (newAvailableQuantity < 0) {
                                            throw new IllegalStateException("Not enough stock in warehouse for item: " + dish.getDishName());
                                        }

                                        warehouse.setAvailableQuantity(newAvailableQuantity);
                                        warehouse.setQuantityUsed(newQuantityUsed);

                                        warehouseRepository.save(warehouse);
                                    }, () -> {
                                        throw new IllegalStateException("Warehouse not found for recipe: " + recipe.getId());
                                    });
                        }
                    }, () -> {
                        throw new IllegalStateException("Dish not found: " + orderItemDto.getDishId());
                    });
        }
    }

    private void sendEmailListOrderItems(String email, List<OrderItemDto> items, String couponId, Double totalPrice, String paymentMethod, String status, Double shippingFee)
            throws MessagingException, UnsupportedEncodingException, DataExitsException {
        String fromAddress = "dvan78281@gmail.com";
        String senderName = "Sync Food";
        String subject = "Your Order has been Placed Successfully";

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        StringBuilder content = new StringBuilder("<html><body style=\"font-family: Arial, sans-serif; color: #333;\">")
                .append("<div class=\"layout-content\" style=\"padding: 20px;\">")
                .append("<h2 style=\"color: #5C8E5F; font-size: 18px;\">Your order has been placed successfully!</h2>")
                .append("<p style=\"color: #5C8E5F; font-size: 14px;\">Here is the list of items you have ordered:</p>")
                .append("<table style=\"border-collapse: collapse; width: 100%; margin-top: 20px; border-radius: 8px; overflow: hidden;\">")
                .append("<thead>")
                .append("<tr style=\"background-color: #81c784;\">")
                .append("<th style=\"border: 1px solid #FFFFFF; padding: 8px; color: #FFFFFF;\">Dish Name</th>") // Màu chữ trắng
                .append("<th style=\"border: 1px solid #FFFFFF; padding: 8px; color: #FFFFFF;\">Thumb Image</th>") // Màu chữ trắng
                .append("<th style=\"border: 1px solid #FFFFFF; padding: 8px; color: #FFFFFF;\">Price</th>") // Màu chữ trắng
                .append("<th style=\"border: 1px solid #FFFFFF; padding: 8px; color: #FFFFFF;\">Selected Options</th>") // Màu chữ trắng
                .append("<th style=\"border: 1px solid #FFFFFF; padding: 8px; color: #FFFFFF;\">Quantity</th>") // Màu chữ trắng
                .append("<th style=\"border: 1px solid #FFFFFF; padding: 8px; color: #FFFFFF;\">Total Price</th>") // Màu chữ trắng
                .append("</tr>")
                .append("</thead>")
                .append("<tbody>");

        Coupon coupon = null;
        if (couponId != null) {
            coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new DataExitsException("Coupon not found"));
        }


        int indexRow = 0;

        for (OrderItemDto item : items) {
            Dish dish = dishRepository.findById(item.getDishId())
                    .orElseThrow(() -> new DataExitsException("Dish not found"));

            double priceItem = (dish.getOfferPrice() != null) ? dish.getOfferPrice() : dish.getPrice();
            double totalOptionsPrice = 0.0;

            Offer offer = offerRepository.findByDishId(dish.getId())
                    .orElse(null);

            String priceItemDisplay = currencyFormat.format(priceItem);
            if (offer != null) {
                int discountPercentage = offer.getDiscountPercentage();
                priceItemDisplay += " (" + discountPercentage + "% off daily offer)";
                priceItem = priceItem * (1 - (discountPercentage / 100.0));
            }

            List<DishOptionSelection> selectedOptions = new ArrayList<>();
            for (String optionId : item.getDishOptionSelectionIds()) {
                DishOptionSelection option = dishOptionSelectionRepository.findById(optionId)
                        .orElseThrow(() -> new DataExitsException("Dish option selection not found"));
                selectedOptions.add(option);
                totalOptionsPrice += option.getAdditionalPrice();
            }

            StringBuilder options = new StringBuilder();
            for (DishOptionSelection option : selectedOptions) {
                options.append(option.getDishOption().getOptionName())
                        .append(" (").append(currencyFormat.format(option.getAdditionalPrice())).append(")").append("<br>");
            }

            double totalItemPrice = (priceItem + totalOptionsPrice) * item.getQuantity();

            String backgroundColor = (indexRow % 2 == 0) ? "#f0f4e8" : "#fff0e0";

            content.append("<tr style=\"background-color: " + backgroundColor + ";\">")
                    .append("<td style=\"border: none; padding: 8px; color: #000000E0;\">").append(dish.getDishName()).append("</td>")
                    .append("<td style=\"border: none; padding: 8px; color: #000000E0;\">")
                    .append("<img src=\"").append(dish.getThumbImage()).append("\" alt=\"").append(dish.getDishName()).append("\" style=\"width: 100px; height: auto;\"/>")
                    .append("</td>")
                    .append("<td style=\"border: none; padding: 8px; color: #000000E0;\">").append(priceItemDisplay).append("</td>")
                    .append("<td style=\"border: none; padding: 8px; color: #000000E0;\">").append(options.toString()).append("</td>")
                    .append("<td style=\"border: none; padding: 8px; color: #000000E0;\">").append(item.getQuantity()).append("</td>")
                    .append("<td style=\"border: none; padding: 8px; color: #000000E0;\">").append(currencyFormat.format(totalItemPrice)).append("</td>")
                    .append("</tr>");

            indexRow++;
        }

        content.append("</tbody>")
                .append("</table>")
                .append("<p style=\"color: #5C8E5F;\">Coupon Code: <strong>").append(coupon != null ? coupon.getCode() : "N/A").append("</strong></p>")
                .append("<p style=\"color: #5C8E5F;\">Payment Method: <strong>").append(paymentMethod).append("</strong></p>")
                .append("<p style=\"color: #5C8E5F;\">Order Status: <strong>").append(status).append("</strong></p>")
                .append("<p style=\"color: #5C8E5F;\">Shipping Fee: <strong>").append(currencyFormat.format(shippingFee)).append("</strong></p>")
                .append("<p style=\"color: #5C8E5F; font-size: 16px;\">Total Price: <strong>").append(currencyFormat.format(totalPrice)).append("</strong></p>")
                .append("<p style=\"color: #5C8E5F;\">Your order will be processed soon.</p>")
                .append("<p style=\"color: #5C8E5F;\">We will notify you when your order is on its way.</p>")
                .append("<p style=\"color: #5C8E5F;\">Thank you for choosing us!</p>")
                .append("</div></body></html>");

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(content.toString(), true);

        javaMailSender.send(message);
    }

    private void sendMailWhenUpdateOrderStatus(String email, String orderId, String status) throws MessagingException, UnsupportedEncodingException {
        String fromAddress = "dvan78281@gmail.com";
        String senderName = "Sync Food";
        String subject = "Order Status Update";
        String content = "<html><body>" +
                "<h2 style=\"color: #13b3e6; font-family: Arial, sans-serif; font-size: 18px;\">" +
                "Your Order Status has been Updated!</h2>" +
                "<p style=\"font-family: Arial, sans-serif; font-size: 14px; color: #333;\">" +
                "Order ID: " + orderId + "</p>" +
                "<p style=\"font-family: Arial, sans-serif; font-size: 14px; color: #333;\">" +
                "New Status: " + status + "</p>" +
                "<p style=\"font-family: Arial, sans-serif; font-size: 14px; color: #333;\">" +
                "Thank you for your patience!</p>" +
                "</body></html>";

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(content, true);

        javaMailSender.send(message);
    }

}
