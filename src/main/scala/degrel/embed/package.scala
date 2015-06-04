package degrel

/**
 * JavaオブジェクトをDEGREL Cellとして振る舞わせるインターフェースを提供します．
 *
 * Draft(2014/12/08):
 * embed.register(Namespace("io"), new degrel.embed.builtins.StdioFactory()),
 * embed.register(Namespace("net.http", new degrel.embed.builtins.WebClientFactory())
 *
 * io.out("Hello world!")
 * --> io.out(0: "Hello world!")
 *
 * import net
 * server = net.http.open(url: "http://example.com", allow_redirect: true)
 * page = server.get(path: "index.html")
 *
 */
package object embed {
  /**
   * DEGREL名前空間にCellFactoryを登録します
   */
  //def register(ns: Namespace, factory: CellFactory) = ???
}
