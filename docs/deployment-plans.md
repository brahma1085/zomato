# AWS Deployment Plan

## 1. Objective

Deploy the existing application using AWS managed services while keeping the frontend on Vercel.

Target:

```text
Vercel
   |
   v
Application Load Balancer
   |
   v
Amazon ECS
   |
   +-- 9 Spring Boot microservices
   +-- Keycloak
   |
   +-- Amazon RDS PostgreSQL
   +-- Amazon OpenSearch Service
   +-- Amazon S3
   +-- AWS Secrets Manager
   +-- Amazon CloudWatch
```

The AWS environment is primarily a learning environment. Cost controls are mandatory.

---

# 2. Important AWS Free Tier / Cost Position

AWS changed its Free Tier model for new accounts created from July 15, 2025.

New AWS customers receive:

- $100 AWS credits at signup
- potential additional credits up to $100
- Free account plan access for eligible services for up to six months
- Always Free offers for selected services

The exact eligibility of each service depends on the account creation date, selected Free/Paid plan, region, resource type and current AWS offer.

Therefore:

> Never assume that creating an AWS resource means it is free.

Check the AWS Free Tier page and the Billing console before deploying each resource.

Create billing alerts before creating the infrastructure.

---

# 3. AWS Region

Choose one AWS region and keep the initial deployment there.

Recommended for India:

```text
ap-south-1
Mumbai
```

Benefits:

- lower latency from India
- good AWS service availability
- useful AWS learning experience
- consistent regional deployment

Do not spread the initial learning environment across multiple regions.

---

# 4. AWS Account Setup

After creating the account:

## 4.1 Root account

Use the root account only for:

- initial account setup
- billing configuration
- account-level security

Do not use root for daily development.

## 4.2 Create administrative IAM identity

Create an IAM user/role for daily administration.

Enable MFA.

## 4.3 Billing protection

Configure:

```text
AWS Budgets
CloudWatch Billing Alerts
Free Tier usage monitoring
```

Create a small budget/alert threshold.

Recommended alerts:

```text
25%
50%
75%
90%
100%
```

The exact budget amount should reflect your acceptable learning spend.

---

# 5. AWS Services to Create

Initial target:

```text
1. VPC
2. Subnets
3. Internet Gateway
4. Security Groups
5. ECR
6. ECS Cluster
7. ALB
8. RDS PostgreSQL
9. OpenSearch
10. Keycloak
11. Secrets Manager
12. CloudWatch Logs
13. S3
14. IAM Roles
15. ACM
16. Route 53 (optional)
```

Do not create everything at once.

---

# 6. Phase 0 — Billing Guardrails

Before infrastructure:

- [ ] Verify AWS account plan.
- [ ] Confirm available credits.
- [ ] Open Billing > Free Tier.
- [ ] Open Billing > Credits.
- [ ] Create a budget.
- [ ] Configure billing alerts.
- [ ] Confirm the selected AWS region.
- [ ] Understand which services are currently eligible for your account.

This phase is mandatory.

---

# 7. Phase 1 — VPC

Create:

```text
VPC
CIDR:
10.0.0.0/16
```

Initial subnet design:

```text
VPC 10.0.0.0/16

Public subnet A
10.0.1.0/24

Public subnet B
10.0.2.0/24

Private subnet A
10.0.11.0/24

Private subnet B
10.0.12.0/24
```

Concept:

```text
                 Internet
                    |
             Internet Gateway
                    |
             +------+------+
             |             |
         Public A       Public B
             |             |
            ALB           ALB
             |
       Private subnets
       |             |
      ECS           ECS
       |             |
      RDS        OpenSearch
```

---

# 8. NAT Gateway Cost Warning

Do not automatically create a NAT Gateway just because many AWS tutorials do.

NAT Gateway can introduce meaningful charges.

For the initial learning deployment, deliberately design around this requirement.

Options:

- keep initial tasks in public subnets with tightly controlled inbound access, or
- use VPC endpoints where appropriate, or
- use an architecture that does not require NAT for the first stage

For a production deployment, revisit this decision and normally use private subnets plus appropriate egress controls.

---

# 9. Security Groups

Create separate security groups.

Example:

```text
sg-alb
sg-ecs
sg-rds
sg-opensearch
```

Rules:

## ALB

Inbound:

```text
443 from Internet
80 from Internet
```

Port 80 should redirect to HTTPS.

## ECS

Inbound:

```text
Application ports only from sg-alb
```

Do not allow:

```text
0.0.0.0/0
```

to backend application ports.

## RDS

Inbound:

```text
5432 from sg-ecs
```

## OpenSearch

Inbound:

```text
443 from sg-ecs
```

