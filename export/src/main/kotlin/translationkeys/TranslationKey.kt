package translationkeys

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.OutputStream

/**
 * This represents a translation key definition as the ggbtrans solver_import API expects it.
 */
data class TranslationKey(val key: String, val comment: String?)

fun writeTranslationKeys(file: OutputStream, entries: List<TranslationKey>) {
    jacksonObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .writeValue(file, entries)
}
