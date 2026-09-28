package com.example

import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.CmsProviderType
import com.example.data.InitialSeedData
import com.example.data.SiteConfigEntity
import com.example.data.StaticSiteGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExampleRobolectricTest {

    @Test
    fun verifyAppContextAndContentfulCmaBundleExport() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("Rasel Dev BD", context.getString(R.string.app_name))

        val config = SiteConfigEntity(
            cmsProvider = CmsProviderType.CONTENTFUL,
            contentfulEnvironment = "master",
            contentfulContentType = "portfolioItem"
        )
        val categories = InitialSeedData.defaultCategories()
        val items = InitialSeedData.defaultContentItems()

        val jsonBundle = StaticSiteGenerator.generateStaticJsonBundle(config, categories, items)
        assertTrue(jsonBundle.contains("Rasel Dev BD"))
        assertTrue(jsonBundle.contains("supportsDirectCmaPublishing"))
        assertTrue(jsonBundle.contains("downloadUrl"))
        assertTrue(jsonBundle.contains("Download Starter Kit (.ZIP)"))

        val cmsClientJs = StaticSiteGenerator.generateHeadlessCmsClientJs(config)
        assertTrue(cmsClientJs.contains("api.contentful.com"))
        assertTrue(cmsClientJs.contains("downloadUrl"))

        val parsed = StaticSiteGenerator.parseStaticJsonBundle(jsonBundle)
        assertEquals("Rasel Dev BD", parsed.config?.siteTitle)
        assertEquals(categories.size, parsed.categories.size)
        assertEquals(items.size, parsed.items.size)
        assertTrue(parsed.items.first().downloadUrl.isNotBlank())
    }
}
