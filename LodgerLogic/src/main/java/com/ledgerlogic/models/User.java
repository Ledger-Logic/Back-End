package com.ledgerlogic.models;

import jakarta.persistence.*;
import lombok.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


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
    private String role = "accountant";
    private String streetAddress;
    private String city;
    private String state;
    private String zipCode;
    private Date   birthDay;
    private Boolean status = false; //true = active & false = inactive
    private Date   passwordExpirationDate;
    private short  failedLoginAttempt;
    private Date   suspensionStartDate;
    private Date   suspensionEndDate;
    private Date   lastLoginDate;
    private Date   accountCreationDate = new Date();
    private String imageUrl;

    @OneToMany
    private List<UserSecurityQuestion>    userSecurityQuestions;
    private List<String> previousPasswords;

    @OneToOne
    private Password password;

    public User(String firstName, String lastName, String email, String role, Password password){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.password = password;
        this.username = generateUsername(firstName, lastName, accountCreationDate);
        previousPasswords.add(password.getContent());
    }

    private String generateUsername(String firstName, String lastName, Date accountCreationDate){
        String twoDigitsMonth = new SimpleDateFormat("MM").format(accountCreationDate);
        String twoDigitsYear  = new SimpleDateFormat("yy").format(accountCreationDate);

        return Character.toString(firstName.charAt(0)).toLowerCase()+
                          lastName.toLowerCase()+
                          twoDigitsMonth+
                          twoDigitsYear;
    }

}
