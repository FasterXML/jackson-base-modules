/*
* Copyright (c) FasterXML, LLC.
* Licensed under the Apache (Software) License, version 2.0.
*/

package perf;

import java.io.IOException;
import java.io.OutputStream;

class NopOutputStream extends OutputStream
{
    public NopOutputStream() { }

    @Override
    public void write(int b) throws IOException { }

    @Override
    public void write(byte[] b) throws IOException {  }

    @Override
    public void write(byte[] b, int offset, int len) throws IOException {  }
}
