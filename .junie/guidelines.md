# Project Guidelines

Use maven for any compilation-related tasks. 

## Kotlin Programming Style and Best Practices

### Code Organization

1. **Functional Programming Approach**
    - Use immutable data structures when possible (`val` over `var`)
    - Leverage tail recursion with the `tailrec` modifier for recursive functions, as seen in the `lexToken` function
    - Use pure functions that avoid side effects
    - Avoid explicit casts and rely on the compilers type inference to make code more maintainable.

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

4. **Code Comments**
   - Avoid comments that explain what simple code does or that state the obvious like:
   ```
   // check if a is greater 5
   if( a > 5)
   ```
   - Use code comments to state why code exists.
   - Use code comments to explain none obvious aspects of the code, especially none-functional ones.
   - Use code comments to explain edge cases or performance optimizations. 

### Design Patterns

1. **Visitor Pattern**
    - As seen in `ast.kt`, use the Visitor pattern for operations on AST nodes
    - Implement the `Visitor` interface for each operation (like `Printer`)

2. **Immutable Data Flow**
    - Use the iterator pattern with immutable state (as in `TokenIterator`)
    - Return new instances rather than mutating existing ones

## Testing

- Do not generate any test cases or test files yourself. 
- Rely on codecrafters to verify the work.

## Development Workflow
- Use the maven target compile for building the project.
- Always build the project before a task is considered completed
- Always fix compilation errors
- After building the project verify the results using the globally installed `codecrafters test` shell command. 
- Separate behavioral and structural changes. 
- If structural and behavioral changes are required, first change the structure. 