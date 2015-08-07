DEGREL -- Distributed REGREL implementation
===========================================

グラフ書き換えを基本にした，分散処理向け言語およびフレームワーク

汎用言語ですが，Java(JVM)向けの分散処理DSLとして作成することを主眼に置いています．


コンパイル方法
--------------
[sbt](http://www.scala-sbt.org)を使用します．

  $ sbt compile


実行方法
-------

### REPLの起動


```
  $ ./bin/run.py
```


### スクリプトの実行

```
  $ ./bin/run.py some_script.dg
```


コード例(REPL)
-------

```
__main__> 1 + 2
{
  3
}
```


グラフ書き換えとは
------------------
