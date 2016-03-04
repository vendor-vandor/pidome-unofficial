/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.network.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.QueryStringDecoder;
import static io.netty.util.internal.ObjectUtil.checkNotNull;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 *
 * @author John
 */
public class Http2Util{
    /**
     * Response header sent in response to the http-&gt;http2 cleartext upgrade request.
     */
    public static final String UPGRADE_RESPONSE_HEADER = "http-to-http2-upgrade";

    /**
     * Size of the block to be read from the input stream.
     */
    private static final int BLOCK_SIZE = 1024;

    private Http2Util() { }

    /**
     * @param string the string to be converted to an integer.
     * @param defaultValue the default value
     * @return the integer value of a string or the default value, if the string is either null or empty.
     */
    public static int toInt(String string, int defaultValue) {
        if (string != null && !string.isEmpty()) {
            return Integer.parseInt(string);
        }
        return defaultValue;
    }

    /**
     * Reads an InputStream into a byte array.
     * @param input the InputStream.
     * @return a byte array representation of the InputStream.
     * @throws IOException if an I/O exception of some sort happens while reading the InputStream.
     */
    public static ByteBuf toByteBuf(InputStream input) throws IOException {
        ByteBuf buf = Unpooled.buffer();
        int n = 0;
        do {
            n = buf.writeBytes(input, BLOCK_SIZE);
        } while (n > 0);
        return buf;
    }

    /**
     * @param query the decoder of query string
     * @param key the key to lookup
     * @return the first occurrence of that key in the string parameters
     */
    public static String firstValue(QueryStringDecoder query, String key) {
        checkNotNull(query, "Query can't be null!");
        List<String> values = query.parameters().get(key);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }
}
