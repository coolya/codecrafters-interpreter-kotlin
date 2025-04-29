import java.io.File
import kotlin.system.exitProcess


enum class TokenType {
    LEFT_PAREN, RIGHT_PAREN
}

data class Token(val type: TokenType, val lexeme: String) {
    override fun toString(): String {
        return "${type.name} $lexeme"
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
     if (fileContents.isNotEmpty()) {
         throw NotImplementedError("Scanner not implemented")
     } else {
         val tokenSteam = mutableListOf<Token>()
         val chars = fileContents.toCharArray()
         for (char in chars) {
             when (char) {
                 '(' -> tokenSteam.add(Token(TokenType.LEFT_PAREN, "("))
                 ')' -> tokenSteam.add(Token(TokenType.RIGHT_PAREN, ")"))
                 else -> println("UNKNOWN_CHAR")
             }
         }
         println(tokenSteam.joinToString(" numm \n"))
         println("EOF  null") // Placeholder, remove this line when implementing the scanner
     }
}
