package example;

import io.github.fssantana.vertgo.Controller;
import io.github.fssantana.vertgo.VertgoHandler;
import io.vertx.core.json.JsonObject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Routes extends VertgoHandler {

    @Override
    protected List<Controller> router() {
        return Arrays.asList(
                new ExampleController()
        );
    }

    public static void main(String[] args) {
        JsonObject input = new JsonObject("{\n" +
                "  \"body\": null,\n" +
                "  \"resource\": \"/users\",\n" +
                "  \"path\": \"/users\",\n" +
                "  \"httpMethod\": \"GET\",\n" +
                "  \"isBase64Encoded\": true,\n" +
                "  \"queryStringParameters\": {\n" +
                "    \"foo\": \"bar\"\n" +
                "  },\n" +
                "  \"pathParameters\": {\n" +
                "    \"proxy\": \"/path/to/resource\"\n" +
                "  },\n" +
                "  \"stageVariables\": {\n" +
                "    \"baz\": \"qux\"\n" +
                "  },\n" +
                "  \"headers\": {\n" +
                "    \"Accept\": \"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\",\n" +
                "    \"Accept-Encoding\": \"gzip, deflate, sdch\",\n" +
                "    \"Accept-Language\": \"en-US,en;q=0.8\",\n" +
                "    \"Cache-Control\": \"max-age=0\",\n" +
                "    \"CloudFront-Forwarded-Proto\": \"https\",\n" +
                "    \"CloudFront-Is-Desktop-Viewer\": \"true\",\n" +
                "    \"CloudFront-Is-Mobile-Viewer\": \"false\",\n" +
                "    \"CloudFront-Is-SmartTV-Viewer\": \"false\",\n" +
                "    \"CloudFront-Is-Tablet-Viewer\": \"false\",\n" +
                "    \"CloudFront-Viewer-Country\": \"US\",\n" +
                "    \"Host\": \"1234567890.execute-api.us-east-1.amazonaws.com\",\n" +
                "    \"Upgrade-Insecure-Requests\": \"1\",\n" +
                "    \"User-Agent\": \"Custom User Agent String\",\n" +
                "    \"Via\": \"1.1 08f323deadbeefa7af34d5feb414ce27.cloudfront.net (CloudFront)\",\n" +
                "    \"X-Amz-Cf-Id\": \"cDehVQoZnx43VYQb9j2-nvCh-9z396Uhbp027Y2JvkCPNLmGJHqlaA==\",\n" +
                "    \"X-Forwarded-For\": \"127.0.0.1, 127.0.0.2\",\n" +
                "    \"X-Forwarded-Port\": \"443\",\n" +
                "    \"X-Forwarded-Proto\": \"https\"\n" +
                "  },\n" +
                "  \"requestContext\": {\n" +
                "    \"accountId\": \"123456789012\",\n" +
                "    \"resourceId\": \"123456\",\n" +
                "    \"stage\": \"prod\",\n" +
                "    \"requestId\": \"c6af9ac6-7b61-11e6-9a41-93e8deadbeef\",\n" +
                "    \"requestTime\": \"09/Apr/2015:12:34:56 +0000\",\n" +
                "    \"requestTimeEpoch\": 1428582896000,\n" +
                "    \"identity\": {\n" +
                "      \"cognitoIdentityPoolId\": null,\n" +
                "      \"accountId\": null,\n" +
                "      \"cognitoIdentityId\": null,\n" +
                "      \"caller\": null,\n" +
                "      \"accessKey\": null,\n" +
                "      \"sourceIp\": \"127.0.0.1\",\n" +
                "      \"cognitoAuthenticationType\": null,\n" +
                "      \"cognitoAuthenticationProvider\": null,\n" +
                "      \"userArn\": null,\n" +
                "      \"userAgent\": \"Custom User Agent String\",\n" +
                "      \"user\": null\n" +
                "    },\n" +
                "    \"path\": \"/prod/path/to/resource\",\n" +
                "    \"resourcePath\": \"/{proxy+}\",\n" +
                "    \"httpMethod\": \"POST\",\n" +
                "    \"apiId\": \"1234567890\",\n" +
                "    \"protocol\": \"HTTP/1.1\"\n" +
                "  }\n" +
                "}");
        Map<String, Object> a = new Routes().handleRequest(input.getMap(), null);
        System.out.println(a);
    }
}
