package org.bovinegenius.forge.language

import scala.util.parsing.combinator.Parsers
import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.input.{Reader,Position,Positional}
import scala.collection.mutable.Stack

object Tokens {
  case class TokenStream(tokens: Seq[Token]) extends Reader[Token] {
    var position: Position = null
    override def atEnd: Boolean = tokens.isEmpty
    override def first: Token = tokens.head
    override def pos: Position = {
      if (tokens.isEmpty) {
        position
      } else {
        position = tokens.head.pos
        position
      }
    }
    override def rest: Reader[Token] = TokenStream(tokens.tail)
  }

  sealed trait Token extends Positional {
    def chars: String
  }

  case class Identifier(override val chars: String) extends Token {
    val name: String = chars
  }

  case class Comma(override val chars: String) extends Token {
    override def toString: String = "Comma"
  }
  case class LeftParen(override val chars: String) extends Token {
    override def toString: String = "LeftParen"
  }
  case class RightParen(override val chars: String) extends Token {
    override def toString: String = "RightParen"
  }
  case class LeftBrace(override val chars: String) extends Token {
    override def toString: String = "LeftBrace"
  }
  case class RightBrace(override val chars: String) extends Token {
    override def toString: String = "RightBrace"
  }
  case class LeftBracket(override val chars: String) extends Token {
    override def toString: String = "LeftBracket"
  }
  case class RightBracket(override val chars: String) extends Token {
    override def toString: String = "RightBracket"
  }
  case class Semicolon(override val chars: String) extends Token {
    override def toString: String = "Semicolon"
  }
  case class Equals(override val chars: String) extends Token {
    override def toString: String = "Equals"
  }

  private def countWhitespace(chars: String): Int =
    chars.foldLeft(0)((b, chr) => chr match {
      case '\n' => b
      case ' '  => b + 1
      case '\t' => b + 2
    })

  case class WhitespaceChunk(override val chars: String) extends Token {
    lazy val amount: Int = countWhitespace(chars)
    override lazy val toString: String = s"WhitespaceChunk(${amount})"
  }

  case class Indent(override val chars: String) extends Token {
    lazy val amount: Int = countWhitespace(chars)
    override lazy val toString: String = s"Indent(${amount})"
  }

  object Indented {
    def unapply(indent: Indent): Option[Int] =
      Some(indent.amount)
  }

  object Whitespace {
    def unapply(token: Token): Boolean =
      token match {
        case WhitespaceChunk(_) => true
        case Indent(_) => true
        case _ => false
      }
  }
}

object ForgeParser extends RegexParsers {
  case class WhitespaceChunk(val chars: String) extends Positional
  case class Indent(val chars: String) extends Positional
  case class Identifier(val chars: String) extends Positional
  case class Paren(val chars: String) extends Positional
  case class Comma(val chars: String) extends Positional

  override def skipWhitespace = false

  def whitespaceChunk: Parser[WhitespaceChunk] =
    """[ \t]+""".r ^^ WhitespaceChunk
  def indent: Parser[Indent] = """\n[ \t]*""".r ^^ Indent

  def tok[T](parser: Parser[T]): Parser[T] =
    rep(whitespaceChunk) ~> parser

  def identifier: Parser[Identifier] =
    positioned("""[a-zA-Z_?!-]+""".r ^^ Identifier)

  def topLevelName: Parser[Identifier] = "\n" ~> identifier

  def identifierT: Parser[Identifier] =
    tok(identifier)

  def paren: Parser[Paren] = positioned(("(" | ")") ^^ Paren)

  def parenT: Parser[Paren] = tok(paren)

  def comma: Parser[Comma] = "," ^^ Comma

  def commaT: Parser[Comma] = tok(comma)

  case class FunctionDef(val name: String, val args: Seq[String])
      extends Positional
  case class TaskDef(val name: String, val deps: Seq[String])
      extends Positional

  def taskDef: Parser[TaskDef] =
    topLevelName ~ ("[" ~> repsep(identifierT, commaT) <~ "]:\n") ^^ {
      case name ~ args => TaskDef(name.chars, args.map(_.chars))
    }

  // def embeddedLanguageSignifier: Parser[EmbeddedLanguageSignifier] =
  //   """""".r
}

object Lexer extends RegexParsers {
  import Tokens._
  override def skipWhitespace = false

  def token: Parser[Token] =
    whitespace | equals | identifier | punc | failure("Expected identifier or punctuation")
  def rawTokens: Parser[Seq[Token]] = rep(token)

  def removeEmptyLines(tokens: Seq[Token]): Seq[Token] =
    tokens.zip((tokens :+ Indent("\n")).tail)
      .filter({
        case (Indented(_), Whitespace()) => false
        case (WhitespaceChunk(_), _) => false
        case _ => true
      })
      .map({
        case (tok, _) => tok
      })

