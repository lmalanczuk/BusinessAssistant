package com.licencjat.BusinessAssistant.entity;

import com.licencjat.BusinessAssistant.entity.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "Users")
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

//    @Column(name = "zoom_user_id")
//    private String zoomUserId;
//
//    @Column(name = "zoom_access_token")
//    private String zoomAccessToken;
//
//    @Column(name = "zoom_refresh_token")
//    private String zoomRefreshToken;
//
//    @Column(name = "zoom_token_expiry")
//    private LocalDateTime zoomTokenExpiry;
    @Column(name = "zego_user_id")
    private String zegoUserId;

    public Users(UUID id, String firstName, String lastName, String email, String username, String password, Role role, Set<Meeting> meetings) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
        this.meetings = meetings;
    }

    private String generateUsername(String firstName, String lastName){
        if(firstName == null || lastName == null){
            throw new IllegalArgumentException("First name and last name cannot be null");
        }
        return (firstName.charAt(0) +"."+ lastName).toLowerCase();
    }


}
