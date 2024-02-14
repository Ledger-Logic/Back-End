package com.ledgerlogic.controllers;

import com.ledgerlogic.models.Account;
import com.ledgerlogic.models.User;
import com.ledgerlogic.services.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin("*")
@RestController
@RequestMapping("/user")
public class UserController {
    public UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PutMapping("/update/{id}")
    public User update(@PathVariable("id") Long id, @RequestBody User user) {
        Optional<User> currentUser = Optional.ofNullable(this.userService.getById(id));
        if (!currentUser.isPresent()) {
            return null;
        } else {
            User currentUserToBeUpdated = currentUser.get();
            if (user.getEmail() != null) {
                if ((userService.userNameIsTaken(user.getEmail())) && (userService.emailIsTaken(user.getEmail())) && !(user.getEmail().equals(currentUserToBeUpdated.getEmail()))) {
                    System.out.println("The email/username already exist");
                    return null;
                } else {
                    currentUserToBeUpdated.setEmail(user.getEmail());
                }
            }
            if (user.getFirstName() != null) {
                currentUserToBeUpdated.setFirstName(user.getFirstName());
            }
            if (user.getLastName() != null) {
                currentUserToBeUpdated.setLastName(user.getLastName());
            }
            if (user.getEmail() != null) {
                currentUserToBeUpdated.setLastName(user.getLastName());
            }
            if (user.getPassword() != null) {
                currentUserToBeUpdated.setLastName(user.getLastName());
            }

            return this.userService.updateUser(currentUserToBeUpdated);
        }
    }

    @GetMapping("/searchById/{id}")
    public User getUserById(@PathVariable("id") Long id){
        return this.userService.getById(id);
    }

    @GetMapping("/searchByFirstname/{firstname}")
    public List<User> getUserById(@PathVariable("firstname") String firstname){
        return this.userService.findByFirstName(firstname);
    }

    @GetMapping("/searchByLastname/{lastname}")
    public List<User> getUserByLastname(@PathVariable("lastname") String lastname){
        return this.userService.findByLastName(lastname);
    }

    @GetMapping("/searchByFullName/{firstname}/{lastname}")
    public List<User> getUserByFullName(@PathVariable("firstname") String firstname, @PathVariable("lastname") String lastname){
        return this.userService.findByFullName(firstname, lastname);
    }

    @DeleteMapping("/delete/{userId}")
    public void deleteUser(@PathVariable("userId") Long userId){
         this.userService.delete(userId);
    }

    @PutMapping("/activate/{id}")
    public Optional<User> activate(@PathVariable Long id) {
        return userService.activate(id);
    }

    @PutMapping("/deactivate/{id}")
    public Optional<User> deactivate(@PathVariable Long id) {
        return userService.deactivate(id);
    }

    @GetMapping("/Accounts")
    public Optional<List<Account>> getAllUserAccounts(@RequestBody User user){
        return userService.findAllUserAccounts(user);
    }
}