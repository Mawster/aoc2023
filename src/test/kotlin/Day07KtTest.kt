import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class Day07KtTest {
    @Test
    fun `calculateHandCardsValues`() {
        assertEquals(202020214, calculateHandCardsValue("2222A"))
        assertEquals(202020201, calculateHandCardsValue("2222J"))
        assertEquals(101010101, calculateHandCardsValue("JJJJJ"))
        assertEquals(101010102, calculateHandCardsValue("JJJJ2"))
    }

    @Test
    fun `determineHandTypeValueWithJokerCards`(){
        assertEquals(100000000000, determineHandTypeValueWithJokerCards("JJJJJ"))
        assertEquals(100000000000, determineHandTypeValueWithJokerCards("2JJJJ"))
        assertEquals(100000000000, determineHandTypeValueWithJokerCards("2222J"))
        assertEquals(90000000000, determineHandTypeValueWithJokerCards("3332J"))
        assertEquals(90000000000, determineHandTypeValueWithJokerCards("332JJ"))
        assertEquals(80000000000, determineHandTypeValueWithJokerCards("3322J"))
        assertEquals(50000000000, determineHandTypeValueWithJokerCards("3254J"))
        assertEquals(40000000000, determineHandTypeValueWithJokerCards("3254A"))

        assertEquals(80000000000, determineHandTypeValueWithJokerCards("Q2Q2Q"))

        assertEquals(80000000000, determineHandTypeValueWithJokerCards("T3T3J"))

        assertEquals(80000000000, determineHandTypeValueWithJokerCards("AABBJ"))

        assertEquals(80000000000, determineHandTypeValueWithJokerCards("AA222"))

        assertEquals(80000000000, determineHandTypeValueWithJokerCards("AAA22"))

        assertEquals(70000000000, determineHandTypeValueWithJokerCards("A9TJT"))
    }
}
