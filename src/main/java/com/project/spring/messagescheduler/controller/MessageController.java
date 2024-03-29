package com.project.spring.messagescheduler.controller;

import com.project.spring.messagescheduler.dto.MessageRequest;
import com.project.spring.messagescheduler.entity.Message;
import com.project.spring.messagescheduler.exceptions.AuthFailedException;
import com.project.spring.messagescheduler.exceptions.ResourceNotFoundException;
import com.project.spring.messagescheduler.service.AuthRequest;
import com.project.spring.messagescheduler.service.MessageService;
import com.project.spring.messagescheduler.utils.StatusUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/message")
public class MessageController {
    private final Logger logger= LoggerFactory.getLogger(MessageController.class);
    @Autowired
    private AuthRequest authRequest;

    @Autowired
    private StatusUtil statusUtil;

    @Autowired
    private MessageService service;

    public MessageController(AuthRequest authRequest, StatusUtil statusUtil, MessageService service) {
        this.authRequest = authRequest;
        this.statusUtil = statusUtil;
        this.service = service;
    }

    @GetMapping("/test")
    public ResponseEntity<String> getTestConnection(){
        return new ResponseEntity<>("Message Controller is working!", HttpStatus.OK);
    }

    /**
     * Sends a message to the client using Gupshup API
     * @param authToken a valid header must be added by the user
     * @param messageRequest message Request object is validated using validator methods
     * @return User about the action viz triggerd
     * @throws AuthFailedException when the token and user id does not match
     * @throws ResourceNotFoundException display error to the user
     */
    @PostMapping("/text")
    @ResponseBody
    public ResponseEntity<Message> sendMessageToClient(@RequestHeader(value ="auth_token", required = true) String authToken,@RequestBody @Valid MessageRequest messageRequest) throws AuthFailedException, ResourceNotFoundException {
        logger.info(messageRequest.toString());
        if(!authRequest.isValidUser(authToken, (long) messageRequest.getUserId())){
            throw new AuthFailedException("Invalid User");
        }
        Optional<Message> message=service.saveMessage(messageRequest);
        if(!message.isPresent()){
            throw new ResourceNotFoundException("Something went wrong");
        }
        return new ResponseEntity<>(message.get(),HttpStatus.CREATED);
    }
    /**
     * The message is retrieved using message ID
     * @param authToken a valid header must be added by the user
     * @param messageId the user sends the messageID
     * @return returns message object or else null
     */
    @GetMapping("/message/{id}")
    public ResponseEntity<Message> retrieveMessageData(@RequestHeader("auth_token") String authToken,@PathVariable Long id,@RequestParam("messageId") Long messageId) throws AuthFailedException, ResourceNotFoundException {
        if(!authRequest.isValidUser(authToken,id)){
            throw new AuthFailedException("Invalid User");
        }
        return new ResponseEntity<>(service.retrieveMessage(messageId),HttpStatus.OK);
    }
    /**
     * The message is retrieved using message ID
     * @param authToken a valid header must be added by the user
     * @return returns message list of message or else displays error
     */
    @GetMapping("/allmessages/{userId}")
    public ResponseEntity<List<Message>> retrieveAllMessages(@RequestHeader("auth_token") String authToken,@PathVariable Long userId) throws AuthFailedException, ResourceNotFoundException {
        if(!authRequest.isValidUser(authToken,userId)){
            throw new AuthFailedException("Invalid User");
        }
        return new ResponseEntity<>(service.retrieveAllMessage(userId),HttpStatus.OK);
    }
    /**
     * The message is retrieved using message ID
     * @param authToken a valid header must be added by the user
     * @param userId user Id is must be valid
     * @param status status must be valid
     * @return returns message list of message or else displays error
     */
    @GetMapping("/message/{userId}/{status}")
    public ResponseEntity<List<Message>> retrieveAllStatusMessages(@RequestHeader("auth_token") String authToken,@PathVariable Long userId,@PathVariable String status) throws AuthFailedException,ResourceNotFoundException {
        if(!authRequest.isValidUser(authToken,userId)){
            throw new AuthFailedException("Invalid User");
        }
        int statusCode=statusUtil.getStatus(status);
        if(statusCode==-1)
            throw new ResourceNotFoundException("Wrong Status Type");
        return new ResponseEntity<>(service.retrieveByStatus(userId,statusCode),HttpStatus.OK);
    }
}