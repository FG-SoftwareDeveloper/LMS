# 🔧 Local Development Setup

## Quick Start with .env

### 1. Copy Environment Template
```bash
cp .env.example .env
```

### 2. Configure Your Environment
Edit `.env` file with your settings:

```bash
# For H2 (in-memory) - Quick start
SPRING_PROFILES_ACTIVE=default

# For Local PostgreSQL
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/lms_local
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=yourpassword

# For Production Testing (Neon DB)
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-xxx.us-east-1.aws.neon.tech:5432/lms_db?sslmode=require
SPRING_DATASOURCE_USERNAME=your-neon-user
SPRING_DATASOURCE_PASSWORD=your-neon-password
```

### 3. Run Application
```bash
./mvnw spring-boot:run
```

## Profile Configurations

### Default Profile (H2 Database)
- **Database**: In-memory H2
- **Console**: http://localhost:9090/h2-console
- **Auto-schema**: Creates tables automatically
- **Best for**: Quick testing, development

### Dev Profile (PostgreSQL)
- **Database**: Local PostgreSQL
- **Migrations**: Flyway enabled
- **Logging**: Debug enabled
- **Best for**: Full-featured development

### Prod Profile (Production)
- **Database**: Neon PostgreSQL
- **Migrations**: Flyway with validation
- **Optimized**: Connection pooling, compressed responses
- **Best for**: Production deployment

## Environment Variables Reference

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | `default` | Active Spring profile |
| `SERVER_PORT` | `9090` | Application port |
| `SPRING_DATASOURCE_URL` | H2 in-memory | Database connection URL |
| `SPRING_DATASOURCE_USERNAME` | `sa` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `` | Database password |
| `SPRING_JPA_SHOW_SQL` | `false` | Show SQL queries in logs |

## Local PostgreSQL Setup

### Using Docker
```bash
docker run --name lms-postgres \
  -e POSTGRES_DB=lms_local \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=password \
  -p 5432:5432 \
  -d postgres:15-alpine
```

### Using Homebrew (macOS)
```bash
brew install postgresql@15
brew services start postgresql@15
createdb lms_local
```

### Using package manager (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo -u postgres createdb lms_local
```

## Testing Different Profiles

### H2 (Quick Start)
```bash
# .env file
SPRING_PROFILES_ACTIVE=default
./mvnw spring-boot:run
```

### Local PostgreSQL
```bash
# .env file
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/lms_local
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password
./mvnw spring-boot:run
```

### Production (Neon DB)
```bash
# .env file
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-xxx.neon.tech:5432/lms_db?sslmode=require
SPRING_DATASOURCE_USERNAME=your-user
SPRING_DATASOURCE_PASSWORD=your-password
./mvnw spring-boot:run
```

## IDE Configuration

### IntelliJ IDEA
1. Run Configuration → Environment Variables
2. Add variables from `.env` file
3. Or install "EnvFile" plugin for automatic .env loading

### VS Code
1. Install "Spring Boot Extension Pack"
2. Use `.vscode/launch.json`:
```json
{
  "type": "java",
  "request": "launch",
  "mainClass": "com.codigo.LMS.LmsApplication",
  "envFile": "${workspaceFolder}/.env"
}
```

## Troubleshooting

### .env Not Loading
- Ensure Spring Boot 3.1+ (✅ you have 3.5.6)
- File must be in project root
- Check for syntax errors (no spaces around =)

### Database Connection Issues
```bash
# Test PostgreSQL connection
psql -h localhost -U postgres -d lms_local

# Check if port is in use
netstat -an | grep 5432
```

### Profile Not Activating
```bash
# Debug active profiles
./mvnw spring-boot:run --debug
# Look for: "The following profiles are active: ..."
```