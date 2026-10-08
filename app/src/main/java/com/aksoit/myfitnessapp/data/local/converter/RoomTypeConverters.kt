package com.aksoit.myfitnessapp.data.local.converter

import androidx.room.TypeConverter
import com.aksoit.myfitnessapp.domain.model.ProtocolConfig
import kotlinx.serialization.json.Json

/**
 * TypeConverters Room. A coluna protocol_config_json guarda JSON tipado estrito de [ProtocolConfig].
 */
class RoomTypeConverters {

    @TypeConverter
    fun protocolConfigToJson(config: ProtocolConfig?): String? = encodeProtocolConfig(config)

    @TypeConverter
    fun jsonToProtocolConfig(json: String?): ProtocolConfig? = decodeProtocolConfig(json)

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
            explicitNulls = false
        }

        fun encodeProtocolConfig(config: ProtocolConfig?): String? =
            config?.let { json.encodeToString(ProtocolConfig.serializer(), it) }

        fun decodeProtocolConfig(value: String?): ProtocolConfig? =
            if (value.isNullOrBlank()) null
            else runCatching { json.decodeFromString(ProtocolConfig.serializer(), value) }.getOrNull()
    }
}
