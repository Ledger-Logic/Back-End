package com.ledgerlogic.repositories;

import com.ledgerlogic.models.Password;
import com.ledgerlogic.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameAndPassword(String userName, Password password);

    User findByEmail(String email);
    User findByUsername(String userName);

    List<User> findByFirstName(String keyword);
    List<User> findByLastName(String lastName);
    List<User> findByFirstNameAndLastName(String firstName, String lastName);
    List<User> findByRole(String role);

    List<User> findByExpirationDateBefore(LocalDate date);

}
