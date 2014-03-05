package org.bovinegenius.forge.xml

case class Body(val items: Node*) {
  override def toString: String =
    items.map(_.toString).mkString

  lazy val isEmpty: Boolean = items.isEmpty
}

sealed abstract class Node
case class Element(
  val name: String,
  val attrs: Seq[(String,String)],
  val body: Body) extends Node {
  override def toString: String = {
    val attrstr = attrs.map(pair => {
      val (name, value) = pair
      "%s=\"%s\"".format(name, value)
    }).mkString(" ")
    val attrText = if (attrstr.isEmpty) "" else " " + attrstr
    if (body.isEmpty) {
      s"<${name}${attrText}/>"
    } else {
      s"<${name}${attrText}>${body.toString}</${name}>"
    }
  }
}

case class Text(val value: String) extends Node {
  override def toString: String = if (value == null) "" else value
}

