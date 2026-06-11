# 🚀 Guía de Despliegue en AWS — Proyecto Innovatech

Guía paso a paso para llevar el **backend** (5 microservicios Java) y el **frontend** (React/Vite) a producción en Amazon Web Services.

> [!CAUTION]
> Desplegar en AWS **genera costos reales**. Un clúster EKS cuesta ~$0.10/hora (~$73/mes) solo por el control plane, más el costo de las instancias EC2. **Apaga los recursos cuando no los uses.**

---

## 📋 Prerequisitos

Antes de comenzar, instala y configura estas herramientas en tu PC:

| Herramienta | Comando para verificar | Instalación |
|---|---|---|
| AWS CLI v2 | `aws --version` | [Descargar](https://aws.amazon.com/cli/) |
| Docker Desktop | `docker --version` | [Descargar](https://www.docker.com/products/docker-desktop/) |
| kubectl | `kubectl version --client` | `choco install kubernetes-cli` |
| eksctl | `eksctl version` | `choco install eksctl` |

### Configurar credenciales AWS
```powershell
aws configure
# AWS Access Key ID: <tu-access-key>
# AWS Secret Access Key: <tu-secret-key>
# Default region name: us-east-1
# Default output format: json
```

### Verificar tu Account ID (lo necesitarás en varios pasos)
```powershell
aws sts get-caller-identity --query Account --output text
# Anota este número, ej: 123456789012
```

---

## FASE 1 — Crear Repositorios en Amazon ECR

ECR es el "Docker Hub privado" de AWS. Kubernetes descargará tus imágenes desde aquí.

### Paso 1.1: Crear un repositorio por cada microservicio

```powershell
$servicios = @(
  "innovatech/ms-api-gateway",
  "innovatech/ms-autenticacion",
  "innovatech/ms-recursos-colaboraciones",
  "innovatech/ms-gestion-proyectos",
  "innovatech/ms-analiticas"
)

foreach ($svc in $servicios) {
  aws ecr create-repository --repository-name $svc --region us-east-1
}
```

### Paso 1.2: Autenticar Docker con ECR

```powershell
# Reemplaza <ACCOUNT_ID> con tu número de cuenta
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com
```

> [!TIP]
> Este login expira cada 12 horas. Si ves errores de "unauthorized" más adelante, vuelve a ejecutar este comando.

---

## FASE 2 — Construir y Subir Imágenes Docker

### Paso 2.1: Build y Push de cada microservicio

Ejecuta desde la raíz del proyecto:

```powershell
# === Variables (modifica con tus valores) ===
$ACCOUNT_ID = "123456789012"   # ← Tu Account ID real
$REGION = "us-east-1"
$REGISTRY = "$ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com"

# === API Gateway ===
docker build -t innovatech/ms-api-gateway ./ms-api_gateway
docker tag innovatech/ms-api-gateway:latest $REGISTRY/innovatech/ms-api-gateway:latest
docker push $REGISTRY/innovatech/ms-api-gateway:latest

# === Autenticación ===
docker build -t innovatech/ms-autenticacion ./ms-autenticacion
docker tag innovatech/ms-autenticacion:latest $REGISTRY/innovatech/ms-autenticacion:latest
docker push $REGISTRY/innovatech/ms-autenticacion:latest

# === Recursos y Colaboraciones ===
docker build -t innovatech/ms-recursos-colaboraciones ./ms-recursos_colaboraciones
docker tag innovatech/ms-recursos-colaboraciones:latest $REGISTRY/innovatech/ms-recursos-colaboraciones:latest
docker push $REGISTRY/innovatech/ms-recursos-colaboraciones:latest

# === Gestión de Proyectos ===
docker build -t innovatech/ms-gestion-proyectos ./ms-gestion_proyectos
docker tag innovatech/ms-gestion-proyectos:latest $REGISTRY/innovatech/ms-gestion-proyectos:latest
docker push $REGISTRY/innovatech/ms-gestion-proyectos:latest

# === Analíticas ===
docker build -t innovatech/ms-analiticas ./ms-analiticas
docker tag innovatech/ms-analiticas:latest $REGISTRY/innovatech/ms-analiticas:latest
docker push $REGISTRY/innovatech/ms-analiticas:latest
```

### Paso 2.2: Verificar que las imágenes están en ECR

```powershell
aws ecr list-images --repository-name innovatech/ms-api-gateway --region us-east-1
# Deberías ver "imageTag": "latest"
```

---

## FASE 3 — Crear la Red (VPC) y Base de Datos (RDS)

### Paso 3.1: Crear la VPC con subredes

```powershell
# Crear VPC
$VPC_ID = aws ec2 create-vpc --cidr-block 10.0.0.0/16 --query "Vpc.VpcId" --output text
aws ec2 modify-vpc-attribute --vpc-id $VPC_ID --enable-dns-hostnames
aws ec2 create-tags --resources $VPC_ID --tags Key=Name,Value=innovatech-vpc

# Internet Gateway
$IGW_ID = aws ec2 create-internet-gateway --query "InternetGateway.InternetGatewayId" --output text
aws ec2 attach-internet-gateway --internet-gateway-id $IGW_ID --vpc-id $VPC_ID

# Subredes públicas (2 zonas, requerido por EKS)
$SUB_PUB_1 = aws ec2 create-subnet --vpc-id $VPC_ID --cidr-block 10.0.1.0/24 --availability-zone us-east-1a --query "Subnet.SubnetId" --output text
$SUB_PUB_2 = aws ec2 create-subnet --vpc-id $VPC_ID --cidr-block 10.0.2.0/24 --availability-zone us-east-1b --query "Subnet.SubnetId" --output text

# Subredes privadas (para RDS)
$SUB_PRIV_1 = aws ec2 create-subnet --vpc-id $VPC_ID --cidr-block 10.0.3.0/24 --availability-zone us-east-1a --query "Subnet.SubnetId" --output text
$SUB_PRIV_2 = aws ec2 create-subnet --vpc-id $VPC_ID --cidr-block 10.0.4.0/24 --availability-zone us-east-1b --query "Subnet.SubnetId" --output text

# Etiquetar subredes para EKS
aws ec2 create-tags --resources $SUB_PUB_1 $SUB_PUB_2 --tags Key=Name,Value=innovatech-public Key=kubernetes.io/role/elb,Value=1
aws ec2 create-tags --resources $SUB_PRIV_1 $SUB_PRIV_2 --tags Key=Name,Value=innovatech-private

# Tabla de rutas pública
$RT_ID = aws ec2 create-route-table --vpc-id $VPC_ID --query "RouteTable.RouteTableId" --output text
aws ec2 create-route --route-table-id $RT_ID --destination-cidr-block 0.0.0.0/0 --gateway-id $IGW_ID
aws ec2 associate-route-table --route-table-id $RT_ID --subnet-id $SUB_PUB_1
aws ec2 associate-route-table --route-table-id $RT_ID --subnet-id $SUB_PUB_2
```

> [!IMPORTANT]
> **Anota** los valores de `$VPC_ID`, `$SUB_PUB_1`, `$SUB_PUB_2`, `$SUB_PRIV_1`, `$SUB_PRIV_2`.

### Paso 3.2: Security Group para RDS

```powershell
$SG_RDS = aws ec2 create-security-group --group-name innovatech-rds-sg --description "RDS MySQL" --vpc-id $VPC_ID --query "GroupId" --output text
aws ec2 authorize-security-group-ingress --group-id $SG_RDS --protocol tcp --port 3306 --cidr 10.0.0.0/16
```

### Paso 3.3: Crear la Base de Datos RDS (MySQL)

```powershell
# Subnet Group para RDS
aws rds create-db-subnet-group --db-subnet-group-name innovatech-db-subnet --db-subnet-group-description "Innovatech DB" --subnet-ids $SUB_PRIV_1 $SUB_PRIV_2

# Crear instancia MySQL
aws rds create-db-instance `
  --db-instance-identifier innovatech-mysql `
  --db-instance-class db.t3.micro `
  --engine mysql --engine-version 8.0 `
  --master-username admin `
  --master-user-password "InnovatechDB2026!" `
  --allocated-storage 20 `
  --db-name innovatech_db `
  --vpc-security-group-ids $SG_RDS `
  --db-subnet-group-name innovatech-db-subnet `
  --no-publicly-accessible --backup-retention-period 7
```

> [!NOTE]
> La instancia RDS tarda **5-10 minutos** en estar disponible. Verifica con:
> ```powershell
> aws rds describe-db-instances --db-instance-identifier innovatech-mysql --query "DBInstances[0].DBInstanceStatus"
> ```

### Paso 3.4: Obtener el Endpoint de RDS

```powershell
aws rds describe-db-instances --db-instance-identifier innovatech-mysql --query "DBInstances[0].Endpoint.Address" --output text
# Ejemplo: innovatech-mysql.cxxxxxxxxxxxx.us-east-1.rds.amazonaws.com
```

**Anota este endpoint** para usarlo en el ConfigMap.

---

## FASE 4 — Crear el Clúster EKS

### Paso 4.1: Crear el clúster con eksctl

```powershell
eksctl create cluster `
  --name innovatech-cluster `
  --region us-east-1 --version 1.29 `
  --vpc-public-subnets "$SUB_PUB_1,$SUB_PUB_2" `
  --nodegroup-name innovatech-nodes `
  --node-type t3.medium --nodes 2 `
  --nodes-min 1 --nodes-max 3 --managed
```

> [!WARNING]
> Este comando tarda **15-20 minutos**. No lo canceles.

### Paso 4.2: Verificar conexión

```powershell
kubectl get nodes
# Deberías ver 2 nodos con STATUS "Ready"
```

### Paso 4.3: Crear el namespace

```powershell
kubectl create namespace innovatech
```

---

## FASE 5 — Desplegar el Backend en Kubernetes

### Paso 5.1: Actualizar los archivos YAML con tus valores

Edita estos archivos en `k8s/`:

**`k8s/configmap.yaml`** — Reemplaza `<TU_RDS_ENDPOINT>` con el endpoint del Paso 3.4.

**`k8s/deployments.yaml`** — Reemplaza `<ACCOUNT_ID>` y `<REGION>` en todas las imágenes.

**`k8s/secret.yaml`** — Verifica la contraseña en Base64:
```powershell
[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("InnovatechDB2026!"))
# Resultado: SW5ub3ZhdGVjaERCMjAyNiE=
```

### Paso 5.2: Aplicar los manifiestos

```powershell
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/deployments.yaml
kubectl apply -f k8s/services.yaml
```

### Paso 5.3: Verificar que los pods están corriendo

```powershell
kubectl get pods -n innovatech
# Todos deben mostrar STATUS: Running (espera 2-3 minutos)

# Si un pod falla:
kubectl logs -n innovatech <nombre-del-pod>
kubectl describe pod -n innovatech <nombre-del-pod>
```

### Paso 5.4: Obtener la URL pública del backend

```powershell
kubectl get svc ms-api-gateway -n innovatech
# EXTERNAL-IP = URL del Load Balancer (tarda 2-5 min)
```

### Paso 5.5: Probar el Backend

```powershell
curl http://<LOAD_BALANCER_URL>/actuator/health
```

---

## FASE 6 — Desplegar el Frontend (React/Vite en S3 + CloudFront)

El frontend se aloja como sitio estático — más rápido y barato que un contenedor.

### Paso 6.1: Configurar la URL del backend

En el proyecto frontend, crea `.env.production`:
```
VITE_BACKEND_URL=http://<LOAD_BALANCER_URL>
```

### Paso 6.2: Compilar el frontend

```powershell
cd <ruta-al-proyecto-frontend>
npm install
npm run build
# Genera la carpeta "dist/"
```

### Paso 6.3: Crear Bucket S3

```powershell
aws s3 mb s3://innovatech-frontend --region us-east-1
aws s3 website s3://innovatech-frontend --index-document index.html --error-document index.html
```

### Paso 6.4: Configurar acceso público

Crea un archivo `bucket-policy.json`:
```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Sid": "PublicRead",
    "Effect": "Allow",
    "Principal": "*",
    "Action": "s3:GetObject",
    "Resource": "arn:aws:s3:::innovatech-frontend/*"
  }]
}
```

```powershell
aws s3api put-public-access-block --bucket innovatech-frontend --public-access-block-configuration BlockPublicAcls=false,IgnorePublicAcls=false,BlockPublicPolicy=false,RestrictPublicBuckets=false
aws s3api put-bucket-policy --bucket innovatech-frontend --policy file://bucket-policy.json
```

### Paso 6.5: Subir archivos

```powershell
aws s3 sync dist/ s3://innovatech-frontend --delete
```

### Paso 6.6: Actualizar CORS en el backend

```powershell
kubectl edit configmap innovatech-config -n innovatech
# Cambia CORS_ALLOWED-ORIGINS a la URL del frontend S3
kubectl rollout restart deployment ms-api-gateway -n innovatech
```

---

## ✅ Verificación Final

| Verificar | Comando |
|---|---|
| Pods corriendo | `kubectl get pods -n innovatech` → todos `Running` |
| Backend responde | Abrir `http://<LB_URL>/actuator/health` |
| Frontend carga | Abrir `http://innovatech-frontend.s3-website-us-east-1.amazonaws.com` |
| Conexión front→back | Probar login o llamadas API desde el frontend |

---

## 🔧 Comandos de Mantenimiento

```powershell
# Logs en tiempo real
kubectl logs -f -n innovatech deployment/ms-autenticacion

# Re-desplegar tras cambio de código
docker build -t innovatech/ms-autenticacion ./ms-autenticacion
docker tag innovatech/ms-autenticacion:latest $REGISTRY/innovatech/ms-autenticacion:latest
docker push $REGISTRY/innovatech/ms-autenticacion:latest
kubectl rollout restart deployment ms-autenticacion -n innovatech

# Escalar réplicas
kubectl scale deployment ms-autenticacion -n innovatech --replicas=3
```

## 💰 Ahorrar Costos

```powershell
# Escalar nodos a 0 (pausa sin destruir)
eksctl scale nodegroup --cluster innovatech-cluster --name innovatech-nodes --nodes 0

# Volver a encender
eksctl scale nodegroup --cluster innovatech-cluster --name innovatech-nodes --nodes 2

# BORRAR TODO (cuando ya no necesites el entorno)
eksctl delete cluster --name innovatech-cluster --region us-east-1
aws rds delete-db-instance --db-instance-identifier innovatech-mysql --skip-final-snapshot
aws s3 rb s3://innovatech-frontend --force
```

---

## 📊 Diagrama de Arquitectura

```
    Usuarios ──► CloudFront + S3 (Frontend React)
                       │
                       │ VITE_BACKEND_URL
                       ▼
                 AWS Load Balancer (puerto 80)
                       │
                 ┌─────▼──────────────────────┐
                 │  EKS Cluster (Kubernetes)   │
                 │                             │
                 │  ms-api-gateway :8080       │
                 │    ├─► ms-autenticacion     │
                 │    ├─► ms-recursos-colab.   │
                 │    ├─► ms-gestion-proy.     │
                 │    └─► ms-analiticas        │
                 └────────────┬────────────────┘
                              │
                       Amazon RDS (MySQL)
                       (subred privada)
```
