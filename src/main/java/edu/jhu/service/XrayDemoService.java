package edu.jhu.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.handlers.TracingHandler;
import com.amazonaws.xray.proxies.apache.http.HttpClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.jhu.controller.data.XrayDto;
import edu.jhu.repository.XrayDemoRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple Service that is responsible for abstracting business logic.
 * It works with SNS and data repositories as needed.
 */
@Service
@Slf4j
public class XrayDemoService {

    private final String serviceHost;
    private final XrayDemoRepository demoRepository;
    private AmazonSNS client;
    private final String topicArn;

    public XrayDemoService(@Value("${service.host}") final String serviceHost, XrayDemoRepository demoRepository, @Value("${topic.arn}") String topicArn) {
        this.serviceHost = serviceHost;
        this.demoRepository = demoRepository;

        //Build an SNS client that can use X-Ray Traces
        this.client = AmazonSNSClientBuilder.standard()
                .withRegion(Regions.fromName(System.getenv("AWS_REGION")))
                .withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder()))
                .build();

        this.topicArn = topicArn;
    }

    /**
     * Receives data object uses an internal HTTP resource to make a POST request.
     * @param data
     * @return
     * @throws Exception
     */
    public XrayDto createNewUser(final XrayDto data) throws Exception {
        log.info("Creating new user");

        //USES AWS X-Ray enabled HttpClient to put traceIDs in request headers.
        CloseableHttpClient httpclient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(serviceHost);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("firstName", data.getFirstName()));
        params.add(new BasicNameValuePair("lastName", data.getLastName()));

        httpPost.setEntity(new UrlEncodedFormEntity(params));

        CloseableHttpResponse response = httpclient.execute(httpPost);

        try {
            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();
            ObjectMapper mapper = new ObjectMapper();
            XrayDto dto = mapper.readValue(inputStream, XrayDto.class);
            log.info(dto.toString());
            EntityUtils.consume(entity);

            return dto;
        } finally {
            response.close();
        }
    }

    public XrayDto createNewUserDb(XrayDto data) {
        return demoRepository.save(data);
    }

    /**
     * Publish a request to SNS topic identified by topicArn global configuration parameter.
     * @param data
     */
    public void sendToSns(XrayDto data) {
        PublishRequest publishRequest = new PublishRequest(topicArn, "UserCreated!" + data.toString());
        client.publish(publishRequest);
    }
}
