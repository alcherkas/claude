---
title: 'Unit 42 "Double Agents": exploiting Vertex AI agent credentials'
url: https://unit42.paloaltonetworks.com/double-agents-vertex-ai/
canonical_url: https://unit42.paloaltonetworks.com/double-agents-vertex-ai/
org: Other
theme: security-governance
subtopic: patterns
source_type: html
tags:
- Other
- security-governance
- patterns
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---
# Unit 42 "Double Agents": exploiting Vertex AI agent credentials

> **Excerpt from the original:** Unit 42 researchers extracted a Vertex AI agent's Per-Project Service Agent (P4SA) service-account credentials from the GCP metadata service, acted on behalf of the agent's identity, and pivoted from the agent's execution context into the consumer project — gaining unrestricted read access to all Google Cloud Storage buckets. Google remediated by moving the ADK deployment workflow to Bring-Your-Own-Service-Account (BYOSA).

**License:** Security research (Palo Alto Networks Unit 42; disclosed 31 March 2026)

**Relevance to securing & governing AI coding agents:** Independent (non-vendor) evidence that an overprivileged agent identity is a concrete, exploitable, and revocable attack surface — the empirical case for least-privilege agent credentials and fast revocation.

[Read the original →](https://unit42.paloaltonetworks.com/double-agents-vertex-ai/)

*Source: Other · hand-catalogued 2026-06-28 (not auto-fetched). Verify version/date at time of use.*
