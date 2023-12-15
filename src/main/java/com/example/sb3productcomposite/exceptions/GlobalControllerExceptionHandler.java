package com.example.sb3productcomposite.exceptions;//package com.example.sb3product.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.HttpErrorInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler {

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    public @ResponseBody HttpErrorInfo handleBadRequestException(ServerHttpRequest serverHttpRequest,BadRequestException exception){
        return createHttpErrorInfo(BAD_REQUEST,serverHttpRequest,exception);
    }

    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidInputException.class)
    public @ResponseBody HttpErrorInfo handleInvalidInputException(ServerHttpRequest serverHttpRequest, InvalidInputException exception){
        return createHttpErrorInfo(UNPROCESSABLE_ENTITY,serverHttpRequest,exception);
    }
    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public @ResponseBody HttpErrorInfo handleNotFoundException(ServerHttpRequest serverHttpRequest, NotFoundException exception){
        return createHttpErrorInfo(NOT_FOUND,serverHttpRequest,exception);
    }


    private HttpErrorInfo createHttpErrorInfo(HttpStatus httpStatus, ServerHttpRequest serverHttpRequest, Exception exception){
        final String path=serverHttpRequest.getPath().pathWithinApplication().value();
        final String message= exception.getMessage();
        log.debug("Returning HTTP status: {}, path: {}, message: {}",httpStatus,path,message);

        return HttpErrorInfo.builder().httpStatus(httpStatus.getReasonPhrase()).path(path).message(message).build();
    }
}
