sealed class Expression {
    interface Visitor<R> {
        fun visitNumberLiteralExpression(expression: NumberLiteral): R
        fun visitGroupingExpression(expression: Grouping): R
        fun visitUnaryExpression(expression: Unary): R
        fun visitBinaryExpression(expression: Binary): R
        fun visitBooleanLiteral(literal: BooleanLiteral): R
    }

    abstract fun <R> accept(visitor: Visitor<R>): R

    data class NumberLiteral(val value: TokenLike.NumberToken) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitNumberLiteralExpression(this)
    }

    data class Grouping(val expression: Expression) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitGroupingExpression(this)
    }

    data class Unary(
        val operator: String,
        val right: Expression
    ) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitUnaryExpression(this)
    }

    data class BooleanLiteral(val value: Boolean) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitBooleanLiteral(this)
    }

    data class Binary(
        val left: Expression,
        val operator: String,
        val right: Expression
    ) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitBinaryExpression(this)
    }


}

