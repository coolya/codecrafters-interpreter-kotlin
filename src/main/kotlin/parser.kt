fun recover(tokens: TokenIterator, message: String): TokenIterator? {
    System.err.println("Error: $message")
    // Skip the current token and return the next one
    return tokens.next()
}

fun primary(tokens: TokenIterator): Pair<Expression, TokenIterator?> {
    return when (tokens.token) {
        is TokenLike.SimpleToken -> {
            when ((tokens.token as TokenLike.SimpleToken).type) {
                TokenType.TRUE -> Pair(Expression.BooleanLiteral(true), tokens.next())
                TokenType.FALSE -> Pair(Expression.BooleanLiteral(false), tokens.next())
                TokenType.NIL -> Pair(Expression.NilLiteral(), tokens.next())
                else -> {
                    val message = "Unexpected token: ${tokens.token}"
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
            Pair(Expression.NumberLiteral(tokens.token as TokenLike.NumberToken), tokens.next())
        }
        else -> {
            val message = "Unexpected token: ${tokens.token}"
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
