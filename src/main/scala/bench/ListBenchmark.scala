package bench

import org.openjdk.jmh.annotations._

@State(Scope.Benchmark)
class BenchmarkState:
  val bigList: List[Int] = (1 to 1000).toList

class ListBenchmark:

  @Benchmark
  def prependList(state: BenchmarkState) = 0 :: state.bigList

  @Benchmark
  def prependSeq(state: BenchmarkState) = 0 +: state.bigList

  @Benchmark
  def appendSeq(state: BenchmarkState) = state.bigList :+ 0

  @Benchmark
  def concatList(state: BenchmarkState) = state.bigList ::: state.bigList

  @Benchmark
  def concatSeq(state: BenchmarkState) = state.bigList ++ state.bigList

  @Benchmark
  def concatList3(state: BenchmarkState) = state.bigList ::: state.bigList ::: state.bigList

  @Benchmark
  def concatSeq3(state: BenchmarkState) = state.bigList ++ state.bigList ++ state.bigList