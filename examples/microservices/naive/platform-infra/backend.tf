terraform {
  # Partial backend configuration. Real values are supplied at init time via
  # -backend-config, e.g.:
  #
  #   terraform init \
  #     -backend-config="bucket=quickbite-tfstate-dev" \
  #     -backend-config="key=platform-infra/dev/terraform.tfstate" \
  #     -backend-config="region=us-east-1" \
  #     -backend-config="dynamodb_table=quickbite-tflocks" \
  #     -backend-config="encrypt=true"
  #
  # State bucket convention: quickbite-tfstate-<env>
  # State key convention:    platform-infra/<env>/terraform.tfstate
  # Lock table:              quickbite-tflocks
  backend "s3" {
    key            = "platform-infra/dev/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "quickbite-tflocks"
    encrypt        = true
  }
}
