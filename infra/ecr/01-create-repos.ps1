# FASE 2.1 - Crear un repositorio ECR por cada microservicio
#
# Requisitos: AWS CLI v2 configurado (aws configure / aws sso login).
# Uso:
#   cd infra/ecr
#   ./01-create-repos.ps1

$ErrorActionPreference = "Stop"

$REGION = "us-east-1"

$ACCOUNT_ID = aws sts get-caller-identity --query Account --output text
Write-Host "Account ID: $ACCOUNT_ID"
Write-Host "Region:     $REGION"

$repos = @(
    "innovatech/ms-api-gateway",
    "innovatech/ms-autenticacion",
    "innovatech/ms-recursos-colaboraciones",
    "innovatech/ms-gestion-proyectos",
    "innovatech/ms-analiticas"
)

foreach ($repo in $repos) {
    Write-Host "`n== Repositorio: $repo =="
    try {
        aws ecr describe-repositories --repository-names $repo --region $REGION | Out-Null
        Write-Host "Ya existe, se omite."
    } catch {
        aws ecr create-repository --repository-name $repo --region $REGION | Out-Null
        Write-Host "Creado."
    }
}

Write-Host "`nListo. Registry: $ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com"
