import java.io.File
import kotlin.system.exitProcess

enum class TokenType {
    LEFT_PAREN, RIGHT_PAREN,
    EOF,
    LEFT_BRACE, RIGHT_BRACE,
    STAR, DOT, COMMA, PLUS, MINUS, SLASH, SLASH_SLASH, SEMICOLON,
    EQUAL, EQUAL_EQUAL, BANG, BANG_EQUAL,
    GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,
    STRING, NUMBER

}

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


class CharIterator(private val chars: CharArray) {
    private var current = -1
    fun hasNext(): Boolean {
        return current + 1 < chars.size
    }

    fun next(): Char? {
        if (hasNext()) {
            current++;
            return current()
        }
        return null;
    }

    fun peek(): Char? {
        if (hasNext()) {
            return chars[current + 1]
        }
        return null;
    }

    fun current(): Char? {
        if (current < 0) return null
        return chars[current]
    }
}

fun CharArray.charIterator(): CharIterator {
    return CharIterator(this);
}

fun CharIterator.nextTokenMatches(possibleNext: Char, matchingType: TokenType, noneMatching: TokenType): TokenLike {
    if (this.peek() == possibleNext) {
        val previous = this.current()
        this.next()
        return TokenLike.SimpleToken(matchingType, previous.toString() + possibleNext.toString())
    }
    return TokenLike.SimpleToken(noneMatching, this.current().toString())
}

fun CharIterator.nextTokenConsumesLine(possibleNext: Char, noneMatching: TokenType): TokenLike? {
    if (this.peek() == possibleNext) {
        val previous = this.current()
        var lexeme = previous.toString() + next().toString()
        while (this.next() != '\n' && this.hasNext()) {
            lexeme += this.current().toString()
        }
        return null
    }
    return TokenLike.SimpleToken(noneMatching, this.current().toString())
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
    val tokenSteam = mutableListOf<TokenLike>()
    if (fileContents.isNotEmpty()) {
        lex(fileContents, tokenSteam)
    }
    tokenSteam.add(TokenLike.SimpleToken(TokenType.EOF, ""))
    println(tokenSteam.filterNot { it is TokenLike.LexicalError }.joinToString("\n"))
    val errors = tokenSteam.filter { it is TokenLike.LexicalError }
    System.err.println(errors.joinToString("\n"))
    if (errors.isNotEmpty()) exitProcess(65)
}

fun lex(fileContents: String, tokenSteam: MutableList<TokenLike>) {
    val chars = fileContents.toCharArray()
    val iterator = chars.charIterator()
    var line = 1
    while (iterator.hasNext()) {
        val char = iterator.next()
        when (char) {
            '(' -> tokenSteam.add(TokenLike.SimpleToken(TokenType.LEFT_PAREN, "("))
            ')' -> tokenSteam.add(TokenLike.SimpleToken(TokenType.RIGHT_PAREN, ")"))
            '{' -> tokenSteam.add(TokenLike.SimpleToken(TokenType.LEFT_BRACE, "{"))
            '}' -> tokenSteam.add(TokenLike.SimpleToken(TokenType.RIGHT_BRACE, "}"))
            '*' -> tokenSteam.add(TokenLike.SimpleToken(TokenType.STAR, "*"))
            ',' -> tokenSteam.add(TokenLike.SimpleToken(TokenType.COMMA, ","))
            '.' -> tokenSteam.add(TokenLike.SimpleToken(TokenType.DOT, "."))
            '-' -> tokenSteam.add(TokenLike.SimpleToken(TokenType.MINUS, "-"))
            '+' -> tokenSteam.add(TokenLike.SimpleToken(TokenType.PLUS, "+"))
            ';' -> tokenSteam.add(TokenLike.SimpleToken(TokenType.SEMICOLON, ";"))
            '=' -> tokenSteam.add(iterator.nextTokenMatches('=', TokenType.EQUAL_EQUAL, TokenType.EQUAL))
            '!' -> tokenSteam.add(iterator.nextTokenMatches('=', TokenType.BANG_EQUAL, TokenType.BANG))
            '>' -> tokenSteam.add(iterator.nextTokenMatches('=', TokenType.GREATER_EQUAL, TokenType.GREATER))
            '<' -> tokenSteam.add(iterator.nextTokenMatches('=', TokenType.LESS_EQUAL, TokenType.LESS))
            '/' -> {
                val nextToken = iterator.nextTokenConsumesLine('/', TokenType.SLASH)
                if (nextToken != null) tokenSteam.add(nextToken) else line++
            }

            '"' -> {
                var literalValue = ""
                var result: TokenLike = TokenLike.LexicalError(line, "Unterminated string.")

                while (iterator.hasNext()) {
                    if (iterator.next() == '"') {
                        result = TokenLike.StringToken(TokenType.STRING, literalValue)
                        break
                    } else if (iterator.current() == '\n') {
                        line++
                        break
                    } else literalValue += iterator.current().toString()
                }
                tokenSteam.add(result)
            }

            in '0'..'9' -> {
                var literalValue = char.toString()

                while (iterator.hasNext()) {
                    if (iterator.peek() in '0'..'9' || iterator.peek() == '.') {
                        literalValue += iterator.next().toString()
                    } else break
                }
                tokenSteam.add(TokenLike.NumberToken(TokenType.NUMBER, literalValue, literalValue.toDouble()))
            }

            '\t', ' ' -> continue
            '\n' -> line++
            else -> tokenSteam.add(TokenLike.LexicalError(line, "Unexpected character: $char"))
        }
    }
}

