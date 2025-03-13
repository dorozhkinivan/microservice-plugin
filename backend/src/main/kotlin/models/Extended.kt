package ru.itmo.ivandor.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class JsonSchema(
    val schema: Schema
) {
    // Значение по умолчанию для объекта Schema
    companion object {
        val default = JsonSchema(
            schema = Schema.default
        )
    }
}

@Serializable
data class Schema(
    val type: String = "object",
    val properties: Properties,
    val required: List<String> = listOf("microservices")
) {
    // Значение по умолчанию для объекта Properties
    companion object {
        val default = Schema(
            properties = Properties.default
        )
    }
}

@Serializable
data class Properties(
    val microservices: Microservices
) {
    // Значение по умолчанию для объекта Microservices
    companion object {
        val default = Properties(
            microservices = Microservices.default
        )
    }
}

@Serializable
data class Microservices(
    val type: String = "array",
    val items: Items
) {
    // Значение по умолчанию для объекта Items
    companion object {
        val default = Microservices(
            items = Items.default
        )
    }
}

@Serializable
data class Items(
    val type: String = "object",
    val properties: ItemProperties,
    val required: List<String> = listOf("name", "classes")
) {
    // Значение по умолчанию для объекта ItemProperties
    companion object {
        val default = Items(
            properties = ItemProperties.default
        )
    }
}

@Serializable
data class ItemProperties(
    val name: Property = Property("string", "Имя микросервиса"),
    val classes: Property1 = Property1("array", "Массив классов", items = Property("string", "Класс, связанный с микросервисом"))
) {
    // Значение по умолчанию
    companion object {
        val default = ItemProperties()
    }
}

@Serializable
data class Property(
    val type: String,
    val description: String,
   // @SerialName("items") val items: Property? = null
)

@Serializable
data class Property1(
    val type: String,
    val description: String,
     @SerialName("items") val items: Property? = null
)

fun main() {
    // Пример использования значений по умолчанию
    val defaultJsonSchema = JsonSchema.default
    println(Json { encodeDefaults = true; prettyPrint = true }.encodeToString(defaultJsonSchema))
}



// response
@Serializable
data class Respp(
    val microservices: List<Microservice>
)

@Serializable
data class Microservice(
    val name: String,
    val classes: List<String>,
    val description: String,
)