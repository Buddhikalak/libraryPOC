# Library Management System

A RESTful API for managing a library system, built with Spring Boot and MySQL.

## Features

- Register and manage borrowers
- Register and manage books
- Borrow and return books
- Track book availability
- Validate ISBN numbers
- Swagger UI documentation

## Technical Stack

- Java 17
- Spring Boot 3.2.3
- Spring Data JPA
- MySQL
- Maven
- Docker
- Swagger/OpenAPI 3.0

## Prerequisites

- Java 17 or higher
- Maven 3.9 or higher
- MySQL 8.0 or higher
- Docker (optional)

## Getting Started

### Local Development

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd library-management-system
   ```

2. Create a MySQL database:
   ```sql
   CREATE DATABASE library_db;
   ```

3. Update the database configuration in `src/main/resources/application-dev.yml` if needed.

4. Build and run the application:
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080/api`.

### Docker Deployment

1. Build the Docker image:
   ```bash
   docker build -t library-management-system .
   ```

2. Run the container:
   ```bash
   docker run -p 8080:8080 \
     -e SPRING_PROFILES_ACTIVE=prod \
     -e MYSQL_URL=jdbc:mysql://mysql:3306/library_db \
     -e MYSQL_USER=library_user \
     -e MYSQL_PASSWORD=library_password \
     library-management-system
   ```

## API Documentation

Once the application is running, you can access the Swagger UI documentation at:
`http://localhost:8080/api/swagger-ui.html`

### Available Endpoints

#### Borrower Management
- `POST /api/borrowers` - Register a new borrower
- `GET /api/borrowers/{id}` - Get a borrower by ID
- `GET /api/borrowers` - Get all borrowers
- `PUT /api/borrowers/{id}` - Update a borrower
- `DELETE /api/borrowers/{id}` - Delete a borrower

#### Book Management
- `POST /api/books` - Register a new book
- `GET /api/books/{id}` - Get a book by ID
- `GET /api/books` - Get all books
- `GET /api/books/isbn/{isbn}` - Get books by ISBN
- `PUT /api/books/{id}` - Update a book
- `DELETE /api/books/{id}` - Delete a book
- `POST /api/books/{bookId}/borrow/{borrowerId}` - Borrow a book
- `POST /api/books/{bookId}/return` - Return a book

## Data Models

### Borrower
- `id` (Long): Unique identifier
- `name` (String): Borrower's name
- `email` (String): Unique email address
- `borrowings` (Set<BookBorrowing>): Current and past book borrowings

### Book
- `id` (Long): Unique identifier
- `isbn` (String): ISBN number (validated format)
- `title` (String): Book title
- `author` (String): Book author
- `available` (boolean): Book availability status
- `borrowings` (Set<BookBorrowing>): Borrowing history

### BookBorrowing
- `id` (Long): Unique identifier
- `book` (Book): Borrowed book
- `borrower` (Borrower): Borrower
- `borrowedAt` (LocalDateTime): Borrowing date and time
- `returnedAt` (LocalDateTime): Return date and time (null if not returned)

## Assumptions and Design Decisions

1. **ISBN Validation**: The system validates ISBN numbers using a regex pattern that supports both ISBN-10 and ISBN-13 formats.

2. **Book Uniqueness**: 
   - Books with the same ISBN must have the same title and author
   - Multiple copies of the same book (same ISBN) are allowed
   - Each copy has a unique ID

3. **Borrowing Rules**:
   - A book can only be borrowed by one borrower at a time
   - A book must be returned before it can be borrowed again
   - The system tracks borrowing history

4. **Email Uniqueness**: Each borrower must have a unique email address

5. **Environment Configuration**:
   - Development: Uses local MySQL database with auto-update schema
   - Production: Uses environment variables for database configuration

6. **Error Handling**:
   - Custom exceptions for business logic errors
   - Global exception handler for consistent error responses
   - Input validation using Jakarta Validation

## Testing

Run the tests using Maven:
```bash
mvn test
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 