# Quadruplet Project Guide

## Build Commands
- **Compile**: `mvn compile`
- **Test all**: `mvn test`
- **Single test**: `mvn test -Dtest=com.cbrew.package.TestClass#testMethod`
- **Package**: `mvn package`
- **Clean build**: `mvn clean install`
- **Generate ANTLR4 code**: `mvn antlr4:antlr4`

## Code Style Guidelines
- **Formatting**: 4-space indentation, avoid long lines (80-120 char limit)
- **Naming**: camelCase for variables/methods, PascalCase for classes
- **Types**: Leverage Kotlin's type system; mark nullability explicitly
- **Imports**: Group by: 1) project (com.cbrew.*), 2) stdlib, 3) external
- **Error handling**: Use nullable types with safe calls (`?.`), elvis operator (`?:`)
- **Documentation**: KDoc style comments for public API methods

## Patterns
- Prefer immutable data when possible
- Use functional programming style with higher-order functions
- Build complex objects with builder pattern
- Use extension functions for clean, concise code
- Pattern match with `when` expressions