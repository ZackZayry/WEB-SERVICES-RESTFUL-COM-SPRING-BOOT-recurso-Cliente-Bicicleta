package br.edu.ifc.bikes.web.exception;

import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.exception.PasswordInvalidException;
import br.edu.ifc.bikes.exception.RecursoDuplicadoException;
import br.edu.ifc.bikes.exception.UsernameUniqueViolationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 422 - campos inválidos (devolve o mapa campo -> mensagem)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> methodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request,
            BindingResult result) {
        return build(request, HttpStatus.UNPROCESSABLE_CONTENT, "Campo(s) inválido(s)", result);
    }

    // 404 - recurso não encontrado
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorMessage> entityNotFoundException(
            EntityNotFoundException ex, HttpServletRequest request) {
        return build(request, HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // 409 - email, CPF, número de série ou username duplicado
    @ExceptionHandler({RecursoDuplicadoException.class, UsernameUniqueViolationException.class})
    public ResponseEntity<ErrorMessage> conflito(RuntimeException ex, HttpServletRequest request) {
        return build(request, HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    // 409 - rede de segurança para violações de constraint do banco (ex.: duas requisições simultâneas)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorMessage> dataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        return build(request, HttpStatus.CONFLICT, "Operação viola uma restrição de integridade dos dados", null);
    }

    // 400 - regras de senha (senha atual incorreta, confirmação divergente...)
    @ExceptionHandler(PasswordInvalidException.class)
    public ResponseEntity<ErrorMessage> passwordInvalidException(
            PasswordInvalidException ex, HttpServletRequest request) {
        return build(request, HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    // 400 - JSON malformado ou corpo ausente
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorMessage> httpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(request, HttpStatus.BAD_REQUEST, "Corpo da requisição ausente ou malformado", null);
    }

    private ResponseEntity<ErrorMessage> build(HttpServletRequest request, HttpStatus status,
                                               String message, BindingResult result) {
        ErrorMessage body = (result == null)
                ? new ErrorMessage(request, status, message)
                : new ErrorMessage(request, status, message, result);
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
    }
}
