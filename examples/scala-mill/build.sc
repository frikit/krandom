import mill._
import mill.scalalib._

object app extends ScalaModule {
  def krandomVersion = sys.props.getOrElse("krandom.version", sys.env.getOrElse("KRANDOM_VERSION", "0.1.0-SNAPSHOT"))

  def scalaVersion = "3.3.7"

  def ivyDeps = Agg(
    ivy"io.github.frikit:krandom-core:$krandomVersion"
  )

  // repositoriesTask must be an anonymous Task (T.task), not a cached Target (T { }),
  // because Seq[coursier.Repository] has no upickle ReadWriter.
  override def repositoriesTask = T.task {
    super.repositoriesTask() ++ Seq(
      coursier.MavenRepository(s"file://${sys.props("user.home")}/.m2/repository")
    )
  }

  object test extends ScalaTests {
    def moduleDeps = Seq(app)

    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.2.19"
    )

    override def repositoriesTask = T.task {
      super.repositoriesTask() ++ Seq(
        coursier.MavenRepository(s"file://${sys.props("user.home")}/.m2/repository")
      )
    }
  }
}
