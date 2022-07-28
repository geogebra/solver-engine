package processor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File

class TranslationProcessor(val categoryFiles: Sequence<File>, val outputDir: File) {

    fun processTranslations() {
        val categories = categoryFiles.map { CategoryReader.parseCategoryFile(it) }

        outputDir.mkdirs()
        createJSONstrings(categories)
    }

    fun createJSONstrings(categories: Sequence<Category>) {
        val outFile = File("$outputDir/DefaultTranslations.json")
        val hashMap: HashMap<String, String> = HashMap<String, String>()

        for (category in categories) {
            for ((k, v) in category.explanations) {
                if (v.defaultTranslation != null) {
                    hashMap.put("${category.metadata.name}.$k", v.defaultTranslation)
                }
            }
        }

        val mapper = jacksonObjectMapper()
        val jsonString = mapper.writeValueAsString(hashMap)

        writeFormattedJSONstring(outFile, jsonString)
    }
}
