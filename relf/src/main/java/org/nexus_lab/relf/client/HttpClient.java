package org.nexus_lab.relf.client;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.Setter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * An HTTP client with retry functionality.
 *
 * @author Ruipeng Zhang
 */
public class HttpClient {
    private static final String TAG = HttpClient.class.getSimpleName();

    private static final int SINGLE_RETRY_ERROR_LIMIT = 10;
    private static final int CONSECUTIVE_ERROR_LIMIT = 30;

    private int consecutiveErrors;
    @Getter
    @Setter
    private int timeout = 0;
    @Getter
    @Setter
    private int retryInterval = 30;
    @Getter
    private CyclicIterableList<Proxy> proxies = new CyclicIterableList<>(new ArrayList<>());
    @Getter
    private CyclicIterableList<String> endpoints = new CyclicIterableList<>(new ArrayList<>());

    /**
     * Add a proxy to the proxy list.
     *
     * @param address the address of the proxy server. Can start with http/https/socks.
     */
    public void addProxy(@NonNull String address) {
        try {
            Proxy.Type type = null;
            URL url = new URL(address);
            int port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
            if (address.startsWith("http:") || address.startsWith("https:")) {
                type = Proxy.Type.HTTP;
            } else if (address.startsWith("socks:")) {
                type = Proxy.Type.SOCKS;
            }
            if (type != null) {
                addProxy(new Proxy(type, new InetSocketAddress(url.getHost(), port)));
                return;
            }
        } catch (MalformedURLException ignored) {
        }
        throw new IllegalArgumentException("Cannot create proxy from address: " + address);
    }

    /**
     * Add a proxy to the proxy list.
     *
     * @param proxy the proxy object.
     */
    public void addProxy(Proxy proxy) {
        proxies.add(proxy);
    }

    /**
     * Add a server root URL.
     *
     * @param url the server root URL.
     */
    public void addEndpoint(@NonNull String url) {
        if (!url.endsWith("/")) {
            endpoints.add(url + "/");
        } else {
            endpoints.add(url);
        }
    }

    /**
     * Send an HTTP request through a proxy and verify the response.
     *
     * @param proxy    the HTTP proxy.
     * @param request  the HTTP request.
     * @param verifier the {@link Verifier} to verify the response.
     * @return the HTTP response.
     * @throws IOException if failed to send the request or the response code is not a success or
     *                     406, or the response cannot be verified.
     */
    public Response request(Proxy proxy, Request request, Verifier verifier) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .proxy(proxy)
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .build();
        Response response = client.newCall(request).execute();
        response = response.newBuilder().body(new CachedResponseBody(response.body())).build();
        if (response.isSuccessful() && verifier != null) {
            try {
                verifier.verify(response);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
        if (response.isSuccessful() || response.code() == 406) {
            return response;
        }
        throw new IOException(response.message() + "(" + response.code() + ")");
    }

    /**
     * Send an HTTP request through the proxies in the proxy list and verify the response.
     *
     * @param request  the HTTP request.
     * @param verifier the {@link Verifier} to verify the response.
     * @return the HTTP response.
     * @throws IOException if failed to send the request or the response code is not a success or
     *                     406, or the response cannot be verified.
     */
    public Response request(Request request, Verifier verifier) throws IOException {
        for (Proxy proxy : proxies) {
            for (int i = 0; i < SINGLE_RETRY_ERROR_LIMIT; i++) {
                try {
                    Response response = request(proxy, request, verifier);
                    consecutiveErrors = 0;
                    return response;
                } catch (IOException e) {
                    if (i == SINGLE_RETRY_ERROR_LIMIT - 1) {
                        Log.w(TAG, "Failed to send request to " + request.url(), e);
                    }
                }
                consecutiveErrors++;
                if (isErrorLimitReached()) {
                    throw new IOException("Too many connection errors to " + request.url());
                }
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException e) {
                    throw new IOException("The retry was interrupted.");
                }
            }
            String proxyUrl = proxy.address() == null ? "[DIRECT]" : proxy.address().toString();
            Log.w(TAG, "Exceeded max number of retries with proxy " + proxyUrl);
        }
        throw new IOException("Failed to connect to " + request.url() + " with all proxies.");
    }

    /**
     * Send an HTTP request through the proxies in the proxy list and verify the response.
     *
     * @param base     the HTTP request information except URL.
     * @param path     the path to the API endpoint relative to the server root URL.
     * @param verifier the {@link Verifier} to verify the response.
     * @return the HTTP response.
     * @throws IOException if failed to send the request or the response code is not a success or
     *                     406, or the response cannot be verified.
     */
    public Response request(Request base, String path, Verifier verifier) throws IOException {
        for (String endpoint : endpoints) {
            URI uri;
            try {
                uri = new URL(endpoint + path).toURI();
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
            Request request = base.newBuilder().url(uri.toString()).build();
            try {
                return request(request, verifier);
            } catch (IOException e) {
                Log.w(TAG, "Failed to send request through endpoint " + endpoint, e);
            }
        }
        throw new IOException("Failed to connect to " + path + " with all endpoints.");
    }

    /**
     * @return true if the client are not experiencing failures right now.
     */
    public boolean isNotFailing() {
        return consecutiveErrors == 0;
    }

    /**
     * @return true if the number of consecutive error exceeds the limit.
     */
    public boolean isErrorLimitReached() {
        return consecutiveErrors >= CONSECUTIVE_ERROR_LIMIT;
    }

    /**
     * A helper interface for verify HTTP response.
     */
    public interface Verifier {
        /**
         * Verify the HTTP response
         *
         * @param response HTTP response to verify
         * @throws Exception if the response is invalid
         */
        void verify(Response response) throws Exception;
    }

    /**
     * A cyclic iterable list which always iterates starting from the last position of last
     * iteration.
     *
     * @param <E> the type of the elements.
     */
    public class CyclicIterableList<E> implements Iterable<E> {
        private final List<E> elements;
        private int head;

        CyclicIterableList(List<E> elements) {
            this.elements = elements;
        }

        /**
         * @param element element to be added.
         * @return if the add operation succeeds.
         */
        public boolean add(E element) {
            return elements.add(element);
        }

        /**
         * Add an element to the current iteration starting poing.
         *
         * @param element element to be added.
         */
        public void addToHead(E element) {
            elements.add(head, element);
        }

        @NonNull
        @Override
        public Iterator<E> iterator() {
            return new Iterator<E>() {
                private int start = head;
                private int next = start;
                private boolean isStarted;

                @Override
                public boolean hasNext() {
                    return elements.size() > 0 && (!isStarted || next != start);
                }

                @Override
                public E next() {
                    isStarted = true;
                    head = next;
                    next = (next + 1) % elements.size();
                    return elements.get(head);
                }
            };
        }
    }

    /**
     * Cached {@link ResponseBody} so we can pass the response body around.
     */
    public class CachedResponseBody extends ResponseBody {
        private final ResponseBody body;
        private final byte[] content;

        public CachedResponseBody(ResponseBody body) throws IOException {
            this.body = body;
            this.content = body == null ? null : body.bytes();
        }

        @Override
        public MediaType contentType() {
            return body == null ? null : body.contentType();
        }

        @Override
        public long contentLength() {
            return body == null ? 0 : body.contentLength();
        }

        @Override
        public BufferedSource source() {
            return body == null ? null : new Buffer().write(content);
        }
    }
}
