package com.ucb.medicalnow.Controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ucb.medicalnow.BL.ConsultBl;
import com.ucb.medicalnow.BL.ConversationBl;
import com.ucb.medicalnow.BL.MedicalHistoryBl;
import com.ucb.medicalnow.Model.ConsultIdModel;
import com.ucb.medicalnow.Model.DoctorMessageModel;
import com.ucb.medicalnow.Model.PatientMessageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/v1/chat")
public class ChatController {


    private MedicalHistoryBl medicalHistoryBl;
    private ConsultBl consultBl;
    private ConversationBl conversationBl;

    @Autowired
    public ChatController(MedicalHistoryBl medicalHistoryBl, ConsultBl consultBl, ConversationBl conversationBl) {
        this.medicalHistoryBl = medicalHistoryBl;
        this.consultBl = consultBl;
        this.conversationBl = conversationBl;
    }

    @Value("${medicalnow.security.secretJwt}")
    private String secretJwt;

    @RequestMapping(
            value = "send/message/patient/{userId}",
            method = RequestMethod.POST,
            produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> addMessageFromPatient (@RequestHeader("Authorization") String authorization,
                                                                      @RequestBody PatientMessageModel patientMessageModel,
                                                                      @PathVariable("userId") Integer userId) {
        // ***********
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
        // ***********

        Map<String, Object> response = new HashMap<>();
        // Verifica si es que existe una historia medica entre el paciente y el doctor
        Map<String, Object> medicalHistoryResult = medicalHistoryBl.medicalHistoryExists(patientMessageModel.getDoctorSpecialtyId(), userId);
        if (medicalHistoryResult.get("exists").equals(true)){
            // Si existe la historia medica
            int medicalHistoryId = Integer.parseInt(medicalHistoryResult.get("id").toString());

            // Verifica si existe una consulta entre el doctor y el paciente
            // La consulta debe estar abierta
            Map<String, Object> consultResult = consultBl.consultExists(medicalHistoryId);
            if(consultResult.get("exists").equals(true)){
                // Si existe la consulta
                int consultId = Integer.parseInt(consultResult.get("id").toString());

                // Se añade el mensaje
                Boolean conversationResponse = conversationBl.addMessageToConversation(consultId, patientMessageModel.getMessage(), userId);
                if (conversationResponse){
                    response.put("response", "The message was added succesfully");
                } else {
                    response.put("response", "The message wasn't added succesfully");
                }
            } else {
                // Si no existe la consulta, se crea una
                Boolean consultResponse = consultBl.createNewConsult(medicalHistoryId);
                int consultId = Integer.parseInt(consultBl.returnConsultId(medicalHistoryId).toString());
                if(consultResponse){

                    // Se añade el mensaje
                    Boolean conversationResponse = conversationBl.addMessageToConversation(consultId, patientMessageModel.getMessage(), userId);
                    if (conversationResponse){
                        response.put("response", "The message was added succesfully");
                    } else {
                        response.put("response", "The message wasn't added succesfully");
                    }
                } else {
                    response.put("response", "Consult not created");
                }
            }
        } else {
            // Si no existe la historia medica, se crea una
            Boolean medicalHistoryResponse = medicalHistoryBl.createMedicalHistory(userId, patientMessageModel.getDoctorSpecialtyId());
            if (medicalHistoryResponse){
                int medicalHistoryId = Integer.parseInt(medicalHistoryBl.returnMedicalHistoryId(patientMessageModel.getDoctorSpecialtyId(), userId).toString());

                // Se crea una consulta
                Boolean consultResponse = consultBl.createNewConsult(medicalHistoryId);
                int consultId = Integer.parseInt(consultBl.returnConsultId(medicalHistoryId).toString());
                if(consultResponse){

                    // Agregar el mensaje
                    Boolean conversationResponse = conversationBl.addMessageToConversation(consultId, patientMessageModel.getMessage(), userId);
                    if (conversationResponse){
                        response.put("response", "The message was added succesfully");
                    } else {
                        response.put("response", "The message wasn't added succesfully");
                    }
                } else {
                    response.put("response", "Consult not created");
                }
            } else {
                response.put("response", "Medical history not created");
            }
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(
            value = "send/message/doctor/{userId}",
            method = RequestMethod.POST,
            produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> addMessageFromDoctor (@RequestHeader("Authorization") String authorization,
                                                                     @RequestBody DoctorMessageModel doctorMessageModel,
                                                                     @PathVariable("userId") Integer userId) {
        // ***********
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
        // ***********

        Map<String, Object> response = new HashMap<>();
        Boolean conversationResponse = conversationBl.addMessageToConversation(doctorMessageModel.getConsultId(), doctorMessageModel.getMessage(), userId);
        if (conversationResponse){
            response.put("response", "The message was added succesfully");
        } else {
            response.put("response", "The message wasn't added succesfully");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(
            value = "patient/{consultId}/messages",
            method = RequestMethod.GET,
            produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getChatForPatient (@RequestHeader("Authorization") String authorization,
                                                                  @PathVariable("consultId") Integer consultId) {

        // *********
        // Decodificando el token
        String tokenJwt = authorization.substring(7);
        DecodedJWT decodedJWT = JWT.decode(tokenJwt);
        //Validando si el token es bueno y de autenticación
        if (!"AUTHN".equals(decodedJWT.getClaim("type").asString())) {
            throw new RuntimeException("El token proporcionado no es un token de autenticación");
        }
        Algorithm algorithm = Algorithm.HMAC256(secretJwt);
        JWTVerifier verifier = JWT.require(algorithm).withIssuer("Medicalnow").build();
        verifier.verify(tokenJwt);
        // *********

        return new ResponseEntity<>(this.conversationBl.returnPatientConversationByConsultId(consultId), HttpStatus.OK);
    }

    @RequestMapping(
            value = "doctor/{consultId}/messages",
            method = RequestMethod.GET,
            produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getChatForDoctor (@RequestHeader("Authorization") String authorization,
                                                                 @PathVariable("consultId") Integer consultId) {
        // *********
        //Decodificando el token
        String tokenJwt = authorization.substring(7);
        DecodedJWT decodedJWT = JWT.decode(tokenJwt);
        //Validando si el token es bueno y de autenticación
        if (!"AUTHN".equals(decodedJWT.getClaim("type").asString())) {
            throw new RuntimeException("El token proporcionado no es un token de autenticación");
        }
        Algorithm algorithm = Algorithm.HMAC256(secretJwt);
        JWTVerifier verifier = JWT.require(algorithm).withIssuer("Medicalnow").build();
        verifier.verify(tokenJwt);
        // *********

        return new ResponseEntity<>(this.conversationBl.returnDoctorConversationByConsultId(consultId), HttpStatus.OK);
    }
}
