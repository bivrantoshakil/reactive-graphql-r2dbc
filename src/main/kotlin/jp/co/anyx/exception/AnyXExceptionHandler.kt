package jp.co.anyx.exception

import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.DataFetchingEnvironment
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter
import org.springframework.stereotype.Component

@Component
class AnyXExceptionHandler : DataFetcherExceptionResolverAdapter() {

    // TODO can be improved further covering more scenarios
    override fun resolveToSingleError(ex: Throwable, env: DataFetchingEnvironment): GraphQLError? {
        return if (ex is AnyXGraphQLException) {
            GraphqlErrorBuilder.newError()
                .errorType(ex.errorType)
                .message(ex.message)
                .path(env.executionStepInfo.path)
                .location(env.field.sourceLocation)
                .build()
        } else {
            super.resolveToSingleError(ex, env)
        }
    }
}
