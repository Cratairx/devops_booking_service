package org.example.bookingService.Client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class CustomerClient {
    private final RestClient restClient;

    public CustomerClient(@Value("${customer-service.base-url}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    public boolean customerExists(Long customerId) {
        try {
            restClient.get().uri("/api/customer/{id}", customerId).retrieve().toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (HttpClientErrorException | HttpServerErrorException | ResourceAccessException e) {

            return false;
        }
    }
}