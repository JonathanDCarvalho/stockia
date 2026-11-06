
package com.stockia.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> tratarDadosInvalidos(
                        MethodArgumentNotValidException exception) {

                Map<String, String> erros = new HashMap<>();

                exception.getBindingResult()
                                .getFieldErrors()
                                .forEach(erro -> erros.put(erro.getField(), erro.getDefaultMessage()));

                Map<String, Object> resposta = Map.of(
                                "timestamp", LocalDateTime.now(),
                                "status", HttpStatus.BAD_REQUEST.value(),
                                "erro", "Bad Request",
                                "mensagens", erros);

                return ResponseEntity
                                .badRequest()
                                .body(resposta);
        }

        @ExceptionHandler(RecursoNaoEncontradoException.class)
        public ResponseEntity<Map<String, Object>> tratarRecursoNaoEncontrado(
                        RecursoNaoEncontradoException exception) {

                Map<String, Object> resposta = Map.of(
                                "timestamp", LocalDateTime.now(),
                                "status", HttpStatus.NOT_FOUND.value(),
                                "erro", "Not Found",
                                "mensagem", exception.getMessage());

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(resposta);
        }

}
