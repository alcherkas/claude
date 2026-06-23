env                     = "dev"
image_tag               = "latest"
image_registry          = "ghcr.io/quickbite"
tfstate_bucket          = "quickbite-tfstate-dev"
desired_count           = 2
kafka_bootstrap_servers = "kafka.quickbite.internal:9092"
