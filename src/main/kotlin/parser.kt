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

// Sealed class to represent the result of parsing a statement
sealed class StatementParseResult {
    // Success case with a statement and next tokens
    data class Success(val statement: Statement, val nextTokens: TokenIterator?) : StatementParseResult()

    // Error case with an error message and next tokens
    data class Error(val message: String, val nextTokens: TokenIterator?) : StatementParseResult()

    /**
     * Apply a transformation to the statement if this is a Success
     */
    inline fun map(transform: (Statement) -> Statement): StatementParseResult = when (this) {
        is Success -> statementSuccess(transform(statement), nextTokens)
        is Error -> this
    }

    /**
     * Chain an operation that returns a StatementParseResult
     */
    inline fun flatMap(transform: (Statement, TokenIterator?) -> StatementParseResult): StatementParseResult = when (this) {
        is Success -> transform(statement, nextTokens)
        is Error -> this
    }

    /**
     * Execute a block of code if this is a Success
     */
    inline fun onSuccess(block: (Statement, TokenIterator?) -> Unit): StatementParseResult {
        if (this is Success) {
            block(statement, nextTokens)
        }
        return this
    }

    /**
     * Fold over the two possible cases
     */
    inline fun <T> fold(
        onSuccess: (Statement, TokenIterator?) -> T,
        onError: (String, TokenIterator?) -> T
    ): T = when (this) {
        is Success -> onSuccess(statement, nextTokens)
        is Error -> onError(message, nextTokens)
    }
}

/**
 * Helper function to create a successful statement parse result
 */
fun statementSuccess(statement: Statement, nextTokens: TokenIterator?): StatementParseResult {
    return StatementParseResult.Success(statement, nextTokens)
}

/**
 * Helper function to create an error statement result
 */
fun statementError(message: String, tokens: TokenIterator?): StatementParseResult {
    return StatementParseResult.Error(message, tokens)
}

/**
 * Helper function to recover from a statement parsing error
 */
fun statementRecover(tokens: TokenIterator, message: String): StatementParseResult {
    // Skip the current token and return an error result
    return statementError(message, tokens.next())
}

fun expression(tokens: TokenIterator): ParseResult {
    // Expression delegates to assignment
    return assignment(tokens)
}

fun assignment(tokens: TokenIterator): ParseResult {
    // Parse the left-hand side as an equality expression
    val expr = equality(tokens)

    return expr.flatMap { leftExpr, nextTokens ->
        if (nextTokens == null) {
            return@flatMap success(leftExpr, null)
        }

        // Check if the next token is an assignment operator
        if (nextTokens.token is TokenLike.SimpleToken && (nextTokens.token as TokenLike.SimpleToken).type == TokenType.EQUAL) {
            // Skip the '=' token
            val afterEqual = nextTokens.next() ?: return@flatMap success(leftExpr, null)

            // Parse the right-hand side as an assignment expression (for right associativity)
            val rightResult = assignment(afterEqual)

            return@flatMap rightResult.flatMap { rightExpr, rightNextTokens ->
                // Check if the left-hand side is a variable
                if (leftExpr is Expression.Variable) {
                    // Create an assignment expression
                    success(Expression.Assignment(leftExpr.name, rightExpr), rightNextTokens)
                } else {
                    // Invalid assignment target
                    error("Invalid assignment target", nextTokens)
                }
            }
        } else {
            // Not an assignment, return the original expression
            success(leftExpr, nextTokens)
        }
    }
}

/**
 * Parse a statement
 */
fun statement(tokens: TokenIterator): StatementParseResult {
    // Check if the current token is a print keyword
    if (tokens.token is TokenLike.SimpleToken) {
        val token = tokens.token as TokenLike.SimpleToken
        when (token.type) {
            TokenType.PRINT -> return printStatement(tokens)
            TokenType.VAR -> return varDeclaration(tokens)
            TokenType.LEFT_BRACE -> return blockStatement(tokens)
            else -> {}
        }
    }

    // Otherwise, it's an expression statement
    return expressionStatement(tokens)
}

/**
 * Parse a print statement
 */
fun printStatement(tokens: TokenIterator): StatementParseResult {
    // Skip the print keyword
    val afterPrint = tokens.next() ?: return statementError("Unexpected end of input after 'print'", null)

    // Parse the expression to be printed
    val exprResult = expression(afterPrint)

    return exprResult.fold(
        onSuccess = { expr, afterExpr ->
            if (afterExpr == null) {
                return@fold statementError("Expected ';' after value", null)
            }

            // Check for semicolon
            if (afterExpr.token is TokenLike.SimpleToken && (afterExpr.token as TokenLike.SimpleToken).type == TokenType.SEMICOLON) {
                statementSuccess(Statement.Print(expr), afterExpr.next())
            } else {
                statementError("Expected ';' after value", afterExpr)
            }
        },
        onError = { message, errorTokens ->
            statementError(message, errorTokens)
        }
    )
}

/**
 * Parse an expression statement
 */
