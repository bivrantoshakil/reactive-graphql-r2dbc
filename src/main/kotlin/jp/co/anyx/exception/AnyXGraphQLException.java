package jp.co.anyx.exception;

import graphql.ErrorClassification;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.util.List;

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
}
