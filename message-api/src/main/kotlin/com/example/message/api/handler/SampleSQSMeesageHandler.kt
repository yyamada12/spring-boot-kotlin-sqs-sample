package com.example.message.api.handler

import io.awspring.cloud.messaging.listener.annotation.SqsListener
import org.springframework.stereotype.Component

data class SampleData(
    val message: String
)

@Component
class SampleSQSMeesageHandler {

    // SqsListener のアノテーションをつけることで、SQSをポーリングしてメッセージを取得するようになる
    // https://spring.pleiades.io/spring-cloud-aws/docs/current/reference/html/index.html#annotation-driven-listener-endpoints
    @SqsListener("sample-queue")
    fun queueListener(data: SampleData) {
        println(data)
    }
}