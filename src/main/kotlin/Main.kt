import java.io.File
import kotlin.system.exitProcess


enum class TokenType {
    LEFT_PAREN, RIGHT_PAREN, EOF, LEFT_BRACE, RIGHT_BRACE
}

data class Token(val type: TokenType, val lexeme: String) {
    override fun toString(): String {
        return "${type.name} $lexeme null"
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
    val tokenSteam = mutableListOf<Token>()
    if (fileContents.isNotEmpty()) {
         val chars = fileContents.toCharArray()
         for (char in chars) {
             when (char) {
                 '(' -> tokenSteam.add(Token(TokenType.LEFT_PAREN, "("))
                 ')' -> tokenSteam.add(Token(TokenType.RIGHT_PAREN, ")"))
                 '{' -> tokenSteam.add(Token(TokenType.LEFT_BRACE, "{"))
                 '}' -> tokenSteam.add(Token(TokenType.RIGHT_BRACE, "}"))
                 else -> println("UNKNOWN_CHAR")
             }
         }
     }
    tokenSteam.add(Token(TokenType.EOF, ""))
    println(tokenSteam.joinToString("\n"))
}
