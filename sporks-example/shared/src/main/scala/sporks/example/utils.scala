package sporks.example

package utils

extension [T](x: T) { def tap(f: T => Unit): T = { f(x); x } }
