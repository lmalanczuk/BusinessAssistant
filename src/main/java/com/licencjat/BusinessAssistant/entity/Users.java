package com.licencjat.BusinessAssistant.entity;

import com.licencjat.BusinessAssistant.entity.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "Users")
// Poniższe adnotacje są kluczowe - wykluczają meetings z hashCode i equals
@EqualsAndHashCode(exclude = "meetings")
@ToString(exclude = "meetings")
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

    @ManyToMany(mappedBy = "participants", fetch = FetchType.LAZY)
    private Set<Meeting> meetings = new HashSet<>();

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

       public void addMeeting(Meeting meeting) {
        if (meetings == null) {
            meetings = new HashSet<>();
        }
        meetings.add(meeting);
    }
}