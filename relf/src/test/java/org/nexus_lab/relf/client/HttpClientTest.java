package org.nexus_lab.relf.client;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.Proxy;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class HttpClientTest {
    private final static String CONTROL_URL = "control";
    private final static Request GET = new Request.Builder().url("http://localhost").get().build();
    private final static HttpClient.Verifier VERIFIER = response -> {
        if (!Arrays.equals(response.body().bytes(), "Good".getBytes())) {
            throw new Exception(
                    String.format("Response does not match: %s != Good", response.body().string()));
        }
    };
    private final static Dispatcher PROXY_DISPATCHER = new Dispatcher() {
        @Override
        public MockResponse dispatch(RecordedRequest request) {
            return new MockResponse().setResponseCode(500);
        }
    };

    private HttpClient client;
    private MockWebServer[] proxies = new MockWebServer[2];
    private MockWebServer[] servers = new MockWebServer[3];

    @Before
    public void setup() throws Exception {
        Logger.getLogger(MockWebServer.class.getName()).setLevel(Level.OFF);
        Logger.getLogger("AndroidLog").setLevel(Level.OFF);

        client = new HttpClient();

        for (int i = 0; i < proxies.length; i++) {
            proxies[i] = new MockWebServer();
            proxies[i].setDispatcher(PROXY_DISPATCHER);
            client.addProxy(proxies[i].url("/").toString());
        }
        client.addProxy(Proxy.NO_PROXY);

        for (int i = 0; i < servers.length; i++) {
            servers[i] = new MockWebServer();
            int serverIndex = i;
            servers[i].setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    if (request.getPath().equals("/control")) {
                        if (serverIndex == servers.length - 1) {
                            return new MockResponse().setBody("Good");
                        } else {
                            return new MockResponse().setBody("Bad");
                        }
                    }
                    return new MockResponse().setResponseCode(404);
                }
            });
            client.addEndpoint(servers[i].url("/").toString());
        }
    }

    @Test
    public void concatenateUrl() throws Exception {
        Response response = client.request(GET, CONTROL_URL, null);
        assertEquals(servers[0].url("/control").toString(), response.request().url().toString());
    }

    @Test
    public void proxySearch() throws Exception {
        String url = "http://www.google.com/";
        client.request(GET.newBuilder().url(url).build(), null);
        assertEquals(url, proxies[0].takeRequest().getPath());
        assertEquals(url, proxies[1].takeRequest().getPath());
    }

    @Test
    public void verifierAndUrlSwitching() throws Exception {
        Response response = client.request(GET, CONTROL_URL, VERIFIER);
        assertArrayEquals("Good".getBytes(), response.body().bytes());
        assertEquals(servers[0].takeRequest().getRequestUrl(), servers[0].url("/control"));
        assertEquals(servers[1].takeRequest().getRequestUrl(), servers[1].url("/control"));
        assertEquals(servers[2].takeRequest().getRequestUrl(), servers[2].url("/control"));
    }

    @Test
    public void temporaryFailure() throws Exception {
        client.setRetryInterval(1);
        servers[servers.length - 1].setDispatcher(new Dispatcher() {
            private int counter = -1;

            @Override
            public MockResponse dispatch(RecordedRequest request) {
                counter++;
                if (counter == 1) {
                    return new MockResponse().setResponseCode(500);
                }
                return new MockResponse().setBody("Good");
            }
        });
        Response response1 = client.request(GET, CONTROL_URL, VERIFIER);
        Response response2 = client.request(GET, CONTROL_URL, VERIFIER);

        assertArrayEquals("Good".getBytes(), response1.body().bytes());
        assertArrayEquals("Good".getBytes(), response2.body().bytes());

        assertEquals(servers[0].takeRequest().getRequestUrl(), servers[0].url("/control"));
        assertEquals(servers[1].takeRequest().getRequestUrl(), servers[1].url("/control"));
        assertEquals(servers[2].takeRequest().getRequestUrl(), servers[2].url("/control"));

        assertTrue(client.isNotFailing());
    }

    @Test
    public void needEnrollment() throws Exception {
        servers[0].setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                return new MockResponse().setResponseCode(406);
            }
        });
        Response response = client.request(GET, CONTROL_URL, VERIFIER);
        assertEquals(406, response.code());
        assertEquals(1, servers[0].getRequestCount());
        assertTrue(client.isNotFailing());
    }

    @Test
    public void connectionErrorRecovery() throws Exception {
        client.setTimeout(1);
        client.getEndpoints().addToHead("http://localhost:1/");

        Response response = client.request(GET, CONTROL_URL, VERIFIER);

        assertArrayEquals("Good".getBytes(), response.body().bytes());
    }

    @After
    public void destroy() throws IOException {
        for (MockWebServer server : servers) {
            server.shutdown();
        }
        for (MockWebServer proxy : proxies) {
            proxy.shutdown();
        }
    }
}