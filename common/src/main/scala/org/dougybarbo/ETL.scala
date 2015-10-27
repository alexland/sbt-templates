
package org.dougybarbo


import java.io.File
import java.nio.file.{
	Paths,
	Files
}
import java.nio.charset.StandardCharsets
import java.text.NumberFormat.{
	getIntegerInstance => gII
}

import util.{
	Random => RND
}
import language.postfixOps
import concurrent._
import concurrent.duration._
import concurrent.duration.{
	FiniteDuration => FinDur
}
import concurrent.Future
import concurrent.{
	ExecutionContext,
	Future
}

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream._
import akka.stream.scaladsl.{
	Flow,
	Source,
	Sink,
	FlowGraph,
	ImplicitMaterializer,
	RunnableGraph
}

// import scalaz._
// import Scalaz._



object ETLMain extends App {

	println("main compiled and running!")

	// commify printed integer values
	val formatter = java.text.NumberFormat.getIntegerInstance
	val fmt = (v:Int) => formatter.format(v)

	implicit lazy val system = ActorSystem("MyActorSystem")
	import system.dispatcher
	implicit val mat = ActorMaterializer()

	val fnx0 = () => (for (i <- (1 to 2)) yield RND.nextInt(100))
		.toVector

	val fnx1 = (q:Vector[Int]) => q match {
		case Vector(a, b) => db.RawDataLine(a, b)
   }

   val rawData = (1 to 10)
			.map(_ => fnx0())
			.map(c => fnx1(c))

	val res = (g:RunnableGraph[Future[Int]]) => g.run()

	val rawDataIn:Source[db.RawDataLine, Unit] = Source(rawData)

	val count: Flow[db.RawDataLine, Int, Unit] = Flow[db.RawDataLine].map(_ => 1)

	def counter:Sink[Int, Future[Int]] = {
		Sink.fold[Int, Int](0) { (u, v) =>
			if (u % 1000 == 0)
				println(s"completed processing line: $u")
			u + 1
   	}
  }

	val g:RunnableGraph[Future[Int]] = rawDataIn
			.via(count)
			.toMat(counter)(Keep.right)

	res(g)
		.foreach(c => println("total lines processed: " + fmt(c)))


	//----- stream graphs created from modular partial graphs -----//

	/**
	*	constructs a source from a partial flow graphs;
	*	Source exposes a special apply method that takes a fn
	*	which returns an Outlet[T]; this unconnected sink
	*	will become the sink that must be connected before
	*	this Source can run
	*	tupleSource is a member of class
	*	akka.stream.scaladsl.Source
	*	this source is a stream of Tuple2[Int,Int]
	*	has shape:
	*	SourceShape[(Int, Int)] = SourceShape(Zip.out)
	*/
	val tupleSource = Source() { implicit b =>
		import FlowGraph.Implicits._
		val zip = b.add(Zip[Int, Int]())
		val d = Seq.fill(25)(RND.nextInt(100)).toList
		def src = Source(d)
		// connect the graph
		src.filter(_ % 2 != 0) ~> zip.in0
		src.filter(_ % 2 == 0) ~> zip.in1
		// expose port
		zip.out
	}

	val firstPair:Future[(Int, Int)] = tupleSource
		.runWith(Sink.head)

	/**
	* evaluates to Option[Try[(Int, Int)]] = Some(Success((55,14)))
	*/
	println(firstPair.value)


	/**
	* tupleSource is a Source so call its runWith
	*	method with a single parameter, a Sink
	*/
	val allPairs:Future[Unit] = tupleSource
		.runWith(Sink.foreach(println(_)))

	/**
	*	same as tupleSource above but a Flow is defined
	*	instead of a Source
	*	FlowShape[Int, (Int, String)] = FlowShape(UniformFanOut.in, Zip.out)
	*/
	val tupleFlow = Flow() { implicit b =>
		import FlowGraph.Implicits._
		// create the graph elements
		val bcast = b.add(Broadcast[Int](2))
		val zmerge = b.add(Zip[Int, String]())
		// connect the elements
		bcast.out(0).map(identity) ~> zmerge.in0
		bcast.out(1).map(_.toString) ~> zmerge.in1
		// expose the ports
		(bcast.in, zmerge.out)
	}

	// define a source
	val s = Seq.fill(25)(RND.nextInt(100)).toList
	def src = Source(s)

	/**
	*	tupleFlow is a Flow so its runWith method
	* must be called with two paramaters
	*	a Source & a Sink
	*/
	tupleFlow
		.runWith(src, Sink.foreach(println(_)))
	/**
	* alternate way to create the same graph
	*/
	src
		.via(tupleFlow)
		.runWith(Sink.foreach(println(_)))






















}