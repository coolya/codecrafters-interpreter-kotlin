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
        // Not implemented yet - will be added in future tasks
        return Value.Nil
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
        // Not implemented yet - will be added in future tasks
        return Value.Nil
    }

    override fun visitStringLiteralExpression(expression: Expression.StringLiteral): Value {
        return Value.String(expression.value.value)
    }
}
