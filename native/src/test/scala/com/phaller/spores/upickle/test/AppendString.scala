package com.phaller.spores.pickle.test

import scala.scalanative.reflect.annotation.EnableReflectiveInstantiation

import com.phaller.spores.Spore


@EnableReflectiveInstantiation
object AppendString extends
    Spore.Builder[String, List[String], List[String]](
  env => strings => strings ::: List(env)
)
