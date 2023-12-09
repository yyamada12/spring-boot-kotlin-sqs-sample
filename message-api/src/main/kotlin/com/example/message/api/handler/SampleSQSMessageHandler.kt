package com.example.message.api.handler

import com.fasterxml.jackson.annotation.JsonProperty
import io.awspring.cloud.sqs.annotation.SqsListener
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SampleSQSMessageHandler {

    // SqsListener のアノテーションをつけることで、SQSをポーリングしてメッセージを取得するようになる
    // https://spring.pleiades.io/spring-cloud-aws/docs/3.0.2/reference/html/index.html#sqslistener-annotation
    @SqsListener("sample-queue")
    fun handle(message: SampleData) {
        println("message received: $message")
        Thread.sleep(25000)
        println("message processed: $message")
    }
}

data class SampleData(
    @JsonProperty("message")
    val message: String,

    @JsonProperty("timestamp")
    val timestamp: String
)