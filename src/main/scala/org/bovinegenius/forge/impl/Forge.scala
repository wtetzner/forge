package org.bovinegenius.forge.impl

import org.bovinegenius.forge.Ant
import org.bovinegenius.forge.Forge
import org.bovinegenius.forge.model._
import org.eclipse.aether.resolution.DependencyResolutionException
import org.bovinegenius.forge.xml._
import org.bovinegenius.forge.Script
import org.bovinegenius.forge.language._
import javax.script._

private class DefaultForge extends Forge {
  override def build(): Unit = {
    println("Started...")
    try {
      val repo = StandardMavenRepository("central", "http://repo1.maven.org/maven2/")
      val repo2 = StandardMavenRepository("local", "file:///Users/wtetzner/.m2/repository/")
      val repo3 = CombinedMavenRepository("combined", Seq(repo, repo2))
      val rhino = repo3.jar(
        groupId = "org.mozilla",
        artifactId = "rhino",
        version = "1.7R4"
      ).ensure
      val proguard = repo3.jar("net.sf.proguard", "proguard-anttask", "4.10").ensure
      println("fetched...")
      // val ant = Ant.tasks
      // ant.echo(message = "Some message")
      // ant.echo(message = Element("blah", Seq("a" -> "b"), Body(Text("asdf"), Element("elem", Seq(), Body()))))
      // val dir1 = "/Users/walter/some-test-dir"
      // val dir2 = new java.io.File("/Users/walter/some-test-dir2")
      // ant.mkdir(dir = dir1)
      // ant.mkdir(dir = dir2)
      // ant.delete(dir = dir1)
      // ant.delete(dir = dir2)
      // ant.delete(dir = dir2)

      // ant.echo(message = "Done.")

      val jstest = Script("test.js")
      jstest.log("DEBUG", "Cool Message")

      Script("test.js").applyDynamic("blah")()

      // val env = Language.define(
      //   Seq(
      //     FunctionDefinition("incNum", List("num"),
      //       List(FunctionCall("inc",
      //         Seq(PositionalArg(Variable("num"))))))))

      // val newEnv: Map[String,Any] =
      //   env + ("inc" -> WrappedFn("inc", "n", (x: Int) => { x + 1 }))
      // val result = Language.eval(
      //   newEnv,
      //   FunctionCall("incNum", Seq(PositionalArg(Literal(8)))))
      // println(s"result: ${result.toString}")
//       println(Lexer.tokens("""generate-parser(grammar, outfile) =
//   stuff
//   call
//   hmm


// """))
//       println(Parser.parse("""generate-parser(grammar, outfile) =
//   stuff
//   call
//   hmm


// """))
      println(ForgeParser.parseAll(ForgeParser.taskDef, 
"""name-x[a,b, c]: *js*
  let x = "asdf";
       xyz

  java.lang.System.out.println(x + ' - hmm');
  java.lang.System.out.println("jar: " + protoc-lib);

""").get.body.body)

      val parsed = ForgeParser.parseAll(ForgeParser.toplevel,
"""name-x[a,b, c]: *js*
  let x = "asdf";

  java.lang.System.out.println(x + ' - hmm');
  java.lang.System.out.println("jar: " + "stuff");


some-task[name-x]: *js*
  let x = "another thingy";

  java.lang.System.out.println(x + ' - hmm');
  java.lang.System.out.println("jar: " + "stuff");


""").get.last.body.body
      val manager: ScriptEngineManager = new ScriptEngineManager(null);
      val engine: ScriptEngine = manager.getEngineByName("JavaScript");
      engine.eval(parsed)

      EmbeddedLanguageFunction(
        "func",
        Seq(),
        EmbeddedLanguage("JavaScript", """
  let x = 'some stuff';
  println('message: ' + x)
""",
        "\n  ")
      ).invoke(Map(), Seq())

      println("Done.")
    } catch {
      case e: DependencyResolutionException => {
        println(s"[ERROR] ${e.getMessage}")
        System.exit(1)
      }
    }
  }
}

object ForgeFactory {
  def defaultForge: Forge = new DefaultForge()
}

