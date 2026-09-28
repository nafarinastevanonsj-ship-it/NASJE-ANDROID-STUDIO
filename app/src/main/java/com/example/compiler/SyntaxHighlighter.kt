package com.example.compiler

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.data.model.FileType

object SyntaxHighlighter {

    private val KEYWORD_COLOR = Color(0xFFCC7832)    // Orange
    private val ANNOTATION_COLOR = Color(0xFFBBB529) // Yellow/Gold
    private val STRING_COLOR = Color(0xFF6A8759)     // Green
    private val NUMBER_COLOR = Color(0xFF6897BB)     // Blue
    private val COMMENT_COLOR = Color(0xFF808080)    // Muted Gray
    private val XML_TAG_COLOR = Color(0xFFE8BF6A)    // Pale Gold
    private val XML_ATTR_COLOR = Color(0xFF9876AA)   // Lilac
    private val TYPE_COLOR = Color(0xFF4EC9B0)       // Cyan / Teal
    private val DEFAULT_TEXT_COLOR = Color(0xFFA9B7C6) // Light Gray

    private val KOTLIN_KEYWORDS = setOf(
        "package", "import", "class", "interface", "fun", "val", "var",
        "return", "if", "else", "when", "for", "while", "do", "break",
        "continue", "null", "true", "false", "this", "super", "object",
        "companion", "override", "private", "public", "protected", "internal",
        "sealed", "data", "enum", "lateinit", "lazy", "by", "in", "is", "as",
        "try", "catch", "finally", "throw", "suspend", "inline", "const"
    )

    private val COMMON_TYPES = setOf(
        "String", "Int", "Boolean", "Float", "Double", "Long", "Unit", "Any",
        "List", "Map", "Set", "Modifier", "Composable", "Bundle", "Context",
        "Color", "Column", "Row", "Box", "Text", "Button", "Card", "Surface",
        "ComponentActivity", "AppCompatActivity", "TextView", "Button", "EditText"
    )

    fun highlightCode(text: String, fileType: FileType): AnnotatedString {
        return buildAnnotatedString {
            append(text)

            when (fileType) {
                FileType.KOTLIN, FileType.JAVA, FileType.GRADLE -> {
                    highlightKotlin(text)
                }
                FileType.XML -> {
                    highlightXml(text)
                }
                else -> {
                    addStyle(SpanStyle(color = DEFAULT_TEXT_COLOR), 0, text.length)
                }
            }
        }
    }

    private fun AnnotatedString.Builder.highlightKotlin(text: String) {
        // Default text color
        addStyle(SpanStyle(color = DEFAULT_TEXT_COLOR, fontFamily = FontFamily.Monospace), 0, text.length)

        // Strings: "..."
        val stringRegex = Regex("\"(\\\\.|[^\"])*\"")
        stringRegex.findAll(text).forEach { match ->
            addStyle(SpanStyle(color = STRING_COLOR), match.range.first, match.range.last + 1)
        }

        // Annotations: @Composable, @Preview, etc.
        val annotationRegex = Regex("@[A-Za-z0-9_]+")
        annotationRegex.findAll(text).forEach { match ->
            addStyle(
                SpanStyle(color = ANNOTATION_COLOR, fontWeight = FontWeight.SemiBold),
                match.range.first,
                match.range.last + 1
            )
        }

        // Keywords and Types
        val wordRegex = Regex("\\b[A-Za-z_][A-Za-z0-9_]*\\b")
        wordRegex.findAll(text).forEach { match ->
            val word = match.value
            when {
                word in KOTLIN_KEYWORDS -> {
                    addStyle(
                        SpanStyle(color = KEYWORD_COLOR, fontWeight = FontWeight.Bold),
                        match.range.first,
                        match.range.last + 1
                    )
                }
                word in COMMON_TYPES -> {
                    addStyle(
                        SpanStyle(color = TYPE_COLOR, fontWeight = FontWeight.Normal),
                        match.range.first,
                        match.range.last + 1
                    )
                }
            }
        }

        // Numbers: 123, 45L, 3.14f
        val numberRegex = Regex("\\b[0-9]+(\\.[0-9]+)?([fFL])?\\b")
        numberRegex.findAll(text).forEach { match ->
            addStyle(SpanStyle(color = NUMBER_COLOR), match.range.first, match.range.last + 1)
        }

        // Single line comments: // ...
        val commentRegex = Regex("//.*$|/\\*[\\s\\S]*?\\*/", RegexOption.MULTILINE)
        commentRegex.findAll(text).forEach { match ->
            addStyle(
                SpanStyle(color = COMMENT_COLOR, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                match.range.first,
                match.range.last + 1
            )
        }
    }

    private fun AnnotatedString.Builder.highlightXml(text: String) {
        addStyle(SpanStyle(color = DEFAULT_TEXT_COLOR, fontFamily = FontFamily.Monospace), 0, text.length)

        // XML Tags: <tag> </tag>
        val tagRegex = Regex("</?[a-zA-Z0-9_\\.:]+")
        tagRegex.findAll(text).forEach { match ->
            addStyle(SpanStyle(color = XML_TAG_COLOR, fontWeight = FontWeight.SemiBold), match.range.first, match.range.last + 1)
        }

        // XML Attribute names: android:name
        val attrRegex = Regex("[a-zA-Z0-9_\\.:]+(?=\\=)")
        attrRegex.findAll(text).forEach { match ->
            addStyle(SpanStyle(color = XML_ATTR_COLOR), match.range.first, match.range.last + 1)
        }

        // XML Attribute values: "value"
        val valueRegex = Regex("\"[^\"]*\"")
        valueRegex.findAll(text).forEach { match ->
            addStyle(SpanStyle(color = STRING_COLOR), match.range.first, match.range.last + 1)
        }

        // Comments: <!-- ... -->
        val commentRegex = Regex("<!--[\\s\\S]*?-->")
        commentRegex.findAll(text).forEach { match ->
            addStyle(SpanStyle(color = COMMENT_COLOR), match.range.first, match.range.last + 1)
        }
    }
}
