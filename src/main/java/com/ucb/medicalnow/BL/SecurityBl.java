package com.ucb.medicalnow.BL;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.hash.Hashing;
import com.ucb.medicalnow.DAO.UserDao;
import com.ucb.medicalnow.Model.TokenRefreshModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class SecurityBl {

    private UserDao userDao;

    @Value("${medicalnow.security.salt}")
    private String salt;

    @Value("${medicalnow.security.secretJwt}")
    private String secretJwt;

    @Autowired
    public SecurityBl (UserDao userDao) { this.userDao = userDao; }

    public Map<String, String> authenticate (String username, String password){
        Map<String, String> result = new HashMap<>();
        // Para aplicar la función hash + salt
        String sha256hex= Hashing.sha256()
                .hashString(password+salt, StandardCharsets.UTF_8)
                .toString();
        Integer userId = userDao.findUserByEmailAndPassword(username,sha256hex);
        if(userId != null){
            result.put("authentication", generateJwt(userId,10,"AUTHN", userDao.findRoleByUser(userId)));
            result.put("refresh", generateJwt(userId, 20, "REFRESH", null));
            result.put("userId", userId.toString());
            return result;
        }else{
            return null;
        }
    }

    public Map<String,String> refresh (TokenRefreshModel tokenRefreshModel){
        Map<String,String> result = new HashMap<>();
        String tokenJwt = tokenRefreshModel.getRefreshToken();
        DecodedJWT decodedJWT = JWT.decode(tokenJwt);
        String userId = decodedJWT.getSubject();
        if(!"REFRESH".equals(decodedJWT.getClaim("type").asString())){
            throw new RuntimeException("El token proporcionado no es un token válido para actualizar");
        }
        // Validación de si el token es bueno y además si es un token de autenticación
        Algorithm algorithm = Algorithm.HMAC256(secretJwt);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer("Medicalnow")
                .build();
        verifier.verify(tokenJwt);
        Integer userIdAsInt = Integer.parseInt(userId);
        result.put("authentication",generateJwt(Integer.parseInt(userId),1,"AUTHN", userDao.findRoleByUser(userIdAsInt)));
        result.put("refresh",generateJwt(Integer.parseInt(userId),2,"REFRESH", null));
        result.put("userId", userId);
        return result;
    }

    private String generateJwt (int userId, int minutes, String type, String role){
        LocalDateTime expiresAt = LocalDateTime.now(ZoneId.systemDefault()).plusMinutes(minutes);
        String token = null;
        try{
            Algorithm algorithm = Algorithm.HMAC256(secretJwt);
            if(role!=null){
                token = JWT.create()
                        .withIssuer("Medicalnow")
                        .withClaim("type", type)
                        .withClaim("role", role)
                        .withSubject(Integer.toString(userId))
                        .withExpiresAt(Date.from(expiresAt.atZone(ZoneId.systemDefault()).toInstant()))
                        .sign(algorithm);

            }else{
                token = JWT.create()
                        .withIssuer("Medicalnow")
                        .withClaim("type", type)
                        .withSubject(Integer.toString(userId))
                        .withExpiresAt(Date.from(expiresAt.atZone(ZoneId.systemDefault()).toInstant()))
                        .sign(algorithm);
            }
        }catch (JWTCreationException exception){
            throw new RuntimeException(exception);
        }
        return token;
    }

    public void validateToken (String authorization){
        // Decodificando el token
        String tokenJwt = authorization.substring(7);
        DecodedJWT decodedJWT = JWT.decode(tokenJwt);
        //Validando si el token es bueno y de autenticación
        if(!"AUTHN".equals(decodedJWT.getClaim("type").asString())){
            throw new RuntimeException("El token proporcionado no es un token de autenticación");
        }
        Algorithm algorithm = Algorithm.HMAC256(secretJwt);
        JWTVerifier verifier = JWT.require(algorithm).withIssuer("Medicalnow").build();
        verifier.verify(tokenJwt);
    }
}
