---
title: 'Google SCC: AI-agent credential-compromise detection & revocation'
url: https://cloud.google.com/security-command-center/docs/findings/threats/agent-engine-iam-anomalous-behavior-service-account-gets-own-iam-policy
canonical_url: https://cloud.google.com/security-command-center/docs/findings/threats/agent-engine-iam-anomalous-behavior-service-account-gets-own-iam-policy
org: Google
theme: security-governance
subtopic: tools
source_type: html
tags:
- Google
- security-governance
- tools
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---
# Google SCC: AI-agent credential-compromise detection & revocation

> **Excerpt from the original:** Security Command Center / Event Threat Detection flags when an AI Agent Engine service account uses its own credentials to query its own IAM roles/permissions (GetIamPolicy) — a possible credential-compromise indicator requiring immediate action. Prescribed response: delete the compromised service account and rotate/delete all of its access keys; where the agent is a Google-provided Agent Runtime service account that cannot be deleted, restrict its permissions to the minimum required.

**License:** Google Cloud product documentation

**Relevance to securing & governing AI coding agents:** The detection + credential-revocation half of agent incident response on GCP. Validated by Unit 42's "Double Agents" Vertex AI exploit, which demonstrated agent service-account credentials are a real, revocable attack surface.

[Read the original →](https://cloud.google.com/security-command-center/docs/findings/threats/agent-engine-iam-anomalous-behavior-service-account-gets-own-iam-policy)

*Source: Google · hand-catalogued 2026-06-28 (not auto-fetched). Verify version/date at time of use.*
