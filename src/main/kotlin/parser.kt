fun recover(tokens: TokenIterator, message: String): TokenIterator? {
    System.err.println("Error: $message")
    // Skip the current token and return the next one
    return tokens.next()
}

fun primary(tokens: TokenIterator): Pair<Expression, TokenIterator?> {
    return when (val token = tokens.token) {
        is TokenLike.SimpleToken -> {
            when (token.type) {
                TokenType.TRUE -> Pair(Expression.BooleanLiteral(true), tokens.next())
                TokenType.FALSE -> Pair(Expression.BooleanLiteral(false), tokens.next())
                TokenType.NIL -> Pair(Expression.NilLiteral(), tokens.next())
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
