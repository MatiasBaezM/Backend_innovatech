# FASE 3.2 - Crear la instancia RDS MySQL
#
# Genera una password aleatoria (no se expone en el historial de comandos),
# crea la instancia RDS usando el SG/subnet group de setup-rds.ps1, guarda la
# password en infra/rds/.db-password (gitignored) y actualiza
# infra/k8s/02-secret.yaml con su valor en base64.
#
# Requisitos: SG innovatech-rds-sg y DB subnet group innovatech-db-subnet ya
# creados (ver setup-rds.ps1).
# Uso:
#   cd infra/rds
#   ./02-create-db-instance.ps1

$ErrorActionPreference = "Continue"

$REGION = "us-east-1"
$SG_RDS = (aws ec2 describe-security-groups --filters "Name=group-name,Values=innovatech-rds-sg" --region $REGION --query "SecurityGroups[0].GroupId" --output text).Trim()

# Password aleatoria de 20 caracteres + sufijo fijo
$chars = (48..57) + (65..90) + (97..122)
$DB_PASSWORD = -join ((1..20) | ForEach-Object { [char](Get-Random -InputObject $chars) })
$DB_PASSWORD = "$DB_PASSWORD!2026"

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

if ($LASTEXITCODE -ne 0) { throw "create-db-instance fallo (exit $LASTEXITCODE)" }

$projectRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")

# Guarda la password en texto plano localmente (no se commitea, ver .gitignore)
$pwFile = Join-Path $PSScriptRoot ".db-password"
Set-Content -Path $pwFile -Value $DB_PASSWORD -NoNewline

# Actualiza infra/k8s/02-secret.yaml con la password en base64
$base64 = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($DB_PASSWORD))
$secretPath = Join-Path $projectRoot "infra/k8s/02-secret.yaml"
(Get-Content $secretPath -Raw) -replace '<BASE64_DB_PASSWORD>', $base64 | Set-Content $secretPath -NoNewline

Write-Host "Instancia RDS 'innovatech-mysql' creandose (tarda 5-10 minutos)."
Write-Host "Password guardada en: $pwFile (no se commitea, ver .gitignore)"
Write-Host "infra/k8s/02-secret.yaml actualizado con la password en base64."
Write-Host "`nVerificar estado:"
Write-Host "  aws rds describe-db-instances --db-instance-identifier innovatech-mysql --region $REGION --query 'DBInstances[0].DBInstanceStatus'"
