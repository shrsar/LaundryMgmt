package com.laundrymgmt.modern.bootstrap;

import com.laundrymgmt.modern.model.Complaint;
import com.laundrymgmt.modern.model.ComplaintStatus;
import com.laundrymgmt.modern.model.LaundryOrder;
import com.laundrymgmt.modern.model.LaundryService;
import com.laundrymgmt.modern.model.OrderStatus;
import com.laundrymgmt.modern.model.Role;
import com.laundrymgmt.modern.model.UserAccount;
import com.laundrymgmt.modern.repository.ComplaintRepository;
import com.laundrymgmt.modern.repository.LaundryOrderRepository;
import com.laundrymgmt.modern.repository.LaundryServiceRepository;
import com.laundrymgmt.modern.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DemoDataInitializer implements CommandLineRunner {

    private final UserAccountRepository userAccountRepository;
    private final LaundryServiceRepository laundryServiceRepository;
    private final LaundryOrderRepository laundryOrderRepository;
    private final ComplaintRepository complaintRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataInitializer(UserAccountRepository userAccountRepository,
                               LaundryServiceRepository laundryServiceRepository,
                               LaundryOrderRepository laundryOrderRepository,
                               ComplaintRepository complaintRepository,
                               PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.laundryServiceRepository = laundryServiceRepository;
        this.laundryOrderRepository = laundryOrderRepository;
        this.complaintRepository = complaintRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userAccountRepository.count() > 0 || laundryServiceRepository.count() > 0) {
            return;
        }

        UserAccount admin = new UserAccount();
        admin.setCustomerCode("ADMIN");
        admin.setDisplayName("Operations Admin");
        admin.setPhone("9999999999");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setSecurityKey("LaundryHQ");
        admin.setRole(Role.ADMIN);
        admin.setSignupDate(LocalDate.now().minusDays(20));

        UserAccount customerOne = new UserAccount();
        customerOne.setCustomerCode("U-100001");
        customerOne.setDisplayName("Anita Roy");
        customerOne.setPhone("9876543210");
        customerOne.setPasswordHash(passwordEncoder.encode("Welcome@1"));
        customerOne.setSecurityKey("Sunrise");
        customerOne.setRole(Role.CUSTOMER);
        customerOne.setSignupDate(LocalDate.now().minusDays(10));

        UserAccount customerTwo = new UserAccount();
        customerTwo.setCustomerCode("U-100002");
        customerTwo.setDisplayName("Rahul Sen");
        customerTwo.setPhone("9123456780");
        customerTwo.setPasswordHash(passwordEncoder.encode("Welcome@1"));
        customerTwo.setSecurityKey("BlueBook");
        customerTwo.setRole(Role.CUSTOMER);
        customerTwo.setSignupDate(LocalDate.now().minusDays(6));

        UserAccount customerThree = new UserAccount();
        customerThree.setCustomerCode("U-100003");
        customerThree.setDisplayName("Meera Das");
        customerThree.setPhone("9988776655");
        customerThree.setPasswordHash(passwordEncoder.encode("Welcome@1"));
        customerThree.setSecurityKey("Petals");
        customerThree.setRole(Role.CUSTOMER);
        customerThree.setSignupDate(LocalDate.now().minusDays(2));

        userAccountRepository.saveAll(List.of(admin, customerOne, customerTwo, customerThree));

        LaundryService everydayWash = createService("SVC-100001", "Everyday use", "Washing", 60, 20);
        LaundryService cottonPremium = createService("SVC-100002", "Cotton", "Washing and Ironing", 120, 35);
        LaundryService woolIron = createService("SVC-100003", "Wool", "Ironing", 90, 25);
        LaundryService silkWash = createService("SVC-100004", "Silk", "Washing", 140, 50);
        LaundryService satinCare = createService("SVC-100005", "Satin", "Washing and Ironing", 160, 60);
        laundryServiceRepository.saveAll(List.of(everydayWash, cottonPremium, woolIron, silkWash, satinCare));

        LaundryOrder orderOne = new LaundryOrder();
        orderOne.setOrderCode("ORD-100001");
        orderOne.setCustomer(customerTwo);
        orderOne.setService(cottonPremium);
        orderOne.setQuantity(2);
        orderOne.setOrderDate(LocalDate.now().minusDays(6));
        orderOne.setDeliveryDate(LocalDate.now().minusDays(3));
        orderOne.setBill(new BigDecimal("310.00"));
        orderOne.setImageUrl(null);
        orderOne.setClothType(cottonPremium.getClothType());
        orderOne.setServiceType(cottonPremium.getServiceType());
        orderOne.setStatus(OrderStatus.RECEIVED);

        LaundryOrder orderTwo = new LaundryOrder();
        orderTwo.setOrderCode("ORD-100002");
        orderTwo.setCustomer(customerThree);
        orderTwo.setService(silkWash);
        orderTwo.setQuantity(4);
        orderTwo.setOrderDate(LocalDate.now().minusDays(3));
        orderTwo.setDeliveryDate(LocalDate.now().plusDays(1));
        orderTwo.setBill(new BigDecimal("760.00"));
        orderTwo.setImageUrl(null);
        orderTwo.setClothType(silkWash.getClothType());
        orderTwo.setServiceType(silkWash.getServiceType());
        orderTwo.setStatus(OrderStatus.ACTIVE);

        LaundryOrder orderThree = new LaundryOrder();
        orderThree.setOrderCode("ORD-100003");
        orderThree.setCustomer(customerOne);
        orderThree.setService(everydayWash);
        orderThree.setQuantity(5);
        orderThree.setOrderDate(LocalDate.now());
        orderThree.setDeliveryDate(LocalDate.now().plusDays(4));
        orderThree.setBill(new BigDecimal("320.00"));
        orderThree.setImageUrl(null);
        orderThree.setClothType(everydayWash.getClothType());
        orderThree.setServiceType(everydayWash.getServiceType());
        orderThree.setStatus(OrderStatus.ACTIVE);

        laundryOrderRepository.saveAll(List.of(orderOne, orderTwo, orderThree));

        Complaint complaintOne = new Complaint();
        complaintOne.setTicketCode("TKT-100001");
        complaintOne.setCustomer(customerTwo);
        complaintOne.setPhoneNumber(customerTwo.getPhone());
        complaintOne.setDescription("The collar came back slightly damp. Please recheck drying quality for the next order.");
        complaintOne.setAttachmentUrl(null);
        complaintOne.setStatus(ComplaintStatus.OPEN);
        complaintOne.setCreatedAt(LocalDateTime.now().minusDays(1));

        Complaint complaintTwo = new Complaint();
        complaintTwo.setTicketCode("TKT-100002");
        complaintTwo.setCustomer(customerThree);
        complaintTwo.setPhoneNumber(customerThree.getPhone());
        complaintTwo.setDescription("Pickup timing was delayed, but the order quality was fine after delivery.");
        complaintTwo.setAttachmentUrl(null);
        complaintTwo.setStatus(ComplaintStatus.RESOLVED);
        complaintTwo.setCreatedAt(LocalDateTime.now().minusDays(4));

        complaintRepository.saveAll(List.of(complaintOne, complaintTwo));
    }

    private LaundryService createService(String code, String clothType, String serviceType, int pricePerItem,
                                         int premiumPerDay) {
        LaundryService laundryService = new LaundryService();
        laundryService.setServiceCode(code);
        laundryService.setClothType(clothType);
        laundryService.setServiceType(serviceType);
        laundryService.setPricePerItem(BigDecimal.valueOf(pricePerItem));
        laundryService.setPremiumPerDay(BigDecimal.valueOf(premiumPerDay));
        return laundryService;
    }
}
