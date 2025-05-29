package com.licencjat.BusinessAssistant.model.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    private String email;

    private  String password;

}
