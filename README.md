# message-api sample

SQS のキューをポーリングしてメッセージを処理するためのアプリケーションのサンプル

## 概要

- SpringBoot + Kotlin で実装
- Spring Cloud AWS を利用

  - https://spring.pleiades.io/spring-cloud-aws/docs/current/reference/html/index.html

- localstack を利用してローカル環境上に構築
  - https://docs.localstack.cloud/overview/

## 準備

- aws-cli のインストール  
  aws のリソースを操作するためのツール  
  好きな手順でインストールすれば良い  
  参考: https://docs.aws.amazon.com/ja_jp/cli/latest/userguide/getting-started-install.html

- awslocal のインストール  
  localstack で aws-cli を利用するためのラッパー  
  aws-cli だけでも endpoint-url を設定すれば良いので必須ではない  
  好きな手順でインストールすれば良い  
  参考: https://github.com/localstack/awscli-local

## localstack の起動

docker compose で起動

```
docker compose up
```

適当なコマンドを叩いて、うまく動いていれば OK

例) 接続しているアカウントの確認コマンド

```
awslocal sts get-caller-identity
```

↓ のような結果が取得できれば OK

```
{
    "UserId": "AKIAIOSFODNN7EXAMPLE",
    "Account": "000000000000",
    "Arn": "arn:aws:iam::000000000000:root"
}
```

## awslocal コマンドを利用した SQS の操作

`awslocal sqs` コマンドを利用する

- help の確認

```
awslocal sqs help
```

- キューの作成

```
awslocal sqs create-queue --queue-name 'sample-queue'
```

- キューの一覧を確認

```
awslocal sqs list-queues
```

- キューにメッセージを送信

```
awslocal sqs send-message --queue-url http://localhost:4566/000000000000/sample-queue --message-body '{"message": "hoge"}'
```

- メッセージを確認

```
awslocal sqs receive-message --queue-url http://localhost:4566/000000000000/sample-queue
```

## message-apiの実装

spring intializr から、以下の設定で Project を生成

```
Project: Gradle - Kotlin
Language: Kotlin
Spring Boot: 2.7.8
Project Metadata
  Group: com.example
  Artifact: message-api
  Name: message-api
  Description: Demo project for Spring Boot
  Package name: com.example.message.api
  Packaging: Jar
  Java: 11
Dependencies: No dependency selected
```

以下の手順で message-api を実装する  

1: 必要なライブラリの追加  
build.gradle.kts に以下を追加

```
implementation("io.awspring.cloud:spring-cloud-starter-aws:2.4.2")
implementation("io.awspring.cloud:spring-cloud-aws-messaging:2.4.2")
```

2: ローカルへの接続設定
application.properties に以下を追加

```
cloud.aws.sqs.endpoint=http://localhost:4566
```

3: handler の実装
com.example.message.api 配下に新規で handler という package を追加し、SampleSQSMeesageHandler.kt を作成

```SampleSQSMeesageHandler.kt
package com.example.message.api.handler

import io.awspring.cloud.messaging.listener.annotation.SqsListener
import org.springframework.stereotype.Component

@Component
class SampleSQSMeesageHandler {

    // SqsListener のアノテーションをつけることで、SQSをポーリングしてメッセージを取得するようになる
    // https://spring.pleiades.io/spring-cloud-aws/docs/current/reference/html/index.html#annotation-driven-listener-endpoints
    @SqsListener("sample-queue")
    fun queueListener(data: String) {
        println(data)
    }
}
```

これだけで OK

## message-api の動作確認

前提: [message-api の実装](#message-apiの実装) で sample-queue を作成していること

1: application を起動
Intellij から起動
or

```
cd message-api
./gradlew bootRun
```

すでに [message-api の実装](#message-apiの実装) でメッセージを送信していた場合は、コンソールに以下のように出力されるはず

```
{"message": "hoge"}
```

2: SQS にメッセージを送信

```
awslocal sqs send-message --queue-url http://localhost:4566/000000000000/sample-queue --message-body '{"message": "fuga"}'
```

3: コンソール出力の確認
message-api で処理されて以下のように出力される

```
{"message": "fuga"}
```

## message-api の追加実装

先の参考実装では、メッセージを String で受け取っているが、
String では扱いにくいため json メッセージを kotlin の data class で受け取るようにする

1: 必要なライブラリの追加  
build.gradle.kts に以下を追加

```
implementation("org.springframework.boot:spring-boot-starter-json")
implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
```

2: メッセージを jackson を利用してマッピングする設定を追加
com.example.message.api 配下に新規で config という package を追加し、 SqsConfig.kt を作成

```
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
```

3: handler の実装

以下の data class を追加
```
data class SampleData(
  val message: String
)
```

handler の method の引数の型を String -> SampleData に変更
```
- fun queueListener(data: String) {
+ fun queueListener(data: SampleData) {
```

これで先ほど同様に動作確認をすると、 json を data class のオブジェクトとして扱えることが確認できる