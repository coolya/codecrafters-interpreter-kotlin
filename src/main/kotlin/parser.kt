fun recover(tokens: TokenIterator, message: String): TokenIterator? {
    System.err.println("Error: $message")
    // Skip the current token and return the next one
    return tokens.next()
}

fun expression(tokens: TokenIterator): Pair<Expression, TokenIterator?> {
    // Expression delegates to term
    return term(tokens)
}

fun term(tokens: TokenIterator): Pair<Expression, TokenIterator?> {
    // Start with a factor expression
    var (expr, nextTokens) = factor(tokens)

    // Continue as long as we find + or - operators
    while (nextTokens != null && nextTokens.token is TokenLike.SimpleToken) {
        val token = nextTokens.token as TokenLike.SimpleToken
        if (token.type == TokenType.PLUS || token.type == TokenType.MINUS) {
            val operator = token.lexeme
            val afterOperator = nextTokens.next() ?: return Pair(expr, null)

            // Parse the right operand
            val (right, afterRight) = factor(afterOperator)

            // Create a binary expression
            expr = Expression.Binary(expr, operator, right)
            nextTokens = afterRight
        } else {
            // If not an addition or subtraction operator, break the loop
            break
        }
    }

    return Pair(expr, nextTokens)
}

fun unary(tokens: TokenIterator): Pair<Expression, TokenIterator?> {
    // Check if the current token is a unary operator (! or -)
    if (tokens.token is TokenLike.SimpleToken) {
        val token = tokens.token as TokenLike.SimpleToken
        if (token.type == TokenType.BANG || token.type == TokenType.MINUS) {
            val operator = token.lexeme
            val nextTokens = tokens.next() ?: return Pair(Expression.NilLiteral(), null)

            // Parse the right operand recursively
            val (right, afterRight) = unary(nextTokens)

            // Create a unary expression
            return Pair(Expression.Unary(operator, right), afterRight)
        }
    }

    // If not a unary operator, delegate to primary
    return primary(tokens)
}

fun factor(tokens: TokenIterator): Pair<Expression, TokenIterator?> {
    // Start with a unary expression
    var (expr, nextTokens) = unary(tokens)

    // Continue as long as we find * or / operators
    while (nextTokens != null && nextTokens.token is TokenLike.SimpleToken) {
        val token = nextTokens.token as TokenLike.SimpleToken
        if (token.type == TokenType.STAR || token.type == TokenType.SLASH) {
            val operator = token.lexeme
            val afterOperator = nextTokens.next() ?: return Pair(expr, null)

            // Parse the right operand
            val (right, afterRight) = unary(afterOperator)

            // Create a binary expression
            expr = Expression.Binary(expr, operator, right)
            nextTokens = afterRight
        } else {
            // If not a multiplication or division operator, break the loop
            break
        }
    }

    return Pair(expr, nextTokens)
}

fun primary(tokens: TokenIterator): Pair<Expression, TokenIterator?> {
    return when (val token = tokens.token) {
        is TokenLike.SimpleToken -> {
            when (token.type) {
                TokenType.TRUE -> Pair(Expression.BooleanLiteral(true), tokens.next())
                TokenType.FALSE -> Pair(Expression.BooleanLiteral(false), tokens.next())
                TokenType.NIL -> Pair(Expression.NilLiteral(), tokens.next())
                TokenType.LEFT_PAREN -> {
                    // Handle parenthesized expressions
                    val nextTokens = tokens.next() ?: return Pair(Expression.NilLiteral(), null)

                    // Parse the expression inside the parentheses
                    val (expr, afterExpr) = expression(nextTokens)
                    if (afterExpr == null) {
                        return Pair(Expression.NilLiteral(), null)
                    }

                    // Check for the closing parenthesis
                    if (afterExpr.token is TokenLike.SimpleToken &&
                        (afterExpr.token as TokenLike.SimpleToken).type == TokenType.RIGHT_PAREN) {
                        // Return the grouped expression with the iterator after the closing parenthesis
                        Pair(Expression.Grouping(expr), afterExpr.next())
                    } else {
                        val message = "Expected ')' after expression"
                        val recoveredTokens = recover(afterExpr, message)
                        if (recoveredTokens != null) {
                            primary(recoveredTokens)
                        } else {
                            Pair(Expression.NilLiteral(), null)
                        }
                    }
                }
                else -> {
                    val message = "Unexpected token: $token"
                    val nextTokens = recover(tokens, message)
                    if (nextTokens != null) {
                        primary(nextTokens)
                    } else {
                        // If we can't recover (end of tokens), return a nil literal as a fallback
                        Pair(Expression.NilLiteral(), null)
                    }
                }
            }
        }
        is TokenLike.NumberToken -> {
            Pair(Expression.NumberLiteral(token), tokens.next())
        }
        is TokenLike.StringToken -> {
            Pair(Expression.StringLiteral(token), tokens.next())
        }
        else -> {
            val message = "Unexpected token: $token"
            val nextTokens = recover(tokens, message)
            if (nextTokens != null) {
                primary(nextTokens)
            } else {
                // If we can't recover (end of tokens), return a nil literal as a fallback
                Pair(Expression.NilLiteral(), null)
            }
        }
    }
}
