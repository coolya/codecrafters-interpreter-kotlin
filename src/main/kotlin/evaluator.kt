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

object Evaluator : Expression.Visitor<Value> {
    override fun visitBinaryExpression(expr: Expression.Binary): Value {
        val left = expr.left.accept(this)
        val right = expr.right.accept(this)

        // Handle binary operators
        return when (expr.operator) {
            "*" -> {
                if (left is Value.Number && right is Value.Number) {
                    Value.Number(left.value * right.value)
                } else {
                    Value.Nil
                }
            }
            "/" -> {
                if (left is Value.Number && right is Value.Number) {
                    Value.Number(left.value / right.value)
                } else {
                    Value.Nil
                }
            }
            "+" -> {
                if (left is Value.Number && right is Value.Number) {
                    Value.Number(left.value + right.value)
                } else if (left is Value.String && right is Value.String) {
                    Value.String(left.value + right.value)
                } else {
                    Value.Nil
                }
            }
            "-" -> {
                if (left is Value.Number && right is Value.Number) {
                    Value.Number(left.value - right.value)
                } else {
                    Value.Nil
                }
            }
            ">" -> {
                if (left is Value.Number && right is Value.Number) {
                    Value.Boolean(left.value > right.value)
                } else {
                    Value.Nil
                }
            }
            "<" -> {
                if (left is Value.Number && right is Value.Number) {
                    Value.Boolean(left.value < right.value)
                } else {
                    Value.Nil
                }
            }
            ">=" -> {
                if (left is Value.Number && right is Value.Number) {
                    Value.Boolean(left.value >= right.value)
                } else {
                    Value.Nil
                }
            }
            "<=" -> {
                if (left is Value.Number && right is Value.Number) {
                    Value.Boolean(left.value <= right.value)
                } else {
                    Value.Nil
                }
            }
            else -> Value.Nil
        }
    }

    override fun visitBooleanLiteral(literal: Expression.BooleanLiteral): Value {
        return Value.Boolean(literal.value)
    }

    override fun visitNilLiteral(literal: Expression.NilLiteral): Value {
        return Value.Nil
    }

    override fun visitNumberLiteralExpression(expression: Expression.NumberLiteral): Value {
        return Value.Number(expression.value.value)
    }

    override fun visitGroupingExpression(expression: Expression.Grouping): Value {
        // Evaluate the expression inside the parentheses
        return expression.expression.accept(this)
    }

    override fun visitUnaryExpression(expression: Expression.Unary): Value {
        val right = expression.right.accept(this)

        return when (expression.operator) {
            "-" -> {
                if (right is Value.Number) {
                    Value.Number(-right.value)
                } else {
                    // If the operand is not a number, we can't negate it
                    Value.Nil
                }
            }
            "!" -> {
                // For truthiness/falsiness: false and nil are falsy, everything else is truthy
                val isTruthy = when (right) {
                    is Value.Boolean -> right.value
                    is Value.Nil -> false
                    else -> true
                }
                Value.Boolean(!isTruthy)
            }
            else -> Value.Nil
        }
    }

    override fun visitStringLiteralExpression(expression: Expression.StringLiteral): Value {
        return Value.String(expression.value.value)
    }
}
