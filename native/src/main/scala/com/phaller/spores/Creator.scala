package com.phaller.spores

import scala.scalanative.unsafe.exported
import scala.scalanative.reflect.Reflect

protected[spores] object Creator {
  private def loadModule(name: String) = {
    val creatorClassOpt = Reflect.lookupLoadableModuleClass(name + "$")
    if (creatorClassOpt.nonEmpty)
      creatorClassOpt.get.loadModule()
    else
      null
  }

  @exported("apply")
  def apply[E, T, R](name: String): Spore.Builder[E, T, R] =
    loadModule(name).asInstanceOf[Spore.Builder[E, T, R]]

  @exported("applyNoEnv")
  def applyNoEnv[T, R](name: String): Builder[T, R] =
    loadModule(name).asInstanceOf[Builder[T, R]]

  @exported("packedBuilder")
  def packedBuilder[T, R](name: String): PackedBuilder[T, R] =
    loadModule(name).asInstanceOf[PackedBuilder[T, R]]
}

