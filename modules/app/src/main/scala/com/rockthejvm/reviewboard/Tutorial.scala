package com.rockthejvm.reviewboard

import com.raquo.airstream.core.{EventStream, Observer}
import com.raquo.airstream.eventbus.EventBus
import com.raquo.airstream.ownership.{OneTimeOwner, Subscription}
import com.raquo.airstream.state.{OwnedSignal, Var}
import com.raquo.airstream.timing.PeriodicStream
import com.raquo.laminar.api.L.{`type`, button, child, div, onClick, p, render, span, styleAttr, given}
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.{HTMLDivElement, console, document}

import scala.util.Try

object Tutorial {
  val staticContent: ReactiveHtmlElement[HTMLDivElement] = div(
    // modifiers
    styleAttr := "color: red",
    p("This is an app"),
    p("rock the JVM but also JS")
  )

  // EventStream - produce values of the same type
  val ticks: PeriodicStream[Int] = EventStream.periodic(1000) // EventStream[Int]
  // subscription - Airstream
  // ownership
  val subscription: Subscription = ticks.addObserver(new Observer[Int] {
    override def onNext(nextValue: Int): Unit = console.log(s"Ticks: ${nextValue}")

    override def onError(err: Throwable): Unit = ()

    override def onTry(nextValue: Try[Int]): Unit = ()
  })(new OneTimeOwner(() => ()))

  scala.scalajs.js.timers.setTimeout(10_000)(subscription.kill())

  val timeUpdated: ReactiveHtmlElement[HTMLDivElement] =
    div(
      span("Time since loaded: "),
      child <-- ticks.map(number => s"$number seconds")
    )

  // EventBus - like EventStreams, but you can push new elements to the stream
  val clickEvents: EventBus[Int] = EventBus[Int]()
  val clickUpdated: ReactiveHtmlElement[HTMLDivElement] = div(
    span("Clicks since loaded: "),
    child <-- clickEvents.events.scanLeft(0)(_ + _).map(number => s"$number clicks"),
    button(
      `type`    := "button",
      styleAttr := "display: block",
      onClick.map(_ => 1) --> clickEvents,
      "Add a Click"
    )
  )

  // Signal - simlar to EventStreams, but they have a current value (state)
  // can be inspected for the current state (if Laminar/ Airstream knows that it has an owner)
  val countSignal: OwnedSignal[Int] = clickEvents.events.scanLeft(0)(_ + _).observe(new OneTimeOwner(() => ()))
  val queryEvents                   = EventBus[Unit]()

  val clicksQueried = div(
    span("Clicks since loaded: "),
    child <-- queryEvents.events.map(_ => countSignal.now()),
    button(
      `type`    := "button",
      styleAttr := "display: block",
      onClick.map(_ => 1) --> clickEvents,
      "Add a Click"
    ),
    button(
      `type`    := "button",
      styleAttr := "display: block",
      onClick.mapTo(()) --> queryEvents,
      "Query Clicks"
    )
  )

  // Var - reactive variables
  val countVar: Var[Int] = Var[Int](0)
  val clicksVar = div(
    span("Clicks so far: "),
    child <-- countVar.signal.map(_.toString),
    button(
      `type`    := "button",
      styleAttr := "display: block",
      //      onClick --> countVar.updater((current, event) => current + 1),
      //      onClick --> countVar.writer.contramap(event => countVar.now() + 1),
      onClick --> (_ => countVar.set(countVar.now() + 1)),
      "Add a Click"
    )
  )

  /**
   *            no state     | with state
   * ------------------------+---------------
   *  read      EventStream  | Signal
   *  -----------------------+ --------------
   *  write     EventBus     | Var
   */
}
