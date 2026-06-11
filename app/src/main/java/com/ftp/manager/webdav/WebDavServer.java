package com.ftp.manager.webdav;

import android.util.Log;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.webserver.SimpleWebServer;
import org.nanohttpd.webserver.WebServerPluginInfo;
import org.nanohttpd.webserver.WebServerPluginManager;

import java.io.File;
import java.util.Map;
import java.util.Base64;

public class WebDavServer extends SimpleWebServer {

    private static final String TAG = "WebDavServer";
    private final String username;
    private final String password;

    public WebDavServer(int port, File rootDir, String username, String password) {
        super("0.0.0.0", port, rootDir, true);
        this.username = username;
        this.password = password;

        // WebDAV eklentisini kaydet
        WebServerPluginManager.registerPluginForMimeType(
                new WebServerPluginInfo("webdav", ".*", org.nanohttpd.webserver.plugin.WebDavPlugin.class),
                ".*"
        );
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (!isAuthorized(session)) {
            return unauthorized();
        }

        Log.d(TAG, session.getMethod() + " " + session.getUri());
        return super.serve(session);
    }

    private boolean isAuthorized(IHTTPSession session) {
        if (username == null || username.isEmpty()) return true;

        Map<String, String> headers = session.getHeaders();
        String auth = headers.get("authorization");
        if (auth == null || !auth.startsWith("Basic ")) return false;

        String base64 = auth.substring(6);
        String decoded = new String(Base64.getDecoder().decode(base64));
        return decoded.equals(username + ":" + password);
    }

    private Response unauthorized() {
        Response r = Response.newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain", "Unauthorized");
        r.addHeader("WWW-Authenticate", "Basic realm=\"WebDAV\"");
        return r;
    }
}
