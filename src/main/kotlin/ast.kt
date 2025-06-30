sealed class Expression {
    interface Visitor<R, E> {
        fun visitNumberLiteralExpression(expression: NumberLiteral, environment: E): Pair<R, E>
        fun visitGroupingExpression(expression: Grouping, environment: E): Pair<R, E>
        fun visitUnaryExpression(expression: Unary, environment: E): Pair<R, E>
        fun visitBinaryExpression(expression: Binary, environment: E): Pair<R, E>
        fun visitBooleanLiteral(literal: BooleanLiteral, environment: E): Pair<R, E>
        fun visitNilLiteral(literal: NilLiteral, environment: E): Pair<R, E>
        fun visitStringLiteralExpression(expression: StringLiteral, environment: E): Pair<R, E>
        fun visitVariableExpression(expression: Variable, environment: E): Pair<R, E>
        fun visitAssignmentExpression(expression: Assignment, environment: E): Pair<R, E>
    }

    // Special case for visitors that don't use the environment (like Printer)
    interface VisitorWithoutEnv<R> : Visitor<R, Unit> {
        override fun visitNumberLiteralExpression(expression: NumberLiteral, environment: Unit): Pair<R, Unit> =
            Pair(visitNumberLiteralExpressionWithoutEnv(expression), Unit)
        override fun visitGroupingExpression(expression: Grouping, environment: Unit): Pair<R, Unit> =
            Pair(visitGroupingExpressionWithoutEnv(expression), Unit)
        override fun visitUnaryExpression(expression: Unary, environment: Unit): Pair<R, Unit> =
            Pair(visitUnaryExpressionWithoutEnv(expression), Unit)
        override fun visitBinaryExpression(expression: Binary, environment: Unit): Pair<R, Unit> =
            Pair(visitBinaryExpressionWithoutEnv(expression), Unit)
        override fun visitBooleanLiteral(literal: BooleanLiteral, environment: Unit): Pair<R, Unit> =
            Pair(visitBooleanLiteralWithoutEnv(literal), Unit)
        override fun visitNilLiteral(literal: NilLiteral, environment: Unit): Pair<R, Unit> =
            Pair(visitNilLiteralWithoutEnv(literal), Unit)
        override fun visitStringLiteralExpression(expression: StringLiteral, environment: Unit): Pair<R, Unit> =
            Pair(visitStringLiteralExpressionWithoutEnv(expression), Unit)
        override fun visitVariableExpression(expression: Variable, environment: Unit): Pair<R, Unit> =
            Pair(visitVariableExpressionWithoutEnv(expression), Unit)
        override fun visitAssignmentExpression(expression: Assignment, environment: Unit): Pair<R, Unit> =
            Pair(visitAssignmentExpressionWithoutEnv(expression), Unit)

        // Simplified methods without environment
        fun visitNumberLiteralExpressionWithoutEnv(expression: NumberLiteral): R
        fun visitGroupingExpressionWithoutEnv(expression: Grouping): R
        fun visitUnaryExpressionWithoutEnv(expression: Unary): R
        fun visitBinaryExpressionWithoutEnv(expression: Binary): R
        fun visitBooleanLiteralWithoutEnv(literal: BooleanLiteral): R
        fun visitNilLiteralWithoutEnv(literal: NilLiteral): R
        fun visitStringLiteralExpressionWithoutEnv(expression: StringLiteral): R
        fun visitVariableExpressionWithoutEnv(expression: Variable): R
        fun visitAssignmentExpressionWithoutEnv(expression: Assignment): R
    }

    abstract fun <R, E> accept(visitor: Visitor<R, E>, environment: E): Pair<R, E>

    // Simplified accept method for visitors without environment
    fun <R> accept(visitor: VisitorWithoutEnv<R>): R = accept(visitor, Unit).first

    data class NumberLiteral(val value: TokenLike.NumberToken) : Expression() {
        override fun <R, E> accept(visitor: Visitor<R, E>, environment: E): Pair<R, E> = visitor.visitNumberLiteralExpression(this, environment)
    }

    data class Grouping(val expression: Expression) : Expression() {
        override fun <R, E> accept(visitor: Visitor<R, E>, environment: E): Pair<R, E> = visitor.visitGroupingExpression(this, environment)
    }

    data class Unary(
        val operator: String,
        val right: Expression
    ) : Expression() {
        override fun <R, E> accept(visitor: Visitor<R, E>, environment: E): Pair<R, E> = visitor.visitUnaryExpression(this, environment)
    }

    data class BooleanLiteral(val value: Boolean) : Expression() {
        override fun <R, E> accept(visitor: Visitor<R, E>, environment: E): Pair<R, E> = visitor.visitBooleanLiteral(this, environment)
    }

    data class NilLiteral(val value : String = "nil") : Expression() {
        override fun <R, E> accept(visitor: Visitor<R, E>, environment: E): Pair<R, E> = visitor.visitNilLiteral(this, environment)
    }

    data class Binary(
        val left: Expression,
        val operator: String,
        val right: Expression
    ) : Expression() {
        override fun <R, E> accept(visitor: Visitor<R, E>, environment: E): Pair<R, E> = visitor.visitBinaryExpression(this, environment)
    }

    data class StringLiteral(val value: TokenLike.StringToken) : Expression() {
        override fun <R, E> accept(visitor: Visitor<R, E>, environment: E): Pair<R, E> = visitor.visitStringLiteralExpression(this, environment)
    }

    data class Variable(val name: String) : Expression() {
        override fun <R, E> accept(visitor: Visitor<R, E>, environment: E): Pair<R, E> = visitor.visitVariableExpression(this, environment)
    }

    data class Assignment(val name: String, val value: Expression) : Expression() {
        override fun <R, E> accept(visitor: Visitor<R, E>, environment: E): Pair<R, E> = visitor.visitAssignmentExpression(this, environment)
    }
}

