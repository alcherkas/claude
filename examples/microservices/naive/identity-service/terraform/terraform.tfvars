env           = "dev"
image_tag     = "latest"
cpu           = 256
memory        = 512
desired_count = 2

# Override with a real secret via TF_VAR_jwt_secret or a *.tfvars.local file.
jwt_secret = "change-me-quickbite-shared-dev-secret-please-rotate-32b"
