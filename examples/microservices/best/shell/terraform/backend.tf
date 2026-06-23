# Partial backend config. Real values are supplied via -backend-config at init:
#   terraform init \
#     -backend-config="bucket=quickbite-tfstate-<env>" \
#     -backend-config="key=shell/<env>/terraform.tfstate" \
#     -backend-config="region=us-east-1" \
#     -backend-config="dynamodb_table=quickbite-tflocks"
terraform {
  backend "s3" {
    key            = "shell/dev/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "quickbite-tflocks"
    encrypt        = true
  }
}
