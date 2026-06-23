terraform {
  # Partial config — real values supplied via -backend-config at init time:
  #   terraform init \
  #     -backend-config="bucket=quickbite-tfstate-dev" \
  #     -backend-config="key=search-service/dev/terraform.tfstate" \
  #     -backend-config="region=us-east-1" \
  #     -backend-config="dynamodb_table=quickbite-tflocks"
  backend "s3" {
    key = "search-service/dev/terraform.tfstate"
  }
}
