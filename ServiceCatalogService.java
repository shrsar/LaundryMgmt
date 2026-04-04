package com.laundrymgmt.modern.service;

import com.laundrymgmt.modern.dto.CatalogDtos;
import com.laundrymgmt.modern.exception.ApiException;
import com.laundrymgmt.modern.model.LaundryService;
import com.laundrymgmt.modern.model.Role;
import com.laundrymgmt.modern.repository.LaundryServiceRepository;
import com.laundrymgmt.modern.util.CodeFactory;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceCatalogService {

    private final LaundryServiceRepository laundryServiceRepository;
    private final SessionService sessionService;
    private final CodeFactory codeFactory;

    public ServiceCatalogService(LaundryServiceRepository laundryServiceRepository, SessionService sessionService,
                                 CodeFactory codeFactory) {
        this.laundryServiceRepository = laundryServiceRepository;
        this.sessionService = sessionService;
        this.codeFactory = codeFactory;
    }

    public List<CatalogDtos.ServiceResponse> listServices() {
        return laundryServiceRepository.findAllByOrderByClothTypeAscServiceTypeAsc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public CatalogDtos.ServiceResponse createService(String authorizationHeader, CatalogDtos.ServiceRequest request) {
        sessionService.requireRole(authorizationHeader, Role.ADMIN);

        LaundryService laundryService = new LaundryService();
        laundryService.setServiceCode(resolveServiceCode(request.serviceCode(), null));
        applyRequest(laundryService, request);
        return toResponse(laundryServiceRepository.save(laundryService));
    }

    @Transactional
    public CatalogDtos.ServiceResponse updateService(String authorizationHeader, Long id,
                                                     CatalogDtos.ServiceRequest request) {
        sessionService.requireRole(authorizationHeader, Role.ADMIN);
        LaundryService existingService = laundryServiceRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Service not found."));

        existingService.setServiceCode(resolveServiceCode(request.serviceCode(), existingService.getServiceCode()));
        applyRequest(existingService, request);
        return toResponse(laundryServiceRepository.save(existingService));
    }

    @Transactional
    public void deleteService(String authorizationHeader, Long id) {
        sessionService.requireRole(authorizationHeader, Role.ADMIN);
        LaundryService existingService = laundryServiceRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Service not found."));
        laundryServiceRepository.delete(existingService);
    }

    public CatalogDtos.ServiceResponse toResponse(LaundryService laundryService) {
        return new CatalogDtos.ServiceResponse(
            laundryService.getId(),
            laundryService.getServiceCode(),
            laundryService.getClothType(),
            laundryService.getServiceType(),
            laundryService.getPricePerItem(),
            laundryService.getPremiumPerDay()
        );
    }

    private void applyRequest(LaundryService laundryService, CatalogDtos.ServiceRequest request) {
        laundryService.setClothType(requireText(request.clothType(), "Cloth type is required."));
        laundryService.setServiceType(requireText(request.serviceType(), "Service type is required."));
        laundryService.setPricePerItem(validateMoney(request.pricePerItem(), "Price per item must be greater than zero."));
        laundryService.setPremiumPerDay(validateNonNegativeMoney(request.premiumPerDay(), "Premium per day cannot be negative."));
    }

    private String resolveServiceCode(String requestedCode, String currentCode) {
        if (requestedCode == null || requestedCode.isBlank()) {
            if (currentCode != null && !currentCode.isBlank()) {
                return currentCode;
            }
            return codeFactory.generate("SVC", laundryServiceRepository::existsByServiceCodeIgnoreCase);
        }

        String normalized = requestedCode.trim().toUpperCase();
        boolean exists = laundryServiceRepository.existsByServiceCodeIgnoreCase(normalized);
        if (exists && (currentCode == null || !normalized.equalsIgnoreCase(currentCode))) {
            throw new ApiException(HttpStatus.CONFLICT, "A service with that code already exists.");
        }
        return normalized;
    }

    private BigDecimal validateMoney(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, message);
        }
        return value;
    }

    private BigDecimal validateNonNegativeMoney(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, message);
        }
        return value;
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }
}
