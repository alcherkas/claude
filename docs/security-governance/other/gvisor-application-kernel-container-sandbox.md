---
title: 'gVisor: Application-kernel container sandbox (runsc)'
url: https://github.com/google/gvisor
canonical_url: https://github.com/google/gvisor
org: Other
theme: security-governance
subtopic: tools
source_type: repo
tags:
- Other
- security-governance
- tools
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---
# gVisor: Application-kernel container sandbox (runsc)

> **Excerpt from the original:** gVisor provides a strong layer of isolation between running applications and the host operating system. It is an application kernel that implements a Linux-like interface. Unlike Linux, it is written in a memory-safe language (Go) and runs in userspace. gVisor includes an Open Container Initiative (OCI) runtime called runsc that integrates with Docker and Kubernetes, making it simple to run sandboxed containers.

**License:** Apache-2.0

**Relevance to securing & governing AI coding agents:** Google's userspace application kernel (memory-safe Go) shipping the OCI runtime runsc; integrates with Docker/Kubernetes to sandbox containers, since ordinary containers share the host kernel and can be escaped by a single vulnerability.

[Read the original →](https://github.com/google/gvisor)

*Source: Other · hand-catalogued 2026-06-28 (not auto-fetched). Verify version/date at time of use.*
