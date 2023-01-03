/*
 * StreamUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.jer.util;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;


/**
 * Stream util class
 *
 * @author patrick
 */
public final class StreamUtil {
    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static class HOLDER {
        static final StreamUtil INSTANCE = new StreamUtil();
    }


    /**
     * Constructor
     */
    private StreamUtil() {
        // NOP
    }


    /**
     * Get the instance
     *
     * @return the instance
     */
    public static StreamUtil getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Convert the InputStream to String we use the Reader.read(char[] buffer) method. We iterate until the Reader return -1 which means
     * there's no more data to read.
     *
     * @param is the input stream
     * @return the result
     * @throws IOException in case of error
     */
    public String convertStreamToStr(InputStream is) throws IOException {
        if (is == null) {
            return "";
        }

        ByteArrayOutputStream dest = new ByteArrayOutputStream();
        channelCopy(is, dest);
        return dest.toString();
    }

    
    /**
     * Convert the InputStream to String we use the Reader.read(char[] buffer) method. We iterate until the Reader return -1 which means
     * there's no more data to read.
     *
     * @param is the input stream
     * @param charsetName the charset name
     * @return the result
     * @throws IOException in case of error
     */
    public String convertStreamToStr(InputStream is, String charsetName) throws IOException {
        if (is == null) {
            return "";
        }

        ByteArrayOutputStream dest = new ByteArrayOutputStream();
        channelCopy(is, dest);
        return dest.toString(charsetName);
    }

    
    /**
     * This method copies data from the src and writes it to the dest until EOF on src.
     * 
     * @param src the source stream
     * @param dest the destination stream
     * @return the copied bytes
     * @exception IOException in case of error
     */
    public long channelCopy(InputStream src, OutputStream dest) throws IOException {
        return channelCopy(Channels.newChannel(src), Channels.newChannel(dest));
    }


    /**
     * This method copies data from the src channel and writes it to the dest channel until EOF on src. 
     * This implementation makes use of compact() on the temp buffer to pack down the data if the buffer wasn't fully drained.  
     * This may result in data copying, but minimizes system calls. It also requires a cleanup loop to make sure all the data gets sent.
     * 
     * @param src the source channel
     * @param dest the destination channel
     * @return the copied bytes
     * @exception IOException in case of error
     */
    public long channelCopy(ReadableByteChannel src, WritableByteChannel dest) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4 * 1024);
        long size = 0;

        while (src.read(buffer) != -1) {
            // prepare the buffer to be drained
            buffer.flip();

            // write to the channel, may block
            size += dest.write(buffer);

            // If partial transfer, shift remainder down
            // If buffer is empty, same as doing clear()
            buffer.compact();
        }

        // EOF will leave buffer in fill state
        buffer.flip();

        // make sure the buffer is fully drained.
        while (buffer.hasRemaining()) {
            size += dest.write(buffer);
        }
        
        return size;
    }
}