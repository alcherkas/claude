env                     = "dev"
image_tag               = "latest"
image_registry          = "quickbite"
container_port          = 8084
desired_count           = 2
kafka_bootstrap_servers = "kafka.quickbite.internal:9092"
restaurant_url          = "http://restaurant-service.quickbite.internal:8082"
menu_url                = "http://menu-service.quickbite.internal:8083"
