package br.edu.ifc.bikes.exception;

// Violação de unicidade (email, CPF, número de série...) -> HTTP 409
public class RecursoDuplicadoException extends RuntimeException {
    public RecursoDuplicadoException(String message) {
        super(message);
    }
}
