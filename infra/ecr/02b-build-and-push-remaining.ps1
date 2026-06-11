# FASE 2.2b - Build y push de las imagenes restantes (ms-api-gateway ya se subio)
#
# Reintento tras un build colgado en mvn dependency:go-offline (red BuildKit).
# Uso:
#   ./infra/ecr/02b-build-and-push-remaining.ps1

$ErrorActionPreference = "Continue"

$REGION = "us-east-1"
$ACCOUNT_ID = aws sts get-caller-identity --query Account --output text
$REGISTRY = "$ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com"

Write-Host "Registry: $REGISTRY"

aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $REGISTRY
if ($LASTEXITCODE -ne 0) { throw "docker login fallo (exit $LASTEXITCODE)" }

$projectRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")

$services = @(
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
    if ($LASTEXITCODE -ne 0) { throw "docker build fallo para $($svc.Dir) (exit $LASTEXITCODE)" }
    docker push $image
    if ($LASTEXITCODE -ne 0) { throw "docker push fallo para $($svc.Dir) (exit $LASTEXITCODE)" }
}

Write-Host "`nListo. Verifica en ECR:"
Write-Host "  aws ecr list-images --repository-name innovatech/ms-autenticacion --region $REGION"
