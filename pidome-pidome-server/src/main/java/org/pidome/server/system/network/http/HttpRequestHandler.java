/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.network.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpData;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.pidome.server.services.clients.remoteclient.RemoteClient;
import org.pidome.server.services.clients.remoteclient.RemoteClientException;
import org.pidome.server.services.clients.remoteclient.RemoteClientInterface;
import org.pidome.server.services.clients.remoteclient.RemoteClientsConnectionPool;
import org.pidome.server.services.clients.socketservice.SocketServiceClient;
import static org.pidome.server.system.network.http.HttpClientHandler.LOG;
import org.pidome.server.services.http.WebRenderInterface;
import org.pidome.server.services.http.Webservice404Exception;
import org.pidome.server.services.http.rpc.PiDomeJSONRPCEndpoint;

/**
 *
 * @author John
 */
public final class HttpRequestHandler {

    /**
     * List with random quotes used in error responses.
     */
    protected static List<String> someRandomQuote = new ArrayList(Arrays.asList(
            "The knowledge of anything, since all things have causes, is not acquired or complete unless it is known by its causes. - Avicenna",
            "Alas, it is my curse. With great intelligence comes great annoyance. - Oscar Wilde",
            "If at first you don't succeed... maybe skydiving isn't for you - Someone who didn't failed the first time",
            "We tend to break stuff, it's not failure, it's learning - John Sirach",
            "Failure is the condiment that gives success its flavor. - Truman Capote",
            "Remember that failure is an event, not a person. - Zig Ziglar",
            "Whoops, detour. - John Sirach"));

    /**
     * Requedt parser constructor.
     */
    private HttpRequestHandler() {

    }

    /**
     * The process part for http1/1.1
     *
     * @param chc
     * @param request
     * @param writer
     */
    protected static void processManagement(ChannelHandlerContext chc, FullHttpRequest request, HttpRequestWriterInterface writer) {
        processManagement(chc, request, writer, null);
    }

