package com.banksystem.entity;

import lombok.Data;

@Data
public class CustomerUpdateDTO {
   private String firstName;
   private String lastName;
   private String email;
   private String address;
   private String phone;
}
