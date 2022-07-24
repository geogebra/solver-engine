package processor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File

object CategoryReader {
    val mapper = ObjectMapper(YAMLFactory()) // Enable YAML parsing
    init {
        mapper.registerModule(
            KotlinModule.Builder()
                .enable(KotlinFeature.NullToEmptyCollection)
                .enable(KotlinFeature.NullToEmptyMap)
                .build()
        ) // Enable Kotlin support
    }

    fun parseCategoryFile(categoryFile: File): Category {
        return categoryFile.inputStream().bufferedReader().use {
            mapper.readValue(it, Category::class.java)
        }
    }
}