    /**
     * Process the request made for http2
     *
     * @param chc The channel context.
     * @param request The url request.
     * @param writer The output writer of type HttpRequestWriterInterface.
     * @param streamId The stream Id in case of http2, when http1 leave null.
     */
    protected static void processManagement(ChannelHandlerContext chc, FullHttpRequest request, HttpRequestWriterInterface writer, String streamId) {
        String plainIp = getPlainIp(chc.channel().remoteAddress());
        String localIp = getPlainIp(chc.channel().localAddress());
        int localPort = getPort(chc.channel().localAddress());
        try {
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
            String fileRequest = queryStringDecoder.path();

            if (fileRequest.equals("/")) {
                fileRequest = "/index.html";
            } else if (fileRequest.endsWith("/")) {
                fileRequest = fileRequest + "index.html";
            }

            String nakedfile = fileRequest.substring(1, fileRequest.lastIndexOf("."));
            String fileType = fileRequest.substring(fileRequest.lastIndexOf(".") + 1);

            String loginError = "";
            RemoteClientInterface client = null;
            RemoteClient remoteClient = null;

            WebRenderInterface renderClass = null;
            
            try {
                Set<Cookie> cookie = cookieParser(request);
                Map<RemoteClientInterface, RemoteClient> clientSet = getAuthorizedClient(request, plainIp, (cookie.isEmpty() ? "" : ((Cookie) cookie.toArray()[0]).getValue()), fileRequest);
                client = clientSet.keySet().iterator().next();
                remoteClient = clientSet.get(client);
            } catch (Exception ex) {
                if(ex instanceof HttpClientNotAuthorizedException){
                    LOG.error("Not authorized at {}", plainIp, request.uri());
                    loginError = "Not authorized or bad username/password";
                } else if ( ex instanceof HttpClientLoggedInOnOtherLocationException){
                    LOG.error("Not authorized at {} (Logged in on other location: {}!)", plainIp, ex.getMessage());
                    loginError = "Client seems to be already logged in on another location";
                } else {
                    LOG.error("Not authorized at: {} (Cookie problem? ({}))", ex, ex.getMessage(), ex);
                    loginError = "Problem getting authentication data, refer to log file";
                }
                if (!request.uri().equals("/jsonrpc.json")) {
                    fileType = "xhtml";
                    nakedfile = "login";
                    fileRequest = "/login.xhtml";
                }
            }
            
            if (!fileType.isEmpty()) {
                switch (fileType) {
                    case "xhtml":
                    case "json":
                    case "upload":
                    case "xml":
                    case "/":
                        if (request.uri().startsWith("/jsonrpc.json")) {
                            renderClass = getJSONRPCRenderer(request);
                        } else if (request.uri().startsWith("/xmlapi/")) {
                            /// This is a temp solution until the xml output has been transfered to the json rpc api.
                            Class classToLoad = Class.forName(HttpServer.getXMLClassesRoot() + nakedfile.replace("xmlapi/", ".Webclient_"));
                            renderClass = (WebRenderInterface) classToLoad.getConstructor().newInstance();
                        } else {
                            Class classToLoad = Class.forName(HttpServer.getDocumentClassRoot() + nakedfile.replace("/", ".Webclient_"));
                            renderClass = (WebRenderInterface) classToLoad.getConstructor().newInstance();
                        }
                        renderClass.setHostData(localIp, localPort, plainIp);
                        renderClass.setRequestData(queryStringDecoder.parameters());
                        Map<String, String> postData = new HashMap<>();
                        Map<String, byte[]> fileMap = new HashMap<>();
                        if (request.method().equals(HttpMethod.POST)) {
                            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);
                            decoder.setDiscardThreshold(0);
                            if (request instanceof HttpContent) {
                                HttpContent chunk = (HttpContent) request;
                                decoder.offer(chunk);
                                try {
                                    while (decoder.hasNext()) {
                                        InterfaceHttpData data = decoder.next();
                                        if (data != null) {
                                            if (data.getHttpDataType().equals(InterfaceHttpData.HttpDataType.Attribute)) {
                                                postData.put(data.getName(), ((HttpData) data).getString());
                                            } else if (data.getHttpDataType().equals(InterfaceHttpData.HttpDataType.FileUpload)) {
                                                FileUpload fileUpload = (FileUpload) data;
                                                fileMap.put(fileUpload.getFilename(), fileUpload.get());
                                            }
                                        }
                                    }
                                } catch (HttpPostRequestDecoder.EndOfDataDecoderException e1) {

                                }
                                if (chunk instanceof LastHttpContent) {
                                    decoder.destroy();
                                    decoder = null;
                                }
                            }
                        }
                        renderClass.setPostData(postData);
                        renderClass.setFileData(fileMap);
                        renderClass.setLoginData(client, remoteClient, loginError);
                        renderClass.collect();
                        renderClass.setTemplate(fileRequest);

                        ByteArrayOutputStream outputWriter = new ByteArrayOutputStream();
                        renderClass.setOutputStream(outputWriter);

                        String output = renderClass.render();
                        outputWriter.close();
                        writer.writeResponse(chc, HttpResponseStatus.OK, output.getBytes(), fileType, streamId, false);
                        break;
                    default:
                        sendStaticFile(chc, writer, fileRequest, queryStringDecoder, streamId);
                    break;
                }
            }
        } catch (ClassNotFoundException | Webservice404Exception ex) {
            LOG.warn("404 error: {} - {} (by {})", ex.getMessage(), ex, plainIp);
            try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw);) {
                ex.printStackTrace(pw);
                writer.writeResponse(chc, HttpResponseStatus.NOT_FOUND, return404Error().getBytes(), "html", streamId, false);
            } catch (IOException exWriters) {
                LOG.error("Problem outputting 404 error: {}", exWriters.getMessage(), exWriters);
            }
        } catch (Exception ex) {
            LOG.error("500 error: {}", ex.getLocalizedMessage(), ex);
            try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw);) {
                ex.printStackTrace(pw);
                String errorOutput = sw.toString() + "\n\n" + getRandQuote();
                writer.writeResponse(chc, HttpResponseStatus.INTERNAL_SERVER_ERROR, (errorOutput + "<br/><br/><p>" + getRandQuote() + "</p>").getBytes(), "", streamId, false);
            } catch (IOException exWriters) {
                LOG.error("Problem outputting 500 error: {}", exWriters.getMessage(), exWriters);
            }
        }
    }

    /**
     * Parses a cookie.
     * When no valid cookie is found an empty set is returned.
     * @param request the FullHttpRequest from netty.
     * @return Cookie set.
     */
    private static Set<Cookie> cookieParser(FullHttpRequest request) {
        Set<Cookie> cookie = null;
        try {
            cookie = CookieDecoder.decode(request.headers().get(HttpHeaders.Names.COOKIE));
        } catch (NullPointerException ex){
            cookie = new HashSet<>();
        }
        return cookie;
    }

    /**
     * Returns a JSON rpc render request.
     *
     * @param request
     * @return Web renderer to be returned to the client.
     */
    private static WebRenderInterface getJSONRPCRenderer(FullHttpRequest request) throws HttpClientNotAuthorizedException {
        return new PiDomeJSONRPCEndpoint();
    }

    /**
     * Returns a random quote.
     * @return 
     */
    private static String getRandQuote() {
        return someRandomQuote.get((int) Math.floor(Math.random() * someRandomQuote.size()));
    }

    /**
     * Sends a static file to the channel handler context.
     * @param chc The channel handler context
     * @param writer The output writer as resource.
     * @param fileName The filename requested
     * @param decoder The query string decoder.
     * @param streamId The stream id in case of http 2
     * @throws Webservice404Exception when the file is not found.
     * @throws IOException When output fails.
     */
    private static void sendStaticFile(ChannelHandlerContext chc, HttpRequestWriterInterface writer, String fileName, QueryStringDecoder decoder, String streamId) throws Webservice404Exception, IOException {
        File file;
        boolean floorPlan = false;
        if (fileName.startsWith("/floorplan/")) {
            floorPlan = true;
            file = new File("resources", fileName);
        } else {
            file = new File(HttpServer.getDocumentRoot(), fileName);
        }
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        ByteArrayOutputStream outputByteArray = new ByteArrayOutputStream( );
        switch(fileType){
            case "html":
                if(!decoder.parameters().containsKey("requesttype") && HttpServer.getHttpHeader()!=null){
                    outputByteArray.write(Files.readAllBytes(new File(HttpServer.getHttpHeader()).toPath()));
                }
                outputByteArray.write(Files.readAllBytes(file.toPath()));
                if(!decoder.parameters().containsKey("requesttype") && HttpServer.getHttpFooter()!=null){
                    outputByteArray.write(Files.readAllBytes(new File(HttpServer.getHttpFooter()).toPath()));
                }
            break;
            default:
                outputByteArray.write(Files.readAllBytes(file.toPath()));
            break;
        }
        if (file.exists() && !file.isDirectory()) {
            writer.writeResponse(chc, HttpResponseStatus.OK, outputByteArray.toByteArray(), fileType, streamId, !floorPlan);
        } else {
            writer.writeResponse(chc, HttpResponseStatus.NOT_FOUND, return404Error().getBytes(), "html", streamId);
        }
        outputByteArray.close();
    }

    /**
     * Returns a 404 string.
     * @return an html compliant 404 string.
     */
    private static String return404Error(){
        return "<html><head/><body><h1>Well, that was a numb request!</h1><p><a href=\"/\">Let's try this</a><br/><br/>" + getRandQuote() + "</p></body></html>";
    }
    
    /**
     * Returns a content type header based on file type name.
     * @param fileType The file type name
     * @return The content type http header.
     */
    protected static String getContentTypeHeader(String fileType) {
        switch (fileType.trim()) {
            case "upload":
                return "text/html; charset=utf-8";
            case "xhtml":
            case "html":
                return "text/html; charset=utf-8";
            case "css":
                return "text/css; charset=utf-8";
            case "js":
                return "text/javascript; charset=utf-8"; /// Allthough allowed it is discouraged (chrome webmaster tools seems to bitch about it: Resource interpreted as Script but transferred with MIME type text/plain: "http://hostname:8000/js/jquery-1.9.1.js".)
            case "ico":
                return "image/x-icon";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "gif":
                return "image/gif";
            case "png":
                return "image/png";
            case "xml":
                return "text/xml; charset=utf-8";
            case "json":
                return "application/json; charset=utf-8";
            case "ttf":
            case "otf":
                return "application/font-sfnt";
            case "woff":
                return "application/font-woff";
            case "woff2":
                return "application/font-woff2";
            case "eot":
                return "application/vnd.ms-fontobject";
            case "svg":
                return "image/svg+xml";
            default:
                return "text/plain; charset=utf-8";
        }
    }

    /**
     * Returns an authorized client if accepted.
     *
     * @param ip
     * @param key
     * @return
     * @throws HttpClientNotAuthorizedException
     * @throws HttpClientLoggedInOnOtherLocationException
     */
    private static Map<RemoteClientInterface, RemoteClient> getAuthorizedClient(HttpRequest request, String ip, String key, String path) throws HttpClientNotAuthorizedException, HttpClientLoggedInOnOtherLocationException {
        Map<RemoteClientInterface, RemoteClient> returnSet = new HashMap<>();
        try {
            for (RemoteClient client : RemoteClientsConnectionPool.getConnectedClients()) {
                if (!client.getKey().equals("") && client.getRemoteSocketAddress().equals(ip) && client.getKey().equals(key)) {
                    returnSet.put(RemoteClientsConnectionPool.getClientBaseByConnection(client), client);
                    return returnSet;
                } else if ((request.getUri().startsWith("/jsonrpc.json") || request.getUri().startsWith("/xmlapi/"))
                        && client.getRemoteSocketAddress().equals(ip)
                        && (client.getType().equals(RemoteClient.Type.WEBSOCKET)) && (client.getDeviceType().equals(RemoteClient.DeviceType.MOBILE))) {
                    returnSet.put(RemoteClientsConnectionPool.getClientBaseByConnection(client), client);
                    return returnSet;
                }
            }
            for (SocketServiceClient client : RemoteClientsConnectionPool.getConnectedDisplayClients()) {
                if ((request.getUri().startsWith("/jsonrpc.json") || request.getUri().startsWith("/xmlapi/"))
                        && client.getRemoteSocketAddress().equals(ip)
                        && (client.getType().equals(RemoteClient.Type.SOCKET)) && (client.getDeviceType().equals(RemoteClient.DeviceType.DISPLAY))) {
                    returnSet.put(RemoteClientsConnectionPool.getIfSocketClientAuthorized(client), client);
                    return returnSet;
                }
            }
        } catch (RemoteClientException ex) {
            LOG.warn("Client on ip {} not authorized", ip);
            throw new HttpClientNotAuthorizedException(ex);
        }
        throw new HttpClientNotAuthorizedException();
    }

    /**
     * Returns the plain ip.
     *
     * @param socket
     * @return
     */
    protected static String getPlainIp(SocketAddress socket) {
        return socket.toString().substring(1, socket.toString().indexOf(":"));
    }

    /**
     * Returns the port from the SocketAddress.
     *
     * @param socket
     * @return
     */
    private static int getPort(SocketAddress socket) {
        return Integer.parseInt(socket.toString().substring(socket.toString().indexOf(":") + 1));
    }

}
