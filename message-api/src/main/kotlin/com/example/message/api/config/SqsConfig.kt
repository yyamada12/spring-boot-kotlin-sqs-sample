package com.example.message.api.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.converter.MappingJackson2MessageConverter

@Configuration
class SqsConfig {

    // jackson を利用してメッセージを変換するための設定を追加
    @Bean
    fun mappingJackson2MessageConverter(objectMapper: ObjectMapper): MappingJackson2MessageConverter {
        val messageConverter = MappingJackson2MessageConverter()
        messageConverter.objectMapper = objectMapper
        return messageConverter
    }
}
