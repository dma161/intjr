package com.game.exception_handling;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class PlayerGlobalException {

    @ExceptionHandler(value = {NoSuchPlayerException.class})
    public ResponseEntity<PlayerIncorrectData> handleException(NoSuchPlayerException exception){
        PlayerIncorrectData data = new PlayerIncorrectData();
        data.setInfo(exception.getMessage());
        return  new ResponseEntity<>(data, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {BadRequeException.class})
    public ResponseEntity<PlayerIncorrectData> handleException(BadRequeException exception){
        PlayerIncorrectData data = new PlayerIncorrectData();
        data.setInfo(exception.getMessage());
        return  new ResponseEntity<>(data, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(value = {Oki.class})
    public ResponseEntity<PlayerIncorrectData> handleException(Oki exception){
        PlayerIncorrectData data = new PlayerIncorrectData();
        data.setInfo(exception.getMessage());
        return  new ResponseEntity<>(data, HttpStatus.OK);
    }
}
