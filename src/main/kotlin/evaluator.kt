sealed class Value {
    object Nil : Value() {
        override fun toString(): String = "nil"
    }

    data class Boolean(val value: kotlin.Boolean) : Value() {
        override fun toString(): String = value.toString()
    }

    // We'll add more value types as needed in future implementations
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
        // Not implemented yet - will be added in future tasks
        return Value.Nil
    }

    override fun visitGroupingExpression(expression: Expression.Grouping): Value {
        // Not implemented yet - will be added in future tasks
        return Value.Nil
    }

    override fun visitUnaryExpression(expression: Expression.Unary): Value {
        // Not implemented yet - will be added in future tasks
        return Value.Nil
    }

    override fun visitStringLiteralExpression(expression: Expression.StringLiteral): Value {
        // Not implemented yet - will be added in future tasks
        return Value.Nil
    }
}