package timeseries

import java.nio.file.Paths

import scala.io.Source
import scala.util.Try

import Args._

case class Args(lines: Iterator[String], timeWindow: Int = DEFAULT_TIMEWINDOW, runAsync: Boolean = false)

object Args {

  val DEFAULT_TIMEWINDOW = 60

  def parse(args: List[String]): Args = {
    args match {
      case inputFilePath :: timeWindowStr :: async :: Nil =>
        Args(getLines(inputFilePath), parseTimeWindow(timeWindowStr), async == "async")
      case inputFilePath :: timeWindowStr :: Nil =>
        Args(getLines(inputFilePath), parseTimeWindow(timeWindowStr))
      case inputFilePath :: Nil =>
        Args(getLines(inputFilePath))
      case _ =>
        throw new IllegalArgumentException("Usage: <path of file to analyse> <optional: time window in secs> <optional: async>")
    }
  }

  private def getLines(filePath: String): Iterator[String] = {
    val inputFile = Paths.get(filePath).toFile
    if (inputFile.canRead) {
      Source.fromFile(inputFile).getLines
    } else {
      throw new IllegalArgumentException(s"Can not read $filePath")
    }
  }

  private def parseTimeWindow(s: String): Int = {
    if (Try(s.toInt).isSuccess) s.toInt else DEFAULT_TIMEWINDOW
  }
}

