package com.example.message.api.handler

import io.awspring.cloud.sqs.annotation.SqsListener
import org.springframework.stereotype.Component

@Component
class SampleSQSMessageHandler {

    // SqsListener のアノテーションをつけることで、SQSをポーリングしてメッセージを取得するようになる
    // https://spring.pleiades.io/spring-cloud-aws/docs/3.0.2/reference/html/index.html#sqslistener-annotation
    @SqsListener("sample-queue")
    fun handle(message: String) {
        println(message)
    }
}