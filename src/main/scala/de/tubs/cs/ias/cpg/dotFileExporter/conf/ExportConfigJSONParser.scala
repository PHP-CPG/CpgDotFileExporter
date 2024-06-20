package de.tubs.cs.ias.cpg.dotFileExporter.conf

import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat}
import java.nio.charset.Charset
import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets.UTF_8

object ExportConfigJSONParser extends DefaultJsonProtocol {

  def readFile(path: String, encoding: Charset = UTF_8): String = {
    val encoded = Files.readAllBytes(Paths.get(path))
    new String(encoded, encoding)
  }

  private implicit class ExtendedJsValue(value: JsValue) {
    def assumeString: JsString = value.asInstanceOf[JsString]
  }

  implicit object EdgeConfigFormat extends JsonFormat[ExportConfig] {

    private def parseNode(node: JsValue,
                          default: Option[NodeConfig]): NodeConfig = {
      val fields = node.asJsObject.fields
      NodeConfig(
        color = fields.get("color") match {
          case Some(value) => value.assumeString.value
          case None =>
            default
              .getOrElse(
                throw new RuntimeException("defaults needs to be provided"))
              .color
        },
        shape = fields.get("shape") match {
          case Some(value) => value.assumeString.value
          case None =>
            default
              .getOrElse(
                throw new RuntimeException("defaults needs to be provided"))
              .shape
        },
        attribute = fields.get("attribute") match {
          case Some(value) => value.assumeString.value
          case None =>
            default
              .getOrElse(
                throw new RuntimeException("defaults needs to be provided"))
              .attribute
        },
        comment = fields.get("comment") match {
          case Some(value) => value.assumeString.value.split(",").toList
          case None =>
            default
              .getOrElse(
                throw new RuntimeException("defaults needs to be provided"))
              .comment
        }
      )
    }

    private def parseEdge(edge: JsValue,
                          default: Option[EdgeConfig]): EdgeConfig = {
      val fields = edge.asJsObject.fields
      EdgeConfig(
        color = fields.get("color") match {
          case Some(value) => value.assumeString.value
          case None =>
            default
              .getOrElse(
                throw new RuntimeException("defaults needs to be provided"))
              .color
        },
        style = fields.get("style") match {
          case Some(value) => value.assumeString.value
          case None =>
            default
              .getOrElse(
                throw new RuntimeException("defaults needs to be provided"))
              .style
        }
      )
    }

    override def read(json: JsValue): ExportConfig = {
      val nodesDefault = parseNode(
        json.asJsObject.fields("nodes").asJsObject.fields("DEFAULTS"),
        None)
      val nodes = json.asJsObject.fields("nodes").asJsObject.fields.map {
        case (str, value) =>
          str -> parseNode(value, Some(nodesDefault))
      }
      val edgesDefault = parseEdge(
        json.asJsObject.fields("edges").asJsObject.fields("DEFAULTS"),
        None)
      val edges = json.asJsObject.fields("edges").asJsObject.fields.map {
        case (str, value) =>
          str -> parseEdge(value, Some(edgesDefault))
      }
      ExportConfig(nodes, edges)
    }

    override def write(obj: ExportConfig): JsValue =
      throw new NotImplementedError()
  }

}
