package com.ucb.medicalnow.Controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ucb.medicalnow.BL.PrescriptionBl;
import com.ucb.medicalnow.Model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/v1/prescription")
public class PrescriptionController {

    private PrescriptionBl prescriptionBl;

    @Value("${medicalnow.security.secretJwt}")
    private String secretJwt;

    @Autowired
    public PrescriptionController (PrescriptionBl prescriptionBl) { this.prescriptionBl = prescriptionBl; }

    @RequestMapping(
            value = "consults/prescription/{userId}",
            method = RequestMethod.GET,
            produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ArrayList<PrescriptionListModel>> returnAllConsultsWithPrescriptions(@RequestHeader("Authorization") String authorization,
                                                                                               @PathVariable("userId") Integer userId){
        // *********
        //Decodificando el token
        String tokenJwt = authorization.substring(7);
        DecodedJWT decodedJWT = JWT.decode(tokenJwt);
        //Validando si el token es bueno y de autenticación
        if(!"AUTHN".equals(decodedJWT.getClaim("type").asString())){
            throw new RuntimeException("El token proporcionado no es un token de autenticación");
        }
        Algorithm algorithm = Algorithm.HMAC256(secretJwt);
        JWTVerifier verifier = JWT.require(algorithm).withIssuer("Medicalnow").build();
        verifier.verify(tokenJwt);
        // *********
        return new ResponseEntity<>(this.prescriptionBl.returnAllConsultsWithPrescriptions(userId), HttpStatus.OK);
    }

    @RequestMapping(
            value = "{consultId}/all",
            method = RequestMethod.GET,
            produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> returnAllPrescriptionsByConsultId (@RequestHeader("Authorization") String authorization,
                                                                                  @PathVariable("consultId") Integer consultId){
        // *********
        //Decodificando el token
        String tokenJwt = authorization.substring(7);
        DecodedJWT decodedJWT = JWT.decode(tokenJwt);
        //Validando si el token es bueno y de autenticación
        if(!"AUTHN".equals(decodedJWT.getClaim("type").asString())){
            throw new RuntimeException("El token proporcionado no es un token de autenticación");
        }
        Algorithm algorithm = Algorithm.HMAC256(secretJwt);
        JWTVerifier verifier = JWT.require(algorithm).withIssuer("Medicalnow").build();
        verifier.verify(tokenJwt);
        // *********
        return new ResponseEntity<>(this.prescriptionBl.returnAllPrescriptionsByConsultId(consultId), HttpStatus.OK);
    }

    @RequestMapping(
            value = "{prescriptionId}/detail",
            method = RequestMethod.GET,
            produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> returnPrescriptionDetail (@RequestHeader("Authorization") String authorization,
                                                                                         @PathVariable("prescriptionId") Integer prescriptionId){
        //Decodificando el token
        String tokenJwt = authorization.substring(7);
        DecodedJWT decodedJWT = JWT.decode(tokenJwt);
        //Validando si el token es bueno y de autenticación
        if(!"AUTHN".equals(decodedJWT.getClaim("type").asString())){
            throw new RuntimeException("El token proporcionado no es un token de autenticación");
        }
        Algorithm algorithm = Algorithm.HMAC256(secretJwt);
        JWTVerifier verifier = JWT.require(algorithm).withIssuer("Medicalnow").build();
        verifier.verify(tokenJwt);

        return new ResponseEntity<>(this.prescriptionBl.returnPrescriptionDetailByPrescriptionId(prescriptionId), HttpStatus.OK);
    }

    @RequestMapping(
            value = "{consultId}/add/detail",
            method = RequestMethod.POST,
            produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> addPrescriptionDetail (@RequestHeader("Authorization") String authorization,
                                       @PathVariable("consultId") Integer consultId,
                                       @RequestBody PrescriptionModel prescriptionModel){
        //Decodificando el token
        String tokenJwt = authorization.substring(7);
        DecodedJWT decodedJWT = JWT.decode(tokenJwt);
        //Validando si el token es bueno y de autenticación
        if(!"AUTHN".equals(decodedJWT.getClaim("type").asString())){
            throw new RuntimeException("El token proporcionado no es un token de autenticación");
        }
        Algorithm algorithm = Algorithm.HMAC256(secretJwt);
        JWTVerifier verifier = JWT.require(algorithm).withIssuer("Medicalnow").build();
        verifier.verify(tokenJwt);

        Map<String, String> response = new HashMap<>();
        Boolean prescriptionUpdated = prescriptionBl.addPrescriptionDetail(consultId, prescriptionModel.getDescription(), prescriptionModel.getContent());
        if(prescriptionUpdated){
            response.put("response", "Products added succesfully");
        } else {
            response.put("response", "Products not added");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
