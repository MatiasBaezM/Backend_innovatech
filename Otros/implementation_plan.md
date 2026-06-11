# Plan de Implementación: Docker & AWS EKS

Este documento detalla el plan paso a paso para transformar el proyecto **Innovatech** de un conjunto de microservicios locales a una arquitectura nativa de la nube, contenerizada con Docker y orquestada en Amazon Web Services (AWS) mediante EKS (Elastic Kubernetes Service).

> [!CAUTION]
> Desplegar infraestructura en AWS (especialmente un clúster EKS y una instancia RDS) **incurre en costos mensuales**, incluso si la usas poco. Asegúrate de apagar los recursos cuando no los uses si no cuentas con un presupuesto para mantenerlos 24/7.

> [!IMPORTANT]
> El plan propone dividir el trabajo en fases muy marcadas. Es fundamental que **no pasemos a la siguiente fase sin haber validado al 100% la anterior**.

## Preguntas Abiertas

> [!NOTE]
> 1. **Herramienta de Infraestructura (IaC):** ¿Prefieres **Terraform**, **AWS CLI**, o **eksctl** para levantar EKS?
> 2. **Base de Datos:** Se asume **Amazon RDS (MySQL)** administrado. ¿Estás de acuerdo?
> 3. **AWS CLI:** ¿Ya tienes configurado `aws-cli` con credenciales válidas?

---

## Progreso por Fases

### ✅ FASE 1: Contenerización Local (Docker) — COMPLETADA
- ✅ **Dockerfiles:** Multi-stage con Java 21, usuario no-root, puertos correctos (5 servicios)
- ✅ **docker-compose.yml:** Red interna, healthcheck MySQL, dependencias configuradas
- ✅ **Circuit Breaker (Resilience4j):** Implementado en API Gateway con fallback controller
- ✅ **CORS dinámico:** Configurable por variable de entorno `CORS_ALLOWED_ORIGINS`
- ✅ **Actuator:** Health checks habilitados en `/actuator/health` (necesario para EKS probes)
- ✅ **Frontend integrado:** `api.ts` usa `VITE_BACKEND_URL` para apuntar al backend

### ⬜ FASE 2: Registry de Imágenes (Amazon ECR)
Kubernetes necesita descargar tus imágenes desde algún lugar. Usaremos Elastic Container Registry.
- **Crear Repositorios:** Crear un repositorio ECR para cada microservicio.
- **Build & Push:** Construir las imágenes Docker finales y subirlas (push) a Amazon ECR.

### ⬜ FASE 3: Infraestructura Base (VPC y RDS)
- **VPC y Subredes:** Crear una VPC personalizada para aislar tu red, con subredes públicas y privadas.
- **Base de Datos (Amazon RDS):** Provisionar una base de datos MySQL en las subredes privadas.

### ⬜ FASE 4: Orquestación (Amazon EKS)
- **Creación del Clúster:** Desplegar el clúster EKS (`innovatech-cluster`) en la VPC creada.
- **Nodos de Trabajo (Node Groups):** Levantar instancias EC2 (ej. `t3.medium`).
- **Configuración Local:** Configurar tu `kubectl` local para apuntar al clúster.

### ⬜ FASE 5: Despliegue en Kubernetes (K8s)
Crearemos una nueva carpeta `k8s/` en tu repositorio con manifiestos YAML:
- **[NEW] k8s/configmap.yaml / k8s/secret.yaml:** Variables de entorno y secretos (DB, CORS).
- **[NEW] k8s/deployments.yaml:** Réplicas de cada microservicio apuntando a imágenes ECR.
- **[NEW] k8s/services.yaml:** Comunicación interna entre pods.
- **[NEW] k8s/ingress.yaml (o Service LoadBalancer):** ALB público asociado al `ms-api_gateway`.

---

## Verificación

1. **Prueba Fase 1:** `docker-compose up` → validar con Postman en `http://localhost:8080`
2. **Prueba Fase 3:** Conectar DBeaver/MySQL Workbench al RDS
3. **Prueba Fase 5:** `kubectl get pods` + `kubectl get services` → probar URL pública del ALB

---

## Configuración para Producción

### Variables de entorno del API Gateway (EKS ConfigMap/Secret)
```yaml
CORS_ALLOWED_ORIGINS: "https://tu-dominio-frontend.com"
AUTH_SERVICE_URL: "http://ms-autenticacion:8081"
RECURSOS_COLABORACIONES_SERVICE_URL: "http://ms-recursos-colaboraciones:8082"
GESTION_PROYECTOS_SERVICE_URL: "http://ms-gestion-proyectos:8083"
ANALITICAS_SERVICE_URL: "http://ms-analiticas:8084"
```

### Variable de entorno del Frontend (.env.production)
```
VITE_BACKEND_URL=https://api.innovatech.example.com
```
