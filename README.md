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
awslocal sqs create-queue --queue-name 'sample-queue' --region ap-northeast-1
```

- キューの一覧を確認

```
awslocal sqs list-queues --region ap-northeast-1
```

- キューにメッセージを送信

```
awslocal sqs send-message --queue-url http://localhost:4566/000000000000/sample-queue --message-body '{"message": "hoge"}' --region ap-northeast-1
```

- メッセージを確認

```
awslocal sqs receive-message --queue-url http://localhost:4566/000000000000/sample-queue --region ap-northeast-1
```

## message-apiの実装

spring intializr から、以下の設定で Project を生成

```
Project: Gradle - Kotlin
Language: Kotlin
Spring Boot: 3.1.3
Project Metadata
  Group: com.example
  Artifact: message-api
  Name: message-api
  Description: Demo project for Spring Boot
  Package name: com.example.message.api
  Packaging: Jar
  Java: 17
Dependencies: No dependency selected
```

以下の手順で message-api を実装する  

1: 必要なライブラリの追加  
build.gradle.kts に以下を追加

```
dependencyManagement {
	imports {
		mavenBom("io.awspring.cloud:spring-cloud-aws-dependencies:3.0.2")
	}
}

dependencies {
  ...(略)

	implementation("io.awspring.cloud:spring-cloud-aws-starter-sqs")
}

```

2: ローカルへの接続設定
application.properties に以下を追加

```
spring.cloud.aws.sqs.endpoint=http://localhost:4566
spring.cloud.aws.sqs.region=ap-northeast-1
```

3: handler の実装
com.example.message.api 配下に新規で handler という package を追加し、SampleSQSMeesageHandler.kt を作成

```SampleSQSMeesageHandler.kt
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
