
package org.dougybarbo.persist

object Persist {

	println("module persist compiled and loaded")

	trait Record {
		def c1: Int
		def c2: Int
	}

	final case class RawDataLine(
		c1:Int,
		c2:Int
	) extends Record

	final case class RawDataLineExt(
		c1:Int,
		c2:Int,
		c3:Int
	) extends Record

	final case class DbRow(
		c1: Int,
		c2: Int,
		c3: Int
	) extends Record

}
