import java.io.File
import kotlin.system.exitProcess




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

val SUPPORTED_COMMANDS = listOf("tokenize", "parse")

fun main(args: Array<String>) {

    if (args.size < 2) {
        System.err.println("Usage: ./your_program.sh tokenize <filename>")
        exitProcess(1)
    }

    val command = args[0]
    val filename = args[1]

    if (!SUPPORTED_COMMANDS.contains(command)) {
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

    if(command == "tokenize") {
        println(tokenSteam.filterNot { it is TokenLike.LexicalError }.joinToString("\n"))
        val errors = tokenSteam.filter { it is TokenLike.LexicalError }
        System.err.println(errors.joinToString("\n"))
        if (errors.isNotEmpty()) exitProcess(65)
        return
    }
    if(command == "parse") {
        val ast = expression(TokenIterator(tokenSteam))
        ast.first.accept(Printer).let { println(it) }
        return
    }
}
