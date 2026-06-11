# FASE 2.2 - Build y push de las imagenes Docker a Amazon ECR
#
# Requisitos: Docker Desktop corriendo, AWS CLI v2 configurado y repos ECR
# ya creados (ver 01-create-repos.ps1).
# Uso (desde cualquier ubicacion, el script resuelve la raiz del repo solo):
#   ./infra/ecr/02-build-and-push.ps1

$ErrorActionPreference = "Stop"

$REGION = "us-east-1"
$ACCOUNT_ID = aws sts get-caller-identity --query Account --output text
$REGISTRY = "$ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com"

Write-Host "Registry: $REGISTRY"

# Login de Docker contra ECR (expira cada 12 horas)
aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $REGISTRY

$projectRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")

$services = @(
    @{ Dir = "ms-api_gateway";          Repo = "innovatech/ms-api-gateway" },
    @{ Dir = "ms-autenticacion";        Repo = "innovatech/ms-autenticacion" },
    @{ Dir = "ms-recursos_colaboraciones"; Repo = "innovatech/ms-recursos-colaboraciones" },
    @{ Dir = "ms-gestion_proyectos";    Repo = "innovatech/ms-gestion-proyectos" },
    @{ Dir = "ms-analiticas";           Repo = "innovatech/ms-analiticas" }
)

foreach ($svc in $services) {
    $context = Join-Path $projectRoot $svc.Dir
    $image = "$REGISTRY/$($svc.Repo):latest"

    Write-Host "`n== $($svc.Dir) -> $image =="
    docker build -t $image $context
    docker push $image
}

Write-Host "`nListo. Verifica en ECR:"
Write-Host "  aws ecr list-images --repository-name innovatech/ms-api-gateway --region $REGION"
