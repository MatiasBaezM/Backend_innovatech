# FASE 3 - Crear la base de datos RDS (MySQL) dentro de la VPC ya usada por EKS
#
# Reutiliza la VPC y subnets definidas en infra/eks-cluster.yaml (vpc default
# del entorno AWS Academy / LabRole), en lugar de crear una VPC nueva con
# peering. El aislamiento de RDS se logra con un Security Group dedicado que
# solo permite el puerto 3306 desde dentro de esa VPC; la instancia se crea
# con --no-publicly-accessible (sin IP publica) aunque las subnets sean
# "publicas".
#
# Requisitos: AWS CLI v2 configurado.
# Uso:
#   cd infra/rds
#   ./setup-rds.ps1

$ErrorActionPreference = "Stop"

$REGION  = "us-east-1"

# Misma VPC y subnets (us-east-1a / us-east-1b) que infra/eks-cluster.yaml
$VPC_ID   = "vpc-07a0ffde3f8d510dc"
$SUBNET_A = "subnet-0a5b6b9f33eb9fb8f"   # us-east-1a
$SUBNET_B = "subnet-02bc61731ed2fd002"   # us-east-1b

# IMPORTANTE: la password NO se hardcodea en el repo. Definila antes de
# correr el script:
#   $env:INNOVATECH_DB_PASSWORD = "TuPasswordSegura123!"
# Debe coincidir con SPRING_DATASOURCE_PASSWORD en infra/k8s/02-secret.yaml
# (ver instrucciones en ese archivo para generar el valor en base64).
if (-not $env:INNOVATECH_DB_PASSWORD) {
    throw "Falta definir la variable de entorno INNOVATECH_DB_PASSWORD"
}
$DB_PASSWORD = $env:INNOVATECH_DB_PASSWORD

# 1. CIDR de la VPC, para restringir el acceso a RDS a trafico intra-VPC
$VPC_CIDR = aws ec2 describe-vpcs --vpc-ids $VPC_ID --region $REGION --query "Vpcs[0].CidrBlock" --output text
Write-Host "VPC: $VPC_ID ($VPC_CIDR)"

# 2. Security Group para RDS
$SG_RDS = aws ec2 create-security-group `
    --group-name innovatech-rds-sg `
    --description "Acceso MySQL para Innovatech (solo intra-VPC)" `
    --vpc-id $VPC_ID `
    --region $REGION `
    --query "GroupId" --output text
Write-Host "Security Group RDS: $SG_RDS"

aws ec2 authorize-security-group-ingress `
    --group-id $SG_RDS `
    --protocol tcp --port 3306 --cidr $VPC_CIDR `
    --region $REGION | Out-Null

# 3. DB Subnet Group (2 AZs requeridas por RDS)
aws rds create-db-subnet-group `
    --db-subnet-group-name innovatech-db-subnet `
    --db-subnet-group-description "Innovatech DB subnet group" `
    --subnet-ids $SUBNET_A $SUBNET_B `
    --region $REGION | Out-Null
Write-Host "DB Subnet Group creado: innovatech-db-subnet"

# 4. Instancia RDS MySQL
aws rds create-db-instance `
    --db-instance-identifier innovatech-mysql `
    --db-instance-class db.t3.micro `
    --engine mysql --engine-version 8.0 `
    --master-username admin `
    --master-user-password $DB_PASSWORD `
    --allocated-storage 20 `
    --db-name innovatech_db `
    --vpc-security-group-ids $SG_RDS `
    --db-subnet-group-name innovatech-db-subnet `
    --no-publicly-accessible `
    --backup-retention-period 7 `
    --region $REGION | Out-Null

Write-Host "`nCreando instancia RDS 'innovatech-mysql' (tarda 5-10 minutos)..."
Write-Host "`nVerificar estado:"
Write-Host "  aws rds describe-db-instances --db-instance-identifier innovatech-mysql --region $REGION --query 'DBInstances[0].DBInstanceStatus'"
Write-Host "`nObtener endpoint (cuando este 'available'):"
Write-Host "  aws rds describe-db-instances --db-instance-identifier innovatech-mysql --region $REGION --query 'DBInstances[0].Endpoint.Address' --output text"
Write-Host "`nUsa ese endpoint para reemplazar <RDS_ENDPOINT> en infra/k8s/01-configmap.yaml"
