package com.caminhos2027.v1.core.apoi

import com.caminhos2027.v1.core.model.Apoi

/** Text search over already-published APOI records. */
object ApoiSearch {
    fun apply(records: List<Apoi>, query: String): List<Apoi> {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isBlank()) return records
        return records.filter { apoi -> searchableText(apoi).contains(normalizedQuery) }
    }

    private fun searchableText(apoi: Apoi): String = normalize(
        buildString {
            append(apoi.name).append(' ')
            append(apoi.description.orEmpty()).append(' ')
            append(apoi.location.locality.orEmpty()).append(' ')
            append(apoi.location.municipality.orEmpty()).append(' ')
            append(apoi.location.reference.orEmpty()).append(' ')
            apoi.services.forEach { append(it.name).append(' ') }
        }
    )

    private fun normalize(value: String): String = value
        .trim()
        .lowercase()
        .fold(StringBuilder()) { result, character ->
            result.append(
                when (character) {
                    'á', 'à', 'â', 'ã', 'ä' -> 'a'
                    'é', 'è', 'ê', 'ë' -> 'e'
                    'í', 'ì', 'î', 'ï' -> 'i'
                    'ó', 'ò', 'ô', 'õ', 'ö' -> 'o'
                    'ú', 'ù', 'û', 'ü' -> 'u'
                    'ç' -> 'c'
                    else -> character
                }
            )
        }
        .toString()
}
