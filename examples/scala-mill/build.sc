import mill._
import mill.scalalib._

object app extends ScalaModule {
  def scalaVersion = "3.3.7"

  def ivyDeps = Agg(
    ivy"io.github.frikit:krandom-scala-api_3:0.1.0"
  )

  override def repositoriesTask = T {
    super.repositoriesTask() ++ Seq(
      coursier.MavenRepository("https://maven.pkg.github.com/frikit/krandom")
    )
  }

  object test extends ScalaTests {
    def moduleDeps = Seq(app)

    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.2.19"
    )

    override def repositoriesTask = T {
      super.repositoriesTask() ++ Seq(
        coursier.MavenRepository("https://maven.pkg.github.com/frikit/krandom")
      )
    }
  }
}
