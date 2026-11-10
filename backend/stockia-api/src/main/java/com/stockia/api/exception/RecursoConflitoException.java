package com.stockia.api.exception;

public class RecursoConflitoException extends RuntimeException {

    public RecursoConflitoException(String mensagem) {
        super(mensagem);
    }
}