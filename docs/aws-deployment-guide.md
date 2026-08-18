# AWS Deployment Walkthrough (Free Tier Optimized)

This guide provides a step-by-step walkthrough for deploying the Zomato UC microservices architecture to AWS, optimized for the **AWS Free Tier**. 

> [!WARNING]
> **Free Tier Realities & 1-2 Hour Testing Strategies**
> AWS does **not** automatically provide $100-$200 in free credits for new accounts (unless you have a specific promotional code like AWS Activate or GitHub Student). Instead, new accounts get 12 months of **Free Tier Usage Limits** (e.g., 750 hours/month of `t2.micro` or `t3.micro` EC2 instances).
>
> **The Microservices Memory Challenge**: You cannot run 9 Spring Boot microservices on a single 1GB RAM `t2.micro` instance. 
> 
> **How to test for 1-2 hours practically for free (or pennies):**
> **Option A (Strictly $0.00)**: You can launch **multiple** `t2.micro` instances (e.g., 5 instances) and attach them to your ECS cluster. Since the Free Tier provides 750 hours per month, running 5 instances for 2 hours equals 10 compute hours. This is well within your 750-hour limit and completely free.
> **Option B (Pennies)**: You can launch a single, powerful `t3.xlarge` instance (4 vCPU, 16GB RAM) which easily fits all services. It is not in the Free Tier, but it only costs ~$0.16 per hour. Testing for 2 hours will cost you literally ~$0.32. Just remember to **terminate everything immediately** after testing.

---

## Phase 1: AWS Account Setup & Prerequisites

