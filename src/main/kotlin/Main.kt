import java.io.Console
import java.io.File
import kotlin.system.exitProcess


enum class TokenType {
    LEFT_PAREN, RIGHT_PAREN,
    EOF,
    LEFT_BRACE, RIGHT_BRACE,
    STAR, DOT, COMMA, PLUS, MINUS, SLASH, SEMICOLON,
    EQUAL, EQUAL_EQUAL, BANG, BANG_EQUAL,
    GREATER, GREATER_EQUAL, LESS, LESS_EQUAL

}

sealed class TokenLike {
    data class Token(val type: TokenType, val lexeme: String) : TokenLike() {
        override fun toString(): String {
            return "${type.name} $lexeme null"
        }
    }

    data class LexicalError(val line: Int, val message: String, val position: Int? = null) : TokenLike() {
        override fun toString(): String {
            return "[line ${line}] Error: ${message}"
        }
    }
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
        val chars = fileContents.toCharArray()
        chars.forEachIndexed { i, char ->
            when (char) {
                '(' -> tokenSteam.add(TokenLike.Token(TokenType.LEFT_PAREN, "("))
                ')' -> tokenSteam.add(TokenLike.Token(TokenType.RIGHT_PAREN, ")"))
                '{' -> tokenSteam.add(TokenLike.Token(TokenType.LEFT_BRACE, "{"))
                '}' -> tokenSteam.add(TokenLike.Token(TokenType.RIGHT_BRACE, "}"))
                '*' -> tokenSteam.add(TokenLike.Token(TokenType.STAR, "*"))
                ',' -> tokenSteam.add(TokenLike.Token(TokenType.COMMA, ","))
                '.' -> tokenSteam.add(TokenLike.Token(TokenType.DOT, "."))
                '-' -> tokenSteam.add(TokenLike.Token(TokenType.MINUS, "-"))
                '+' -> tokenSteam.add(TokenLike.Token(TokenType.PLUS, "+"))
                ';' -> tokenSteam.add(TokenLike.Token(TokenType.SEMICOLON, ";"))
                '=' -> tokenSteam.add(chars.nextTokenMatches(i, '=', TokenType.EQUAL_EQUAL, TokenType.EQUAL))
                '!' -> tokenSteam.add(chars.nextTokenMatches(i, '=', TokenType.BANG_EQUAL, TokenType.BANG))
                '>' -> tokenSteam.add(chars.nextTokenMatches(i, '=', TokenType.GREATER_EQUAL, TokenType.GREATER))
                '<' -> tokenSteam.add(chars.nextTokenMatches(i, '=', TokenType.LESS_EQUAL, TokenType.LESS))
                else -> tokenSteam.add(TokenLike.LexicalError(1, "Unexpected character: $char"))
            }
        }
    }
    tokenSteam.add(TokenLike.Token(TokenType.EOF, ""))
    println(tokenSteam.filter { it is TokenLike.Token }.joinToString("\n"))
    val errors = tokenSteam.filter { it is TokenLike.LexicalError }
    System.err.println(errors.joinToString("\n"))
    if (errors.isNotEmpty()) exitProcess(65)
}

fun CharArray.nextTokenMatches(
    currentIndex: Int, nextToken: Char, matchTokenType: TokenType, notMatchTokenType: TokenType
): TokenLike.Token {
    // End of stream is reached
    if (currentIndex + 1 == this.size) {
        return TokenLike.Token(notMatchTokenType, this[currentIndex].toString())
    } else {
        val token = this[currentIndex + 1]
        return if (token ==  nextToken) {
            TokenLike.Token(matchTokenType, listOf(this[currentIndex], token).joinToString(""))
        } else {
            TokenLike.Token(notMatchTokenType, token.toString())
        }
    }
}