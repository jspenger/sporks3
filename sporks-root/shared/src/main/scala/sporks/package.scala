/** Simple and safe serialization/pickling library for closures/functions in
  * Scala 3.
  *
  * A [[Spork]] wraps a closures which is safe to serialize and deserialize.
  * Create a Spork using the factories in sporks.jvm.Spork or
  * sporks.jvm.AutoCapture, or by using the [[sporks.SporkBuilder]] or
  * [[sporks.SporkClassBuilder]].
  */
package object sporks {

  export sporks.ReadWriters.given

}