### 1. Create an AWS Account
1. Go to [aws.amazon.com](https://aws.amazon.com/) and click **Create an AWS Account**.
2. Follow the signup process (requires a valid credit/debit card for identity verification; a $1 temporary hold is placed and refunded).
3. Once logged in as the **Root User**, set up MFA (Multi-Factor Authentication) immediately.

### 2. Create an IAM Admin User (Security Best Practice)
1. Search for **IAM** (Identity and Access Management) in the top search bar.
2. Go to **Users** -> **Create user**.
3. Name the user (e.g., `admin`). Check **Provide user access to the AWS Management Console**.
4. In permissions, choose **Attach policies directly** and select `AdministratorAccess`.
5. Log out of the root account and log in using this new IAM user account.

### 3. Install AWS CLI Locally
1. Download and install the [AWS CLI](https://aws.amazon.com/cli/).
2. In your terminal, run `aws configure`.
3. Enter your IAM User's **Access Key ID** and **Secret Access Key** (generate these from the IAM User console -> Security Credentials -> Create Access Key).
4. Default region: `ap-south-1` (Mumbai) or your preferred region.

---

## Phase 2: Provisioning Databases (Stateful Services)

### 1. Amazon RDS (PostgreSQL)
1. Search for **RDS** in the AWS console. Click **Create database**.
2. **Method**: Standard create.
3. **Engine**: PostgreSQL.
4. **Templates**: **Free tier** (This is crucial!).
5. **Settings**:
   - DB instance identifier: `zomato-rds`
   - Master username: `admin` (or your choice)
   - Master password: Create a strong password.
6. **Instance configuration**: `db.t3.micro` or `db.t4g.micro` (Included in Free Tier).
7. **Storage**: 20 GB General Purpose SSD (gp2).
8. **Connectivity**: Choose **Publicly accessible** if you want to connect from your local pgAdmin to run the `rds-init.sql` script (Ensure the Security Group allows inbound TCP port 5432 only from your specific IP address).
9. Click **Create database**. Once available, copy the **Endpoint** URL.

### 2. Amazon OpenSearch (Elasticsearch)
> [!NOTE]
> OpenSearch provides a free tier of 750 hours per month of a `t2.small.search` or `t3.small.search` instance and 10GB of storage.
1. Search for **OpenSearch Service**. Click **Create domain**.
2. **Domain creation method**: Standard create.
3. **Templates**: **Dev/test** (There is no explicit "Free tier" template, you must manually select the free tier instance).
4. **Deployment options**: Standby (1-AZ).
5. **Instance type**: Change to `t3.small.search`.
6. **Storage**: 10 GB EBS.
7. **Network**: Public access (for ease of testing, but restrict IP addresses).
8. **Fine-grained access control**: Create a master user and password.
9. Click **Create domain**.

### 3. Amazon ElastiCache (Redis)
> [!NOTE]
> ElastiCache provides 750 hours/month of a `cache.t2.micro` or `cache.t3.micro` node.
1. Search for **ElastiCache**. Click **Create cluster**.
2. Select **Redis OSS**.
3. **Cluster mode**: Disabled.
4. **Node type**: Change to `cache.t3.micro`.
5. Click **Create**.

---

## Phase 3: Infrastructure Configuration

### 1. AWS Secrets Manager (or Parameter Store)
> [!IMPORTANT]
> Secrets Manager offers a **30-day free trial**, after which it costs $0.40/secret/month. To stay 100% free indefinitely, you would need to use AWS Systems Manager Parameter Store instead, but Secrets Manager is industry standard.
1. Search for **Secrets Manager**. Click **Store a new secret**.
2. Select **Other type of secret**.
3. Add key/value pairs for your database and Keycloak credentials:
   - `DB_USERNAME`: admin
   - `DB_PASSWORD`: [your-rds-password]
4. Name the secret: `zomato/rds-credentials`.
5. Repeat for external API keys (`GROQ_API_KEY`, etc.).
6. Note the **Secret ARNs**; you will need to update `deployment/aws/ecs-task-definitions.json` with these ARNs.

### 2. Elastic Container Registry (ECR)
> [!NOTE]
> ECR offers 500 MB per month of free storage. You will exceed this with 9 Java images. To avoid small charges (few cents/dollars), delete images when not actively testing.
1. Search for **ECR** and click **Create repository**.
2. Create a repository for **each** service (e.g., `api-gateway`, `user-service`, `discovery-server`). Keep them Private.
3. Once created, click on a repository and click **View push commands**. Use these commands locally to build and push your Docker images.

---

## Phase 4: Deploying to ECS (Elastic Container Service)

Because of the Free Tier RAM limitations, we will configure an ECS cluster using EC2 instances rather than Fargate (Fargate has no free tier).

### 1. Create an ECS Cluster
1. Search for **ECS**. Click **Clusters** -> **Create cluster**.
2. Name: `zomato-cluster`.
3. **Infrastructure**: Select **Amazon EC2 instances**.
4. **Provisioning model**: On-demand.
5. **EC2 instance type**: `t2.micro` or `t3.micro` (Free tier eligible). 
   *Note: If you want all services to run, change this to a `t3.xlarge` (Not free).*
6. Click **Create**.

### 2. Update Task Definitions
1. Open `deployment/aws/ecs-task-definitions.json`.
2. Update `<AWS_ACCOUNT_ID>`, `<RDS_ENDPOINT>`, and `<ACCOUNT_ID>` placeholders with your actual AWS Account ID and RDS Endpoint.
3. Ensure the memory allocations in the JSON are as small as possible if you are attempting to run them on small instances.

### 3. Register Task Definitions
In your local terminal, register the task definitions with ECS:
```bash
aws ecs register-task-definition --cli-input-json file://deployment/aws/ecs-task-definitions.json
```

### 4. Create ECS Services
For each task definition you registered, you need to run it as a Service.
1. In the ECS Console, go to your `zomato-cluster`.
2. In the Services tab, click **Create**.
3. **Compute options**: Capacity provider strategy (Use the EC2 instances).
4. **Task definition**: Select the family (e.g., `discovery-server-task`).
5. **Service name**: `discovery-server`.
6. **Desired tasks**: 1.
7. Click **Create**.

> **Crucial Order of Operations**:
> 1. Deploy `discovery-server` first.
> 2. Wait for it to be healthy.
> 3. Deploy `keycloak-task` and stateful dependent services.
> 4. Deploy all backend microservices (`user-service`, `restaurant-service`, etc.).
> 5. Deploy `api-gateway` last.

---

## Phase 5: Exposing to the Internet (ALB)

> [!NOTE]
> Elastic Load Balancing provides 750 hours/month free.
1. Search for **EC2**, go to **Load Balancers** on the left menu.
2. Click **Create Load Balancer** -> **Application Load Balancer**.
3. Name: `zomato-alb`.
4. Scheme: **Internet-facing**.
5. Network mapping: Select at least two subnets.
6. **Security Groups**: Create a new SG that allows HTTP (80) and HTTPS (443) from Anywhere.
7. **Listeners and Routing**: Create a Target Group.
   - Target type: Instances.
   - Point the target group to the EC2 instance(s) running your ECS cluster.
   - Specifically, set the port to the host port mapped to your `api-gateway` (e.g., 8080).
8. Once created, copy the **DNS name** of the ALB. This is your `<YOUR_AWS_ALB_DNS_NAME>`.

## Phase 6: Post-Deployment Verification

1. Take the ALB DNS name and update your `.env.aws.example` and ECS task definitions if you haven't already.
2. Connect to your RDS instance using pgAdmin or DBeaver. Open `deployment/aws/rds-init.sql` and run it to create the individual databases for your microservices.
3. Verify the Eureka dashboard by port-forwarding or checking the ECS logs to ensure all microservices have successfully registered.
4. Test the API Gateway by sending a POST request to `http://<ALB-DNS-NAME>/api/users/register` (or similar).

---
## Summary of Free Tier Caveats
- **Compute (ECS/EC2)**: Running all 9 Java microservices concurrently **will not fit** in a single Free Tier 1GB RAM limit. You must use either **Option A** (multiple `t2.micro` instances, keeping total monthly hours under 750) or **Option B** (a single paid instance like `t3.xlarge` for pennies per hour during your test).
- **Storage (ECR)**: 500MB limit. 9 Java container images will exceed 1.5GB. You will be charged a few cents/dollars for the extra storage over a month, but it will be negligible for a 2-hour test.
- **Secrets Manager**: Free for 30 days only.

To avoid unexpected charges, **terminate all resources (RDS, OpenSearch, EC2, ElastiCache) when you are done testing.**
