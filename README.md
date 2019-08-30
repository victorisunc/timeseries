Time Series Analyzer
====================
Computes local information within a rolling time window of length T, such
as:

● number of measurements in a window

● minimum of measurements in a window

● maximumof measurements in a window

● rolling sum

File Input Format:
------------------

`<timestamp in sec> <price ratio>`

```
1355270609 1.80215
1355270621 1.80185
1355270646 1.80195
...
```

Format of Output
-----------

Table where each row represents analysis for one position of
rolling window over time-series.

● T—number of seconds since beginning of epoch at which rolling window ends.

● V— measurement of price ratio attime T.

● N — number of measurements in the window.

● RS — a rolling sum of measurements in the window.

● MinV— minimum price ratio in the window.

● MaxV—maximum price ratio in the window.


```
T          V       N RS      MinV    MaxV
---------------------------------------------
1355270609 1.80215 1 1.80215 1.80215 1.80215
1355270621 1.80185 2 3.604 1.80185 1.80215
1355270646 1.80195 3 5.40595 1.80185 1.80215
...
1355271507 1.80265 3 5.40835 1.80265 1.80295
1355271562 1.80275 2 3.6054 1.80265 1.80275
1355271588 1.80295 2 3.6057 1.80275 1.80295
```


### Considerations
 - Assumes that each row in the input file has only numbers for the first column and a number string as second column
 - Assumes that there could be an error in any line of the input file,
 in that case the program skips the line and continues instead of exiting with an exception
 - **Bonus**: Implemented two versions of the processor, synchronous, and another where
  the time window computation returns a Future. The Future based processor can be called with an "async" as third argument.
  The Future based approach is, in general, given a large enough input file, 2x as faster than the synchronous version
 - **Bonus**: Made TimeWindow variable passable as an argument to the CLI.


### Test
```sh
sbt test
```
### Test output:
```aidl
[info] ArgsSpec:
[info] Args
[info]   when input file exists and time window is provided
[info]   - should parse correctly
[info]   when input file exists, time window is provided, and async option is provided
[info]   - should parse correctly
[info]   when input file exists and time window is not provided
[info]   - should parse correctly with a default time window of 60
[info]   when input file exists but time window is not provided as an Int
[info]   - should parse file correctly and use default time window of 60
[info]   when input file does not exist
[info]   - should throw an exception
[info] TimeSeriesSpec:
[info] TimeSeries
[info]   when analyzing window given zero measurements
[info]   - should return empty TimeSeriesRow
[info]   when analyzing window given one measurement
[info]   - should return one TimeSeriesRow
[info]   when analyzing window given increasing measurements per sec
[info]   - should return calculated TimeSeriesRow at 60th second
[info]   when processAsync given test input file
[info]   - should eventually return a table containing the time series
[info] Run completed in 413 milliseconds.
[info] Total number of tests run: 9
[info] Suites: completed 2, aborted 0
[info] Tests: succeeded 9, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
```

### Run
```
$ sbt 'run <path of file to analyse> <optional: time window in secs> <optional: async>'
```

```sh
$ sbt 'run data_scala.txt'
```

### Run (Async)
```sh
$ sbt 'run data_scala.txt 60 async'
```

## Improvements
 - Use Scopt: Simple Scala command line options parsing
 - Use Akka Streams to read CSV as Source, time window processor as Flow,
 and console output as Sink
 - Use Spark's built in time series analyser
