terraform {
  # Partial config. Real values are supplied via -backend-config at init time, e.g.:
  #   terraform init \
  #     -backend-config="bucket=quickbite-tfstate-dev" \
  #     -backend-config="dynamodb_table=quickbite-tflocks" \
  #     -backend-config="region=us-east-1"
  backend "s3" {
    key     = "delivery-service/dev/terraform.tfstate"
    encrypt = true
  }
}
