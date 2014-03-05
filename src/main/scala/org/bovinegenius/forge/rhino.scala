package org.bovinegenius.forge

import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.Function
import java.io.{InputStreamReader,FileInputStream}
import scala.language.dynamics

case class Script(val path: String) extends Dynamic {
  private lazy val context: Context = Context.enter()
  private lazy val scope: Scriptable = context.initStandardObjects()

  private lazy val evaluated: (Scriptable, Object) = {
    val newScope = context.newObject(scope)
    newScope.setPrototype(scope)
    newScope.setParentScope(null)
    val reader = new InputStreamReader(new FileInputStream(path))
    try {
      val result = context.evaluateReader(newScope, reader, path, 0, null)
      (newScope, result)
    } finally {
      reader.close
    }
  }

  def run = {
    val (_, result) = evaluated
    result
  }

  def applyDynamic(name: String)(args: Any*) = {
    val (scope, _) = evaluated
    val func = scope.get(name, scope);
    func match {
      case f: Function => {
        f.call(context, scope, scope, args.toArray.asInstanceOf[Array[Object]])
      }
      case _ => throw new RuntimeException("Invalid Function")
    }
  }
}

