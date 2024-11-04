package org.sommiersys.sommiersys.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.NotFoundException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ControllerAdvice
public class CustomExceptionHandler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @ExceptionHandler(NotFoundException.class)
    public Object handleNotFoundException(NotFoundException ex, HttpServletRequest request) {
        String acceptHeader = request.getHeader(ViewConstants.REQUEST_HEADER);

        if (acceptHeader != null && acceptHeader.contains(ViewConstants.HEADER_CONTAINS_HTML)) {
            ModelAndView mav = new ModelAndView(ViewConstants.ERROR_404_PAGE);
            mav.addObject("message", ex.getMessage());
            return mav;
        }

        ErrorDTO errorObject = new ErrorDTO(
                request.getRequestURI(),
                "Not Found",
                ex.getMessage(),
                LocalDateTime.now().format(FORMATTER),
                HttpStatus.NOT_FOUND.value()
        );

        return new ResponseEntity<>(errorObject, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleNotFoundException(AccessDeniedException ex, HttpServletRequest request) {
        String acceptHeader = request.getHeader(ViewConstants.REQUEST_HEADER);

        if (acceptHeader != null && acceptHeader.contains(ViewConstants.HEADER_CONTAINS_HTML)) {
            ModelAndView mav = new ModelAndView(ViewConstants.ERROR_500_PAGE);
            mav.addObject("message", ex.getMessage());
            return mav;
        }

        ErrorDTO errorObject = new ErrorDTO(
                request.getRequestURI(),
                "Not Found",
                ex.getMessage(),
                LocalDateTime.now().format(FORMATTER),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(errorObject, HttpStatus.BAD_REQUEST);
    }
}
