import java.io.File
import kotlin.system.exitProcess

enum class TokenType {
    LEFT_PAREN, RIGHT_PAREN, EOF, LEFT_BRACE, RIGHT_BRACE, STAR, DOT, COMMA, PLUS, MINUS, SLASH, SEMICOLON, EQUAL, EQUAL_EQUAL, BANG, BANG_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL, STRING, NUMBER, IDENTIFIER, AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR, PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,
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


fun CharArray.charIterator(): CharacterIterator {
    return CharacterIterator(this);
}

fun CharacterIterator.nextTokenMatches(
    possibleNext: Char, matchingType: TokenType, noneMatching: TokenType
): Pair<CharacterIterator?, TokenLike> {
    val next = this.next()
    if (next?.char == possibleNext) {
        val previous = this.char
        return next.next() to TokenLike.SimpleToken(matchingType, previous.toString() + possibleNext.toString())
    }
    return this.next() to TokenLike.SimpleToken(noneMatching, this.char.toString())
}

fun CharacterIterator.nextTokenConsumesLine(
    possibleNext: Char, noneMatching: TokenType
): Pair<CharacterIterator?, TokenLike?> {
    var current = this.next()
    if (current?.char == possibleNext) {
        while (current?.char != null && current.char != '\n') {
            current = current.next()
        }
        return current to null
    }
    return this.next() to TokenLike.SimpleToken(noneMatching, this.char.toString())
}

fun main(args: Array<String>) {

    if (args.size < 2) {
        System.err.println("Usage: ./your_program.sh tokenize <filename>")
        exitProcess(1)
    }

    val command = args[0]
    val filename = args[1]

    if (command != "tokenize") {
        System.err.println("Unknown command: ${command}")
        exitProcess(1)
    }

    val fileContents = File(filename).readText()

    // Uncomment this block to pass the first stage
    var tokenSteam = if (fileContents.isNotEmpty()) {
        lexToken(fileContents.toCharArray().charIterator(), 1, listOf()) + TokenLike.SimpleToken(TokenType.EOF, "")
    } else {
        listOf(TokenLike.SimpleToken(TokenType.EOF, ""))
    }
    println(tokenSteam.filterNot { it is TokenLike.LexicalError }.joinToString("\n"))
    val errors = tokenSteam.filter { it is TokenLike.LexicalError }
    System.err.println(errors.joinToString("\n"))
    if (errors.isNotEmpty()) exitProcess(65)
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
            tokenStream + TokenLike.LexicalError(line, "Unexpected character: ${iterator?.char}")
        )
    }
}
/*
    fun lex(fileContents: String, tokenSteam: MutableList<TokenLike>) {
        val chars = fileContents.toCharArray()
        var iterator: CharacterIterator? = chars.charIterator()
        var line = 1
        while (iterator?.char != null) {
            when (iterator.char) {
                '(' -> {
                    tokenSteam.add(TokenLike.SimpleToken(TokenType.LEFT_PAREN, "("))
                    iterator = iterator.next()
                }

                ')' -> {
                    tokenSteam.add(TokenLike.SimpleToken(TokenType.RIGHT_PAREN, ")"))
                    iterator = iterator.next()
                }

                '{' -> {
                    tokenSteam.add(TokenLike.SimpleToken(TokenType.LEFT_BRACE, "{"))
                    iterator = iterator.next()
                }

                '}' -> {
                    tokenSteam.add(TokenLike.SimpleToken(TokenType.RIGHT_BRACE, "}"))
                    iterator = iterator.next()
                }

                '*' -> {
                    tokenSteam.add(TokenLike.SimpleToken(TokenType.STAR, "*"))
                    iterator = iterator.next()
                }

                ',' -> {
                    tokenSteam.add(TokenLike.SimpleToken(TokenType.COMMA, ","))
                    iterator = iterator.next()
                }

                '.' -> {
                    tokenSteam.add(TokenLike.SimpleToken(TokenType.DOT, "."))
                    iterator = iterator.next()
                }

                '-' -> {
                    tokenSteam.add(TokenLike.SimpleToken(TokenType.MINUS, "-"))
                    iterator = iterator.next()
                }

                '+' -> {
                    tokenSteam.add(TokenLike.SimpleToken(TokenType.PLUS, "+"))
                    iterator = iterator.next()
                }

                ';' -> {
                    tokenSteam.add(TokenLike.SimpleToken(TokenType.SEMICOLON, ";"))
                    iterator = iterator.next()
                }

                '=' -> {
                    val (next, token) = iterator.nextTokenMatches('=', TokenType.EQUAL_EQUAL, TokenType.EQUAL)
                    tokenSteam.add(token)
                    iterator = next
                }

                '!' -> {
                    val (next, token) = iterator.nextTokenMatches('=', TokenType.BANG_EQUAL, TokenType.BANG)
                    tokenSteam.add(token)
                    iterator = next
                }

                '>' -> {
                    val (next, token) = iterator.nextTokenMatches('=', TokenType.GREATER_EQUAL, TokenType.GREATER)
                    tokenSteam.add(token)
                    iterator = next
                }

                '<' -> {
                    val (next, token) = iterator.nextTokenMatches('=', TokenType.LESS_EQUAL, TokenType.LESS)
                    tokenSteam.add(token)
                    iterator = next
                }

                '/' -> {
                    val (next, token) = iterator.nextTokenConsumesLine('/', TokenType.SLASH)
                    if (token != null) tokenSteam.add(token)
                    iterator = next
                }

                '"' -> {
                    var literalValue = ""
                    var result: TokenLike = TokenLike.LexicalError(line, "Unterminated string.")
                    iterator = iterator.next()
                    while (iterator?.char != null) {
                        if (iterator.char == '"') {
                            result = TokenLike.StringToken(TokenType.STRING, literalValue)
                            iterator = iterator.next()
                            break
                        } else if (iterator.char == '\n') {
                            line++
                            iterator = iterator.next()
                            break
                        } else literalValue += iterator.char.toString()
                        iterator = iterator.next()
                    }
                    tokenSteam.add(result)
                }

                in '0'..'9' -> {
                    var literalValue = iterator.char.toString()
                    iterator = iterator.next()
                    while (iterator?.char != null) {
                        val char = iterator.char
                        if (char in '0'..'9' || iterator.char == '.') {
                            literalValue += char.toString()
                            iterator = iterator.next()
                        } else break
                    }
                    tokenSteam.add(TokenLike.NumberToken(TokenType.NUMBER, literalValue, literalValue.toDouble()))
                }

                in 'a'..'z', in 'A'..'Z', '_' -> {
                    var identifier = iterator.char.toString()
                    iterator = iterator.next()
                    while (iterator?.char != null) {
                        val char = iterator.char
                        if (char in 'a'..'z' || char in 'A'..'Z' || char in '0'..'9' || char == '_') {
                            identifier += char.toString()
                            iterator = iterator.next()
                        } else break
                    }
                    if (keywords.containsKey(identifier)) tokenSteam.add(
                        TokenLike.SimpleToken(
                            keywords[identifier]!!, identifier
                        )
                    )
                    else tokenSteam.add(TokenLike.SimpleToken(TokenType.IDENTIFIER, identifier))
                }

                '\t', ' ' -> {
                    iterator = iterator.next()
                    continue
                }

                '\n' -> {
                    line++
                    iterator = iterator.next()
                    continue
                }

                else -> {
                    tokenSteam.add(TokenLike.LexicalError(line, "Unexpected character: ${iterator.char}"))
                    iterator = iterator.next()
                }
            }
        }
    }
*/