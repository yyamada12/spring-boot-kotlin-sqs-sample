#!/bin/bash

# 引数チェック
if [ "$#" -ne 1 ]; then
    echo "使用法: $0 <並列実行数>"
    exit 1
fi

# 並列実行数
num_parallel=$1

# SQSメッセージ送信関数
send_sqs_message() {
    local msg_number=$1
    local timestamp=$(date +%s) # 現在のタイムスタンプ
    local message_body="{\"message\": \"msg${msg_number}\", \"timestamp\": \"${timestamp}\"}"

    awslocal sqs send-message --queue-url http://localhost:4566/000000000000/sample-queue --message-body "${message_body}" --region ap-northeast-1
}

# 並列実行
for i in $(seq 1 $num_parallel); do
    send_sqs_message $i &
done

# すべてのバックグラウンドプロセスの終了を待つ
wait
