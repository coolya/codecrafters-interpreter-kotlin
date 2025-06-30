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

// Immutable class to store variable bindings
data class Environment(private val values: Map<String, Value> = mapOf()) {
    // Returns a new Environment with the variable defined
    fun define(name: String, value: Value?): Environment {
        val newValues = values.toMutableMap()
        newValues[name] = value ?: Value.Nil
        return Environment(newValues)
    }

    fun get(name: String): Value? {
        return values[name]
    }

    // Returns a new Environment with the variable updated
    fun set(name: String, value: Value): Pair<Environment, Value?> {
        if (!values.containsKey(name)) {
            return Pair(this, null)
        }
        val newValues = values.toMutableMap()
        newValues[name] = value
        return Pair(Environment(newValues), value)
    }
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
object StatementEvaluator : Statement.Visitor<StatementEvaluationResult, Environment> {
    override fun visitPrintStatement(statement: Statement.Print, environment: Environment): Pair<StatementEvaluationResult, Environment> {
        // Evaluate the expression to be printed
        val (result, newEnv) = statement.expression.accept(Evaluator, environment)

        return when (result) {
            is EvaluationResult.Success -> {
                // Print the value
                println(result.value)
                Pair(statementSuccess(), newEnv)
            }
            is EvaluationResult.Error -> {
                // Propagate the error
                Pair(statementError(result.message), newEnv)
            }
        }
    }

    override fun visitExpressionStatement(statement: Statement.ExpressionStatement, environment: Environment): Pair<StatementEvaluationResult, Environment> {
        // Evaluate the expression
        val (result, newEnv) = statement.expression.accept(Evaluator, environment)

        return when (result) {
            is EvaluationResult.Success -> {
                // Expression statements don't produce output
                Pair(statementSuccess(), newEnv)
            }
            is EvaluationResult.Error -> {
                // Propagate the error
                Pair(statementError(result.message), newEnv)
            }
        }
    }

    override fun visitVarStatement(statement: Statement.Var, environment: Environment): Pair<StatementEvaluationResult, Environment> {
        // Evaluate the initializer if it exists
        val (value, newEnv) = if (statement.initializer != null) {
            val (result, env) = statement.initializer.accept(Evaluator, environment)
            when (result) {
                is EvaluationResult.Success -> Pair(result.value, env)
                is EvaluationResult.Error -> return Pair(statementError(result.message), env)
            }
        } else {
            Pair(null, environment)
        }

        // Define the variable in the environment
        val updatedEnv = newEnv.define(statement.name, value)

        return Pair(statementSuccess(), updatedEnv)
    }

    override fun visitBlockStatement(statement: Statement.Block, environment: Environment): Pair<StatementEvaluationResult, Environment> {
        // Evaluate each statement in the block in order
        var currentEnv = environment
        var result: StatementEvaluationResult = statementSuccess()

        for (stmt in statement.statements) {
            val (stmtResult, newEnv) = stmt.accept(this, currentEnv)
            currentEnv = newEnv

            // If a statement evaluation resulted in an error, propagate the error
            if (stmtResult is StatementEvaluationResult.Error) {
                return Pair(stmtResult, currentEnv)
            }

            result = stmtResult
        }

        return Pair(result, currentEnv)
    }
}

object Evaluator : Expression.Visitor<EvaluationResult, Environment> {
    override fun visitVariableExpression(expression: Expression.Variable, environment: Environment): Pair<EvaluationResult, Environment> {
        val value = environment.get(expression.name)
        return if (value != null) {
            Pair(success(value), environment)
        } else {
            Pair(error("Undefined variable '${expression.name}'"), environment)
        }
    }

    override fun visitBinaryExpression(expr: Expression.Binary, environment: Environment): Pair<EvaluationResult, Environment> {
        val (leftResult, leftEnv) = expr.left.accept(this, environment)
        val (rightResult, rightEnv) = expr.right.accept(this, leftEnv)

        // If either operand evaluation resulted in an error, propagate the error
        if (leftResult is EvaluationResult.Error) {
            return Pair(leftResult, rightEnv)
        }
        if (rightResult is EvaluationResult.Error) {
            return Pair(rightResult, rightEnv)
        }

        // Extract values from successful results
        val left = (leftResult as EvaluationResult.Success).value
        val right = (rightResult as EvaluationResult.Success).value

        // Handle binary operators
        val result = when (expr.operator) {
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
                } else if (left is Value.Boolean && right is Value.Boolean) {
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
                } else if (left is Value.Boolean && right is Value.Boolean) {
                    success(Value.Boolean(left.value != right.value))
                } else {
                    // Different types are always not equal
                    success(Value.Boolean(true))
                }
            }
            else -> success(Value.Nil)
        }

        return Pair(result, rightEnv)
    }

    override fun visitBooleanLiteral(literal: Expression.BooleanLiteral, environment: Environment): Pair<EvaluationResult, Environment> {
        return Pair(success(Value.Boolean(literal.value)), environment)
    }

    override fun visitNilLiteral(literal: Expression.NilLiteral, environment: Environment): Pair<EvaluationResult, Environment> {
        return Pair(success(Value.Nil), environment)
    }

    override fun visitNumberLiteralExpression(expression: Expression.NumberLiteral, environment: Environment): Pair<EvaluationResult, Environment> {
        return Pair(success(Value.Number(expression.value.value)), environment)
    }

    override fun visitGroupingExpression(expression: Expression.Grouping, environment: Environment): Pair<EvaluationResult, Environment> {
        // Evaluate the expression inside the parentheses
        return expression.expression.accept(this, environment)
    }

    override fun visitUnaryExpression(expression: Expression.Unary, environment: Environment): Pair<EvaluationResult, Environment> {
        val (rightResult, rightEnv) = expression.right.accept(this, environment)

        // If the operand evaluation resulted in an error, propagate the error
        if (rightResult is EvaluationResult.Error) {
            return Pair(rightResult, rightEnv)
        }

        // Extract value from successful result
        val right = (rightResult as EvaluationResult.Success).value

        val result = when (expression.operator) {
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

        return Pair(result, rightEnv)
    }

    override fun visitStringLiteralExpression(expression: Expression.StringLiteral, environment: Environment): Pair<EvaluationResult, Environment> {
        return Pair(success(Value.String(expression.value.value)), environment)
    }

    override fun visitAssignmentExpression(expression: Expression.Assignment, environment: Environment): Pair<EvaluationResult, Environment> {
        // Evaluate the value to be assigned
        val (valueResult, valueEnv) = expression.value.accept(this, environment)

        // If the value evaluation resulted in an error, propagate the error
        if (valueResult is EvaluationResult.Error) {
            return Pair(valueResult, valueEnv)
        }

        // Extract value from successful result
        val value = (valueResult as EvaluationResult.Success).value

        // Set the variable in the environment
        val (newEnvironment, result) = valueEnv.set(expression.name, value)

        return if (result != null) {
            // Return the assigned value
            Pair(success(result), newEnvironment)
        } else {
            // If the variable doesn't exist, return an error
            Pair(error("Undefined variable '${expression.name}'"), valueEnv)
        }
    }
}