sealed class Statement {
    interface Visitor<R, E> {
        fun visitPrintStatement(statement: Print, environment: E): Pair<R, E>
        fun visitExpressionStatement(statement: ExpressionStatement, environment: E): Pair<R, E>
        fun visitVarStatement(statement: Var, environment: E): Pair<R, E>
    }

    // Special case for visitors that don't use the environment (like StatementPrinter)
    interface VisitorWithoutEnv<R> : Visitor<R, Unit> {
        override fun visitPrintStatement(statement: Print, environment: Unit): Pair<R, Unit> =
            Pair(visitPrintStatementWithoutEnv(statement), Unit)
        override fun visitExpressionStatement(statement: ExpressionStatement, environment: Unit): Pair<R, Unit> =
            Pair(visitExpressionStatementWithoutEnv(statement), Unit)
        override fun visitVarStatement(statement: Var, environment: Unit): Pair<R, Unit> =
            Pair(visitVarStatementWithoutEnv(statement), Unit)

        // Simplified methods without environment
        fun visitPrintStatementWithoutEnv(statement: Print): R
        fun visitExpressionStatementWithoutEnv(statement: ExpressionStatement): R
        fun visitVarStatementWithoutEnv(statement: Var): R
    }

    abstract fun <R, E> accept(visitor: Visitor<R, E>, environment: E): Pair<R, E>

    // Simplified accept method for visitors without environment
    fun <R> accept(visitor: VisitorWithoutEnv<R>): R = accept(visitor, Unit).first

    data class Print(val expression: Expression) : Statement() {
        override fun <R, E> accept(visitor: Visitor<R, E>, environment: E): Pair<R, E> = visitor.visitPrintStatement(this, environment)
    }

    data class ExpressionStatement(val expression: Expression) : Statement() {
        override fun <R, E> accept(visitor: Visitor<R, E>, environment: E): Pair<R, E> = visitor.visitExpressionStatement(this, environment)
    }

    data class Var(val name: String, val initializer: Expression?) : Statement() {
        override fun <R, E> accept(visitor: Visitor<R, E>, environment: E): Pair<R, E> = visitor.visitVarStatement(this, environment)
    }
}
