package org.example.bookingService.Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class CustomerClient {


   
    private static final Logger log = (Logger) LoggerFactory.getLogger(CustomerClient.class);

    private final RestClient restClient;

    public CustomerClient(@Value("${customer-service.base-url}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    public boolean customerExists(Long customerId) {
        try {
            restClient.get().uri("/api/customer/{id}", customerId).retrieve().toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("customer-service: customer {} not found", customerId);
            return false;
        } catch (HttpClientErrorException | HttpServerErrorException | ResourceAccessException e) {
            log.error("customer-service call failed for customer {}", customerId, e);
            return false;
        }
    }
}
