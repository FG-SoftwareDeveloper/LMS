# 🚀 Render Deployment Guide for LMS Application

## Quick Deploy Options

### Option 1: One-Click Deploy (Recommended)
1. **Fork/Clone this repository** to your GitHub account
2. **Click Deploy Button** (once you add this to your README):
   ```markdown
   [![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/YOUR-USERNAME/LMS)
   ```
3. **Configure Environment Variables** in Render dashboard

### Option 2: Manual Setup

#### Step 1: Create Render Account
- Go to [render.com](https://render.com)
- Sign up with GitHub account

#### Step 2: Create PostgreSQL Database
1. In Render Dashboard → **New** → **PostgreSQL**
2. Name: `lms-postgres`
3. Database Name: `lms_db`
4. User: `lms_user`
5. Plan: **Starter** (free) or **Standard** (production)
6. **Create Database**
7. **Save connection details** (you'll need them)

#### Step 3: Create Web Service
1. In Render Dashboard → **New** → **Web Service**
2. **Connect your GitHub repository**
3. Configure:
   ```
   Name: lms-app
   Runtime: Docker
   Build Command: (leave empty - Docker handles this)
   Start Command: (leave empty - Docker handles this)
   Plan: Starter ($7/month) or Standard ($25/month)
   ```

#### Step 4: Set Environment Variables
In your web service settings, add these environment variables:

| Key | Value |
|-----|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://[HOST]:[PORT]/[DB_NAME]?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | `[USERNAME from DB step]` |
| `SPRING_DATASOURCE_PASSWORD` | `[PASSWORD from DB step]` |

**Get the values from your PostgreSQL database:**
- Go to your PostgreSQL service in Render
- Copy the **External Database URL**
- Format it as JDBC URL: `jdbc:postgresql://...`

#### Step 5: Deploy
1. **Push code** to your GitHub repository
2. Render will **automatically build and deploy**
3. Check **Logs** tab for deployment progress
4. Visit your app URL once deployment completes

## Environment Variables Example

```bash
# Your PostgreSQL connection from Render will look like:
SPRING_DATASOURCE_URL=jdbc:postgresql://dpg-xxxxx-a.oregon-postgres.render.com:5432/lms_db_xxxx?sslmode=require
SPRING_DATASOURCE_USERNAME=lms_user_xxxx
SPRING_DATASOURCE_PASSWORD=your-generated-password
```

## Features Enabled for Production

✅ **Docker multi-stage build** (optimized image size)  
✅ **Health checks** via `/actuator/health`  
✅ **Auto-scaling** with Render  
✅ **SSL/HTTPS** automatic  
✅ **PostgreSQL** database  
✅ **Flyway migrations** (auto-run on startup)  
✅ **Memory optimization** (512MB max heap)  
✅ **Security** (non-root container user)  

## Monitoring & Debugging

### Health Check
Your app will be available at: `https://your-app-name.onrender.com`
Health endpoint: `https://your-app-name.onrender.com/actuator/health`

### Logs
- **Build logs**: Render Dashboard → Your Service → Logs (during deployment)
- **Runtime logs**: Same location (after deployment)

### Common Issues

1. **Build Timeout**:
   - Increase build timeout in Render settings
   - Or optimize Dockerfile caching

2. **Database Connection Failed**:
   - Check environment variables are correct
   - Verify PostgreSQL service is running
   - Test connection string format

3. **Out of Memory**:
   - Increase plan or optimize JVM settings in Dockerfile
   - Current setting: `-Xmx512m`

4. **Slow First Request**:
   - Normal for free tier (cold starts)
   - Upgrade to paid plan for always-on

## Sample User Accounts (After First Deploy)

| Role | Username | Email | Password |
|------|----------|--------|----------|
| Admin | admin | admin@learnhub.com | AdminPass123! |
| Instructor | john_instructor | john.instructor@learnhub.com | InstructorPass456! |
| Student | testuser | test.user@example.com | password123 |

## Cost Estimate

**Free Tier:**
- PostgreSQL: Free (1GB storage, 1M rows)
- Web Service: $7/month (Starter plan)

**Production:**
- PostgreSQL: $7/month (Standard)
- Web Service: $25/month (Standard plan)

## Next Steps After Deployment

1. **Test the application** with sample users
2. **Upload course images** to `/images/courses/`
3. **Configure custom domain** (optional)
4. **Set up monitoring** (Render provides basic metrics)
5. **Enable auto-deploy** from main branch

## Support

- **Render Docs**: [render.com/docs](https://render.com/docs)
- **Spring Boot on Render**: [render.com/docs/deploy-spring-boot](https://render.com/docs/deploy-spring-boot)
- **PostgreSQL Setup**: [render.com/docs/databases](https://render.com/docs/databases)