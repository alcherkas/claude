package com.quickbite.notification.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

/**
 * Tiny, dependency-free template renderer. Maps a template key + the source event payload to a
 * human-readable message. In production this would be a real templating engine (Handlebars/Thymeleaf)
 * with localized copy; here it is a deterministic switch so the demo stays self-contained.
 */
@Component
public class TemplateRenderer {

    /**
     * @param template logical template key (e.g. {@code order.created})
     * @param payload  the raw event payload JSON
     * @return the rendered message body
     */
    public String render(String template, JsonNode payload) {
        return switch (template) {
            case "order.created" -> "Hi customer, your order " + shortId(payload, "orderId")
                    + " has been placed for " + money(payload) + ". We'll let you know when it's confirmed.";
            case "order.status_changed" -> "Order " + shortId(payload, "orderId")
                    + " is now " + text(payload, "newStatus") + ".";
            case "payment.captured" -> "Payment for order " + shortId(payload, "orderId")
                    + " was successful (" + money(payload) + "). Thank you!";
            case "payment.failed" -> "We couldn't process your payment for order "
                    + shortId(payload, "orderId") + ". Please try another method.";
            case "payment.refunded" -> "A refund of " + money(payload)
                    + " for order " + shortId(payload, "orderId") + " is on its way.";
            case "delivery.status_changed" -> "Your delivery for order " + shortId(payload, "orderId")
                    + " is now " + text(payload, "status") + ".";
            default -> "Update on order " + shortId(payload, "orderId") + ".";
        };
    }

    private String shortId(JsonNode payload, String field) {
        String v = text(payload, field);
        return v.length() > 8 ? "#" + v.substring(0, 8) : "#" + v;
    }

    private String text(JsonNode payload, String field) {
        JsonNode node = payload.get(field);
        return (node == null || node.isNull()) ? "" : node.asText();
    }

    /** Formats {@code totalCents}/{@code amountCents} + {@code currency} when present. */
    private String money(JsonNode payload) {
        JsonNode cents = payload.has("totalCents") ? payload.get("totalCents")
                : payload.get("amountCents");
        if (cents == null || cents.isNull()) {
            return "your order total";
        }
        String currency = text(payload, "currency");
        if (currency.isBlank()) {
            currency = "USD";
        }
        return String.format("%.2f %s", cents.asLong() / 100.0, currency);
    }
}
