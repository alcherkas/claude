# Partial backend config — real values supplied via -backend-config at init:
#   terraform init \
#     -backend-config="bucket=quickbite-tfstate-dev" \
#     -backend-config="dynamodb_table=quickbite-tflocks" \
#     -backend-config="region=us-east-1"
terraform {
  backend "s3" {
    key     = "cart-service/dev/terraform.tfstate"
    encrypt = true
  }
}