Do not expose RDS/OpenSearch publicly.

---

# 10. Phase 2 — ECR

Create one repository per independently deployed image.

Example:

```text
restaurant-service
user-service
search-service
recommendation-service
review-service
location-service
notification-service
payment-service
api-gateway
keycloak
```

If the application has different names, retain the existing service names.

Build:

```bash
docker buildx build   --platform linux/amd64,linux/arm64   -t <repository>:<version>   --push .
```

For the first AWS deployment, choose one architecture consistently for ECS.

---

# 11. Phase 3 — CloudWatch Logs

Create log groups:

```text
/aws/ecs/service-1
/aws/ecs/service-2
...
/aws/ecs/service-9
/aws/ecs/keycloak
```

Set retention deliberately.

Do not keep unlimited logs in the learning account.

---

# 12. Phase 4 — RDS PostgreSQL

Create a PostgreSQL RDS instance eligible for the current AWS Free Tier/credits for your account.

Initial development configuration:

```text
Engine:
PostgreSQL

Deployment:
Single-AZ / development configuration

Storage:
Minimum practical size

Backups:
Keep only what is appropriate for the learning environment
```

Do not choose Multi-AZ for the free learning environment unless you intentionally accept the cost.

Database:

```text
application_db
```

Create separate users where appropriate.

Example:

```text
application_user
keycloak_user
```

---

# 13. RDS Security

RDS must not have a public security-group rule.

Desired:

```text
ECS
 |
 | TCP 5432
 v
RDS
```

Not:

```text
Internet
 |
 | TCP 5432
 v
RDS
```

---

# 14. Phase 5 — OpenSearch

Create Amazon OpenSearch Service only after verifying the current Free Tier/credit impact in the AWS console.

Initial development goal:

```text
Single-node / smallest practical eligible configuration
```

Do not create a production-sized OpenSearch cluster for the learning environment.

Configure access so the application reaches OpenSearch privately.

Application:

```text
ECS
 |
 | HTTPS
 v
OpenSearch
```

---

# 15. OpenSearch Compatibility Test

Before migrating application search:

1. Create a test index.
2. Create the required mappings.
3. Index sample restaurant data.
4. Run the application's main search queries.
5. Test filters.
6. Test sorting.
7. Test geo-distance queries.
8. Test aggregations.
9. Test autocomplete if applicable.
10. Compare results with local Elasticsearch.

Only then migrate the complete search workload.

---

# 16. Phase 6 — Secrets Manager

Create secrets for:

```text
RDS username
RDS password
Keycloak database credentials
Keycloak admin bootstrap credentials
OpenSearch credentials/API authentication if required
Third-party API keys
```

Do not put secrets into:

- Dockerfile
- Git
- ECS task-definition plaintext
- application.yml

---

# 17. Phase 7 — ECS Cluster

Create:

```text
ECS Cluster
    |
    +-- API Gateway
    +-- Service 1
    +-- Service 2
    +-- ...
    +-- Service 9
    +-- Keycloak
```

For learning, first deploy only:

```text
API Gateway
Service 1
```

Verify the complete path before deploying the remaining services.

---

# 18. ECS Launch Strategy

Use two learning stages.

## Stage A — Fargate

Use Fargate to learn:

- ECS task definitions
- ECS services
- networking
- load balancing
- health checks
- service deployment
- CloudWatch logs

Important:

Fargate charges are based on requested vCPU/memory/time; ECS itself has no separate management fee for Fargate. Verify that your task sizes fit your available Free Tier credits before running continuously.

## Stage B — ECS on EC2

After learning Fargate, deploy ECS on EC2.

This teaches:

- ECS capacity providers
- EC2 container instances
- Docker runtime
- ECS agent
- cluster capacity
- task placement
- scaling

This is valuable for your AWS/DevOps learning.

---

# 19. ECS Task Definition

Each service gets a task definition.

Example:

```text
restaurant-service-task
```

Container:

```text
Name:
restaurant-service

Image:
<account>.dkr.ecr.<region>.amazonaws.com/restaurant-service:<version>

Port:
8080

Environment:
DB_HOST
DB_PORT
DB_NAME
KEYCLOAK_ISSUER_URI
SEARCH_ENDPOINT
FRONTEND_ORIGIN
```

Secrets should be injected from Secrets Manager.

---

# 20. ECS Resource Sizing

Do not start with large resources.

For each Spring Boot service:

```text
CPU:
smallest practical task CPU

Memory:
smallest practical memory
```

Measure actual:

- startup memory
- idle memory
- peak memory
- CPU usage

Then increase resources.

For the 9-service architecture, resource optimization is essential.

---

# 21. Phase 8 — Keycloak on ECS

