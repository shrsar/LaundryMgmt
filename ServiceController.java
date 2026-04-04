package com.laundrymgmt.modern.controller;

import com.laundrymgmt.modern.dto.CatalogDtos;
import com.laundrymgmt.modern.dto.CommonDtos;
import com.laundrymgmt.modern.service.ServiceCatalogService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceCatalogService serviceCatalogService;

    public ServiceController(ServiceCatalogService serviceCatalogService) {
        this.serviceCatalogService = serviceCatalogService;
    }

    @GetMapping
    public List<CatalogDtos.ServiceResponse> listServices() {
        return serviceCatalogService.listServices();
    }

    @PostMapping
    public CatalogDtos.ServiceResponse createService(
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
        @RequestBody CatalogDtos.ServiceRequest request
    ) {
        return serviceCatalogService.createService(authorizationHeader, request);
    }

    @PutMapping("/{id}")
    public CatalogDtos.ServiceResponse updateService(
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
        @PathVariable Long id,
        @RequestBody CatalogDtos.ServiceRequest request
    ) {
        return serviceCatalogService.updateService(authorizationHeader, id, request);
    }

    @DeleteMapping("/{id}")
    public CommonDtos.MessageResponse deleteService(
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
        @PathVariable Long id
    ) {
        serviceCatalogService.deleteService(authorizationHeader, id);
        return new CommonDtos.MessageResponse("Service deleted.");
    }
}
