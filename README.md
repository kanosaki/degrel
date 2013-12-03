DEGREL - Distributed REGREL
===========================

DEGRELはREGRELを分散処理向けに再実装したものです.

1. Modules
----------

* control
    - DEGRELインスタンスとの通信やそれの起動終了等
* core
    - グラフの表現
* engine
    - DEGRELサービスインスタンス用
* front
    - 構文解析器等
* message
    - control用等にアクターのメッセージをまとめたもの
* tonberry
    - 内部用グラフクエリライブラリ
* utils
    - ユーティリティ群