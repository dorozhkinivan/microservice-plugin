package ru.itmo.ivandor.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class JsonSchema(
    val schema: Schema
) {
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
    companion object {
        val default = ItemProperties()
    }
}

@Serializable
data class Property(
    val type: String,
    val description: String,
)

@Serializable
data class Property1(
    val type: String,
    val description: String,
     @SerialName("items") val items: Property? = null
)


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

// request
@Serializable
data class Request(
    val classes: List<Class>,
)

@Serializable
data class Class(
    val name: String,
    val methods: List<String>,
)