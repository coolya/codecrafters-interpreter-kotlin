// Sealed class to represent the result of parsing (discriminated union)
sealed class ParseResult {
    // Success case with an expression and next tokens
    data class Success(val expression: Expression, val nextTokens: TokenIterator?) : ParseResult()

    // Error case with an error message and next tokens
    data class Error(val message: String, val nextTokens: TokenIterator?) : ParseResult()

    /**
     * Apply a transformation to the expression if this is a Success
     */
    inline fun map(transform: (Expression) -> Expression): ParseResult = when (this) {
        is Success -> success(transform(expression), nextTokens)
        is Error -> this
    }

    /**
     * Chain an operation that returns a ParseResult
     */
    inline fun flatMap(transform: (Expression, TokenIterator?) -> ParseResult): ParseResult = when (this) {
        is Success -> transform(expression, nextTokens)
        is Error -> this
    }

    /**
     * Execute a block of code if this is a Success
     */
    inline fun onSuccess(block: (Expression, TokenIterator?) -> Unit): ParseResult {
        if (this is Success) {
            block(expression, nextTokens)
        }
        return this
    }

    /**
     * Fold over the two possible cases
     */
    inline fun <T> fold(
        onSuccess: (Expression, TokenIterator?) -> T,
        onError: (String, TokenIterator?) -> T
    ): T = when (this) {
        is Success -> onSuccess(expression, nextTokens)
        is Error -> onError(message, nextTokens)
    }
}

/**
 * Helper function to create a successful parse result
 */
fun success(expression: Expression, nextTokens: TokenIterator?): ParseResult {
    return ParseResult.Success(expression, nextTokens)
}

/**
 * Helper function to create an error result
 */
fun error(message: String, tokens: TokenIterator?): ParseResult {
    return ParseResult.Error(message, tokens)
}

fun recover(tokens: TokenIterator, message: String): ParseResult {
    // Skip the current token and return an error result
    return error(message, tokens.next())
}

fun expression(tokens: TokenIterator): ParseResult {
    // Expression delegates to equality
    return equality(tokens)
}

fun equality(tokens: TokenIterator): ParseResult {
    // Handle equality operators
    return parseBinaryExpression(
        tokens,
        ::comparison,
        listOf(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)
    )
}

fun comparison(tokens: TokenIterator): ParseResult {
    // Handle comparison operators
    return parseBinaryExpression(
        tokens,
        ::term,
        listOf(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)
    )
}

/**
 * Helper function to parse binary expressions with the given operator types
 */
fun parseBinaryExpression(
    tokens: TokenIterator,
    parseOperand: (TokenIterator) -> ParseResult,
    operatorTypes: List<TokenType>
): ParseResult {
    // Start with parsing the left operand
    return parseOperand(tokens).flatMap { leftExpr, nextTokens ->
        // Use a recursive helper function to process operators without mutable variables
        parseBinaryExpressionRec(leftExpr, nextTokens, parseOperand, operatorTypes)
    }
}

/**
 * Recursive helper function to parse binary expressions without mutable variables
 */
private fun parseBinaryExpressionRec(
    expr: Expression,
    tokens: TokenIterator?,
    parseOperand: (TokenIterator) -> ParseResult,
    operatorTypes: List<TokenType>
): ParseResult {
    // Base case: no more tokens or not a simple token
    if (tokens == null || tokens.token !is TokenLike.SimpleToken) {
        return success(expr, tokens)
    }

    val token = tokens.token as TokenLike.SimpleToken

    // If not one of the specified operators, we're done
    if (token.type !in operatorTypes) {
        return success(expr, tokens)
    }

    val operator = token.lexeme
    val afterOperator = tokens.next() ?: return success(expr, null)

    // Parse the right operand
    return parseOperand(afterOperator).fold(
        onSuccess = { rightExpr, rightNextTokens ->
            // Create a new binary expression
            val newExpr = Expression.Binary(expr, operator, rightExpr)
            // Recursively process the next operator
            parseBinaryExpressionRec(newExpr, rightNextTokens, parseOperand, operatorTypes)
        },
        onError = { message, errorTokens ->
            // If there's an error, return it immediately
            error(message, errorTokens)
        }
    )
}

fun term(tokens: TokenIterator): ParseResult {
    // Handle addition and subtraction operators
    return parseBinaryExpression(
        tokens,
        ::factor,
        listOf(TokenType.PLUS, TokenType.MINUS)
    )
}

fun unary(tokens: TokenIterator): ParseResult {
    // Check if the current token is a unary operator (! or -)
    if (tokens.token is TokenLike.SimpleToken) {
        val token = tokens.token as TokenLike.SimpleToken
        if (token.type == TokenType.BANG || token.type == TokenType.MINUS) {
            val operator = token.lexeme
            val nextTokens = tokens.next() ?: return success(Expression.NilLiteral(), null)

            // Parse the right operand recursively and map the result
            return unary(nextTokens).map { rightExpr ->
                // Create a unary expression
                Expression.Unary(operator, rightExpr)
            }
        }
    }

    // If not a unary operator, delegate to primary
    return primary(tokens)
}

fun factor(tokens: TokenIterator): ParseResult {
    // Handle multiplication and division operators
    return parseBinaryExpression(
        tokens,
        ::unary,
        listOf(TokenType.STAR, TokenType.SLASH)
    )
}

fun primary(tokens: TokenIterator): ParseResult {
    return when (val token = tokens.token) {
        is TokenLike.SimpleToken -> {
            when (token.type) {
                TokenType.TRUE -> success(Expression.BooleanLiteral(true), tokens.next())
                TokenType.FALSE -> success(Expression.BooleanLiteral(false), tokens.next())
                TokenType.NIL -> success(Expression.NilLiteral(), tokens.next())
                TokenType.LEFT_PAREN -> {
                    // Handle parenthesized expressions
                    val nextTokens = tokens.next() ?: return success(Expression.NilLiteral(), null)

                    // Parse the expression inside the parentheses and handle the result
                    expression(nextTokens).flatMap { expr, afterExpr ->
                        if (afterExpr == null) {
                            return success(Expression.NilLiteral(), null)
                        }

                        // Check for the closing parenthesis
                        if (afterExpr.token is TokenLike.SimpleToken &&
                            (afterExpr.token as TokenLike.SimpleToken).type == TokenType.RIGHT_PAREN) {
                            // Return the grouped expression with the iterator after the closing parenthesis
                            success(Expression.Grouping(expr), afterExpr.next())
                        } else {
                            val message = "Expected ')' after expression"
                            recover(afterExpr, message)
                        }
                    }
                }
                else -> {
                    val message = "Unexpected token: $token"
                    recover(tokens, message)
                }
            }
        }
        is TokenLike.NumberToken -> {
            success(Expression.NumberLiteral(token), tokens.next())
        }
        is TokenLike.StringToken -> {
            success(Expression.StringLiteral(token), tokens.next())
        }
        else -> {
            val message = "Unexpected token: $token"
            recover(tokens, message)
        }
    }
}
