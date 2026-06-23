terraform {
  # Partial backend config. Real values are supplied at init time via
  # -backend-config (bucket/region/dynamodb_table from the environment), e.g.:
  #   terraform init \
  #     -backend-config="bucket=quickbite-tfstate-dev" \
  #     -backend-config="region=us-east-1" \
  #     -backend-config="dynamodb_table=quickbite-tflocks"
  backend "s3" {
    key     = "restaurant-service/dev/terraform.tfstate"
    encrypt = true
  }
}