Deploy Keycloak as an ECS service.

Architecture:

```text
ALB
 |
 +--> auth.example.com
          |
          v
       Keycloak
          |
          v
      RDS PostgreSQL
```

Keycloak should use RDS rather than an ephemeral container database.

Configure:

```text
KC_DB=postgres
KC_DB_URL=<RDS connection>
KC_DB_USERNAME=<secret>
KC_DB_PASSWORD=<secret>
KC_HOSTNAME=auth.example.com
```

Use the current Keycloak container configuration documented for the exact version being deployed.

---

# 22. Phase 9 — Application Load Balancer

Create:

```text
Internet
   |
   v
ALB
```

Listeners:

```text
HTTP :80
HTTPS :443
```

Redirect:

```text
80 -> 443
```

Use ACM for TLS.

Routing can be:

```text
api.example.com
    |
    v
API Gateway / backend ECS

auth.example.com
    |
    v
Keycloak
```

Alternatively use path-based routing if your application design requires it.

---

# 23. Phase 10 — ACM

Request an ACM certificate for:

```text
example.com
api.example.com
auth.example.com
```

or the exact domain structure you choose.

Use DNS validation.

Do not manage TLS certificates manually inside containers when ALB can terminate TLS.

---

# 24. Phase 11 — Vercel Integration

Keep Angular on Vercel.

Configure production environment variables:

```text
API_BASE_URL=https://api.example.com
KEYCLOAK_URL=https://auth.example.com
KEYCLOAK_REALM=<realm>
KEYCLOAK_CLIENT_ID=<client>
```

Update backend CORS:

```text
https://<your-vercel-domain>
```

Test:

```text
Browser
  |
  v
Vercel
  |
  +--> Keycloak
  |
  +--> AWS API
```

---

# 25. Phase 12 — S3

Use S3 if the application stores:

- images
- user uploads
- exports
- documents
- generated files

Create:

```text
application-assets-<environment>
```

Use IAM roles instead of embedding access keys.

---

# 26. Phase 13 — IAM

Create separate roles:

```text
ECS execution role
ECS task role
Deployment role
Developer/admin role
```

Principle:

```text
Least privilege
```

For example:

```text
ECS task
   |
   +--> Read specific Secrets Manager secrets
   +--> Write CloudWatch logs
   +--> Read/write specific S3 bucket
```

Do not attach AdministratorAccess to application tasks.

---

# 27. Phase 14 — CI/CD

Recommended pipeline:

```text
Developer
   |
   v
GitHub
   |
   v
Build
   |
   +--> Unit Tests
   |
   +--> Integration Tests
   |
   +--> Docker Build
   |
   +--> Security Scan
   |
   v
Amazon ECR
   |
   v
ECS Deployment
   |
   v
ALB Health Check
   |
   v
Deployment Successful
```

Use GitHub Actions initially.

You can later recreate the same pipeline using Jenkins to reinforce your existing Jenkins knowledge.

---

# 28. Deployment Order

Do not deploy the entire application in one attempt.

Use this sequence:

```text
1. AWS account + billing guardrails
2. IAM
3. VPC
4. Security Groups
5. ECR
6. CloudWatch
7. RDS
8. OpenSearch
9. ECS cluster
10. One Spring Boot service
11. ALB
12. HTTPS
13. Keycloak
14. Authentication integration
15. Remaining services
16. S3
17. CI/CD
18. Monitoring
```

---

# 29. First Successful Deployment Milestone

The first milestone should be:

```text
Vercel
   |
   v
ALB HTTPS
   |
   v
ECS
   |
   v
One Spring Boot service
   |
   v
RDS PostgreSQL
```

Do not introduce all 9 services until this works.

---

# 30. Second Milestone

Add:

```text
Keycloak
   |
   v
RDS PostgreSQL

Angular
   |
   v
Keycloak
   |
   v
JWT
   |
   v
Spring Boot
```

Verify login and JWT validation.

---

# 31. Third Milestone

Add:

```text
OpenSearch
```

Then verify:

```text
Spring Boot
   |
   v
OpenSearch
```

---

# 32. Fourth Milestone

Deploy all remaining services:

```text
MS1
MS2
MS3
MS4
MS5
MS6
MS7
MS8
MS9
```

Test service-to-service communication.

---

# 33. Fifth Milestone — CI/CD

Push code:

```text
GitHub
   |
   v
CI
   |
   v
ECR
   |
   v
ECS
```

Verify a code change reaches AWS without manual Docker image copying.

---

# 34. Production-Like Security Checklist

