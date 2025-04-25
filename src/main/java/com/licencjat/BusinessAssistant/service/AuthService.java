package com.licencjat.BusinessAssistant.service;

import com.licencjat.BusinessAssistant.entity.Users;
import com.licencjat.BusinessAssistant.entity.enums.Role;
import com.licencjat.BusinessAssistant.model.UserDTO;
import com.licencjat.BusinessAssistant.model.request.LoginRequest;
import com.licencjat.BusinessAssistant.model.request.RegisterRequest;
import com.licencjat.BusinessAssistant.model.response.AuthResponse;
import com.licencjat.BusinessAssistant.repository.UserRepository;
import com.licencjat.BusinessAssistant.security.UserPrincipal;
import com.licencjat.BusinessAssistant.util.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthService(
            UserRepository userRepository,
            JwtTokenProvider tokenProvider,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
       if(userRepository.findByEmail(request.getEmail()).isPresent()) {
           throw new IllegalStateException("Email already taken");
       }
        Users user = new Users();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); //TODO: implement password encoder
        user.setUsername(generateUsername(request.getFirstName(), request.getLastName()));
        user.setRole(Role.USER);
        Users savedUser = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);

        return new AuthResponse(jwt, mapToUserDTO(savedUser));
   }

   public AuthResponse login(LoginRequest request) {
         Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                          request.getEmail(),
                          request.getPassword()));

         SecurityContextHolder.getContext().setAuthentication(authentication);

         String jwt = tokenProvider.generateToken(authentication);

       UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
         Users user = userRepository.findByEmail(userPrincipal.getEmail())
                 .orElseThrow(() -> new IllegalStateException("User not found"));

         return new AuthResponse(jwt, mapToUserDTO(user));


   }

   private UserDTO mapToUserDTO(Users user) {
       UserDTO dto = new UserDTO();

            dto.setId(user.getId());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setEmail(user.getEmail());
            dto.setRole(user.getRole());
            return dto;
   }


    private String generateUsername(String firstName, String lastName) {
       if(firstName.isEmpty() || lastName.isEmpty()) {
              throw new IllegalStateException("First name or last name cannot be empty");
       }
       String baseUsername = firstName + "." + lastName;
       String username = baseUsername;
       if(userRepository.findByUsername(baseUsername).isPresent()) {
           throw new IllegalStateException("Username already taken");
       }
       int count = 1;
        while(userRepository.findByUsername(baseUsername + count).isPresent()) {
            username = baseUsername + count++;
        }

        return username;
    }
}
