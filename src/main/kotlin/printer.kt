object Printer : Expression.VisitorWithoutEnv<String> {
    override fun visitBinaryExpressionWithoutEnv(expr: Expression.Binary): String =
        "(${expr.operator} ${expr.left.accept(this)} ${expr.right.accept(this)})"

    override fun visitBooleanLiteralWithoutEnv(literal: Expression.BooleanLiteral): String =
        "${literal.value}"

    override fun visitNilLiteralWithoutEnv(literal: Expression.NilLiteral): String =
        "nil"

    override fun visitNumberLiteralExpressionWithoutEnv(expression: Expression.NumberLiteral): String =
        "${expression.value.value}"

    override fun visitGroupingExpressionWithoutEnv(expression: Expression.Grouping): String =
        "(group ${expression.expression.accept(this)})"

    override fun visitUnaryExpressionWithoutEnv(expression: Expression.Unary): String =
        "(${expression.operator} ${expression.right.accept(this)})"

    override fun visitStringLiteralExpressionWithoutEnv(expression: Expression.StringLiteral): String =
        "${expression.value.value}"

    override fun visitVariableExpressionWithoutEnv(expression: Expression.Variable): String =
        "${expression.name}"

    override fun visitAssignmentExpressionWithoutEnv(expression: Expression.Assignment): String =
        "(= ${expression.name} ${expression.value.accept(this)})"
}

object StatementPrinter : Statement.VisitorWithoutEnv<String> {
    override fun visitPrintStatementWithoutEnv(statement: Statement.Print): String {
        val exprStr = statement.expression.accept(Printer)
        return "(print $exprStr)"
    }

    override fun visitExpressionStatementWithoutEnv(statement: Statement.ExpressionStatement): String {
        val exprStr = statement.expression.accept(Printer)
        return "(expr $exprStr)"
    }

    override fun visitVarStatementWithoutEnv(statement: Statement.Var): String {
        val initStr = if (statement.initializer != null) {
            statement.initializer.accept(Printer)
        } else {
            "nil"
        }
        return "(var ${statement.name} $initStr)"
    }

    override fun visitBlockStatementWithoutEnv(statement: Statement.Block): String {
        val statementsStr = statement.statements.joinToString(" ") { it.accept(this) }
        return "(block $statementsStr)"
    }
}
