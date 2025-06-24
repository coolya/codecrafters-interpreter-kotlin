object Printer : Expression.Visitor<String> {
    override fun visitBinaryExpression(expr: Expression.Binary): String =
        "(${expr.operator} ${expr.left.accept(this)} ${expr.right.accept(this)})"

    override fun visitNumberLiteralExpression(expression: Expression.NumberLiteral): String =
        "${expression.value.value}"


    override fun visitGroupingExpression(expression: Expression.Grouping): String {
        TODO("Not yet implemented")
    }

    override fun visitUnaryExpression(expression: Expression.Unary): String {
        TODO("Not yet implemented")
    }
}