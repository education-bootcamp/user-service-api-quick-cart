package com.devstack.system.service.impl;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import java.util.*;

@Service
public class JwtService {

    @Value("${public.key.string}")
    private  String publicKeyString;

    public String getEmail(String token){
        try{
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyString);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(spec);

            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build().parseClaimsJws(token);

            Claims body = claimsJws.getBody();
            return body.get("email", String.class);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
    public List<String> getRoles(String token) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyString);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(spec);

            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build().parseClaimsJws(token);

            Claims body = claimsJws.getBody();
            Map<String, List<String>> realmAccess = (Map<String, List<String>>) body.get("realm_access");
            return realmAccess.get("roles");

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}
