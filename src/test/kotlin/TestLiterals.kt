import java.io.File
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertEquals

class TestLiterals {
    @Test
    fun testStringLiteral() {
        // Create a temporary file with a string literal
        val tempFile = File.createTempFile("test", ".lox")
        tempFile.writeText("\"Hello, World!\"")

        // Capture stdout
        val originalOut = System.out
        val outContent = ByteArrayOutputStream()
        System.setOut(PrintStream(outContent))

        try {
            // Run the evaluator
            main(arrayOf("evaluate", tempFile.absolutePath))

            // Check the output
            assertEquals("Hello, World!", outContent.toString().trim())
        } finally {
            // Restore stdout and delete the temp file
            System.setOut(originalOut)
            tempFile.delete()
        }
    }

    @Test
    fun testNumberLiteral() {
        // Create a temporary file with a number literal
        val tempFile = File.createTempFile("test", ".lox")
        tempFile.writeText("10.40")

        // Capture stdout
        val originalOut = System.out
        val outContent = ByteArrayOutputStream()
        System.setOut(PrintStream(outContent))

        try {
            // Run the evaluator
            main(arrayOf("evaluate", tempFile.absolutePath))

            // Check the output
            assertEquals("10.4", outContent.toString().trim())
        } finally {
            // Restore stdout and delete the temp file
            System.setOut(originalOut)
            tempFile.delete()
        }
    }
}