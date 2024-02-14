package com.ledgerlogic.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;


@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long   userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role = "accountant";
    private String streetAddress;
    private String city;
    private String state;
    private String zipCode;
    private Date   birthDay;
    private Boolean status = true; //true = active & false = inactive
    private Date   passwordExpirationDate;
    private short  failedLoginAttempt;
    private Date   suspensionStartDate;
    private Date   suspensionEndDate;
    private Date   lastLoginDate;
    private Date   accountCreationDate;
    private int    securityQ1Id;
    private int    securityQ2Id;
    private int    securityQ3Id;

    private String imageUrl;

    public User(String firstName, String lastName, String email, String role, String username, String password){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.username = username;
        this.password = password;
    }

}