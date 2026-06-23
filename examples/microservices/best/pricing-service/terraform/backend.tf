terraform {
  # Partial backend config. Real values are supplied at init time, e.g.:
  #   terraform init \
  #     -backend-config="bucket=quickbite-tfstate-dev" \
  #     -backend-config="dynamodb_table=quickbite-tflocks" \
  #     -backend-config="region=us-east-1"
  backend "s3" {
    key = "pricing-service/dev/terraform.tfstate"
  }
}
