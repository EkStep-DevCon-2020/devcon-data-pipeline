package org.ekstep.ep.samza.util;

import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;

public class HttpClient {

    private String apiHost;
    private OkHttpClient httpClient;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private Gson gson = new Gson();
    private String authHeader = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiIyZWU4YTgxNDNiZWE0NDU4YjQxMjcyNTU5ZDBhNTczMiJ9.7m4mIUaiPwh_o9cvJuyZuGrOdkfh0Nm0E_25Cl21kxE";

    public HttpClient(){}

    public HttpClient(String apiHost) {
        this.apiHost = apiHost;
        this.httpClient = new OkHttpClient();
    }

    public okhttp3.Response registryReadApi(String osid) throws IOException {
        VisitorElement visitorRequest = new VisitorElement(new Visitor(osid));
        RegistryRequest regRequest = new RegistryRequest(visitorRequest);
        RequestBody body = RequestBody.create(JSON, gson.toJson(regRequest));
        Request request = new Request.Builder()
                .url(apiHost + "/api/reg/read")
                .post(body)
                .addHeader("Authorization", authHeader)
                .addHeader("Content-Type", "application/json")
                .build();
        return httpClient.newCall(request).execute();
    }

    public Visitor getVisitorInfo(String osid) throws IOException {
        okhttp3.Response httpResponse = registryReadApi(osid);
        String responseBody = httpResponse.body().string();
        Visitor visitor = parseOrgResponse(responseBody).getResult().getVisitor();
        return visitor;
    }

    public Response parseOrgResponse(String responseBody) {
        return new Gson().fromJson(responseBody, Response.class);
    }
}
