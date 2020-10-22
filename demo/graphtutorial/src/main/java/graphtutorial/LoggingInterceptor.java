package graphtutorial;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.www.http.HttpClient;

import java.io.IOException;

/**
 * @author yusufu
 */

class LoggingInterceptor implements Interceptor {
    @Override public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        final Logger log = LoggerFactory.getLogger(HttpClient.class);
        long t1 = System.nanoTime();
        System.out.println("OkHttp: "+ String.format("Sending request %s on %s%n%s",
                request.url(), chain.connection(), request.headers()));


/*        final Request copy = request.newBuilder().build();
        if(copy != null && copy.body()!=null) {
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            System.out.println("OkHttp Request Body: "+ buffer.readUtf8());
        }*/

        Response response = chain.proceed(request);

        long t2 = System.nanoTime();
        log.debug("OkHttp", String.format("Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6d, response.headers()));

        return response;
    }
}
