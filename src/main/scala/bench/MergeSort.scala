package bench

import scala.collection.parallel.CollectionConverters.*
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import java.util.concurrent.*
import scala.util.DynamicVariable

import org.openjdk.jmh.annotations.*

val forkJoinPool = ForkJoinPool()

val cutoff = 2000

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

def mergeSort[A](list: Seq[A])(using ord: Ordering[A]): Seq[A] =
  def merge(l: Seq[A], r: Seq[A]): Seq[A] =
    if l.isEmpty then r
    else if r.isEmpty then l
    else if ord.lt(l.head, r.head) then
      l.head +: merge(l.tail, r)
    else
      r.head +: merge(l, r.tail)

  if list.length <= 1 then list
  else
    val (left, right) = list.splitAt(list.length / 2)
    merge(mergeSort(left), mergeSort(right))
