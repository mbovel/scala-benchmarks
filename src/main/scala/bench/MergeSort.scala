package bench

import scala.collection.parallel.CollectionConverters.*
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import java.util.concurrent.*
import scala.util.DynamicVariable

import org.openjdk.jmh.annotations.*
import scala.annotation.tailrec
import scala.reflect.ClassTag

val forkJoinPool = ForkJoinPool()

abstract class TaskScheduler:
  def schedule[T](body: => T): ForkJoinTask[T]
  def parallel[A, B](taskA: () => A, taskB: () => B): (A, B) =
    val right = task {
      taskB
    }
    val left = taskA()
    (left, right.join())

class DefaultTaskScheduler extends TaskScheduler:
  def schedule[T](body: => T): ForkJoinTask[T] =
    val t = new RecursiveTask[T]:
      def compute = body
    Thread.currentThread match
      case wt: ForkJoinWorkerThread =>
        t.fork()
      case _ =>
        forkJoinPool.execute(t)
    t

val scheduler =
  DynamicVariable[TaskScheduler](DefaultTaskScheduler())

def task[T](body: () => T): ForkJoinTask[T] =
  scheduler.value.schedule(body())

def parallel[A, B](taskA: () => A, taskB: () => B): (A, B) =
  scheduler.value.parallel(taskA, taskB)

/*
inline def parallel[A](inline a: () => A, inline b: () => A): (A,A) =
  (a(), b())
 */
/*
inline def parallel[A](inline a: () => A, inline b: () => A): (A,A) =
  val two = List(a, b).par.map(f => f())
  (two.head, two.tail.head)
 */

val cutoff = 2000

object ListMergeSort:
  def merge[T](xs: List[T], ys: List[T])(using ord: Ordering[T]): List[T] =
    (xs, ys) match
      case (Nil, ys) => ys
      case (xs, Nil) => xs
      case (x :: xs1, y :: ys1) =>
        if ord.lt(x, y) then x :: merge(xs1, ys)
        else y :: merge(xs, ys1)

  def msort[T](xs: List[T])(using ord: Ordering[T]): List[T] =
    val n = xs.length / 2
    if n == 0 then xs
    else
      val (fst, snd) = xs.splitAt(n)
      if n < cutoff then
        merge(msort(fst), msort(snd))
      else
        val (s1, s2) = parallel(() => msort(fst), () => msort(snd))
        merge(s1, s2)
end ListMergeSort

object SeqMergeSort:
  @scala.annotation.tailrec
  def merge[T](acc: Seq[T], xs: Seq[T], ys: Seq[T])(using ord: Ordering[T]): Seq[T] =
    if xs.isEmpty then acc ++ ys
    else if ys.isEmpty then acc ++ xs
    else
      val x = xs.head
      val y = ys.head
      if ord.lt(x, y) then
        merge(acc :+ x, xs.tail, ys)
      else
        merge(acc :+ y, xs, ys.tail)

  def msort[T](xs: Seq[T])(using ord: Ordering[T]): Seq[T] =
    val n = xs.length / 2
    if n == 0 then xs
    else
      val (fst, snd) = xs.splitAt(n)
      if n < cutoff then
        merge(Vector(), msort(fst), msort(snd))
      else
        val (s1, s2) = parallel(() => msort(fst), () => msort(snd))
        merge(Vector(), s1, s2)
end SeqMergeSort

object MutMergeSort:
  @tailrec
  def merge[T: ClassTag](
      src: Array[T],
      src1Start: Int,
      src1End: Int,
      src2Start: Int,
      src2End: Int,
      dst: Array[T],
      dstStart: Int
  )(using ord: Ordering[T]): Unit =
    if src1Start < src1End then
      if src2Start < src2End then
        if ord.lt(src(src1Start), src(src2Start)) then
          dst(dstStart) = src(src1Start)
          merge(src, src1Start + 1, src1End, src2Start, src2End, dst, dstStart + 1)
        else
          dst(dstStart) = src(src2Start)
          merge(src, src1Start, src1End, src2Start + 1, src2End, dst, dstStart + 1)
      else
        System.arraycopy(src, src1Start, dst, dstStart, src1End - src1Start)
    else
      System.arraycopy(src, src2Start, dst, dstStart, src2End - src2Start)

  def msort[T: ClassTag](xs: IArray[T], par: (=> Unit, => Unit) => Unit)(using
      ord: Ordering[T]
  ): IArray[T] =
    def sort(src: Array[T], dst: Array[T], from: Int, until: Int, depth: Int): Unit =
      if until - from > 1 then
        val mid = (from + until) / 2
        par(sort(dst, src, from, mid, depth + 1), sort(dst, src, mid, until, depth + 1))
        merge(src, from, mid, mid, until, dst, from)
      else ()

    val tmp1 = Array.from(xs)
    val tmp2 = Array.from(xs)
    sort(tmp1, tmp2, 0, xs.length, 0)
    IArray.from(tmp2)
end MutMergeSort
