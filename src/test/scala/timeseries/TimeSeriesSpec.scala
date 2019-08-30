package timeseries

import org.scalatest._
import scala.io.Source
import Args.DEFAULT_TIMEWINDOW

class TimeSeriesSpec extends AsyncWordSpec with Matchers {

  "TimeSeries" when {

    val testFile = getClass.getResource("/data_scala_short.txt")
    val analyzer = TimeSeriesAnalyzer

    "analyzing window given zero measurements" should {
      "return empty TimeSeriesRow" in {
        analyzer.analyzeWindow(Seq.empty) shouldEqual TimeSeriesRow(0, 0, 0, 0, 0, 0)
      }
    }

    "analyzing window given one measurement" should {
      "return one TimeSeriesRow" in {
        val result = analyzer.analyzeWindow(Seq(Measurement(1, 1)))
        result shouldEqual TimeSeriesRow(1, 1, 1, 1, 1, 1)
      }
    }

    "analyzing window given increasing measurements per sec" should {
      "return calculated TimeSeriesRow at 60th second" in {
        val oneSecondTicks = (1 to DEFAULT_TIMEWINDOW) map {
          sec => Measurement(sec, sec)
        }
        val expectedRollingSum = (1 to DEFAULT_TIMEWINDOW).sum
        val result = analyzer.analyzeWindow(oneSecondTicks)
        result shouldEqual TimeSeriesRow(60, 60, 60, expectedRollingSum, 1, 60)
      }
    }

    "processAsync given test input file" should {
      "eventually return a table containing the time series" in {
        val expectedOutput =
          """|1355270609 1.80215 1 1.80215 1.80215 1.80215
             |1355270621 1.80185 2 3.604 1.80185 1.80215
             |1355270646 1.80195 3 5.40595 1.80185 1.80215
             |1355270702 1.80225 2 3.6042 1.80195 1.80225
             |1355270702 1.80215 3 5.40635 1.80195 1.80225
             |1355270829 1.80235 1 1.80235 1.80235 1.80235
             |1355270854 1.80205 2 3.6044 1.80205 1.80235
             |1355270868 1.80225 3 5.40665 1.80205 1.80235
             |1355271000 1.80245 1 1.80245 1.80245 1.80245
             |1355271023 1.80285 2 3.6053 1.80245 1.80285
             |1355271024 1.80275 3 5.40805 1.80245 1.80285
             |1355271026 1.80285 4 7.2109 1.80245 1.80285
             |1355271027 1.80265 5 9.01355 1.80245 1.80285
             |1355271056 1.80275 6 10.8163 1.80245 1.80285
             |1355271428 1.80265 1 1.80265 1.80265 1.80265
             |1355271466 1.80275 2 3.6054 1.80265 1.80275
             |1355271471 1.80295 3 5.40835 1.80265 1.80295
             |1355271507 1.80265 3 5.40835 1.80265 1.80295
             |1355271562 1.80275 2 3.6054 1.80265 1.80275
             |1355271588 1.80295 2 3.6057 1.80275 1.80295""".stripMargin.split("\n")
        val futureAsserts = analyzer.processAsync(Source.fromFile(testFile.getFile).getLines, DEFAULT_TIMEWINDOW).map {
          timeSeries =>
            timeSeries.toList.zip(expectedOutput) map {
              case (timeSeriesRow, expectedLine) => timeSeriesRow.toString shouldEqual expectedLine
            }
        }
        futureAsserts.map { asserts => all(asserts) shouldEqual Succeeded }
      }
    }

  }
}