  def removeParenIndents(tokens: Seq[Token]): Seq[Token] = {
    val parens: Stack[Token] = Stack()
    tokens.filter({
      case tok @ LeftParen(_) => {
        parens.push(tok)
        true
      }
      case RightParen(_) => {
        parens.pop
        true
      }
      case Indent(_) => {
        if (!parens.isEmpty) {
          false
        } else {
          true
        }
      }
      case _ => true
    })
  }

  def insertBraces(tokens: Seq[Token]): Seq[Token] = {
    val stack: Stack[Indent] = Stack()
    var resultTokens = tokens.flatMap({
      case indent @ Indented(num) => {
        if (stack.isEmpty || num > stack.top.amount) {
          stack.push(indent)
          Seq(LeftBrace("{"))
        } else if(num < stack.top.amount) {
          var braces: Seq[Token] = Seq()
          while (num < stack.top.amount) {
            stack.pop
            braces = RightBrace("}") +: braces
          }
          braces
        } else {
          Seq(Semicolon(";"))
        }
      }
      case token => Seq(token)
    })
    while (!stack.isEmpty) {
      stack.pop
      resultTokens = resultTokens :+ RightBrace("}")
    }
    resultTokens
  }

  def tokens(input: String): Seq[Token] =
    insertBraces(
      removeParenIndents(
        removeEmptyLines(parseAll(rawTokens, input).get)))

  def tokenInput(input: String): Reader[Token] =
    TokenStream(tokens(input))

  def whitespace: Parser[Token] = indent | whitespaceChunk
  def whitespaceChunk: Parser[Token] = """[ \t]+""".r ^^ WhitespaceChunk
  def indent: Parser[Token] = """\n[ \t]*""".r ^^ Indent

  def identifier: Parser[Token] =
    positioned("[a-zA-Z<>_?*-][0-9a-zA-Z<>_?*-]*".r ^^ Identifier)

  def equals: Parser[Token] = positioned("=" ^^ Equals)

  def punc: Parser[Token] =
    comma | paren | brace | bracket

  def paren: Parser[Token] = leftParen | rightParen
  def brace: Parser[Token] = leftBrace | rightBrace
  def bracket: Parser[Token] = leftBracket | rightBracket

  def comma: Parser[Token] = positioned("," ^^ Comma)
  def leftParen: Parser[Token] = positioned("(" ^^ LeftParen)
  def rightParen: Parser[Token] = positioned(")" ^^ RightParen)
  def leftBrace: Parser[Token] = positioned("{" ^^ LeftBrace)
  def rightBrace: Parser[Token] = positioned("}" ^^ RightBrace)
  def leftBracket: Parser[Token] = positioned("[" ^^ LeftBracket)
  def rightBracket: Parser[Token] = positioned("]" ^^ RightBracket)
}

object Parser extends Parsers {
  import Tokens._
  override type Input = Reader[Token]
  override type Elem = Token

  def parse(input: String) =
    functionDefinition.apply(Lexer.tokenInput(input))

  def paramsList: Parser[Seq[String]] =
    leftParen ~> repsep(identifier, comma) <~ rightParen ^^ (items =>
      items.map({
        case Identifier(name) => name
      }))

  def block: Parser[Seq[Expression]] =
    leftBrace ~> repsep(identifier, semicolon) <~ rightBrace ^^ (items =>
      items.map({
        case Identifier(name) => Variable(name)
      }))

  def functionHeader: Parser[(String, Seq[String])] =
    identifier ~ paramsList ^^ {
      case x @ (Identifier(name) ~ args) => (name, x._2)
    }

  def functionDefinition: Parser[FunctionDefinition] =
    functionHeader ~ equals ~ block ^^ {
      case ((name, args) ~ _ ~ block) =>
        FunctionDefinition(name, args, block)
    }

  def equals: Parser[Token] = elem("=", (tok: Token) => {
    tok match {
      case Equals(_) => true
      case _ => false
    }
  })

  def identifier: Parser[Token] = elem("identifier", (token: Token) => {
    token match {
      case Identifier(_) => true
      case _ => false
    }
  })

  def leftParen: Parser[Token] = elem("(", (tok: Token) => {
    tok match {
      case LeftParen(_) => true
      case _ => false
    }
  })

  def rightParen: Parser[Token] = elem(")", (tok: Token) => {
    tok match {
      case RightParen(_) => true
      case _ => false
    }
  })

  def leftBrace: Parser[Token] = elem("{", (tok: Token) => {
    tok match {
      case LeftBrace(_) => true
      case _ => false
    }
  })

  def rightBrace: Parser[Token] = elem("}", (tok: Token) => {
    tok match {
      case RightBrace(_) => true
      case _ => false
    }
  })

  def comma: Parser[Token] = elem(",", (tok: Token) => {
    tok match {
      case Comma(_) => true
      case _ => false
    }
  })

  def semicolon: Parser[Token] = elem(";", (tok: Token) => {
    tok match {
      case Semicolon(_) => true
      case _ => false
    }
  })
}

