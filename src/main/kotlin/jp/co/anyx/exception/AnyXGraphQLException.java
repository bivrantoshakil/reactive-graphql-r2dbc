package jp.co.anyx.exception;

import graphql.ErrorClassification;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AnyXGraphQLException extends RuntimeException implements GraphQLError {

    private ErrorType errorType;

    public AnyXGraphQLException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorClassification getErrorType() {
        return errorType;
    }

    @Override
    public Map<String, Object> getExtensions() {
        return Collections.singletonMap("errorMessage", super.getMessage());
    }
}
