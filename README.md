# Dev-Able

**An AI-powered application builder backend built with Java, Spring
Boot, and microservices.**

Dev-Able is a backend platform for AI-assisted code generation and
project workspaces. Users can create projects, manage project files,
collaborate through project membership, stream AI-generated responses,
and deploy projects through a Kubernetes-backed workflow. The repository
also includes account authentication, subscription and Stripe billing
functionality, service discovery, centralized configuration, and
Kubernetes deployment resources.

**Repository:**
[MjayK-18/dev-able](https://github.com/MjayK-18/dev-able)

## Contents

-   [Features](#features)
-   [Architecture](#architecture)
-   [Technology Stack](#technology-stack)
-   [Repository Structure](#repository-structure)
-   [Request and Generation Flow](#request-and-generation-flow)
-   [Getting Started](#getting-started)
-   [Configuration](#configuration)
-   [Building the Services](#building-the-services)
-   [Kubernetes Deployment](#kubernetes-deployment)
-   [CI/CD](#cicd)
-   [Security Notes](#security-notes)
-   [Roadmap](#roadmap)

## Features

### AI-assisted code generation

-   Stream AI responses over **Server-Sent Events (SSE)**.
-   Integrate with an OpenAI model through Spring AI.
-   Provide code-generation tools that interact with the workspace
    service.
-   Use project file-tree context to help ground generation in the
    existing project.
-   Store chat sessions, messages, and chat events.

### Project workspaces

-   Create, list, update, and soft-delete projects.
-   Read a project's file tree and individual file contents.
-   Manage project members and roles.
-   Trigger project deployment through the workspace service.
-   Use Kubernetes integration for deployment workflows.

### Accounts and subscriptions

-   Sign up and log in through the account service.
-   Use JWT-based authentication support shared across services and at
    the API gateway.
-   Integrate subscription and checkout flows with Stripe.
-   Provide a customer portal and handle Stripe payment webhooks.
-   Track usage-related data in the intelligence service.

### Distributed backend infrastructure

-   Route requests through Spring Cloud Gateway.
-   Use Eureka for service discovery and Spring Cloud Config for
    centralized configuration.
-   Use Kafka for asynchronous event-driven workflows.
-   Use PostgreSQL/pgvector, Redis, and MinIO infrastructure manifests.
-   Deploy application workloads to Kubernetes, with GKE-oriented CI/CD
    workflows.

## Architecture

The repository is organized as a set of Spring Boot services plus shared
infrastructure.

``` mermaid
flowchart TD
    Client[Client / Frontend] --> Gateway[API Gateway]
    Gateway --> Account[Account Service]
    Gateway --> Workspace[Workspace Service]
    Gateway --> Intelligence[Intelligence Service]

    Config[Config Service] -. centralized configuration .-> Account
    Config -. centralized configuration .-> Gateway
    Config -. centralized configuration .-> Workspace
    Config -. centralized configuration .-> Intelligence

    Discovery[Discovery Service / Eureka] -. service discovery .-> Gateway
    Discovery -. service discovery .-> Account
    Discovery -. service discovery .-> Workspace
    Discovery -. service discovery .-> Intelligence

    Intelligence --> LLM[OpenAI via Spring AI]
    Intelligence --> ChatDB[(PostgreSQL)]
    Intelligence --> Kafka[(Kafka)]
    Workspace --> WorkspaceDB[(PostgreSQL / pgvector infrastructure)]
    Workspace --> Redis[(Redis)]
    Workspace --> Storage[(MinIO object storage)]
    Kafka -. file-storage events .-> Workspace
    Workspace --> Runner[Kubernetes deployment / preview runner]
```

The diagram summarizes the intended components visible in the source and
deployment manifests. Exact runtime routes, data ownership, and
infrastructure wiring depend on the external configuration and
Kubernetes environment.

## Technology Stack

  -----------------------------------------------------------------------
  Area                                Technologies
  ----------------------------------- -----------------------------------
  Language                            Java 21

  Backend framework                   Spring Boot 4

  API gateway                         Spring Cloud Gateway (WebFlux)

  Service discovery                   Netflix Eureka

  Centralized configuration           Spring Cloud Config

  AI integration                      Spring AI, OpenAI model starter

  Inter-service communication         OpenFeign and Apache Kafka

  Persistence                         Spring Data JPA, PostgreSQL

  Database/vector infrastructure      PostgreSQL with pgvector deployment
                                      manifest

  Cache                               Redis

  Object storage                      MinIO

  Authentication                      JWT, Spring Security

  Payments                            Stripe

  Container image build               Jib

  Orchestration                       Kubernetes

  CI/CD                               GitHub Actions, Docker Hub, Google
                                      Cloud/GKE

  Mapping / boilerplate               MapStruct, Lombok
  -----------------------------------------------------------------------

> Dependency versions are defined in the individual Maven POM files.
> Check those files for the exact versions used by each service.

## Repository Structure

``` text
dev-able/
├── account-service/       # Authentication, users, subscriptions, Stripe billing
├── api-gateway/           # Gateway routing, CORS, JWT authentication filter
├── common-lib/            # Shared DTOs, events, security utilities, exceptions
├── config-service/        # Spring Cloud Config Server
├── discovery-service/     # Eureka service registry
├── intelligence-service/  # AI generation, chat streaming, history, usage
├── workspace-service/     # Projects, files, members, storage, deployments
├── k8s/
│   ├── infra/             # Ingress, namespaces, network policies, runner pool
│   ├── proxy/             # Proxy deployment resources
│   ├── services/          # Application service manifests
│   └── stateful/          # Kafka, MinIO, pgvector, Redis manifests
└── .github/workflows/     # GitHub Actions deployment workflows
```

## Request and Generation Flow

A typical AI-assisted project interaction follows this broad flow:

1.  A client sends a request through the API gateway.
2.  The gateway applies its configured routing and JWT authentication
    behavior.
3.  The intelligence service receives a chat request and checks
    project-edit authorization.
4.  Spring AI invokes the configured OpenAI model, with
    project/file-tree context and code-generation tools.
5.  The service streams response chunks to the client using SSE.
6.  Code-generation tools can call workspace operations to interact with
    project files.
7.  Chat and event data are persisted, and Kafka is used for
    asynchronous workflows such as file-storage events where configured.

The exact sequence can vary by operation. Consult the service
implementations and the environment's external configuration for
production behavior.

## Getting Started

### Prerequisites

-   **JDK 21**
-   Git
-   Docker, if you plan to run infrastructure or containers locally
-   Access to the configured OpenAI API
-   PostgreSQL, Redis, Kafka, and MinIO as required by the selected
    services
-   Access to the Spring Cloud Config repository/configuration
-   Kubernetes and `kubectl` only if testing deployment functionality

### Clone the repository

``` bash
git clone https://github.com/MjayK-18/dev-able.git
cd dev-able
```

### Configuration comes first

The application services import their configuration from Spring Cloud
Config. The config service points to a separate Git repository:

`https://github.com/MjayK-18/lovable-config-server`

Make sure you have access to the required configuration and that the
config service can retrieve it. Do not assume the services can start
from this repository alone: their database, broker, AI, security, and
service-discovery settings are environment-dependent.

### Build the shared library

From the repository root:

``` bash
cd common-lib
./mvnw clean install
```

On Windows PowerShell:

``` powershell
.\mvnw.cmd clean install
```

The service POMs depend on `common-lib` version `1.0.0`, so install it
to your local Maven repository before building dependent services.

### Build an application service

For example, to build the intelligence service:

``` bash
cd ../intelligence-service
./mvnw clean package
```

On Windows PowerShell:

``` powershell
.\mvnw.cmd clean package
```

Repeat for `account-service`, `api-gateway`, `config-service`,
`discovery-service`, and `workspace-service` as needed. There is no
root-level aggregator POM in the repository, so builds are run from each
module directory.

> **Running locally:** the exact startup order and required environment
> variables depend on the external configuration. Start the
> infrastructure and configuration/discovery dependencies required by
> your selected service before starting it.

## Configuration

Review each service's `src/main/resources/application.yaml`, the
external config repository, and the Kubernetes manifests to identify the
full configuration requirements.

Common configuration areas include:

-   `CONFIG_SERVER_URL` --- URL of the Spring Cloud Config Server;
    application defaults point to `http://localhost:8888`.
-   PostgreSQL connection details and credentials.
-   Kafka bootstrap servers and topic configuration.
-   Redis connection settings.
-   MinIO endpoint and credentials.
-   OpenAI API credentials and model configuration.
-   JWT signing/verification settings.
-   Stripe API keys and webhook signing secret.
-   Kubernetes deployment settings and cloud credentials.

The names above describe configuration areas; confirm exact property
names in the external configuration before setting them.

Use environment variables, Kubernetes Secrets, or a local untracked
configuration file for sensitive values. Never commit populated secrets
or credentials.

## Building the Services

Each service has its own Maven project and Maven Wrapper. The project
uses Java 21 and Spring Boot 4. A typical build command from a service
directory is:

``` bash
./mvnw clean package
```

For container images, the service POMs configure the Jib Maven plugin.
The GitHub Actions workflows use Jib to build and push images to Docker
Hub, tagging each image with the commit SHA and `latest`.

Build the shared library before dependent services:

``` bash
cd common-lib
./mvnw clean install
```

Then build the required service from its own directory.

## Kubernetes Deployment

The `k8s/` directory contains resources for:

-   Application services and frontend.
-   Ingress and namespaces.
-   Core and preview network policies.
-   A runner pool for project execution/preview workloads.
-   Kafka, MinIO, pgvector, and Redis infrastructure.
-   A proxy deployment.

Before applying any manifest:

1.  Review namespaces, image names, ports, resource limits, and service
    references.
2.  Supply the required Secrets and ConfigMaps.
3.  Verify the cluster has the required permissions and infrastructure.
4.  Apply manifests in the appropriate dependency order.
5.  Check rollout status, pod health, logs, and service connectivity.

Example inspection commands:

``` bash
kubectl get namespaces
kubectl get pods -A
kubectl get services -A
kubectl get deployments -A
```

The manifests are environment-specific; review them before applying to a
cluster.

## CI/CD

GitHub Actions workflows are included for the account service, API
gateway, and config service. These workflows run on pushes to configured
branches when files under the relevant service directory change.

The workflows generally:

1.  Check out the repository.
2.  Set up JDK 21.
3.  Build the service image with Jib.
4.  Push the image to Docker Hub using the commit SHA as a tag.
5.  Authenticate to Google Cloud using Workload Identity Federation.
6.  Fetch GKE credentials.
7.  Update the Kubernetes deployment image and wait for rollout status.

Configure the required GitHub Actions secrets in repository settings.
The workflow references include Docker Hub credentials and GCP Workload
Identity/GKE settings. Review the workflows before enabling deployments
in your own environment.

## Security Notes

-   Keep OpenAI, Stripe, database, Redis, MinIO, Docker Hub, and GCP
    credentials out of source control.
-   Use Kubernetes Secrets or an approved secret-management system for
    deployment credentials.
-   Restrict internal service endpoints and review network policies
    before exposing services.
-   Validate authentication and authorization at each service boundary,
    not only at the gateway.
-   Verify Stripe webhook signatures and use the correct webhook secret
    for each environment.
-   Review the preview/runner isolation model before running untrusted
    generated code.

## Roadmap

Potential next steps to improve the developer and operator experience:

-   Add a Docker Compose profile for repeatable local development.
-   Document exact environment variables and provide a safe
    `.env.example`.
-   Add API examples and response schemas for the primary endpoints.
-   Add an end-to-end sequence diagram for AI generation and deployment.
-   Expand integration and failure-path tests across service boundaries.
-   Document deployment prerequisites, rollback steps, and
    observability.
-   Add product screenshots or a demo link if a frontend demo is
    available.

## License

No license was verified in the supplied source archive. Add a `LICENSE`
file and update this section if you want to specify reuse and
distribution terms.

------------------------------------------------------------------------

Built by [MjayK-18](https://github.com/MjayK-18).
