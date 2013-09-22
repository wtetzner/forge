package org.bovinegenius.forge

import java.net.URLClassLoader
import org.apache.tools.ant.Project
import org.apache.tools.ant.NoBannerLogger
import org.apache.tools.ant.{Task => AntTask}
import scala.language.dynamics
import java.lang.reflect.Method
import java.beans._
import scala.collection.JavaConverters._

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

  private var _descs: Map[String,Method] = null
  private def descriptors = {
    if (_descs == null) {
      val descs = List.fromArray(Introspector.getBeanInfo(task.getClass).getPropertyDescriptors)
      _descs = makeMap(descs map { desc =>
        (desc.getName, desc.getWriteMethod)
      })
    }
    // println(_descs.keySet)
    _descs
  }

  def setProperties(props: Seq[(String,Any)]) {
    val descs = descriptors
    props.foreach { case (name, value) =>
      descs(name).invoke(task, value.asInstanceOf[Object])
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
  private val ant: Ant = apply()
  def tasks = ant.tasks
  def taskNames = ant.taskNames
  def apply(): Ant = {
    Jar("org.apache.ant", "ant", "1.9.2").load()
    new DefaultAnt()
  }
}

private class DefaultAnt extends Ant {
  private var currentProject: Project = null
  def project = {
    if (currentProject == null) {
      getClass.getClassLoader.asInstanceOf[URLClassLoader].getURLs foreach {
        url =>
      }
      currentProject = makeProject()
    }
    currentProject
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

