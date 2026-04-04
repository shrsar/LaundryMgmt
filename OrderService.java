package com.laundrymgmt.modern.service;

import com.laundrymgmt.modern.dto.OrderDtos;
import com.laundrymgmt.modern.exception.ApiException;
import com.laundrymgmt.modern.model.LaundryOrder;
import com.laundrymgmt.modern.model.LaundryService;
import com.laundrymgmt.modern.model.OrderStatus;
import com.laundrymgmt.modern.model.OtpPurpose;
import com.laundrymgmt.modern.model.Role;
import com.laundrymgmt.modern.model.UserAccount;
import com.laundrymgmt.modern.repository.LaundryOrderRepository;
import com.laundrymgmt.modern.repository.LaundryServiceRepository;
import com.laundrymgmt.modern.util.CodeFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final LaundryOrderRepository laundryOrderRepository;
    private final LaundryServiceRepository laundryServiceRepository;
    private final SessionService sessionService;
    private final OtpService otpService;
    private final CodeFactory codeFactory;

    public OrderService(LaundryOrderRepository laundryOrderRepository,
                        LaundryServiceRepository laundryServiceRepository,
                        SessionService sessionService,
                        OtpService otpService,
                        CodeFactory codeFactory) {
        this.laundryOrderRepository = laundryOrderRepository;
        this.laundryServiceRepository = laundryServiceRepository;
        this.sessionService = sessionService;
        this.otpService = otpService;
        this.codeFactory = codeFactory;
    }

    public List<OrderDtos.OrderResponse> listOrders(String authorizationHeader) {
        UserAccount viewer = sessionService.requireUser(authorizationHeader);
        List<LaundryOrder> orders = viewer.getRole() == Role.ADMIN
            ? laundryOrderRepository.findAllByOrderByOrderDateDescIdDesc()
            : laundryOrderRepository.findAllByCustomer_IdOrderByOrderDateDescIdDesc(viewer.getId());

        return orders.stream().map(this::toResponse).toList();
    }

    @Transactional
    public OrderDtos.OrderResponse createOrder(String authorizationHeader, OrderDtos.OrderRequest request) {
        UserAccount customer = sessionService.requireRole(authorizationHeader, Role.CUSTOMER);
        LaundryService laundryService = laundryServiceRepository.findById(requireServiceId(request.serviceId()))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Selected service was not found."));

        int quantity = requireQuantity(request.quantity());
        LocalDate deliveryDate = requireDeliveryDate(request.deliveryDate());

        if (!otpService.verify(customer, OtpPurpose.ORDER_CONFIRMATION, request.otpCode())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Wrong or expired OTP.");
        }

        BigDecimal bill = calculateBill(laundryService, quantity, deliveryDate);

        LaundryOrder laundryOrder = new LaundryOrder();
        laundryOrder.setOrderCode(codeFactory.generate("ORD", code ->
            laundryOrderRepository.findByOrderCodeIgnoreCase(code).isPresent()
        ));
        laundryOrder.setCustomer(customer);
        laundryOrder.setService(laundryService);
        laundryOrder.setQuantity(quantity);
        laundryOrder.setOrderDate(LocalDate.now());
        laundryOrder.setDeliveryDate(deliveryDate);
        laundryOrder.setBill(bill);
        laundryOrder.setImageUrl(normalizeOptionalText(request.imageUrl()));
        laundryOrder.setClothType(laundryService.getClothType());
        laundryOrder.setServiceType(laundryService.getServiceType());
        laundryOrder.setStatus(OrderStatus.ACTIVE);

        return toResponse(laundryOrderRepository.save(laundryOrder));
    }

    @Transactional
    public OrderDtos.OrderResponse markReceived(String authorizationHeader, String orderCode) {
        UserAccount customer = sessionService.requireRole(authorizationHeader, Role.CUSTOMER);
        LaundryOrder laundryOrder = laundryOrderRepository.findByOrderCodeIgnoreCase(orderCode)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found."));

        if (!laundryOrder.getCustomer().getId().equals(customer.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "That order does not belong to you.");
        }
        if (laundryOrder.getStatus() == OrderStatus.RECEIVED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "That order is already marked as received.");
        }

        laundryOrder.setStatus(OrderStatus.RECEIVED);
        return toResponse(laundryOrderRepository.save(laundryOrder));
    }

    public OrderDtos.OrderResponse toResponse(LaundryOrder laundryOrder) {
        return new OrderDtos.OrderResponse(
            laundryOrder.getId(),
            laundryOrder.getOrderCode(),
            laundryOrder.getCustomer().getCustomerCode(),
            laundryOrder.getCustomer().getDisplayName(),
            laundryOrder.getService().getServiceCode(),
            laundryOrder.getClothType(),
            laundryOrder.getServiceType(),
            laundryOrder.getQuantity(),
            laundryOrder.getOrderDate(),
            laundryOrder.getDeliveryDate(),
            laundryOrder.getBill(),
            laundryOrder.getImageUrl(),
            laundryOrder.getStatus()
        );
    }

    private BigDecimal calculateBill(LaundryService laundryService, int quantity, LocalDate deliveryDate) {
        LocalDate today = LocalDate.now();
        long differenceInDays = ChronoUnit.DAYS.between(today, deliveryDate);
        BigDecimal bill = laundryService.getPricePerItem().multiply(BigDecimal.valueOf(quantity));

        if (differenceInDays <= 5) {
            bill = bill.add(laundryService.getPremiumPerDay().multiply(BigDecimal.valueOf(5 - differenceInDays)));
        }
        return bill;
    }

    private Long requireServiceId(Long serviceId) {
        if (serviceId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A service must be selected.");
        }
        return serviceId;
    }

    private int requireQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Quantity must be at least 1.");
        }
        return quantity;
    }

    private LocalDate requireDeliveryDate(LocalDate deliveryDate) {
        if (deliveryDate == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Delivery date is required.");
        }
        if (deliveryDate.isBefore(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Delivery date cannot be in the past.");
        }
        return deliveryDate;
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
