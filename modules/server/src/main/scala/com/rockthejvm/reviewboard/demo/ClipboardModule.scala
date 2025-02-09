package com.rockthejvm.reviewboard.demo

// https://github.com/agilesteel/spells/blob/master/src/main/scala/spells/ClipboardModule.scala
object ClipboardModule {

  import java.awt.Toolkit
  import java.awt.datatransfer.{DataFlavor, StringSelection}
  import java.util.logging.Level
  import scala.util.Try

  /** Utility object, which provides an API to write and read from the operating systems' clipboard. */
  object Clipboard {
    Vector(
      "java.awt",
      "java.awt.event",
      "java.awt.focus",
      "java.awt.mixing",
      "sun.awt",
      "sun.awt.windows",
      "sun.awt.X11"
    ) foreach disableLogging

    private def disableLogging(loggerName: String): Unit =
      java.util.logging.Logger getLogger loggerName setLevel Level.OFF

    private lazy val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard

    /**
     * Writes the content `String` to the operating systems' clipboard.
     *
     * @param content the `String` to write
     * @return an instance of `Try[Unit]`
     */
    final def writeString(content: String): Try[Unit] =
      Try {
        val contentAsStringSelection = new StringSelection(content)
        clipboard.setContents(
          contentAsStringSelection,
          contentAsStringSelection
        )
      }

    /**
     * Reads the content of the operating systems' clipboard.
     *
     * @return the `String` content of the operating system's clipboard wrapped in a `Try`
     */
    final def readString: Try[String] =
      Try {
        clipboard
          .getContents(null)
          .getTransferData(DataFlavor.stringFlavor)
          .asInstanceOf[String]
      }
  }

}
