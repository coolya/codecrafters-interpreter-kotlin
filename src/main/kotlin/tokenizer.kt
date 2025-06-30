enum class TokenType {
    LEFT_PAREN, RIGHT_PAREN, EOF, LEFT_BRACE, RIGHT_BRACE, STAR, DOT, COMMA, PLUS, MINUS, SLASH, SEMICOLON,
    EQUAL, EQUAL_EQUAL, BANG, BANG_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,
    STRING, NUMBER, IDENTIFIER,
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR, PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,
}

val keywords = mapOf(
    "and" to TokenType.AND,
    "class" to TokenType.CLASS,
    "else" to TokenType.ELSE,
    "false" to TokenType.FALSE,
    "fun" to TokenType.FUN,
    "for" to TokenType.FOR,
    "if" to TokenType.IF,
    "nil" to TokenType.NIL,
    "or" to TokenType.OR,
    "print" to TokenType.PRINT,
    "return" to TokenType.RETURN,
    "super" to TokenType.SUPER,
    "this" to TokenType.THIS,
    "true" to TokenType.TRUE,
    "var" to TokenType.VAR,
    "while" to TokenType.WHILE,
)

sealed class TokenLike {
    data class SimpleToken(val type: TokenType, val lexeme: String) : TokenLike() {
        override fun toString(): String {
            return "${type.name} $lexeme null"
        }
    }

    data class StringToken(val type: TokenType, val value: String) : TokenLike() {
        override fun toString(): String {
            return "${type.name} \"$value\" $value"
        }
    }

    data class NumberToken(val type: TokenType, val lexeme: String, val value: Double) : TokenLike() {
        override fun toString(): String {
            return "${type.name} $lexeme $value"
        }
    }

    data class LexicalError(val line: Int, val message: String, val position: Int? = null) : TokenLike() {
        override fun toString(): String {
            return "[line ${line}] Error: ${message}"
        }
    }
}
fun advanceToLineEnd(iterator: CharacterIterator?): CharacterIterator? {
    var current = iterator
    while (current?.char != null && current.char != '\n') {
        current = current.next()
    }
    return current
}

fun advanceUntilToken(iterator: CharacterIterator?, token: Char): Pair<String?, CharacterIterator?> {
    var current = iterator
    var result = ""
    while (current?.char != null && current.char != '\n') {
        if (current.char == token) {
            return result to current.next()
        } else {
            result += current.char.toString()
            current = current.next()
        }
    }
    return null to current?.next()
}

fun advanceFor(iterator: CharacterIterator?, tokens: List<Char>): Pair<String, CharacterIterator?> {
    var current = iterator
    var result = ""
    while (current?.char != null && current.char in tokens) {
        result += current.char.toString()
        current = current.next()
    }
    return result to current
}

