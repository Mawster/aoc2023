import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class Day11KtTest {
    @Test
    fun `calculateGalaxyPositionsWithExpansionFactor`() {
        val calculatedPoint = calculateGalaxyPositionsWithExpansionFactor(
            listOf(
                Point(2,2)
            ),
            listOf(0, 1),
            listOf(0,1),
            1000000
        )
        assertEquals(2000000, calculatedPoint.first().x)
        assertEquals(2000000, calculatedPoint.first().y)
    }

}
