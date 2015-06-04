package degrel.graphbuilder

trait BuildPhase {

}

/**
 * 初期状態
 * @note 他の`Builder[T]`に全く依存せず行える処理に関しては可能な限りコンストラクタ内で行います
 *       例えば変数を登録しシンボルテーブルツリーの構築はこのフェーズですでに終了していることが
 *       期待されます
 *
 */
object NothingDone extends BuildPhase

/**
 * 各々の`Builder`が変数登録などが終了した状態で，周囲のレキシカルなデータを参照しながら
 * どのようなグラフを構築するかを決定します．
 *
 * @note このときに他`Builder`の`Builder.header`を参照しないでください
 */
object MainPhase extends BuildPhase

/**
 * 決定された`Builder`の状態を元に，実際に`degrel.core`のインスタンスを生成し
 * グラフを構築します
 */
object FinalizePhase extends BuildPhase

/**
 * 終了状態
 */
object Completed extends BuildPhase
