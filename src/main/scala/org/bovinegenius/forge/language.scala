package org.bovinegenius.forge.language

sealed trait Expression

sealed trait FunctionCall
case class PositionalCall(
  val name: String,
  val args: List[Expression]) extends FunctionCall with Call
case class KeywordCall(
  val name: String,
  val args: List[(String, Expression)]
) extends FunctionCall with Expression

sealed trait Literal extends Expression {
  val value: Any
}
case class FInt(override val value: Any) extends Literal
case class FDouble(override val value: Any) extends Literal
case class FString(override val value: Any) extends Literal
case class FBool(override val value: Any) extends Literal
case class FList(override val value: Any) extends Literal

sealed trait Call extends Expression
case class MethodCall(val obj: Expression, val invocation: FunctionCall) extends Call

sealed trait Definition
case class VariableDefinition(val name: String, val value: Any)
  extends Definition
case class FunctionDefinition(
  val name: String,
  val args: List[String],
  val body: List[Expression]) extends Definition
case class TargetDefinition(
  val name: String,
  val deps: List[String],
  val body: List[Call]
) extends Definition




