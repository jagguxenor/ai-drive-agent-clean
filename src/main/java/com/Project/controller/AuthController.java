package com.Project.controller;

import java.util.Map;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    @GetMapping("/user")
    public Map<String, Object> getUser(OAuth2AuthenticationToken auth) {

        Map<String, Object> attributes = auth.getPrincipal().getAttributes();

        return Map.of(
                "name", attributes.get("name"),
                "email", attributes.get("email"),
                "picture", attributes.get("picture")
        );
    }
}