fun expressionStatement(tokens: TokenIterator): StatementParseResult {
    // Parse the expression
    val exprResult = expression(tokens)

    return exprResult.fold(
        onSuccess = { expr, afterExpr ->
            if (afterExpr == null) {
                return@fold statementError("Expected ';' after expression", null)
            }

            // Check for semicolon
            if (afterExpr.token is TokenLike.SimpleToken && (afterExpr.token as TokenLike.SimpleToken).type == TokenType.SEMICOLON) {
                statementSuccess(Statement.ExpressionStatement(expr), afterExpr.next())
            } else {
                statementError("Expected ';' after expression", afterExpr)
            }
        },
        onError = { message, errorTokens ->
            statementError(message, errorTokens)
        }
    )
}

/**
 * Parse a block statement
 */
fun blockStatement(tokens: TokenIterator): StatementParseResult {
    // Skip the left brace
    val afterLeftBrace = tokens.next() ?: return statementError("Unexpected end of input after '{'", null)

    // Parse statements until we encounter a right brace
    val statements = mutableListOf<Statement>()
    var currentTokens = afterLeftBrace

    while (currentTokens.token !is TokenLike.SimpleToken ||
           (currentTokens.token as TokenLike.SimpleToken).type != TokenType.RIGHT_BRACE) {

        // Parse a statement
        val statementResult = statement(currentTokens)

        when (statementResult) {
            is StatementParseResult.Success -> {
                statements.add(statementResult.statement)
                currentTokens = statementResult.nextTokens ?:
                    return statementError("Unexpected end of input in block", null)
            }
            is StatementParseResult.Error -> {
                return statementResult
            }
        }

        // If we've reached the end of input without finding a closing brace
        if (currentTokens.token !is TokenLike.SimpleToken) {
            return statementError("Expected '}' at end of block", currentTokens)
        }
    }

    // Skip the right brace
    val afterRightBrace = currentTokens.next()

    return statementSuccess(Statement.Block(statements), afterRightBrace)
}

/**
 * Parse a variable declaration
 */
fun varDeclaration(tokens: TokenIterator): StatementParseResult {
    // Skip the 'var' keyword
    val afterVar = tokens.next() ?: return statementError("Unexpected end of input after 'var'", null)

    // Expect an identifier
    if (afterVar.token !is TokenLike.SimpleToken || (afterVar.token as TokenLike.SimpleToken).type != TokenType.IDENTIFIER) {
        return statementError("Expected variable name", afterVar)
    }

    val name = (afterVar.token as TokenLike.SimpleToken).lexeme
    var afterName = afterVar.next() ?: return statementError("Unexpected end of input after variable name", null)

    // Check for initializer
    var initializer: Expression? = null
    if (afterName.token is TokenLike.SimpleToken && (afterName.token as TokenLike.SimpleToken).type == TokenType.EQUAL) {
        // Skip the '=' token
        val afterEqual = afterName.next() ?: return statementError("Unexpected end of input after '='", null)

        // Parse the initializer expression
        val initResult = expression(afterEqual)

        return initResult.fold(
            onSuccess = { expr, afterExpr ->
                initializer = expr
                afterName = afterExpr ?: return@fold statementError("Unexpected end of input after initializer", null)

                // Check for semicolon
                if (afterName.token is TokenLike.SimpleToken && (afterName.token as TokenLike.SimpleToken).type == TokenType.SEMICOLON) {
                    statementSuccess(Statement.Var(name, initializer), afterName.next())
                } else {
                    statementError("Expected ';' after variable declaration", afterName)
                }
            },
            onError = { message, errorTokens ->
                statementError(message, errorTokens)
            }
        )
    }

    // No initializer, just check for semicolon
    if (afterName.token is TokenLike.SimpleToken && (afterName.token as TokenLike.SimpleToken).type == TokenType.SEMICOLON) {
        return statementSuccess(Statement.Var(name, null), afterName.next())
    } else {
        return statementError("Expected ';' after variable declaration", afterName)
    }
}

/**
 * Parse a program (a list of statements)
 */
fun program(tokens: TokenIterator): List<StatementParseResult> {
    val statements = mutableListOf<StatementParseResult>()
    var currentTokens: TokenIterator? = tokens

    // Parse statements until we reach the end of the input
    while (currentTokens != null &&
           (currentTokens.token !is TokenLike.SimpleToken ||
            (currentTokens.token as? TokenLike.SimpleToken)?.type != TokenType.EOF)) {
        val stmtResult = statement(currentTokens)
        statements.add(stmtResult)

        // Update the current tokens for the next iteration
        currentTokens = stmtResult.fold(
            onSuccess = { _, nextTokens -> nextTokens },
            onError = { _, nextTokens -> nextTokens }
        )

        // If we've reached the end of the input, break
        if (currentTokens == null) {
            break
        }
    }

    return statements
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
                TokenType.IDENTIFIER -> success(Expression.Variable(token.lexeme), tokens.next())
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
