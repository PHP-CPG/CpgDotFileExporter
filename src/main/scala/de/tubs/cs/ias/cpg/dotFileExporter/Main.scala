package de.tubs.cs.ias.cpg.dotFileExporter

import DotFileCreatorExpansion.CpgDotFileCreator
import de.tubs.cs.ias.cpg.dotFileExporter.conf.{ExportConfig, ExportConfigJSONParser}
import de.tubs.cs.ias.cpg.dotFileExporter.conf.ExportConfigJSONParser._
import io.shiftleft.codepropertygraph.Cpg
import spray.json.JsonParser
import scala.sys.exit

object Main {

  def main(argvs: Array[String]): Unit = {
    val parser = de.halcony.argparse
      .Parser("CPG Dotfile Exporter", "exports a given cpg to dot format")
      .addPositional("config", "the config to be used")
      .addPositional("cpg", "the path to the cpg binary")
      .addPositional("dot", "the output file")
      .addFlag("ignoreUnknownNodes", "i", "ignore")
    try {
      val pargs = parser.parse(argvs)
      val exportConf: ExportConfig =
        JsonParser(
          ExportConfigJSONParser.readFile(pargs.getValue[String]("config")))
          .convertTo[ExportConfig]
      val cpg: Cpg = Cpg.withStorage(pargs.getValue[String]("cpg"))
      val dot = pargs.getValue[String]("dot")
      println(pargs.getValue[Boolean]("ignoreUnknownNodes"))
      cpg.graph.exportToFile(dot,
                       exportConf,
                       pargs.getValue[Boolean]("ignoreUnknownNodes"))
    } catch {
      case _: de.halcony.argparse.ParsingException => exit(1)
    }
  }

}
