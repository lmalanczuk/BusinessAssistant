package com.licencjat.BusinessAssistant.entity;

import com.licencjat.BusinessAssistant.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Entity
@Data
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
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToMany(mappedBy = "participants")
    private Set<Meeting> meetings;

}
