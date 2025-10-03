# LMS Application - PostgreSQL Migration Guide

## 🚀 Production Deployment with Neon DB

### Prerequisites
1. **Neon DB Account**: Create account at [neon.tech](https://neon.tech)
2. **Railway/Render/Fly Account**: Choose your deployment platform

### Environment Setup

#### 1. Environment Variables
Set these in your deployment platform:

```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-XXXXXX.us-east-1.aws.neon.tech:5432/elearn_db?sslmode=require
SPRING_DATASOURCE_USERNAME=<neon-user>
SPRING_DATASOURCE_PASSWORD=<neon-password>
```

#### 2. JVM Configuration
For optimized memory usage in production:

```bash
JAVA_OPTS=-Xms128m -Xmx256m -XX:+UseG1GC
```

### Local Development vs Production

#### Development (Default Profile)
- Uses H2 in-memory database
- Auto-creates schema on startup
- No Flyway migrations
- Access H2 console at: http://localhost:9090/h2-console

```bash
./mvnw spring-boot:run
```

#### Production (prod profile)
- Uses PostgreSQL (Neon DB)
- Schema managed by Flyway migrations
- Optimized connection pooling
- Compressed responses

```bash
./mvnw spring-boot:run --spring.profiles.active=prod
```

### Database Migration

#### Flyway Migrations
Located in `src/main/resources/db/migration/`:

- `V1__Create_initial_schema.sql` - Creates all tables and indexes
- `V2__Insert_sample_data.sql` - Inserts sample users and courses

#### Sample Users (Production)
| Username | Email | Password | Role |
|----------|-------|----------|------|
| admin | admin@learnhub.com | AdminPass123! | ADMIN |
| john_instructor | john.instructor@learnhub.com | InstructorPass456! | INSTRUCTOR |
| testuser | test.user@example.com | password123 | STUDENT |

### Railway Deployment

1. **Connect Repository**: Link your GitHub repository
2. **Set Environment Variables**:
   ```
   SPRING_PROFILES_ACTIVE=prod
   SPRING_DATASOURCE_URL=<neon-connection-string>
   SPRING_DATASOURCE_USERNAME=<neon-username>
   SPRING_DATASOURCE_PASSWORD=<neon-password>
   ```
3. **Deploy**: Railway auto-deploys on git push

### Neon DB Setup

1. Create new database: `elearn_db`
2. Get connection string from Neon dashboard
3. Ensure SSL mode is required: `?sslmode=require`
4. Test connection with provided credentials

### Features
- ✅ PostgreSQL production database
- ✅ H2 development database  
- ✅ Flyway schema migrations
- ✅ Connection pooling (HikariCP)
- ✅ Memory optimized JVM settings
- ✅ Compressed HTTP responses
- ✅ Sample data seeding

### Monitoring
- Application logs: Check Railway/platform logs
- Database connections: Monitor via Neon dashboard
- Memory usage: JVM metrics in platform monitoring

### Troubleshooting

#### Common Issues:
1. **Connection timeout**: Check Neon DB connection string and SSL settings
2. **Migration failures**: Verify SQL syntax for PostgreSQL
3. **Memory issues**: Adjust JVM heap settings via JAVA_OPTS
4. **Port conflicts**: Default port 9090, configure via SERVER_PORT env var

#### Debug Commands:
```bash
# Test database connection
./mvnw spring-boot:run --spring.profiles.active=prod --debug

# Check Flyway status
./mvnw flyway:info -Dflyway.configFiles=src/main/resources/application-prod.yml
```