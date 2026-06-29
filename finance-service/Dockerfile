# Finance Service Dockerfile — Node.js single-stage build
#
# Single-stage is appropriate here because Node.js does not require
# a compile step — the source files run directly on the runtime.
# (Contrast with the Java multi-stage Dockerfiles that need Maven to build.)

FROM node:22-alpine

# Set working directory inside the container
WORKDIR /app

# Copy dependency manifests first (layer caching — only re-runs npm install
# when package.json or package-lock.json actually change)
COPY package*.json ./

# Install only production dependencies — dev/test deps not needed at runtime.
# Using npm install instead of npm ci to avoid lock file version mismatch
# between the host npm and the npm version bundled in the base image.
RUN npm install --omit=dev

# Copy the rest of the application source
COPY src ./src

# Document the port the service listens on
EXPOSE 8082

# Environment variables with safe defaults (overridden by docker-compose)
ENV PORT=8082
ENV MONGO_URI=mongodb://finance-mongodb:27017/financedb

# Start the application
CMD ["node", "src/app.js"]
