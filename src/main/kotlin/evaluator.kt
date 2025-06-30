sealed class Value {
    object Nil : Value() {
        override fun toString(): kotlin.String = "nil"
    }

    data class Boolean(val value: kotlin.Boolean) : Value() {
        override fun toString(): kotlin.String = value.toString()
    }

    data class String(val value: kotlin.String) : Value() {
        override fun toString(): kotlin.String = value
    }

    data class Number(val value: Double) : Value() {
        override fun toString(): kotlin.String {
            // Format number with minimum decimal places without losing precision
            return if (value == value.toLong().toDouble()) {
                value.toLong().toString()
            } else {
                // Remove trailing zeros
                val formatted = value.toString()
                if (formatted.contains('.')) {
                    formatted.trimEnd('0').trimEnd('.')
                } else {
                    formatted
                }
            }
        }
    }
}

// Sealed class to represent the result of evaluation (discriminated union)
sealed class EvaluationResult {
    // Success case with a value
    data class Success(val value: Value) : EvaluationResult() {
        override fun toString(): String = value.toString()
    }

    // Error case with an error message
    data class Error(val message: String) : EvaluationResult()

    /**
     * Apply a transformation to the value if this is a Success
     */
    inline fun map(transform: (Value) -> Value): EvaluationResult = when (this) {
        is Success -> success(transform(value))
        is Error -> this
    }

    /**
     * Chain an operation that returns an EvaluationResult
     */
    inline fun flatMap(transform: (Value) -> EvaluationResult): EvaluationResult = when (this) {
        is Success -> transform(value)
        is Error -> this
    }

    /**
     * Execute a block of code if this is a Success
     */
    inline fun onSuccess(block: (Value) -> Unit): EvaluationResult {
        if (this is Success) {
            block(value)
        }
        return this
    }

    /**
     * Fold over the two possible cases
     */
    inline fun <T> fold(
        onSuccess: (Value) -> T,
        onError: (String) -> T
    ): T = when (this) {
        is Success -> onSuccess(value)
        is Error -> onError(message)
    }
}

/**
 * Helper function to create a successful evaluation result
 */
fun success(value: Value): EvaluationResult {
    return EvaluationResult.Success(value)
}

/**
 * Helper function to create an error result
 */
fun error(message: String): EvaluationResult {
    return EvaluationResult.Error(message)
}

// Sealed class to represent the result of evaluating a statement
sealed class StatementEvaluationResult {
    // Success case
    object Success : StatementEvaluationResult()

    // Error case with an error message
    data class Error(val message: String) : StatementEvaluationResult()
}

/**
 * Helper function to create a successful statement evaluation result
 */
fun statementSuccess(): StatementEvaluationResult {
    return StatementEvaluationResult.Success
}

/**
 * Helper function to create an error statement evaluation result
 */
fun statementError(message: String): StatementEvaluationResult {
    return StatementEvaluationResult.Error(message)
}

/**
 * Evaluator for statements
 */
object StatementEvaluator : Statement.Visitor<StatementEvaluationResult> {
    override fun visitPrintStatement(statement: Statement.Print): StatementEvaluationResult {
        // Evaluate the expression to be printed
        val result = statement.expression.accept(Evaluator)

        return when (result) {
            is EvaluationResult.Success -> {
                // Print the value
                println(result.value)
                statementSuccess()
            }
            is EvaluationResult.Error -> {
                // Propagate the error
                statementError(result.message)
            }
        }
    }

    override fun visitExpressionStatement(statement: Statement.ExpressionStatement): StatementEvaluationResult {
        // Evaluate the expression
        val result = statement.expression.accept(Evaluator)

        return when (result) {
            is EvaluationResult.Success -> {
                // Expression statements don't produce output
                statementSuccess()
            }
            is EvaluationResult.Error -> {
                // Propagate the error
                statementError(result.message)
            }
        }
    }
}

