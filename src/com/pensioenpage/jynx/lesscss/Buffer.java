// Copyright 2007-2009, PensioenPage B.V.
package com.pensioenpage.jynx.lesscss;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
import org.apache.tools.ant.taskdefs.StreamPumper;

/**
 * An <code>ExecuteStreamHandler</code> implementation that stores all
 * output in a buffer.
 *
 * @version $Revision: 10190 $ $Date: 2009-08-25 17:49:35 +0200 (di, 25 aug 2009) $
 * @author <a href="mailto:ernst@pensioenpage.com">Ernst de Haan</a>
 */
class Buffer extends Object implements ExecuteStreamHandler {

   //-------------------------------------------------------------------------
   // Constructors
   //-------------------------------------------------------------------------

   /**
    * Constructs a new <code>Buffer</code>.
    */
   public Buffer() {
      _outBuffer = new ByteArrayOutputStream();
      _errBuffer = new ByteArrayOutputStream();
   }


   //-------------------------------------------------------------------------
   // Fields
   //-------------------------------------------------------------------------

   private final ByteArrayOutputStream _outBuffer;
   private final ByteArrayOutputStream _errBuffer;
   private Thread _outThread;
   private Thread _errThread;


   //----------------------------------------------------------------------
   // Methods
   //----------------------------------------------------------------------

   public void setProcessInputStream(OutputStream os) {
      // ignore, we don't send input to the process
   }

   public void setProcessOutputStream(InputStream is) {
      _outThread = new Thread(new StreamPumper(is, _outBuffer));
   }

   public void setProcessErrorStream(InputStream is) {
      _errThread = new Thread(new StreamPumper(is, _errBuffer));
   }

   public void start() {
      _outThread.start();
      _errThread.start();
   }

   public void stop() {
      try {
         _outThread.join();
      } catch (InterruptedException e) {
         // ignore
      }
      try {
         _errThread.join();
      } catch (InterruptedException e) {
         // ignore
      }
   }

   public void writeOutTo(OutputStream os) throws IOException {
      _outBuffer.writeTo(os);
   }

   public String getOutString() {
      return _outBuffer.toString();
   }

   public void writeErrTo(OutputStream os) throws IOException {
      _errBuffer.writeTo(os);
   }

   public String getErrString() {
      return _errBuffer.toString();
   }
}
