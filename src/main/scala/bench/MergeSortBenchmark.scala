package bench

import scala.collection.parallel.CollectionConverters.*
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import java.util.concurrent.*
import scala.util.DynamicVariable

import org.openjdk.jmh.annotations.*

@State(Scope.Benchmark)
@Fork(1, jvmArgs = Array("-Xss10m"))
class MergeSortBenchmark:
  def noPar(a: => Unit, b: => Unit) =
    a
    b

  util.Random.setSeed(451L)
  val lst: List[Int] = List.fill(5000)(util.Random.nextInt())
  val vec: Vector[Int] = lst.toVector
  val iarr: IArray[Int] = IArray.from(lst)

  @Benchmark
  def listFunctional  = ListMergeSort.msort(lst)

  @Benchmark
  def vectorFunctional  = SeqMergeSort.msort(vec)

  @Benchmark
  def iarrayFunctional = SeqMergeSort.msort(iarr)

  @Benchmark
  def iarrayMut = MutMergeSort.msort(iarr, noPar)

  @Benchmark
  def listBaseline = lst.sorted 

  @Benchmark
  def vectorBaseline = vec.sorted

  @Benchmark
  def iarrayBaseline = iarr.sorted
