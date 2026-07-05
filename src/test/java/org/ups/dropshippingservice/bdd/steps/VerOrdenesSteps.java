package org.ups.dropshippingservice.bdd.steps;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class VerOrdenesSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private String xProviderId;
    private ResponseEntity<String> response;

    @Dado("que el header X-Provider-Id es {string}")
    public void queElHeaderXProviderIdEs(String providerId) {
        this.xProviderId = providerId;
    }

    @Dado("existe una orden PENDIENTE con id {string} para {string}")
    public void existeOrdenPendiente(String orderId, String providerId) {
        // Seed data in data.sql provides these orders; no action needed
    }

    @Dado("la orden {string} ya fue aceptada")
    public void laOrdenYaFueAceptada(String orderId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Provider-Id", xProviderId != null ? xProviderId : "prov-001");
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{\"estimatedDispatchDate\":\"2026-12-01\"}", headers);
        restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/orders/" + orderId + "/accept",
                HttpMethod.PUT, entity, String.class);
    }

    @Cuando("^el proveedor consulta GET /api/v1/providers/([^/]+)/orders$")
    public void elProveedorConsultaOrders(String providerId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Provider-Id", xProviderId);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/providers/" + providerId + "/orders",
                HttpMethod.GET, entity, String.class);
    }

    @Cuando("el proveedor acepta la orden {string} con fecha {string}")
    public void elProveedorAceptaLaOrden(String orderId, String date) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Provider-Id", xProviderId);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"estimatedDispatchDate\":\"" + date + "\"}";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/orders/" + orderId + "/accept",
                HttpMethod.PUT, entity, String.class);
    }

    @Cuando("el proveedor rechaza la orden {string} con motivo {string}")
    public void elProveedorRechazaLaOrden(String orderId, String reason) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Provider-Id", xProviderId != null ? xProviderId : "prov-001");
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"rejectionReason\":\"" + reason + "\"}";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/orders/" + orderId + "/reject",
                HttpMethod.PUT, entity, String.class);
    }

    @Entonces("la respuesta HTTP es {int}")
    public void laRespuestaHTTPEs(int statusCode) {
        assertThat(response.getStatusCode().value()).isEqualTo(statusCode);
    }

    @Y("la respuesta contiene una lista con al menos {int} orden")
    public void laRespuestaContieneListaConAlMenos(int minCount) {
        String body = response.getBody();
        assertThat(body).isNotNull();
        long count = body.chars().filter(c -> c == '{').count();
        assertThat(count).isGreaterThanOrEqualTo(minCount);
    }

    @Y("cada orden incluye código de producto, descripción, cantidad, dirección, contacto y fecha esperada")
    public void cadaOrdenIncluyeCamposRequeridos() {
        String body = response.getBody();
        assertThat(body).contains("orderCode");
        assertThat(body).contains("product");
        assertThat(body).contains("deliveryAddress");
        assertThat(body).contains("customerContact");
        assertThat(body).contains("expectedDeliveryDate");
    }

    @Y("la respuesta contiene una lista vacía")
    public void laRespuestaContieneListaVacia() {
        assertThat(response.getBody()).isEqualTo("[]");
    }

    @Y("el estado de la orden en la respuesta es {string}")
    public void elEstadoDeLaOrdenEs(String expectedStatus) {
        assertThat(response.getBody()).contains("\"status\":\"" + expectedStatus + "\"");
    }

    @Y("el estado de la orden rechazada es {string}")
    public void elEstadoDeLaOrdenRechazadaEs(String expectedStatus) {
        assertThat(response.getBody()).contains("\"status\":\"" + expectedStatus + "\"");
    }

    @Y("el código de error es {string}")
    public void elCodigoDeErrorEs(String errorCode) {
        assertThat(response.getBody()).contains("\"code\":\"" + errorCode + "\"");
    }
}
