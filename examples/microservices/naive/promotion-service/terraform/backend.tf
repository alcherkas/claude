# Partial backend config. Real values supplied via -backend-config at init time, e.g.:
#   terraform init \
#     -backend-config="bucket=quickbite-tfstate-dev" \
#     -backend-config="key=promotion-service/dev/terraform.tfstate" \
#     -backend-config="region=us-east-1" \
#     -backend-config="dynamodb_table=quickbite-tflocks"
terraform {
  backend "s3" {
    key            = "promotion-service/dev/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "quickbite-tflocks"
    encrypt        = true
  }
}
