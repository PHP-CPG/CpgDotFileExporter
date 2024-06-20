package de.tubs.cs.ias.cpg.dotFileExporter

import de.halcony.creator.dotfile.{DirectedEdge, DirectedGraph, Node => DotFileNode}
import de.tubs.cs.ias.cpg.dotFileExporter.conf.{EdgeConfig, ExportConfig, NodeConfig}
import overflowdb.{Edge, Graph, Node}

import scala.sys.process._
import java.io.{FileWriter, PrintWriter}
import java.nio.file.{Files, Path}
import scala.collection.mutable.{Map => MMap, Set => MSet}
import scala.jdk.CollectionConverters._

object DotFileCreatorExpansion {

  implicit class NodeDotNode(node: Node) {
    def toDotFileNode(implicit nc: NodeConfig): DotFileNode = {
      // this below is fairly yolo
      val label: String = (nc.attribute match {
        case "SPECIAL:LABEL" => node.label()
        case "SPECIAL:ID"    => node.id().toString
        case attr            => node.property(attr).asInstanceOf[String]
      }).replace("{","").replace("}","")
      //println(node.propertyKeys())
      val commentContent: String = nc.getComment
        .flatMap {
          case "ALL" =>
            node.propertyKeys().asScala.toList.flatMap { key =>
              try {
                List(s"$key=${node.property(key).toString}")
              } catch {
                case _: Throwable => List()
              }
            }
          case elem =>
            try {
              List(s"$elem=${node.property(elem).asInstanceOf[String]}")
            } catch {
              case _: Throwable => List()
            }
        }
        .mkString(",")
      val attributeList: Seq[(String, String)] = nc.getAttributeList ++ List(
        ("comment", commentContent))
      DotFileNode(node.id().toString, label, attributeList: _*)
    }
  }

  implicit class EdgeDotFile(edge: Edge) {

    def toDotFileEdge(implicit ec: EdgeConfig): DirectedEdge = DirectedEdge(
      edge.outNode().id().toString,
      edge.inNode().id().toString,
      edge.label(),
      ec.getAttributeList: _*
    )

  }

  implicit class CpgDotFileCreator(graph: Graph) {

    private val nodes: MMap[Long, DotFileNode] = MMap()
    private var edges: MSet[DirectedEdge] = MSet()

    private def addNodes(exportConfig: ExportConfig): Unit = {
      exportConfig.nodes.foreach {
        case (str, config) =>
          implicit val c: NodeConfig = config
          graph
            .nodes(str)
            .asScala
            .foreach(node => nodes.addOne((node.id(), node.toDotFileNode)))
      }
    }

    private def addEdges(exportConfig: ExportConfig): Unit = {

      exportConfig.edges.foreach {
        case (str, config) =>
          implicit val ec: EdgeConfig = config
          graph
            .edges(str)
            .asScala
            .foreach(edge => edges.addOne(edge.toDotFileEdge))
      }
    }

    private def handleMissingNodes(exportConfig: ExportConfig,
                                   ignore: Boolean): Unit = {
      val missing: Set[Long] = edges.flatMap { de: DirectedEdge =>
        val start = de.start
        val target = de.target
        val mstart = if (!nodes.contains(start.toLong)) {
          List(start.toLong)
        } else {
          List()
        }
        val mtarget = if (!nodes.contains(target.toLong)) {
          List(target.toLong)
        } else {
          List()
        }
        mstart ++ mtarget
      }.toSet
      if (ignore) {
        edges = edges.filterNot(
          edge =>
            missing.contains(edge.start.toLong) || missing.contains(
              edge.target.toLong))
      } else {
        implicit val defaultNodeConfig: NodeConfig =
          exportConfig.nodes("DEFAULTS")
        missing.foreach { id =>
          nodes.addOne((id, graph.node(id).toDotFileNode))
        }
      }
    }

    private def createDirectedGraph(
        config: ExportConfig,
        ignoreUnknownNodes: Boolean): DirectedGraph = {
      implicit val dotFile: DirectedGraph = new DirectedGraph("cpg")
      addNodes(config)
      addEdges(config)
      handleMissingNodes(config, ignoreUnknownNodes)
      nodes.foreach(elem => dotFile.addNode(elem._2))
      edges.foreach(elem => dotFile.addEdge(elem))
      dotFile
    }

    def exportToFile(path: String,
                     config: ExportConfig,
                     ignoreUnknownNodes: Boolean): Unit = {
      implicit val c: ExportConfig = config
      new PrintWriter(path) {
        write(createDirectedGraph(config, ignoreUnknownNodes).dotString)
        close()
      }
    }

    def show(dot : String = "dot", view : String = "xdg-open")(implicit conf : ExportConfig) : Unit = {
      val dotTmp: Path = Files.createTempFile("cpgdot", ".dot")
      val svgTmp: Path = Files.createTempFile("cpgdot", ".svg")
      val svgTmpWriter = new FileWriter(svgTmp.toFile)
      val dotTmpWriter = new FileWriter(dotTmp.toFile)
      try {
        dotTmpWriter.write(createDirectedGraph(conf,ignoreUnknownNodes = false).dotString)
        dotTmpWriter.flush()
        val dotOutput = s"$dot ${dotTmp.toFile.getAbsolutePath} -Tsvg".!!
        svgTmpWriter.write(dotOutput)
        svgTmpWriter.flush()
        s"$view ${svgTmp.toFile.getAbsolutePath}".!
      } finally {
        dotTmpWriter.close()
        Files.delete(dotTmp)
      }
    }

  }

}
