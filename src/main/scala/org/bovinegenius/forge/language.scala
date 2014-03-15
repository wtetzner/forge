package org.bovinegenius.forge.language

import scala.language.dynamics
import scala.util.parsing.input.Positional

sealed trait Expression

sealed trait Argument {
  val value: Expression
}
case class PositionalArg(override val value: Expression) extends Argument
case class NamedArg(
  val name: String,
  override val value: Expression)
    extends Argument

sealed trait Call extends Expression
case class FunctionCall(
  val name: String,
  val args: Seq[Argument]
) extends Call
case class MethodCall(val obj: Expression, val invocation: FunctionCall) extends Call

case class Literal(val value: Any) extends Expression {
  override def toString: String = {
    value match {
      case str: String => "\"%s\"".format(value.toString)
      case _ => value.toString
    }
  }
}

case class Variable(val name: String) extends Expression {
  override def toString: String = name
}

sealed trait Function {
  def name: String
  def params: Seq[String]
  def invoke(env: Map[String,Any], args: Seq[Any]): Any
  def invoke(args: Seq[Any]): Any = {
    invoke(Map(), args)
  }
}

sealed trait Definition {
  def name: String
}

case class VariableDefinition(
  override val name: String,
  val value: Expression)
    extends Definition {
  override def toString: String = s"${name} = ${value.toString}"
}

sealed trait FunctionDefinition extends Definition with Function
case class NormalFunctionDefinition(
  override val name: String,
  override val params: Seq[String],
  val body: Seq[Expression]) extends FunctionDefinition {
  override def invoke(env: Map[String,Any], args: Seq[Any]) = {
    val newEnv: Map[String, Any] = env ++ Map(params.zip(args): _*)
    body.map(expr => Language.eval(newEnv, expr)).last
  }
  override def toString: String = {
    val argNames = params.mkString(", ")
    val bodyStr = body.map(_.toString).mkString("; ")
    s"${name}(${argNames}) = { ${bodyStr} }"
  }
}

case class EmbeddedLanguageFunction(
  override val name: String,
  override val params: Seq[String],
  val body: EmbeddedLanguage
) extends FunctionDefinition {
  override def invoke(env: Map[String,Any], args: Seq[Any]) = {
    import javax.script._
    val manager: ScriptEngineManager = new ScriptEngineManager(null)
    val engine: ScriptEngine = manager.getEngineByName(body.name)
    engine.eval(body.body)
  }
  override def toString: String = {
    val argNames = params.mkString(", ")
    s"${name}(${argNames}) = *${body.name}*${body.text}"
  }
}

case class EmbeddedLanguage(
  val name: String,
  text: String,
  indent: String) extends Positional {
  private def countWhitespace(chars: String): Int =
    chars.foldLeft(0)((b, chr) => chr match {
      case '\n' => b
      case ' '  => b + 1
      case '\t' => b + 2
    })

  private lazy val indentAmount = countWhitespace(indent)

  lazy val body = {
    text.lines.map({ line =>
      if (line.length <= indentAmount) {
        ""
      } else {
        line.substring(indentAmount)
      }
    }).mkString("\n").trim + "\n"
  }
}

case class TargetDefinition(
  override val name: String,
  val deps: List[String],
  val body: Seq[Call]
) extends Definition

class WrappedFn1[T,S](override val name: String, param: String, fn: T => S) extends Function {
  override val params: Seq[String] = List(param)
  override def invoke(env: Map[String,Any], args: Seq[Any]): Any = {
    fn(args.head.asInstanceOf[T])
  }
}

object WrappedFn {
  def apply[T, S](name: String, param: String, fn: T => S) = {
    new WrappedFn1(name, param, fn)
  }
}

object Language {
  def define(definitions: Seq[Definition]): Map[String,Definition] = {
    Map(definitions.map(d => (d.name -> d)): _*)
  }

  def eval(env: Map[String, Any], expr: Expression): Any = {
    expr match {
      case lit: Literal => lit.value
      case Variable(name) => env(name)
      case FunctionCall(name, args) => {
        val func: Any = eval(env, Variable(name))
        func match {
          case f: Function =>
            f.invoke(env, args.map(x => eval(env, x.value)))
        }
      }
    }
  }
}




