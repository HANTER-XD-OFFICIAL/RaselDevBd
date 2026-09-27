package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SeedPortfolioData
import com.example.data.SiteConfigEntity
import com.example.data.StaticSiteGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Rasel Dev BD", appName)
    }

    @Test
    fun `static site generator round trip json bundle preserves categories and items`() {
        val config = SiteConfigEntity(siteTitle = "Rasel Dev BD")
        val categories = SeedPortfolioData.defaultCategories()
        val items = SeedPortfolioData.defaultContentItems()

        val jsonBundle = StaticSiteGenerator.generateStaticJsonBundle(
            config = config,
            categories = categories,
            publishedItems = items
        )
        assertTrue(jsonBundle.contains("Rasel Dev BD"))

        val parsed = StaticSiteGenerator.parseStaticJsonBundle(jsonBundle, "2026")
        assertEquals(categories.size, parsed.categories.size)
        assertEquals(items.size, parsed.items.size)
        assertEquals("Rasel Dev BD", parsed.config?.siteTitle)
    }
}
