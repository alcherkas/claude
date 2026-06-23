# react-mfe module

Hosts a QuickBite React micro-frontend (Vite + Module Federation build) as a
static site: a **private S3 bucket** fronted by a **CloudFront** distribution using
**Origin Access Control (OAC)**. SPA deep-linking is handled by mapping CloudFront
`403`/`404` responses to `/index.html` with a `200`, so React Router routes resolve
client-side.

## Behaviour

- The bucket is fully private (public access blocked, `BucketOwnerEnforced`
  ownership). Only the CloudFront distribution can read objects, granted via a
  bucket policy scoped to the distribution ARN (`AWS:SourceArn` condition).
- `domain_name` is optional. When unset, the distribution serves on its
  `*.cloudfront.net` domain with the default certificate. When set, pass
  `acm_certificate_arn` (a us-east-1 cert) for TLS on the alias.
- `price_class` defaults to `PriceClass_100`.

## Inputs

| Name | Type | Default | Description |
|------|------|---------|-------------|
| `name` | string | — | MFE name (e.g. `checkout-mfe`). |
| `environment` | string | — | Deployment environment. |
| `domain_name` | string | `null` | Optional custom domain (CNAME). |
| `acm_certificate_arn` | string | `null` | Optional us-east-1 ACM cert for the domain. |
| `price_class` | string | `PriceClass_100` | CloudFront price class. |
| `tags` | map(string) | `{}` | Extra tags. |

## Outputs

| Name | Description |
|------|-------------|
| `bucket_name` | S3 origin bucket name (sync the build into it). |
| `distribution_id` | CloudFront distribution id (invalidate on deploy). |
| `domain` | Public domain (custom or CloudFront). |
