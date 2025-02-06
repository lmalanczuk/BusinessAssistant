package com.licencjat.BusinessAssistant.entity;

import com.licencjat.BusinessAssistant.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class Users {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String username;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToMany(mappedBy = "participants")
    private Set<Meeting> meetings;


    public Users( String firstName, String lastName, String email, String username, String password, Role role, Set<Meeting> meetings) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = generateUsername(firstName, lastName);
        this.password = password;
        this.role = role;
    }

    private String generateUsername(String firstName, String lastName){
        if(firstName == null || lastName == null){
            throw new IllegalArgumentException("First name and last name cannot be null");
        }
        return (firstName.charAt(0) +"."+ lastName).toLowerCase();
    }
}
