package dev.ishant.easyniddle

import androidx.navigation3.runtime.NavKey

/**
 * Values captured while matching an incoming deep link.
 *
 * Path parameters are decoded from the URL. Query parameters are decoded too;
 * when a query key appears more than once, the last value is exposed.
 */
public class NiddleDeepLinkMatch internal constructor(
    public val uri: String,
    public val pathParameters: Map<String, String>,
    public val queryParameters: Map<String, String>,
) {
    public fun pathParameter(name: String): String =
        requireNotNull(pathParameters[name]) {
            "Deep link does not contain path parameter '$name'."
        }

    public fun queryParameter(name: String): String? = queryParameters[name]
}

private data class ParsedDeepLinkUri(
    val scheme: String,
    val authority: String,
    val pathSegments: List<String>,
    val queryParameters: Map<String, String>,
)

internal data class DeepLinkDefinition(
    val scheme: String,
    val authority: String,
    val pathSegments: List<String>,
    val parameterNames: List<String?>,
    val pattern: String,
    val destination: (NiddleDeepLinkMatch) -> NavKey,
) {
    val specificity: Int get() = parameterNames.count { it == null }

    fun match(uri: String, parsed: ParsedDeepLinkUri): Pair<Int, NiddleDeepLinkMatch>? {
        if (!scheme.equals(parsed.scheme, ignoreCase = true)) return null
        if (!authority.equals(parsed.authority, ignoreCase = true)) return null
        if (pathSegments.size != parsed.pathSegments.size) return null

        val parameters = linkedMapOf<String, String>()
        pathSegments.indices.forEach { index ->
            val parameterName = parameterNames[index]
            val actualSegment = parsed.pathSegments[index]
            if (parameterName == null) {
                if (pathSegments[index] != actualSegment) return null
            } else {
                parameters[parameterName] = decodeUrlComponent(actualSegment, plusAsSpace = false)
            }
        }
        return specificity to NiddleDeepLinkMatch(uri, parameters, parsed.queryParameters)
    }
}

/**
 * Immutable deep-link resolver. Register URL patterns and map their parameters
 * to app-owned Navigation 3 route keys.
 */
public class NiddleDeepLinkRegistry internal constructor(
    private val definitions: List<DeepLinkDefinition>,
) {
    /** Returns the matching route, or null when no registered pattern matches. */
    public fun resolve(uri: String): NavKey? {
        val parsed = parseDeepLinkUri(uri) ?: return null
        val matches = definitions.mapNotNull { definition ->
            definition.match(uri, parsed)?.let { (specificity, match) ->
                Triple(specificity, definition, match)
            }
        }
        if (matches.isEmpty()) return null

        val highestSpecificity = matches.maxOf { it.first }
        val bestMatches = matches.filter { it.first == highestSpecificity }
        check(bestMatches.size == 1) {
            "Deep link '$uri' is ambiguous. Matching patterns: " +
                bestMatches.joinToString { it.second.pattern }
        }
        val bestMatch = bestMatches.single()
        return bestMatch.second.destination(bestMatch.third)
    }

    /** Resolves a deep link or throws a readable error when no pattern matches. */
    public fun requireResolve(uri: String): NavKey =
        requireNotNull(resolve(uri)) {
            "No Niddle deep-link pattern matches '$uri'."
        }
}

/** DSL for registering URL patterns and their typed destination factories. */
public class NiddleDeepLinkBuilder internal constructor() {
    private val definitions = mutableListOf<DeepLinkDefinition>()

