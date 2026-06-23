env           = "dev"
image_tag     = "latest"
cpu           = 256
memory        = 512
desired_count = 2

container_port = 8080

# Override with a real secret via TF_VAR_jwt_secret or a *.tfvars.local file.
jwt_secret = "change-me-quickbite-shared-dev-secret-please-rotate-32b"

identity_url     = "http://identity-service.quickbite.internal:8081"
restaurant_url   = "http://restaurant-service.quickbite.internal:8082"
menu_url         = "http://menu-service.quickbite.internal:8083"
search_url       = "http://search-service.quickbite.internal:8084"
cart_url         = "http://cart-service.quickbite.internal:8085"
pricing_url      = "http://pricing-service.quickbite.internal:8086"
promotion_url    = "http://promotion-service.quickbite.internal:8087"
order_url        = "http://order-service.quickbite.internal:8088"
payment_url      = "http://payment-service.quickbite.internal:8089"
wallet_url       = "http://wallet-service.quickbite.internal:8090"
driver_url       = "http://driver-service.quickbite.internal:8091"
delivery_url     = "http://delivery-service.quickbite.internal:8092"
notification_url = "http://notification-service.quickbite.internal:8093"
review_url       = "http://review-service.quickbite.internal:8094"
