package com.banksystem.services;

import com.banksystem.dto.RegisterRequestDTO;
import com.banksystem.dto.TellerDTO;
import com.banksystem.entity.*;
import com.banksystem.enums.RolesType;
import com.banksystem.exception.ResourceNotFoundException;
import com.banksystem.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final TellerRepository tellerRepository;
    private final BranchRepository branchRepository; // Required to link Branch
    private final PasswordEncoder passwordEncoder;

    // Constructor Injection
    public AuthService(UserRepository userRepository,
                       CustomerRepository customerRepository,
                       TellerRepository tellerRepository,
                       BranchRepository branchRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.tellerRepository = tellerRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Customer registerCustomer(RegisterRequestDTO request) {
        // 0. Validate Branch
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));

        // 1. Create User (Authentication Entity)
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(RolesType.CUSTOMER);

        // Save User first to generate ID
        user = userRepository.save(user);

        // 2. Create Customer (Business Entity)
        Customer customer = new Customer();
        customer.setUser(user); // LINKING User to Customer
        customer.setBranch(branch); // LINKING Branch to Customer

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setAadharNumber(request.getAadharNumber());
        customer.setPanNumber(request.getPanNumber());

        // Auto-generate business fields
        customer.setCustomerId("CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        customer.setIsActive(true);
        customer.setCreatedAt(LocalDateTime.now());

        // Set initial transaction PIN same as password (hashed) or a default
        customer.setTransactionPinHash(passwordEncoder.encode(request.getPassword()));

        return customerRepository.save(customer);
    }

    @Transactional
    public Teller createTeller(TellerDTO tellerDTO) {
        // 0. Validate Branch
        Branch branch = branchRepository.findById(tellerDTO.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", tellerDTO.getBranchId()));

        // 1. Create User (Authentication Entity)
        User user = new User();
        user.setUsername(tellerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(tellerDTO.getPassword()));
        user.setRole(RolesType.TELLER);

        user = userRepository.save(user);

        // 2. Create Teller (Business Entity)
        Teller teller = new Teller();
        teller.setUser(user); // LINKING
        teller.setBranch(branch); // LINKING

        teller.setFullName(tellerDTO.getFirstName() + " " + tellerDTO.getLastName());
        teller.setUsername(tellerDTO.getUsername()); // Redundant if mapped to User, but keeping for legacy compatibility
        teller.setEmail(tellerDTO.getEmail());
        teller.setPasswordHash(passwordEncoder.encode(tellerDTO.getPassword())); // Redundant but keeping for legacy

        // Map Teller's specific account details
        teller.setAccountId(tellerDTO.getAccountId());
        teller.setAccountNumber(tellerDTO.getAccountNumber());

        teller.setIsActive(true);
        teller.setCreatedAt(LocalDateTime.now());

        return tellerRepository.save(teller);
    }
}