- [ ] MFA enabled.
- [ ] Root account not used for development.
- [ ] No hard-coded secrets.
- [ ] RDS private.
- [ ] OpenSearch private.
- [ ] Backend ports not public.
- [ ] ALB is the public entry point.
- [ ] HTTPS enabled.
- [ ] CORS restricted.
- [ ] IAM least privilege.
- [ ] S3 bucket public access blocked unless explicitly required.
- [ ] CloudWatch log retention configured.
- [ ] AWS Budget configured.
- [ ] Unused resources removed.

---

# 35. Cost-Control Checklist

Before stopping work each day:

```text
[ ] Check ECS services
[ ] Check running EC2 instances
[ ] Check Fargate tasks
[ ] Check RDS
[ ] Check OpenSearch
[ ] Check ALB
[ ] Check NAT Gateway
[ ] Check public IPv4
[ ] Check EBS volumes
[ ] Check CloudWatch logs
[ ] Check S3
```

During learning, destroy resources that are not needed.

Do not assume "stopped" means "zero cost" for every AWS service.

---

# 36. Development vs Learning vs Production

## Local

```text
Docker Compose
PostgreSQL
Elasticsearch
Keycloak
9 services
```

## AWS Learning

```text
Vercel
ALB
ECS
Keycloak
RDS
OpenSearch
ECR
CloudWatch
Secrets Manager
S3
IAM
VPC
```

## Future Production

```text
Vercel / CloudFront
      |
WAF
      |
ALB
      |
ECS/EKS
      |
Private services
      |
RDS Multi-AZ
OpenSearch cluster
S3
Secrets Manager
CloudWatch
X-Ray/OpenTelemetry
```

Do not build the production architecture yet.

---

# 37. Rollback Strategy

Every image must have a version:

```text
service:1.0.0
service:1.0.1
service:git-a1b2c3d
```

Never rely exclusively on:

```text
latest
```

If deployment fails:

```text
ECS
 |
 v
Previous task definition
 |
 v
Previous image
```

Roll back to the last known-good version.

---

# 38. Observability Roadmap

Initial:

```text
CloudWatch Logs
CloudWatch Metrics
ALB metrics
ECS metrics
RDS metrics
OpenSearch metrics
```

Later:

```text
OpenTelemetry
     |
     v
Distributed tracing
```

Recommended correlation:

```text
Frontend request
      |
      v
ALB
      |
      v
Service A
      |
      v
Service B
      |
      v
Service C
```

Carry a correlation/trace ID through the calls.

---

# 39. Final Target Architecture

```text
                         VERCEL
                    Angular Frontend
                           |
                           | HTTPS
                           v
                    Route 53 / DNS
                           |
                           v
                    ACM TLS Certificate
                           |
                           v
                 Application Load Balancer
                           |
          +----------------+----------------+
          |                                 |
          v                                 v
     API / Backend                      Keycloak
          |                                 |
          v                                 v
     Amazon ECS                        RDS PostgreSQL
          |
    +-----+-----+
    |     |     |
   MS1   MS2   MS3
    |
   ...
    |
   MS9
    |
    +------------------+
    |                  |
    v                  v
RDS PostgreSQL      OpenSearch
    |
    v
CloudWatch

Supporting:
IAM
Secrets Manager
ECR
S3
VPC
Security Groups
Budgets
```

---

# 40. Completion Criteria

The AWS deployment is complete when:

- [ ] Vercel frontend works against AWS.
- [ ] HTTPS works.
- [ ] ALB routes correctly.
- [ ] Keycloak login works.
- [ ] JWT validation works.
- [ ] All 9 microservices are deployed.
- [ ] Service-to-service calls work.
- [ ] RDS PostgreSQL works.
- [ ] OpenSearch search works.
- [ ] S3 upload/download works where applicable.
- [ ] Secrets are managed by Secrets Manager.
- [ ] Logs appear in CloudWatch.
- [ ] ECS health checks work.
- [ ] Failed containers restart.
- [ ] A new Docker image can be deployed through CI/CD.
- [ ] Previous version can be restored.
- [ ] No backend database/search ports are public.
- [ ] Billing alerts are active.
- [ ] Unused AWS resources can be identified and removed.

---

# 41. Recommended Learning Sequence

Learn each AWS concept while implementing it:

```text
1. IAM
2. Regions / AZs
3. VPC
4. Subnets
5. Internet Gateway
6. Security Groups
7. ECR
8. Docker on AWS
9. ECS
10. Fargate
11. ALB
12. RDS
13. Keycloak
14. OpenSearch
15. Secrets Manager
16. CloudWatch
17. S3
18. Route 53
19. ACM
20. CI/CD
21. ECS on EC2
22. Autoscaling
23. Observability
24. Cost optimization
```

This sequence intentionally takes you from basic AWS networking through a complete containerized microservices deployment.