tailrec fun lexToken(iterator: CharacterIterator?, line: Int, tokenStream: List<TokenLike>): List<TokenLike> {
    return when (iterator?.char) {
        '(' -> lexToken(iterator.next(), line, tokenStream + TokenLike.SimpleToken(TokenType.LEFT_PAREN, "("))
        ')' -> lexToken(iterator.next(), line, tokenStream + TokenLike.SimpleToken(TokenType.RIGHT_PAREN, ")"))
        '{' -> lexToken(iterator.next(), line, tokenStream + TokenLike.SimpleToken(TokenType.LEFT_BRACE, "{"))
        '}' -> lexToken(iterator.next(), line, tokenStream + TokenLike.SimpleToken(TokenType.RIGHT_BRACE, "}"))
        '*' -> lexToken(iterator.next(), line, tokenStream + TokenLike.SimpleToken(TokenType.STAR, "*"))
        ',' -> lexToken(iterator.next(), line, tokenStream + TokenLike.SimpleToken(TokenType.COMMA, ","))
        '.' -> lexToken(iterator.next(), line, tokenStream + TokenLike.SimpleToken(TokenType.DOT, "."))
        '-' -> lexToken(iterator.next(), line, tokenStream + TokenLike.SimpleToken(TokenType.MINUS, "-"))
        '+' -> lexToken(iterator.next(), line, tokenStream + TokenLike.SimpleToken(TokenType.PLUS, "+"))
        ';' -> lexToken(iterator.next(), line, tokenStream + TokenLike.SimpleToken(TokenType.SEMICOLON, ";"))
        '=' -> {
            val next = iterator.next()
            if (next?.char == '=') {
                lexToken(next.next(), line, tokenStream + TokenLike.SimpleToken(TokenType.EQUAL_EQUAL, "=="))
            } else lexToken(next, line, tokenStream + TokenLike.SimpleToken(TokenType.EQUAL, "="))
        }

        '!' -> {
            val next = iterator.next()
            if (next?.char == '=') {
                lexToken(next.next(), line, tokenStream + TokenLike.SimpleToken(TokenType.BANG_EQUAL, "!="))
            } else lexToken(next, line, tokenStream + TokenLike.SimpleToken(TokenType.BANG, "!"))
        }

        '>' -> {
            val next = iterator.next()
            if (next?.char == '=') {
                lexToken(next.next(), line, tokenStream + TokenLike.SimpleToken(TokenType.GREATER_EQUAL, ">="))
            } else lexToken(next, line, tokenStream + TokenLike.SimpleToken(TokenType.GREATER, ">"))
        }

        '<' -> {
            val next = iterator.next()
            if (next?.char == '=') {
                lexToken(next.next(), line, tokenStream + TokenLike.SimpleToken(TokenType.LESS_EQUAL, "<="))
            } else lexToken(next, line, tokenStream + TokenLike.SimpleToken(TokenType.LESS, "<"))
        }

        '/' -> {
            val next = iterator.next()
            if (next?.char == '/') {
                lexToken(advanceToLineEnd(iterator), line, tokenStream)
            } else lexToken(next, line, tokenStream + TokenLike.SimpleToken(TokenType.SLASH, "/"))
        }

        '"' -> {
            val (literalValue, next) = advanceUntilToken(iterator.next(), '"')
            if (literalValue == null) {
                lexToken(next, line, tokenStream + TokenLike.LexicalError(line, "Unterminated string."))
            } else {
                lexToken(next, line, tokenStream + TokenLike.StringToken(TokenType.STRING, literalValue))
            }
        }

        in '0'..'9' -> {
            val (literalValue, next) = advanceFor(iterator, ('0'..'9') + '.')
            lexToken(
                next,
                line,
                tokenStream + TokenLike.NumberToken(TokenType.NUMBER, literalValue, literalValue.toDouble())
            )
        }

        in 'a'..'z', in 'A'..'Z', '_' -> {
            val (literalValue, next) = advanceFor(iterator, ('a'..'z') + ('A'..'Z') + ('0'..'9') + '_')
            if (keywords.containsKey(literalValue)) lexToken(
                next,
                line,
                tokenStream + TokenLike.SimpleToken(keywords[literalValue]!!, literalValue)
            )
            else lexToken(next, line, tokenStream + TokenLike.SimpleToken(TokenType.IDENTIFIER, literalValue))
        }

        '\t', ' ' -> lexToken(iterator.next(), line, tokenStream)
        '\n' -> lexToken(iterator.next(), line + 1, tokenStream)
        null -> tokenStream
        else -> lexToken(
            iterator.next(),
            line,
            tokenStream + TokenLike.LexicalError(line, "Unexpected character: ${iterator.char}")
        )
    }
}

class TokenIterator(private val tokens: List<TokenLike>, private val current : Int = 0) {
    init {
        require(current < tokens.size)
        require(current >= 0)
    }

    fun next(): TokenIterator? {
        return if (current + 1 < tokens.size) TokenIterator(tokens, current + 1) else null
    }

    fun previous(): TokenIterator? {
        return if (current > 0) TokenIterator(tokens, current - 1) else null
    }

    val token : TokenLike get() = tokens[current]
}