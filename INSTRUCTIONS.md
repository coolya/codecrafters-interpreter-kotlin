# Lox Interpreter Development Instructions

## Project Overview
This repository contains a Kotlin implementation of the Lox interpreter from the book "Crafting Interpreters" by Robert Nystrom. The project follows the challenge structure from CodeCrafters.io and is organized to implement the interpreter in progressive stages.

## Repository Structure
- `src/main/kotlin/` - Contains the Kotlin source files:
  - `Main.kt` - Entry point for the program
  - `ast.kt` - Abstract Syntax Tree definitions
  - `chars.kt` - Character handling utilities
  - `parser.kt` - Parser implementation
  - `printer.kt` - AST printer
  - `tokenizer.kt` - Lexical analysis/tokenization
- `your_program.sh` - Shell script to run the program
- `README.md` - Project information and stage instructions

## Kotlin Setup and Configuration

### Environment Requirements
- Kotlin version 2.0 or higher is required
- JDK 17 is recommended for compatibility
- Use IntelliJ IDEA for the best Kotlin development experience

### Building and Running
1. Ensure Kotlin is properly installed: `kotlin -version`
2. Use the provided shell script: `./your_program.sh tokenize <filename>`
3. For local development, you can also use `kotlinc src/main/kotlin/*.kt -include-runtime -d interpreter.jar` followed by `java -jar interpreter.jar tokenize <filename>`

## Development Workflow
1. Read the README.md to understand the current stage requirements
2. Implement the required functionality in the appropriate Kotlin files
3. Run tests using `./your_program.sh`
4. Commit changes with descriptive messages
5. Push to origin master to submit your solution

## Kotlin Programming Style and Best Practices

### Code Organization

1. **Functional Programming Approach**
   - Use immutable data structures when possible (`val` over `var`)
   - Leverage tail recursion with the `tailrec` modifier for recursive functions, as seen in the `lexToken` function
   - Use pure functions that avoid side effects

2. **Sealed Classes and Pattern Matching**
   - Use sealed classes for representing restricted class hierarchies as seen in `Expression` and `TokenLike`
   - Utilize Kotlin's `when` expressions for exhaustive pattern matching

3. **Extension Functions**
   - Use extension functions to add functionality to existing classes without inheritance
   - Example: `fun CharArray.charIterator()` adds iterator functionality to char arrays

4. **Null Safety**
   - Leverage Kotlin's null safety features with nullable types (`?`) and safe calls (`?.`)
   - Use the Elvis operator (`?:`) for providing default values

### Style Conventions

1. **Naming Conventions**
   - Use camelCase for functions, properties, and local variables
   - Use PascalCase for classes and interfaces
   - Use UPPER_SNAKE_CASE for constants and enum entries

2. **Code Structure**
   - Keep functions small and focused on a single responsibility
   - Use data classes for simple value objects
   - Prefer immutability: use `val` instead of `var` when possible

3. **Error Handling**
   - Use result-returning functions (like `Pair<Result, NextState>`) rather than exceptions for expected error conditions
   - Reserve exceptions for exceptional conditions

### Design Patterns

1. **Visitor Pattern**
   - As seen in `ast.kt`, use the Visitor pattern for operations on AST nodes
   - Implement the `Visitor` interface for each operation (like `Printer`)

2. **Immutable Data Flow**
   - Use the iterator pattern with immutable state (as in `TokenIterator`) 
   - Return new instances rather than mutating existing ones

## Implementation Progress

The implementation follows the chapters in "Crafting Interpreters":

1. **Scanning/Tokenization**: (Completed)
   - Implemented in `tokenizer.kt` with token types defined in `TokenType` enum
   - Token stream generation through `lexToken` function

2. **Parsing**: (In Progress)
   - AST structure defined in `ast.kt`
   - Parser implementation in `parser.kt`
   - Currently implementing the `primary` function for handling primary expressions

## Testing and Debugging

### Running Tests

1. **Basic Testing**
   ```sh
   ./your_program.sh tokenize <filename>
   ```

2. **Creating Test Files**
   - Create small Lox program files in a `test/` directory to validate different language features
   - Use a systematic approach: test each feature in isolation before combining

3. **Error Testing**
   - Test error handling by creating files with deliberate syntax errors
   - Verify that proper error messages are displayed on stderr
   - Error codes: The program should exit with code 65 for lexical errors

### Debugging Techniques

1. **Tracing Execution**
   - Add debug print statements using `println` for troubleshooting
   - For TokenIterator debugging, add a `toString()` method that shows the current token and position

2. **AST Visualization**
   - Use the `printer.kt` implementation to visualize the structure of parsed expressions
   - Implement missing visitor methods to ensure complete visualization

3. **Step-by-Step Execution**
   - Use IntelliJ's debugger to step through the parsing process
   - Set breakpoints at key decision points in recursive descent functions

4. **Testing Parser Components**
   - Test parser components individually before integrating them
   - For the `primary` function, test with simple literals first, then parenthesized expressions

## References and Resources

### Essential References
1. [Crafting Interpreters](https://craftinginterpreters.com/) by Robert Nystrom
2. [Lox Language Specification](https://craftinginterpreters.com/the-lox-language.html)

### Kotlin Resources
1. [Kotlin Official Documentation](https://kotlinlang.org/docs/home.html)
2. [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
3. [Kotlin Standard Library API](https://kotlinlang.org/api/latest/jvm/stdlib/)

### Interpreter Design Resources
1. [Recursive Descent Parsing](https://craftinginterpreters.com/parsing-expressions.html)
2. [Visitor Pattern in Kotlin](https://kotlinlang.org/docs/sealed-classes.html#sealed-classes-and-when-expression)
3. [Abstract Syntax Trees Visualization](https://astexplorer.net/)

### Functional Programming in Kotlin
1. [Tail Recursion in Kotlin](https://kotlinlang.org/docs/functions.html#tail-recursive-functions)
2. [Immutable Data Structures](https://kotlinlang.org/docs/collections-overview.html#collection-types)
