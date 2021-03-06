# Enums

This typeclass allows to use `parse`, `name` and `enumerate` methods on Algebraic Data Types. It can be very useful if you need to serialize and deserialize your types (in Json, in a database...).
You can [read here](https://underscore.io/blog/posts/2014/09/03/enumerations.html) why it's better to use sealed case objects than Scala Enumerations.

```scala
sealed trait Season
case object Winter extends Season
case object Spring extends Season
case object Summer extends Season
case object Fall extends Season

implicit val seasonEnumerable = new Enumerable[Season] {
  override def enumerate: Set[Season] = Sealed.values[Season] 
  //OR override def enumerate: Set[Season] = Set(Winter, Spring, Summer, Fall)
}

Enumeration.name(Winter) // "winter"
Enumeration.parse[Season]("winter") // Some(Winter)
Enumeration.enumerate[Season] // Set(Winter, Spring, Summer, Fall)
```

It is also possible to use custom namings :

```scala

implicit val seasonEnumerable = new Enumerable[Season] {
  override def enumerate = Sealed.values[Season]

  override def name(s: Season) = {
    s match {
      case Winter => "WINTER_SEASON"
      case other => super.name(other)
    }
  }
}

Enumeration.name(Winter) // "WINTER_SEASON"
Enumeration.parse[Season]("WINTER_SEASON") // Some(Winter)
```
