# Enterprise S3 Browser

A standalone web application for browsing and managing S3-compatible storage (AWS S3, MinIO, etc.).

## Features

- **Web-based Interface**: Modern, responsive web UI built with Bootstrap
- **Authentication**: JWT-based authentication with configurable credentials
- **S3 Operations**: 
  - Browse folders and files
  - Upload files (drag & drop or file selection)
  - Download files
  - Delete files
  - Navigate folder hierarchy
- **Configuration Management**: 
  - Runtime S3 configuration updates
  - Connection testing
  - Support for various S3-compatible storages
- **Enterprise Ready**: 
  - Security best practices
  - Comprehensive error handling
  - API documentation with Swagger/OpenAPI
  - Actuator endpoints for monitoring

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.6+
- S3-compatible storage (AWS S3, MinIO, etc.)

### Default Configuration

The application comes with default MinIO configuration:
- **Endpoint**: http://localhost:9000
- **Access Key**: minioadmin
- **Secret Key**: minioadmin
- **Bucket**: default-bucket
- **Region**: us-east-1

### Running the Application

1. **Clone and build**:
   ```bash
   cd backend
   mvn clean package
   ```

2. **Run the application**:
   ```bash
   java -jar target/s3-browser-1.0.0.jar
   ```

3. **Access the application**:
   - Web UI: http://localhost:8080
   - API Documentation: http://localhost:8080/swagger-ui.html
   - Health Check: http://localhost:8080/actuator/health

4. **Login**:
   - Default username: `admin`
   - Default password: `admin`

## Configuration

### Environment Variables

You can configure the application using environment variables:

```bash
# S3 Configuration
export S3_ENDPOINT=http://your-s3-endpoint:9000
export S3_ACCESS_KEY=your-access-key
export S3_SECRET_KEY=your-secret-key
export S3_BUCKET=your-bucket
export S3_REGION=us-east-1
export S3_PATH_STYLE_ACCESS=true

# Authentication
export APP_AUTH_USERNAME=your-admin-username
export APP_AUTH_PASSWORD=your-admin-password

# JWT
export JWT_SECRET=your-jwt-secret-key-min-32-chars
export JWT_EXPIRATION=86400000

# Run the application
java -jar target/s3-browser-1.0.0.jar
```

### Runtime Configuration

You can also update S3 settings through the web interface:
1. Login to the web application
2. Click on your username in the top-right corner
3. Select "Configuration"
4. Update the S3 settings and test the connection
5. Save the configuration

## API Endpoints

### Authentication
- `POST /api/auth/login` - Authenticate user
- `GET /api/auth/validate` - Validate JWT token
- `POST /api/auth/logout` - Logout user

### S3 Operations
- `GET /api/s3/objects` - List objects (with optional prefix)
- `GET /api/s3/objects/{key}/metadata` - Get object metadata
- `GET /api/s3/objects/{key}/download` - Download object
- `POST /api/s3/objects/{key}` - Upload object
- `DELETE /api/s3/objects/{key}` - Delete object
- `GET /api/s3/buckets` - List buckets
- `GET /api/s3/test-connection` - Test S3 connection

### Configuration
- `GET /api/config` - Get current S3 configuration
- `PUT /api/config` - Update S3 configuration
- `POST /api/config/test` - Test S3 configuration

## Security

- JWT-based authentication
- CORS configuration for cross-origin requests
- Input validation and sanitization
- Secure password handling
- No sensitive information in logs

## Deployment

### Docker (Example)

```dockerfile
FROM openjdk:17-jre-slim
COPY target/s3-browser-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Production Considerations

1. **Use strong credentials**:
   - Change default admin username/password
   - Use a strong JWT secret (32+ characters)

2. **Configure HTTPS**:
   - Use a reverse proxy (nginx, Apache)
   - Configure SSL certificates

3. **Environment-specific settings**:
   - Use environment variables for configuration
   - Consider using external configuration management

4. **Monitoring**:
   - Actuator endpoints are available at `/actuator/*`
   - Configure logging levels as needed

## Development

### Project Structure

```
└── backend/
    ├── src/main/java/com/enterprise/s3browser/
    │   ├── S3BrowserApplication.java
    │   ├── config/          # Configuration classes
    │   ├── controller/      # REST controllers
    │   ├── dto/             # Data transfer objects
    │   ├── exception/       # Exception handling
    │   ├── model/           # Domain models
    │   ├── security/        # Security components
    │   └── service/         # Business logic
    └── src/main/resources/
        ├── application.yml  # Application configuration
        └── static/          # Embedded frontend files
            ├── index.html   # Main HTML file
            ├── css/app.css # Styles
            └── js/app.js   # JavaScript application
```

### Building from Source

```bash
# Build the complete application (backend + embedded frontend)
cd backend
mvn clean package

# The frontend files are automatically embedded in the JAR
# Single JAR contains everything needed to run
```

## Troubleshooting

### Common Issues

1. **Connection refused to S3**:
   - Verify S3 endpoint URL and port
   - Check if S3 service is running
   - Verify network connectivity

2. **Authentication failed**:
   - Check S3 access key and secret key
   - Verify bucket exists and is accessible

3. **Upload/Download issues**:
   - Check file permissions
   - Verify bucket policies
   - Check network timeouts

### Logs

- Application logs are written to console by default
- Log level can be configured in `application.yml`
- For debugging, set `com.enterprise.s3browser: DEBUG`

## License

MIT License - see LICENSE file for details.