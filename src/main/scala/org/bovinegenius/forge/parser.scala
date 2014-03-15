package org.bovinegenius.forge.language

import scala.util.parsing.combinator.Parsers
import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.input.{Reader,Position,Positional}
import scala.collection.mutable.Stack

object ForgeParser extends RegexParsers {
  private def countWhitespace(chars: String): Int =
    chars.foldLeft(0)((b, chr) => chr match {
      case '\n' => b
      case ' '  => b + 1
      case '\t' => b + 2
    })

  case class WhitespaceChunk(val chars: String) extends Positional
  case class Indent(val chars: String) extends Positional {
    lazy val amount: Int = countWhitespace(chars)
    override lazy val toString: String = s"Indent(${amount})"
  }
  case class Identifier(val chars: String) extends Positional
  case class Paren(val chars: String) extends Positional
  case class Comma(val chars: String) extends Positional

  override def skipWhitespace = false

  def whitespaceChunk: Parser[WhitespaceChunk] =
    """[ \t]+""".r ^^ WhitespaceChunk
  def indent: Parser[Indent] = """\n[ \t]*""".r ^^ Indent

  def tok[T](parser: Parser[T]): Parser[T] =
    opt(whitespaceChunk) ~> parser

  def identifier: Parser[Identifier] =
    positioned("""[a-zA-Z_?!-]+""".r ^^ Identifier)

  def topLevelName: Parser[Identifier] = """(?:\n)|(?:^)""".r ~> identifier

  def identifierT: Parser[Identifier] =
    tok(identifier)

  def paren: Parser[Paren] = positioned(("(" | ")") ^^ Paren)

  def parenT: Parser[Paren] = tok(paren)

  def comma: Parser[Comma] = "," ^^ Comma

  def commaT: Parser[Comma] = tok(comma)

  case class FunctionDef(val name: String, val args: Seq[String])
      extends Positional
  case class TaskDef(
    val name: String,
    val deps: Seq[String],
    val body: CustomLanguage)
      extends Positional
  case class CustomLanguage(
    val name: String,
    text: String,
    indent: Indent) extends Positional {
    lazy val body = {
      text.lines.map({ line =>
        if (line.length <= indent.amount) {
          ""
        } else {
          line.substring(indent.amount)
        }
      }).mkString("\n").trim + "\n"
    }
  }

  def emptyLine: Parser[String] = """\n[ \t]*""".r
  def nonemptyLine: Parser[String] =
    """[ \t]*[^ \t][^\n]+""".r
  def indentedLine(indent: String): Parser[String] =
    indent ~ nonemptyLine ^^ {
      case indent ~ text => indent + text
    }
  def languageLines(indent: String): Parser[String] =
    rep(indentedLine(indent) | emptyLine) ^^ {
      case lines => lines.mkString
    }

  def languageSpecifier: Parser[String] =
    (opt(whitespaceChunk) ~ "*") ~> identifier <~ ("*" ~ opt(whitespaceChunk)) ^^ (_.chars)

  def languageBody: Parser[CustomLanguage] =
    for {
      specifier <- languageSpecifier;
      indent <- indent
      restLine <- nonemptyLine
      restBody <- languageLines(indent.chars)
    } yield(CustomLanguage(
      name = specifier,
      text = indent.chars + restLine + restBody,
      indent = indent))

  def taskDef: Parser[TaskDef] =
    positioned(
      topLevelName ~ ("[" ~> repsep(identifierT, commaT) <~ "]:") ~
        languageBody ^^ {
        case name ~ args ~ body =>
          TaskDef(name.chars, args.map(_.chars), body)
      })

  def toplevel: Parser[Seq[TaskDef]] = rep(taskDef)
}

