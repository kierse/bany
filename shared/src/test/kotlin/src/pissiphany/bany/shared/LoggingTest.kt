package src.pissiphany.bany.shared

import com.pissiphany.bany.shared.logger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.io.path.createTempFile

class LoggingTest {
    private val logger by logger()

    @Test
    fun `logger - verify caller namespace`() {
        // redirect logs to local file
        val tempFile = createTempFile()
            .toFile()
            .apply { deleteOnExit() }
        System.setProperty("org.slf4j.simpleLogger.logFile", tempFile.absolutePath)

        logger.info { "test!" }
        val lines = tempFile.readLines()

        assertTrue(logger.isDebugEnabled)
        assertEquals(1, lines.size)
        assertEquals("[${Thread.currentThread().name}] INFO ${LoggingTest::class.java.canonicalName} - test!", lines.first())
    }
}