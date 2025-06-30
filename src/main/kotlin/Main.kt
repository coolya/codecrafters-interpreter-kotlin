import java.io.File
import kotlin.system.exitProcess
import EvaluationResult
import StatementEvaluationResult
import StatementParseResult

/**
 * Represents the result of processing a program, which can be either a list of statements or a single expression
 */
sealed class ProgramProcessResult<T> {
    // Result for a single expression
    data class SingleExpression<T>(val result: T) : ProgramProcessResult<T>()

    // Result for a list of statements
    data class MultipleStatements<T>(val results: List<T>) : ProgramProcessResult<T>()
}

/**
 * Process a program, which can be either a list of statements or a single expression.
 * This function handles the common logic for both parsing and evaluation.
 *
 * @param tokens The token stream to process
 * @param processExpression A function that processes a single expression and returns a result of type R
 * @param processStatement A function that processes a single statement and returns a result of type R
 * @return A ProgramProcessResult containing either a single expression result or multiple statement results
 */
fun <R> processProgram(
    tokens: List<TokenLike>,
    processExpression: (Expression) -> R,
    processStatement: (Statement) -> R
): ProgramProcessResult<R> {
    // First try to parse as a program (list of statements)
    val statements = program(TokenIterator(tokens))

    // Check for syntax errors
    val syntaxError = statements.find { it is StatementParseResult.Error }

    // If there's a syntax error about missing semicolons, try parsing as a single expression
    if (syntaxError != null && (syntaxError as StatementParseResult.Error).message.contains("Expected ';'")) {
        // Parse as a single expression
        val parseResult = expression(TokenIterator(tokens))

        // Handle the result based on its type
        return when (parseResult) {
            is ParseResult.Error -> {
                // Log the error message and exit with error code if a syntax error occurred during parsing
                System.err.println("Error: ${parseResult.message}")
                exitProcess(65)
            }
            is ParseResult.Success -> {
                // Process the expression and return the result
                ProgramProcessResult.SingleExpression(processExpression(parseResult.expression))
            }
        }
    } else if (syntaxError != null) {
        // If there's a syntax error that's not about missing semicolons, report it
        val error = syntaxError as StatementParseResult.Error
        System.err.println("Error: ${error.message}")
        exitProcess(65)
    } else {
        // Process each statement and collect the results
        val results = statements.map { statement ->
            when (statement) {
                is StatementParseResult.Success -> {
                    // Process the statement and return the result
                    processStatement(statement.statement)
                }
                is StatementParseResult.Error -> {
                    // This should not happen as we've already checked for syntax errors
                    System.err.println("Error: ${statement.message}")
                    exitProcess(65)
                }
            }
        }
        return ProgramProcessResult.MultipleStatements(results)
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

val SUPPORTED_COMMANDS = listOf("tokenize", "parse", "evaluate", "run")

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
        val errors = tokenSteam.filter { it is TokenLike.LexicalError }
        if (errors.isNotEmpty()) {
            System.err.println(errors.joinToString("\n"))
            exitProcess(65)
        }

        // Create an initial environment
        var environment = Environment()

        // Process the program using our generic function
        val result = processProgram(
            tokens = tokenSteam,
            processExpression = { expr ->
                // Print the AST for the expression
                val str = expr.accept(Printer)
                println(str)
                Unit // Return Unit as we're just printing
            },
            processStatement = { stmt ->
                // Print the AST for the statement
                val str = stmt.accept(StatementPrinter)
                println(str)
                Unit // Return Unit as we're just printing
            }
        )

        return
    }

    if(command == "evaluate") {
        val errors = tokenSteam.filter { it is TokenLike.LexicalError }
        if (errors.isNotEmpty()) {
            System.err.println(errors.joinToString("\n"))
            exitProcess(65)
        }

        // Create an initial environment
        var environment = Environment()

        // Process the program using our generic function
        val result = processProgram(
            tokens = tokenSteam,
            processExpression = { expr ->
                // Evaluate the expression and handle the result
                val (evalResult, newEnv) = expr.accept(Evaluator, environment)
                // Update the environment for the next evaluation
                environment = newEnv
                when (evalResult) {
                    is EvaluationResult.Success -> {
                        // Print the successful result
                        println(evalResult.value)
                    }
                    is EvaluationResult.Error -> {
                        // Handle runtime errors by printing to stderr and exiting with code 70
                        System.err.println(evalResult.message)
                        exitProcess(70)
                    }
                }
                Unit // Return Unit as we're just printing
            },
            processStatement = { stmt ->
                // Evaluate the statement
                val (evalResult, newEnv) = stmt.accept(StatementEvaluator, environment)
                // Update the environment for the next evaluation
                environment = newEnv
                when (evalResult) {
                    is StatementEvaluationResult.Success -> {
                        // For expression statements, we don't print anything
                        // For print statements, the evaluator already prints the result
                    }
                    is StatementEvaluationResult.Error -> {
                        // Handle runtime errors by printing to stderr and exiting with code 70
                        System.err.println(evalResult.message)
                        exitProcess(70)
                    }
                }
                Unit // Return Unit as we're just evaluating
            }
        )

        return
    }

    if(command == "run") {
        val errors = tokenSteam.filter { it is TokenLike.LexicalError }
        if (errors.isNotEmpty()) {
            System.err.println(errors.joinToString("\n"))
            exitProcess(65)
        }

        // For the "run" command, we don't allow single expressions without semicolons
        // Parse the program (a list of statements)
        val statements = program(TokenIterator(tokenSteam))

        // Check for syntax errors
        val syntaxError = statements.find { it is StatementParseResult.Error }
        if (syntaxError != null) {
            val error = syntaxError as StatementParseResult.Error
            System.err.println("Error: ${error.message}")
            exitProcess(65)
        }

        // Create an initial environment
        var environment = Environment()

        // Process each statement
        for (statement in statements) {
            when (statement) {
                is StatementParseResult.Success -> {
                    // Evaluate the statement
                    val (result, newEnv) = statement.statement.accept(StatementEvaluator, environment)
                    // Update the environment for the next evaluation
                    environment = newEnv
                    when (result) {
                        is StatementEvaluationResult.Success -> {
                            // Continue to the next statement
                        }
                        is StatementEvaluationResult.Error -> {
                            // Handle runtime errors by printing to stderr and exiting with code 70
                            System.err.println(result.message)
                            exitProcess(70)
                        }
                    }
                }
                is StatementParseResult.Error -> {
                    // This should not happen as we've already checked for syntax errors
                    System.err.println("Error: ${statement.message}")
                    exitProcess(65)
                }
            }
        }

        return
    }
}
