package sporks


trait Duplicable[T] {
  def duplicate(value: T): T
}


object Duplicable {

  def duplicate[T](value: T)(using duplicable: Duplicable[T]): T = {
    duplicable.duplicate(value)
  }

  given Duplicable[Int] =     new Duplicable[Int] { def duplicate(value: Int): Int = value }
  given Duplicable[String] =  new Duplicable[String] { def duplicate(value: String): String = value }
  given Duplicable[Boolean] = new Duplicable[Boolean] { def duplicate(value: Boolean): Boolean = value }
  given Duplicable[Double] =  new Duplicable[Double] { def duplicate(value: Double): Double = value }
  given Duplicable[Float] =   new Duplicable[Float] { def duplicate(value: Float): Float = value }
  given Duplicable[Long] =    new Duplicable[Long] { def duplicate(value: Long): Long = value }
  given Duplicable[Short] =   new Duplicable[Short] { def duplicate(value: Short): Short = value }
  given Duplicable[Byte] =    new Duplicable[Byte] { def duplicate(value: Byte): Byte = value }
  given Duplicable[Char] =    new Duplicable[Char] { def duplicate(value: Char): Char = value }
  given Duplicable[Unit] =    new Duplicable[Unit] { def duplicate(value: Unit): Unit = value }

  given [T]: Duplicable[Spork[T]] = new Duplicable[Spork[T]] {
    def duplicate(value: Spork[T]): Spork[T] = value
  }

}
