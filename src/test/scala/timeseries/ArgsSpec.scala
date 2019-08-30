package timeseries

import org.scalatest._

class ArgsSpec extends WordSpec with Matchers {

  "Args" when {

    val testFile = getClass.getResource("/data_scala_short.txt")

    "input file exists and time window is provided" should {
      "parse correctly" in {
        val args = List(testFile.getPath, "10")
        val result = Args.parse(args)
        result.timeWindow shouldEqual 10
        result.lines.size shouldEqual 20
        result.runAsync shouldEqual false
      }
    }

    "input file exists, time window is provided, and async option is provided" should {
      "parse correctly" in {
        val args = List(testFile.getPath, "10", "async")
        val result = Args.parse(args)
        result.timeWindow shouldEqual 10
        result.lines.size shouldEqual 20
        result.runAsync shouldEqual true
      }
    }

    "input file exists and time window is not provided" should {
      "parse correctly with a default time window of 60" in {
        val args = List(testFile.getPath)
        val result = Args.parse(args)
        result.timeWindow shouldEqual 60
        result.lines.size shouldEqual 20
        result.runAsync shouldEqual false
      }
    }

    "input file exists but time window is not provided as an Int" should {
      "parse file correctly and use default time window of 60" in {
        val args = List(testFile.getPath)
        val result = Args.parse(args)
        result.timeWindow shouldEqual 60
        result.lines.size shouldEqual 20
        result.runAsync shouldEqual false
      }
    }

    "input file does not exist" should {
      "throw an exception" in {
        val args = List("/file_does_not_exist.txt")
        an [IllegalArgumentException] should be thrownBy Args.parse(args)
      }
    }
  }
}
