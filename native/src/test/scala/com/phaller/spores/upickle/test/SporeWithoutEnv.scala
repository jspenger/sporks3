package com.phaller.spores.pickle.test

import scala.scalanative.reflect.annotation.EnableReflectiveInstantiation

import com.phaller.spores.Builder


@EnableReflectiveInstantiation
object SporeWithoutEnv extends Builder[Int, Int](
  (x: Int) => x + 1
)
