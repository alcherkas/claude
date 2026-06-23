# QuickBite — root convenience targets.
#
# This Makefile orchestrates the local docker stack and helps build every
# sibling repo. Service/MFE repos are checked out alongside this directory.

# ---- repo lists (must match PLATFORM_SPEC.md §1.1 / §1.2) -------------------
SERVICES := identity-service restaurant-service menu-service search-service \
            cart-service pricing-service promotion-service order-service \
            payment-service wallet-service driver-service delivery-service \
            notification-service review-service api-gateway

MFES := shell discovery-mfe checkout-mfe order-tracking-mfe account-mfe \
        restaurant-admin-mfe driver-portal-mfe

COMPOSE := docker compose

# Terraform apply order: terraform-modules -> platform-infra -> services -> mfes
TF_ENV ?= dev

.PHONY: help up down logs build-services build-mfes \
        tf-init tf-plan tf-apply fmt clean

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-16s\033[0m %s\n", $$1, $$2}'

# ---- local stack -----------------------------------------------------------
up: ## Build and start the full local stack (postgres, kafka, services, MFEs)
	$(COMPOSE) up -d --build

down: ## Stop and remove the local stack (keeps the postgres volume)
	$(COMPOSE) down

logs: ## Tail logs from every container
	$(COMPOSE) logs -f --tail=100

# ---- per-repo builds -------------------------------------------------------
build-services: ## mvn verify in every Java service repo
	@for s in $(SERVICES); do \
		echo ">> building $$s"; \
		( cd ../$$s && mvn -B -q verify ) || exit 1; \
	done

build-mfes: ## npm ci + build in every MFE repo
	@for m in $(MFES); do \
		echo ">> building $$m"; \
		( cd ../$$m && npm ci && npm run build ) || exit 1; \
	done

# ---- terraform -------------------------------------------------------------
# ORDER: always apply bottom-up so module sources and remote-state outputs
# resolve:  terraform-modules (source-only) -> platform-infra -> services -> mfes.
TF_DIRS := ../platform-infra $(addprefix ../,$(SERVICES)) $(addprefix ../,$(MFES))

tf-init: ## terraform init (platform-infra, then services, then mfes)
	@for d in $(TF_DIRS); do \
		echo ">> terraform init $$d"; \
		( cd $$d/terraform && terraform init \
			-backend-config="key=$$(basename $$d)/$(TF_ENV)/terraform.tfstate" ) || exit 1; \
	done

tf-plan: ## terraform plan across all infra repos in dependency order
	@for d in $(TF_DIRS); do \
		echo ">> terraform plan $$d"; \
		( cd $$d/terraform && terraform plan -var="environment=$(TF_ENV)" ) || exit 1; \
	done

tf-apply: ## terraform apply in order: platform-infra -> services -> mfes
	@for d in $(TF_DIRS); do \
		echo ">> terraform apply $$d"; \
		( cd $$d/terraform && terraform apply -auto-approve -var="environment=$(TF_ENV)" ) || exit 1; \
	done

# ---- housekeeping ----------------------------------------------------------
fmt: ## Format Terraform and run Maven/Prettier formatters where present
	@for d in $(TF_DIRS); do \
		( cd $$d/terraform && terraform fmt -recursive ) || true; \
	done
	@for s in $(SERVICES); do ( cd ../$$s && mvn -q spotless:apply ) 2>/dev/null || true; done
	@for m in $(MFES); do ( cd ../$$m && npm run format ) 2>/dev/null || true; done

clean: ## Remove build artifacts and tear down the stack + volumes
	$(COMPOSE) down -v --remove-orphans
	@for s in $(SERVICES); do ( cd ../$$s && mvn -q clean ) 2>/dev/null || true; done
	@for m in $(MFES); do rm -rf ../$$m/dist ../$$m/node_modules 2>/dev/null || true; done
