package com.example.InventoryManagementSystem.service;



import com.example.InventoryManagementSystem.dto.CustomerRequestDTO;
import com.example.InventoryManagementSystem.dto.CustomerResponseDTO;

import java.util.List;

public interface CustomerService {

    CustomerResponseDTO createCustomer(CustomerRequestDTO dto);

    CustomerResponseDTO getCustomerById(Long id);

    List<CustomerResponseDTO> getAllCustomers();

    CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO dto);

    void deleteCustomer(Long id);
}
