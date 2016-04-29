package io.github.hamsters

import scala.annotation.tailrec

sealed trait HList {
  type Plus[L <: HList] <: HList

  def ::[U](v: U): HList
}

class HNil extends HList {
  type Plus[L <: HList] = L

  def ::[T](v: T) = HCons(v, this)
}

object HNil extends HNil

case class Appender[L1 <: HList, L2 <: HList, R <: HList](fn: (L1, L2) => R) {
  def apply(l1: L1, l2: L2) = fn(l1, l2)
}

case class HCons[T, U <: HList](head: T, tail: U) extends HList {
  type Plus[L <: HList] = HCons[T, U#Plus[L]]

  def ::[V](v: V) = HCons(v, this)

  //FIXME
  //def ++[L2 <: HList](l2: L2) = HList.++(this,l2)

  def foldLeft[V](zero: V)(f: (V, Any) => V): V = {
    @tailrec
    def foldLeft0(accu: V, head: Any, tail: HList): V = {
      val newAccu = f(accu, head)
      tail match {
        case _: HNil => newAccu
        case h: HCons[_, _] => foldLeft0(newAccu, h.head, h.tail)
      }
    }
    foldLeft0(zero, head, tail)
  }

  def filter(p: (Any) => Boolean): HList = {
    @tailrec
    def filter0(accu: HList, rest: HList): HList = {
      rest match {
        case _: HNil => accu
        case r: HCons[_, _] => accu match {
          case _: HNil => if (p(r.head)) filter0(r.head :: HNil, r.tail) else filter0(accu, r.tail)
          case a: HCons[_, _] => if (p(r.head)) filter0(a +++ (r.head :: HNil), r.tail) else filter0(accu, r.tail)
        }
      }
    }
    filter0(HNil, this)
  }

  def foreach(f: (Any) => Unit): Unit = map(f)

  def map[V <: HList](f: (Any) => Any): HList = {

    @tailrec
    def map0(accu: HList, rest: HList): HList = {
      rest match {
        case _: HNil => accu
        case r: HCons[_, _] => accu match {
          case _: HNil => map0(f(r.head) :: HNil, r.tail)
          case a: HCons[_, _] => map0(a +++ (f(r.head) :: HNil), r.tail)
        }
      }
    }
    map0(HNil, this)
  }

  //FIXME use ++ instead
  private def +++[V <: HList](l2: V): HCons[T, U] = {
    def append[X <: HList](l1: HCons[T, X], l2: V): HCons[T, _] = {
      l1.tail match {
        case HNil => HCons(l1.head, l2)
        case h: HCons[T, U] => l1.head :: append(h, l2)
      }
    }
    append(this, l2) match {
      case h: HCons[T, U] => h
    }
  }

  //FIXME
  //def +[V](v: V) = HList.++(this, v :: HNil)
}

object HList {
  def ++[L1 <: HList, L2 <: HList](l1: L1, l2: L2)(implicit f: Appender[L1, L2, L1#Plus[L2]]): L1#Plus[L2] = f(l1, l2)

  implicit def nilAppender[L <: HList]: Appender[HNil, L, L] = Appender((v: HNil, l: L) => l)

  implicit def consAppender[T, L1 <: HList, L2 <: HList, R <: HList](implicit f: Appender[L1, L2, R]): Appender[HCons[T, L1], L2, HCons[T, R]] = {
    Appender[HCons[T, L1], L2, HCons[T, R]]((l1: HCons[T, L1], l2: L2) => HCons(l1.head, f(l1.tail, l2)))
  }
}