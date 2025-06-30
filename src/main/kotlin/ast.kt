sealed class Expression {
    interface Visitor<R> {
        fun visitNumberLiteralExpression(expression: NumberLiteral): R
        fun visitGroupingExpression(expression: Grouping): R
        fun visitUnaryExpression(expression: Unary): R
        fun visitBinaryExpression(expression: Binary): R
        fun visitBooleanLiteral(literal: BooleanLiteral): R
        fun visitNilLiteral(literal: NilLiteral): R
        fun visitStringLiteralExpression(expression: StringLiteral): R
        fun visitVariableExpression(expression: Variable): R
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

    data class NilLiteral(val value : String = "nil") : Expression() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitNilLiteral(this)
    }

    data class Binary(
        val left: Expression,
        val operator: String,
        val right: Expression
    ) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitBinaryExpression(this)
    }

    data class StringLiteral(val value: TokenLike.StringToken) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitStringLiteralExpression(this)
    }

    data class Variable(val name: String) : Expression() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitVariableExpression(this)
    }
}

sealed class Statement {
    interface Visitor<R> {
        fun visitPrintStatement(statement: Print): R
        fun visitExpressionStatement(statement: ExpressionStatement): R
        fun visitVarStatement(statement: Var): R
    }

    abstract fun <R> accept(visitor: Visitor<R>): R

    data class Print(val expression: Expression) : Statement() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitPrintStatement(this)
    }

    data class ExpressionStatement(val expression: Expression) : Statement() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitExpressionStatement(this)
    }

    data class Var(val name: String, val initializer: Expression?) : Statement() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitVarStatement(this)
    }
}
