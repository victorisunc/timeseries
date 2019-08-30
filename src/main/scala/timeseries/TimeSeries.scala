package timeseries

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

case class Measurement(ts: Long, priceRatio: Double)

case class TimeSeriesRow(ts: Long,
                         priceRatio: Double,
                         nMeasurements: Int,
                         rollingSum: Double,
                         minPriceRatio: Double,
                         maxPriceRatio: Double) {
  override def toString = {
    s"$ts ${fmt(priceRatio)} $nMeasurements ${fmt(rollingSum)} ${fmt(minPriceRatio)} ${fmt(maxPriceRatio)}"
  }

  private def fmt(d: Double, scale: Int = 5): Double = {
    BigDecimal(d).setScale(scale, BigDecimal.RoundingMode.HALF_UP).toDouble
  }
}

object TimeSeriesAnalyzer extends TimeSeriesAnalyzer

trait TimeSeriesAnalyzer {

  val InputRowPattern = "(\\d+)\\s+(.*)".r

  def processAsync(lines: Iterator[String], timeWindow: Int): Future[Iterator[TimeSeriesRow]] = {
    def window(measurements: Seq[Measurement], measurement: Measurement) = {
      measurements.dropWhile(_.ts < measurement.ts - timeWindow) :+ measurement
    }

    Future.sequence(lines.flatMap {
      case InputRowPattern(ts, priceRatio) => Some(Measurement(ts.toLong, priceRatio.toDouble))
      case badLine =>
        println(s"Error parsing line: $badLine. Skipping it.")
        None
    }.scanLeft(Seq[Measurement]())(window)
      .drop(1)
      .map(x => Future(analyzeWindow(x))))
  }

  def outputAsync(fRows: Future[Iterator[TimeSeriesRow]]): Future[Unit] = {
    fRows.map(_.map(println)).map(_.mkString("\n"))
  }

  def process(lines: Iterator[String], timeWindow: Int): Iterator[TimeSeriesRow] = {
    def window(measurements: Seq[Measurement], measurement: Measurement) = {
      measurements.dropWhile(_.ts < measurement.ts - timeWindow) :+ measurement
    }

    lines.flatMap {
      case InputRowPattern(ts, priceRatio) => Some(Measurement(ts.toLong, priceRatio.toDouble))
      case badLine =>
        println(s"Error parsing line: $badLine. Skipping it.")
        None
    }.scanLeft(Seq[Measurement]())(window)
      .drop(1)
      .map(analyzeWindow)
  }

  def output(rows: Iterator[TimeSeriesRow]): Unit = {
    rows.map(_.toString).foreach(println(_))
  }

  def analyzeWindow(window: Seq[Measurement]): TimeSeriesRow = {
    window.foldLeft(TimeSeriesRow(0, 0, 0, 0, 0, 0)) { (first, second) =>
      TimeSeriesRow(
        ts = second.ts,
        priceRatio = second.priceRatio,
        nMeasurements = first.nMeasurements + 1,
        rollingSum = first.rollingSum + second.priceRatio,
        minPriceRatio = if (first.minPriceRatio == 0D) second.priceRatio else Math.min(first.minPriceRatio, second.priceRatio),
        maxPriceRatio = Math.max(first.maxPriceRatio, second.priceRatio)
      )
    }
  }

  def printHeader(): Unit = {
    println(s"T          V       N RS      MinV    MaxV")
    println("-" * 45)
  }
}

object TimeSeries extends App with TimeSeriesAnalyzer {

  def runAsync(args: Args): Future[Unit] = {
    printHeader()
    outputAsync(processAsync(args.lines, args.timeWindow))
  }

  def run(args: Args): Unit = {
    printHeader()
    output(process(args.lines, args.timeWindow))
  }

  val inputArgs = Args.parse(args.toList)

  if (inputArgs.runAsync) {
    Await.result(runAsync(inputArgs), Duration.Inf)
  } else {
    run(inputArgs)
  }
}


