package edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind;

/**
 * Wrapper used when interface does not allow throwing a checked
 * {@link JsonMappingException}
 */
@SuppressWarnings("serial")
public class RuntimeJsonMappingException extends RuntimeException
{
    public RuntimeJsonMappingException(JsonMappingException cause) {
        super(cause);
    }

    public RuntimeJsonMappingException(String message) {
        super(message);
    }

    public RuntimeJsonMappingException(String message, JsonMappingException cause) {
        super(message, cause);
    }
}
