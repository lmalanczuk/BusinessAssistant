package com.licencjat.BusinessAssistant.service;

import com.licencjat.BusinessAssistant.model.UserDTO;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    public UserDTO registerUser(UserDTO userDTO);

    public UserDTO loginUser(String username, String password);


}
