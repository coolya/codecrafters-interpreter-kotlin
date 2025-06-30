import java.io.File
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertEquals

class TestUnaryOperators {
    @Test
    fun testNegationOperator() {
        // Create a temporary file with a negation expression
        val tempFile = File.createTempFile("test", ".lox")
        tempFile.writeText("-42")

        // Capture stdout
        val originalOut = System.out
        val outContent = ByteArrayOutputStream()
        System.setOut(PrintStream(outContent))

        try {
            // Run the evaluator
            main(arrayOf("evaluate", tempFile.absolutePath))

            // Check the output
            assertEquals("-42", outContent.toString().trim())
        } finally {
            // Restore stdout and delete the temp file
            System.setOut(originalOut)
            tempFile.delete()
        }
    }

    @Test
    fun testNotOperatorOnFalse() {
        // Create a temporary file with a not expression on false
        val tempFile = File.createTempFile("test", ".lox")
        tempFile.writeText("!false")

        // Capture stdout
        val originalOut = System.out
        val outContent = ByteArrayOutputStream()
        System.setOut(PrintStream(outContent))

        try {
            // Run the evaluator
            main(arrayOf("evaluate", tempFile.absolutePath))

            // Check the output
            assertEquals("true", outContent.toString().trim())
        } finally {
            // Restore stdout and delete the temp file
            System.setOut(originalOut)
            tempFile.delete()
        }
    }

    @Test
    fun testNotOperatorOnTrue() {
        // Create a temporary file with a not expression on true
        val tempFile = File.createTempFile("test", ".lox")
        tempFile.writeText("!true")

        // Capture stdout
        val originalOut = System.out
        val outContent = ByteArrayOutputStream()
        System.setOut(PrintStream(outContent))

        try {
            // Run the evaluator
            main(arrayOf("evaluate", tempFile.absolutePath))

            // Check the output
            assertEquals("false", outContent.toString().trim())
        } finally {
            // Restore stdout and delete the temp file
            System.setOut(originalOut)
            tempFile.delete()
        }
    }

    @Test
    fun testNotOperatorOnNil() {
        // Create a temporary file with a not expression on nil
        val tempFile = File.createTempFile("test", ".lox")
        tempFile.writeText("!nil")

        // Capture stdout
        val originalOut = System.out
        val outContent = ByteArrayOutputStream()
        System.setOut(PrintStream(outContent))

        try {
            // Run the evaluator
            main(arrayOf("evaluate", tempFile.absolutePath))

            // Check the output
            assertEquals("true", outContent.toString().trim())
        } finally {
            // Restore stdout and delete the temp file
            System.setOut(originalOut)
            tempFile.delete()
        }
    }

    @Test
    fun testNotOperatorOnNumber() {
        // Create a temporary file with a not expression on a number
        val tempFile = File.createTempFile("test", ".lox")
        tempFile.writeText("!5")

        // Capture stdout
        val originalOut = System.out
        val outContent = ByteArrayOutputStream()
        System.setOut(PrintStream(outContent))

        try {
            // Run the evaluator
            main(arrayOf("evaluate", tempFile.absolutePath))

            // Check the output
            assertEquals("false", outContent.toString().trim())
        } finally {
            // Restore stdout and delete the temp file
            System.setOut(originalOut)
            tempFile.delete()
        }
    }

    @Test
    fun testNotOperatorOnString() {
        // Create a temporary file with a not expression on a string
        val tempFile = File.createTempFile("test", ".lox")
        tempFile.writeText("!\"hello\"")

        // Capture stdout
        val originalOut = System.out
        val outContent = ByteArrayOutputStream()
        System.setOut(PrintStream(outContent))

        try {
            // Run the evaluator
            main(arrayOf("evaluate", tempFile.absolutePath))

            // Check the output
            assertEquals("false", outContent.toString().trim())
        } finally {
            // Restore stdout and delete the temp file
            System.setOut(originalOut)
            tempFile.delete()
        }
    }
}