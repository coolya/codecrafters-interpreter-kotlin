object Printer : Expression.Visitor<String> {
    override fun visitBinaryExpression(expr: Expression.Binary): String =
        "(${expr.operator} ${expr.left.accept(this)} ${expr.right.accept(this)})"

    override fun visitBooleanLiteral(literal: Expression.BooleanLiteral): String =
        "${literal.value}"

    override fun visitNilLiteral(literal: Expression.NilLiteral): String =
        "nil"

    override fun visitNumberLiteralExpression(expression: Expression.NumberLiteral): String =
        "${expression.value.value}"

    override fun visitGroupingExpression(expression: Expression.Grouping): String =
        "(group ${expression.expression.accept(this)})"

    override fun visitUnaryExpression(expression: Expression.Unary): String =
        "(${expression.operator} ${expression.right.accept(this)})"

    override fun visitStringLiteralExpression(expression: Expression.StringLiteral): String =
        "${expression.value.value}"
}
