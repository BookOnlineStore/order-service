package com.bookstore.orderservice.order.web;

import com.bookstore.orderservice.book.BookDto;
import com.bookstore.orderservice.book.BookNotFoundException;
import com.bookstore.orderservice.book.InsufficientStockException;
import com.bookstore.orderservice.order.OrderNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Book controller advice.
 */
@RestControllerAdvice
public class BookControllerAdvice {

    /**
     * Handle book not found api error.
     *
     * @param ex the ex
     * @return the api error
     */
    @ExceptionHandler(BookNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleBookNotFound(BookNotFoundException ex) {
        ApiError.ErrorInfo errorInfo = new ApiError.ErrorInfo();
        errorInfo.setEntity(BookDto.class.getSimpleName());
        errorInfo.setMessage(ex.getMessage());
        return ApiError.builder()
                .errors(List.of(errorInfo)).build();
    }

    /**
     * Handle order not found api error.
     *
     * @param ex the ex
     * @return the api error
     */
    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleOrderNotFound(OrderNotFoundException ex) {
        ApiError.ErrorInfo errorInfo = new ApiError.ErrorInfo();
        errorInfo.setEntity(BookDto.class.getSimpleName());
        errorInfo.setMessage(ex.getMessage());
        return ApiError.builder()
                .errors(List.of(errorInfo)).build();
    }

    /**
     * Handle insufficient stock api error.
     *
     * @param ex the ex
     * @return the api error
     */
    @ExceptionHandler(InsufficientStockException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInsufficientStock(InsufficientStockException ex) {
        ApiError.ErrorInfo errorInfo = new ApiError.ErrorInfo();
        errorInfo.setEntity(BookDto.class.getSimpleName());
        errorInfo.setMessage(ex.getMessage());
        return ApiError.builder()
                .errors(List.of(errorInfo)).build();
    }

    /**
     * Handle method argument not valid api error.
     *
     * @param ex the ex
     * @return the api error
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<ApiError.ErrorInfo> errorInfors = ex.getBindingResult()
                .getFieldErrors().stream()
                .map(fieldError ->
                        new ApiError.ErrorInfo(
                                fieldError.getObjectName(),
                                fieldError.getField(),
                                fieldError.getRejectedValue(),
                                fieldError.getDefaultMessage())
                ).collect(Collectors.toList());
        return new ApiError(errorInfors);
    }

    /**
     * Handle missing servlet response parameter api error.
     *
     * @param ex the ex
     * @return the api error
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingServletResponseParameter(MissingServletRequestParameterException ex) {
        ApiError.ErrorInfo errorInfo = new ApiError.ErrorInfo();
        errorInfo.setProperty(ex.getParameterName());
        errorInfo.setMessage(ex.getMessage());
        return ApiError.builder()
                .errors(List.of(errorInfo)).build();
    }

    /**
     * Handle type mismatch api error.
     *
     * @param ex the ex
     * @return the api error
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ApiError.ErrorInfo errorInfo = new ApiError.ErrorInfo();
        errorInfo.setProperty(ex.getName());
        errorInfo.setMessage(ex.getName() + " should be of type " + ex.getRequiredType().getSimpleName());
        return ApiError.builder()
                .errors(List.of(errorInfo)).build();
    }

}
