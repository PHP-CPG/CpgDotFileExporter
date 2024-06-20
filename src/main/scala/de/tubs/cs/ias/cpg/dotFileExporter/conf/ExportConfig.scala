package de.tubs.cs.ias.cpg.dotFileExporter.conf

import de.tubs.cs.ias.cpg.dotFileExporter.conf.ExportConfigJSONParser.EdgeConfigFormat
import spray.json.JsonParser

import java.io.File
import scala.io.Source

case class EdgeConfig(color: String, style: String) {
  def getAttributeList: List[(String, String)] = List(
    ("color", color),
    ("style", style)
  )
}

case class NodeConfig(color: String,
                      shape: String,
                      attribute: String,
                      comment: List[String]) {

  def getComment: List[String] = {
    if (comment.contains("ALL")) {
      List("ALL")
    } else {
      comment
    }
  }

  def getAttributeList: List[(String, String)] = List(
    ("color", color),
    ("shape", shape)
  )
}

case class ExportConfig(nodes: Map[String, NodeConfig],
                        edges: Map[String, EdgeConfig]) {

  def getNodeType(label : String) : NodeConfig = nodes.get(label) match {
    case Some(conf) => conf
    case None => nodes("DEFAULTS")
  }

  def getEdgeConfig(label : String) : EdgeConfig = edges.get(label) match {
    case Some(conf) => conf
    case None => edges("DEFAULTS")
  }

  def getRelevantEdgeTypes: Set[String] = edges.keySet
  def getRelevantNodeTypes: Set[String] = nodes.keySet

}

object ExportConfig {

  def apply(file : File) : ExportConfig = {
    val source = Source.fromFile(file)
    try {
      JsonParser(source.mkString).convertTo[ExportConfig]
    } finally {
      source.close()
    }
  }

}
