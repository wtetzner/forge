package org.bovinegenius.forge

import java.io.File
import java.net.URLClassLoader
import org.apache.tools.ant.Project
import org.apache.tools.ant.NoBannerLogger
import org.apache.tools.ant.{Task => AntTask}
import scala.language.dynamics
import java.lang.reflect.Method
import java.beans._
import scala.collection.JavaConverters._

sealed trait AntException
case class CoercionException(val message: String)
    extends Exception(message) with AntException

private object Coercions {
  def coerceString[T](value: String, cls: Class[T]): Any = {
    val FileClass = classOf[java.io.File]
    val StringClass = classOf[java.lang.String]
    val BooleanClass = classOf[java.lang.Boolean]
    val CharClass = classOf[java.lang.Character]
    cls match {
      case null => ""
      case FileClass  => new File(value)
      case StringClass => value
      case BooleanClass => (value == "true" || value == "yes" || value == "on")
      case CharClass => value(0)
      case _ => value.toString
    }
  }

  def coerceFile[T](value: File, cls: Class[T]): Any = {
    val FileClass = classOf[java.io.File]
    val StringClass = classOf[java.lang.String]
    val BooleanClass = classOf[java.lang.Boolean]
    val CharClass = classOf[java.lang.Character]
    cls match {
      case FileClass  => value
      case StringClass => value.toString
      case BooleanClass => false
      case CharClass => value.toString()(0)
      case _ => throw CoercionException("Unknown File coercion to type: %s".format(cls.getName))
    }
  }

  def coerce[T](value: Any, cls: Class[T]): Any = {
    value match {
      case v: String => coerceString(v, cls)
      case f: File => coerceFile(f, cls)
      case _ => {
        val StringClass = classOf[java.lang.String]
        cls match {
          case StringClass => if (value == null) "" else value.toString
          case _ => value
        }
      }
    }
  }
}

class Tasks(ant: Ant) extends Dynamic {
  def applyDynamicNamed(name: String)(args: (String, Any)*) = {
    val task = ant.task(name)
    task.setProperties(args)
    task.run()
  }
}

class Task(task: AntTask) {
  def setProperty(name: String, value: Any) {
    setProperties(List(name -> value))
  }

  private def makeMap(pairs: Seq[(String,Method)]): Map[String,Method] = {
    val emptyMap = Map[String,Method]()
    pairs.foldLeft(emptyMap) { case (sofar, item) =>
      sofar.updated(item._1, item._2)
    }
  }

  private lazy val descriptors: Map[String,Method] = {
    val descs = Introspector.getBeanInfo(task.getClass)
                            .getPropertyDescriptors
                            .toList
    makeMap(descs map { desc =>
      (desc.getName, desc.getWriteMethod)
    })
  }

  def setProperties(props: Seq[(String,Any)]) {
    val descs = descriptors
    props.foreach { case (name, value) =>
      val desc = descs(name)
      val input = Coercions.coerce(value, desc.getParameterTypes()(0))
      desc.invoke(task, input.asInstanceOf[Object])
    }
  }

  def run() {
    task.execute()
  }
}

trait Ant {
  def project: Project
  def task(name: String): Task
  def taskNames(): scala.collection.Set[String]
  def tasks: Tasks
}

object Ant {
  private lazy val ant = apply()
  def task(name: String) = ant.task(name)
  def tasks = ant.tasks
  def taskNames = ant.taskNames
  def apply(): Ant = {
    new DefaultAnt()
  }
}

private class DefaultAnt extends Ant {
  lazy val project = {
    makeProject()
  }

  private def makeProject() = {
    val project = new Project()
    val logger = new NoBannerLogger()
    logger.setOutputPrintStream(System.out)
    logger.setErrorPrintStream(System.err)
    logger.setMessageOutputLevel(Project.MSG_INFO)
    project.init()
    project.addBuildListener(logger)
    project
  }

  def task(name: String): Task = {
    val proj = project
    val task = proj.createTask(name)
    task.init()
    task.setProject(proj)
    new Task(task)
  }

  def task(name: String, props: Seq[(String,Any)]): Task = {
    val task = this.task(name)
    task.setProperties(props)
    task
  }

  def taskNames(): scala.collection.Set[String] = {
    project.getTaskDefinitions.keySet.asScala
  }

  def tasks = {
    new Tasks(this)
  }
}

