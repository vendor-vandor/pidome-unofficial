/*
 * Copyright 2013 John Sirach <john.sirach@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pidome.client.loggers;


import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.ConsoleAppender.Target;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.OutputStreamManager;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;


/**
 *
 * @author John Sirach
 */
@Plugin(name = "ConsoleWriter", category = "Core", elementType = "appender", printObject = true)
public final class ConsoleWriter <T extends Serializable> extends AbstractOutputStreamAppender {
 
    private static ConsoleManagerFactory factory = new ConsoleManagerFactory();
    
    static Layout layout;
    
    private ConsoleWriter(final String name, 
                          final Layout<T> layout, 
                          final Filter filter,
                          final OutputStreamManager manager,
                          final boolean handleExceptions) {
        super(name, layout, filter, handleExceptions, true, manager);
        this.layout = layout;
    }
 
    /**
     * Create a Console Appender.
     * @param <S>
     * @param layout
     * @param filter
     * @param t
     * @param name
     * @return The appender
     */
    @PluginFactory
    public static <S extends Serializable> ConsoleWriter<S> createAppender(@PluginAttribute("name") String name,
                                              @PluginAttribute("ignoreExceptions") String ignore,
                                              @PluginElement("Layout") Layout layout,
                                              @PluginElement("Filters") Filter filter) {
        if (layout == null) {
            @SuppressWarnings({"unchecked"})
            Layout<S> l = (Layout<S>)PatternLayout.createLayout(null, null, null, null, null);
            layout = l;
        }
        final Target target = Target.SYSTEM_OUT;
        return new ConsoleWriter<>("ConsoleWriter", layout, filter, getManager(target, layout), false);
    }
    
    private static OutputStreamManager getManager(final Target target, final Layout layout) {
        final String type = target.name();
        final OutputStream os = getOutputStream(target);
        return StreamManager.getManager(target.name(), new FactoryData(os, type), factory);
    }
    

    private static OutputStream  getOutputStream(Target target){
        String encoding = Charset.defaultCharset().name();
        try {
            return new PrintStream(new ConsoleOutputStream(), true, encoding);
        } catch (UnsupportedEncodingException ex) { // a really big oops?
            throw new IllegalStateException("Unsupported default encoding " + encoding, ex);
        }
    }
    
    /**
     * Data to pass to factory method.
     */
    private static class FactoryData {
        private final OutputStream os;
        private final String type;
        
        /**
         * Constructor.
         * @param os The OutputStream.
         * @param type The name of the target.
         */
        public FactoryData(final OutputStream os, final String type) {
            this.os = os;
            this.type = type;
        }
    }

    
    /**
     * Factory to create the Appender.
     */
    private static class ConsoleManagerFactory implements ManagerFactory<OutputStreamManager, FactoryData> {

        /**
         * Create an OutputStreamManager.
         * @param name The name of the entity to manage.
         * @param data The data required to create the entity.
         * @return The OutputStreamManager
         */
        @Override
        public OutputStreamManager createManager(final String name, final FactoryData data) {
            return new StreamManager(data.os, data.type);
            
            ///// een eigen outputstreammanager maken en deze implementeren
            
        }
    }
    
    private static class StreamManager extends OutputStreamManager {
        protected StreamManager(OutputStream os, String streamName) {
            super(os,streamName, layout);
        }
    }
    
    /**
     * An implementation of OutputStream that redirects to the
     * current System.err.
     *
     */
    private static class ConsoleOutputStream extends OutputStream {
        
        public ConsoleOutputStream() {}

        @Override
        public void close() {
            //// Not used
        }

        @Override
        public void flush() {
            //// Not used
        }

        @Override
        public void write(final byte[] b) throws IOException {
            //System.err.write(b);
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            //System.err.write(b, off, len);
            Console.addLog(new String(b));
        }

        @Override
        public void write(final int b) {
            //System.err.write(b);
        }
    }
    
}