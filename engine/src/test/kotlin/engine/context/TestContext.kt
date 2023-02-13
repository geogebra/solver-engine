package engine.context

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestContext {

    private class TestResource(override val resourceData: ResourceData) : Resource {
        constructor(curriculum: Curriculum? = null, preferDecimals: Boolean? = null) : this(
            ResourceData(
                curriculum = curriculum,
                preferDecimals = preferDecimals,
            ),
        )
    }

    private val usResource = TestResource(curriculum = Curriculum.US)
    private val euResource = TestResource(curriculum = Curriculum.EU)
    private val decResource = TestResource(preferDecimals = true)
    private val noDecResource = TestResource(preferDecimals = false)
    private val euNoDecResource = TestResource(curriculum = Curriculum.EU, preferDecimals = false)

    @Test
    fun testNullContextSelectsDefaultResource() {
        val ctx = Context()
        assertEquals(ctx.selectBestResource(euResource, listOf(usResource)), euResource)
        assertEquals(ctx.selectBestResource(usResource, listOf(euResource)), usResource)
        assertEquals(ctx.selectBestResource(decResource, listOf(noDecResource, usResource)), decResource)
        assertEquals(ctx.selectBestResource(noDecResource, listOf(decResource, usResource)), noDecResource)
    }

    @Test
    fun testUSContextSelectsUSResourceElseDefaultResource() {
        val ctx = Context(Curriculum.US)
        assertEquals(ctx.selectBestResource(usResource, listOf(euResource)), usResource)
        assertEquals(ctx.selectBestResource(euResource, listOf(usResource)), usResource)
        assertEquals(ctx.selectBestResource(decResource, listOf(noDecResource)), decResource)
        assertEquals(ctx.selectBestResource(noDecResource, listOf(decResource)), noDecResource)
    }

    @Test
    fun testDecContextSelectsDecResourceElseDefaultResource() {
        val ctx = Context(preferDecimals = true)
        assertEquals(ctx.selectBestResource(usResource, listOf(euResource)), usResource)
        assertEquals(ctx.selectBestResource(euResource, listOf(usResource)), euResource)
        assertEquals(ctx.selectBestResource(decResource, listOf(noDecResource)), decResource)
        assertEquals(ctx.selectBestResource(noDecResource, listOf(decResource)), decResource)
    }

    @Test
    fun testCompositeContext() {
        val ctx = Context(Curriculum.EU, preferDecimals = false)
        assertEquals(ctx.selectBestResource(usResource, listOf(euResource)), euResource)
        assertEquals(ctx.selectBestResource(euResource, listOf(usResource)), euResource)
        assertEquals(ctx.selectBestResource(decResource, listOf(noDecResource)), noDecResource)
        assertEquals(ctx.selectBestResource(noDecResource, listOf(decResource)), noDecResource)
        assertEquals(
            ctx.selectBestResource(noDecResource, listOf(euResource, euNoDecResource, usResource)),
            euNoDecResource,
        )
    }
}