    /**
     * Registers a URL pattern such as `https://example.com/users/{userId}`.
     *
     * Query values are read from the incoming URL, not from the pattern.
     * Placeholders must occupy a complete path segment.
     */
    public fun <Route : NavKey> deepLink(
        pattern: String,
        destination: (NiddleDeepLinkMatch) -> Route,
    ) {
        val parsed = parseDeepLinkUri(pattern)
            ?: throw IllegalArgumentException(
                "Invalid deep-link pattern '$pattern'. Use scheme://host/path."
            )
        require(parsed.queryParameters.isEmpty()) {
            "Deep-link pattern must not contain query parameters: '$pattern'."
        }

        val names = parsed.pathSegments.map { segment ->
            if (segment.startsWith("{") && segment.endsWith("}")) {
                segment.substring(1, segment.length - 1).also { name ->
                    require(name.isNotBlank() && name.none { it == '{' || it == '}' }) {
                        "Invalid path parameter '$segment' in deep-link pattern '$pattern'."
                    }
                }
            } else {
                require(!segment.contains('{') && !segment.contains('}')) {
                    "Placeholders must occupy a complete path segment in '$pattern'."
                }
                null
            }
        }
        val parameterNames = names.filterNotNull()
        require(parameterNames.size == parameterNames.toSet().size) {
            "Path parameter names must be unique in deep-link pattern '$pattern'."
        }
        require(definitions.none { it.pattern == pattern }) {
            "Duplicate deep-link pattern: '$pattern'."
        }

        definitions += DeepLinkDefinition(
            scheme = parsed.scheme,
            authority = parsed.authority,
            pathSegments = parsed.pathSegments,
            parameterNames = names,
            pattern = pattern,
            destination = { match -> destination(match) },
        )
    }

    internal fun build(): NiddleDeepLinkRegistry =
        NiddleDeepLinkRegistry(definitions.toList())
}

/** Builds an immutable deep-link registry. */
public fun niddleDeepLinks(
    builder: NiddleDeepLinkBuilder.() -> Unit,
): NiddleDeepLinkRegistry {
    val deepLinkBuilder = NiddleDeepLinkBuilder()
    deepLinkBuilder.builder()
    return deepLinkBuilder.build()
}

private fun parseDeepLinkUri(uri: String): ParsedDeepLinkUri? {
    val withoutFragment = uri.substringBefore('#')
    val schemeEnd = withoutFragment.indexOf("://")
    if (schemeEnd <= 0) return null

    val scheme = withoutFragment.substring(0, schemeEnd)
    if (!scheme.first().isLetter() || scheme.any { !(it.isLetterOrDigit() || it in "+-.") }) {
        return null
    }

    val remainder = withoutFragment.substring(schemeEnd + 3)
    val queryStart = remainder.indexOf('?')
    val address = if (queryStart >= 0) remainder.substring(0, queryStart) else remainder
    val query = if (queryStart >= 0) remainder.substring(queryStart + 1) else ""
    val slashIndex = address.indexOf('/')
    val authority = if (slashIndex >= 0) address.substring(0, slashIndex) else address
    if (authority.isBlank()) return null

    val rawPath = if (slashIndex >= 0) address.substring(slashIndex + 1) else ""
    val pathSegments = if (rawPath.isEmpty()) emptyList() else rawPath.split('/')

    val queryParameters = linkedMapOf<String, String>()
    if (query.isNotEmpty()) {
        query.split('&').forEach { pair ->
            if (pair.isEmpty()) return@forEach
            val separator = pair.indexOf('=')
            val rawName = if (separator >= 0) pair.substring(0, separator) else pair
            val rawValue = if (separator >= 0) pair.substring(separator + 1) else ""
            val name = decodeUrlComponent(rawName, plusAsSpace = true)
            val value = decodeUrlComponent(rawValue, plusAsSpace = true)
            queryParameters[name] = value
        }
    }

    return ParsedDeepLinkUri(
        scheme = scheme,
        authority = authority,
        pathSegments = pathSegments,
        queryParameters = queryParameters,
    )
}

private fun decodeUrlComponent(value: String, plusAsSpace: Boolean): String = buildString {
    var index = 0
    while (index < value.length) {
        val character = value[index]
        when {
            character == '%' -> {
                val bytes = mutableListOf<Byte>()
                while (index < value.length && value[index] == '%') {
                    require(index + 2 < value.length) { "Malformed percent-encoding in URL component." }
                    val byte = value.substring(index + 1, index + 3).toIntOrNull(16)
                        ?: throw IllegalArgumentException("Malformed percent-encoding in URL component.")
                    bytes += byte.toByte()
                    index += 3
                }
                append(bytes.toByteArray().decodeToString(throwOnInvalidSequence = true))
            }
            character == '+' && plusAsSpace -> {
                append(' ')
                index++
            }
            else -> {
                append(character)
                index++
            }
        }
    }
}
