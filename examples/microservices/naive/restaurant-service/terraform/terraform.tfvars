env                     = "dev"
image_tag               = "latest"
container_port          = 8082
desired_count           = 2
identity_url            = "http://identity-service.quickbite.internal:8081"
kafka_bootstrap_servers = "kafka.quickbite.internal:9092"