object Evaluator : Expression.Visitor<EvaluationResult> {
    override fun visitBinaryExpression(expr: Expression.Binary): EvaluationResult {
        val leftResult = expr.left.accept(this)
        val rightResult = expr.right.accept(this)

        // If either operand evaluation resulted in an error, propagate the error
        if (leftResult is EvaluationResult.Error) {
            return leftResult
        }
        if (rightResult is EvaluationResult.Error) {
            return rightResult
        }

        // Extract values from successful results
        val left = (leftResult as EvaluationResult.Success).value
        val right = (rightResult as EvaluationResult.Success).value

        // Handle binary operators
        return when (expr.operator) {
            "*" -> {
                if (left is Value.Number && right is Value.Number) {
                    try {
                        success(Value.Number(left.value * right.value))
                    } catch (e: ArithmeticException) {
                        error("Runtime error: ${e.message}")
                    }
                } else {
                    error("Runtime error: Operands must be numbers")
                }
            }
            "/" -> {
                if (left is Value.Number && right is Value.Number) {
                    if (right.value == 0.0) {
                        error("Runtime error: Division by zero")
                    } else {
                        try {
                            success(Value.Number(left.value / right.value))
                        } catch (e: ArithmeticException) {
                            error("Runtime error: ${e.message}")
                        }
                    }
                } else {
                    error("Runtime error: Operands must be numbers")
                }
            }
            "+" -> {
                if (left is Value.Number && right is Value.Number) {
                    try {
                        success(Value.Number(left.value + right.value))
                    } catch (e: ArithmeticException) {
                        error("Runtime error: ${e.message}")
                    }
                } else if (left is Value.String && right is Value.String) {
                    success(Value.String(left.value + right.value))
                } else {
                    error("Runtime error: Operands must be two numbers or two strings")
                }
            }
            "-" -> {
                if (left is Value.Number && right is Value.Number) {
                    try {
                        success(Value.Number(left.value - right.value))
                    } catch (e: ArithmeticException) {
                        error("Runtime error: ${e.message}")
                    }
                } else {
                    error("Runtime error: Operands must be numbers")
                }
            }
            ">" -> {
                if (left is Value.Number && right is Value.Number) {
                    success(Value.Boolean(left.value > right.value))
                } else {
                    error("Runtime error: Operands must be numbers")
                }
            }
            "<" -> {
                if (left is Value.Number && right is Value.Number) {
                    success(Value.Boolean(left.value < right.value))
                } else {
                    error("Runtime error: Operands must be numbers")
                }
            }
            ">=" -> {
                if (left is Value.Number && right is Value.Number) {
                    success(Value.Boolean(left.value >= right.value))
                } else {
                    error("Runtime error: Operands must be numbers")
                }
            }
            "<=" -> {
                if (left is Value.Number && right is Value.Number) {
                    success(Value.Boolean(left.value <= right.value))
                } else {
                    error("Runtime error: Operands must be numbers")
                }
            }
            "==" -> {
                if (left is Value.Number && right is Value.Number) {
                    success(Value.Boolean(left.value == right.value))
                } else if (left is Value.String && right is Value.String) {
                    success(Value.Boolean(left.value == right.value))
                } else {
                    // Different types are never equal
                    success(Value.Boolean(false))
                }
            }
            "!=" -> {
                if (left is Value.Number && right is Value.Number) {
                    success(Value.Boolean(left.value != right.value))
                } else if (left is Value.String && right is Value.String) {
                    success(Value.Boolean(left.value != right.value))
                } else {
                    // Different types are always not equal
                    success(Value.Boolean(true))
                }
            }
            else -> success(Value.Nil)
        }
    }

    override fun visitBooleanLiteral(literal: Expression.BooleanLiteral): EvaluationResult {
        return success(Value.Boolean(literal.value))
    }

    override fun visitNilLiteral(literal: Expression.NilLiteral): EvaluationResult {
        return success(Value.Nil)
    }

    override fun visitNumberLiteralExpression(expression: Expression.NumberLiteral): EvaluationResult {
        return success(Value.Number(expression.value.value))
    }

    override fun visitGroupingExpression(expression: Expression.Grouping): EvaluationResult {
        // Evaluate the expression inside the parentheses
        return expression.expression.accept(this)
    }

    override fun visitUnaryExpression(expression: Expression.Unary): EvaluationResult {
        val rightResult = expression.right.accept(this)

        // If the operand evaluation resulted in an error, propagate the error
        if (rightResult is EvaluationResult.Error) {
            return rightResult
        }

        // Extract value from successful result
        val right = (rightResult as EvaluationResult.Success).value

        return when (expression.operator) {
            "-" -> {
                if (right is Value.Number) {
                    success(Value.Number(-right.value))
                } else {
                    // If the operand is not a number, return an error result
                    error("Operand must be a number for unary operator '-'")
                }
            }
            "!" -> {
                // For truthiness/falsiness: false and nil are falsy, everything else is truthy
                val isTruthy = when (right) {
                    is Value.Boolean -> right.value
                    is Value.Nil -> false
                    else -> true
                }
                success(Value.Boolean(!isTruthy))
            }
            else -> success(Value.Nil)
        }
    }

    override fun visitStringLiteralExpression(expression: Expression.StringLiteral): EvaluationResult {
        return success(Value.String(expression.value.value))
    }
}
